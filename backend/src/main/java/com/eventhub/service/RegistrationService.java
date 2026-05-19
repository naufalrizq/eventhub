package com.eventhub.service;

import com.eventhub.entity.Event;
import com.eventhub.entity.Registration;
import com.eventhub.entity.TicketType;
import com.eventhub.entity.User;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for Registration-related business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    /**
     * Register user for an event
     */
    @Transactional
    public Registration registerForEvent(User user, Long eventId, Long ticketTypeId, int quantity) {
        log.info("Registering user {} for event {} with ticket type {}", 
                user.getEmail(), eventId, ticketTypeId);

        // Validate event
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getStatus() != Event.EventStatus.PUBLISHED) {
            throw new RuntimeException("Event is not available for registration");
        }

        if (event.getStartDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot register for past events");
        }

        // Check if user already registered
        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new RuntimeException("User already registered for this event");
        }

        // Validate ticket type
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
            .orElseThrow(() -> new RuntimeException("Ticket type not found"));

        if (!ticketType.getEvent().equals(event)) {
            throw new RuntimeException("Ticket type does not belong to this event");
        }

        if (!ticketType.isAvailable()) {
            throw new RuntimeException("Ticket type is not available");
        }

        if (ticketType.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Not enough tickets available");
        }

        // Check max per user limit
        if (ticketType.getMaxPerUser() != null && quantity > ticketType.getMaxPerUser()) {
            throw new RuntimeException("Quantity exceeds maximum allowed per user");
        }

        // Reserve tickets
        ticketType.reserveTickets(quantity);
        ticketTypeRepository.save(ticketType);

        // Create registration
        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setTicketType(ticketType);
        registration.setQuantity(quantity);
        registration.setTotalAmount(ticketType.getPrice().multiply(BigDecimal.valueOf(quantity)));
        registration.setStatus(Registration.RegistrationStatus.PENDING_PAYMENT);
        registration.setPaymentStatus(Registration.PaymentStatus.PENDING);
        registration.setConfirmationCode(generateConfirmationCode());
        registration.setPaymentDeadline(LocalDateTime.now().plusHours(24)); // 24 hours to pay

        Registration savedRegistration = registrationRepository.save(registration);

        // Update event registration count
        event.setRegistrationCount(event.getRegistrationCount() + quantity);
        eventRepository.save(event);

        log.info("Registration created successfully with ID: {}", savedRegistration.getId());
        return savedRegistration;
    }

    /**
     * Cancel registration
     */
    @Transactional
    public void cancelRegistration(Long registrationId, User user) {
        log.info("Cancelling registration {} for user {}", registrationId, user.getEmail());

        Registration registration = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new RuntimeException("Registration not found"));

        // Verify ownership
        if (!registration.getUser().equals(user)) {
            throw new RuntimeException("Not authorized to cancel this registration");
        }

        // Check if cancellation is allowed
        if (registration.getStatus() == Registration.RegistrationStatus.CANCELLED) {
            throw new RuntimeException("Registration is already cancelled");
        }

        if (registration.getEvent().getStartDate().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new RuntimeException("Cannot cancel registration less than 24 hours before event");
        }

        // Release tickets
        TicketType ticketType = registration.getTicketType();
        ticketType.releaseTickets(registration.getQuantity());
        ticketTypeRepository.save(ticketType);

        // Update registration status
        registration.setStatus(Registration.RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now());
        registrationRepository.save(registration);

        // Update event registration count
        Event event = registration.getEvent();
        event.setRegistrationCount(Math.max(0, event.getRegistrationCount() - registration.getQuantity()));
        eventRepository.save(event);

        log.info("Registration cancelled successfully: {}", registrationId);
    }

    /**
     * Confirm payment for registration
     */
    @Transactional
    public Registration confirmPayment(Long registrationId, String paymentReference) {
        log.info("Confirming payment for registration {}", registrationId);

        Registration registration = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new RuntimeException("Registration not found"));

        if (registration.getPaymentStatus() == Registration.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Payment already confirmed");
        }

        if (registration.getStatus() == Registration.RegistrationStatus.CANCELLED) {
            throw new RuntimeException("Cannot confirm payment for cancelled registration");
        }

        // Update payment status
        registration.setPaymentStatus(Registration.PaymentStatus.COMPLETED);
        registration.setStatus(Registration.RegistrationStatus.CONFIRMED);
        registration.setPaymentReference(paymentReference);
        registration.setPaidAt(LocalDateTime.now());

        Registration updatedRegistration = registrationRepository.save(registration);

        log.info("Payment confirmed for registration: {}", registrationId);
        return updatedRegistration;
    }

    /**
     * Check in user for event
     */
    @Transactional
    public Registration checkInUser(String confirmationCode) {
        log.info("Checking in user with confirmation code: {}", confirmationCode);

        Registration registration = registrationRepository.findByConfirmationCode(confirmationCode)
            .orElseThrow(() -> new RuntimeException("Invalid confirmation code"));

        if (registration.getStatus() != Registration.RegistrationStatus.CONFIRMED) {
            throw new RuntimeException("Registration is not confirmed");
        }

        if (registration.getCheckedInAt() != null) {
            throw new RuntimeException("User already checked in");
        }

        // Check if event is happening now or has started
        Event event = registration.getEvent();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getStartDate().minusHours(1))) {
            throw new RuntimeException("Check-in not yet available");
        }

        registration.setCheckedInAt(now);
        Registration updatedRegistration = registrationRepository.save(registration);

        log.info("User checked in successfully for registration: {}", registration.getId());
        return updatedRegistration;
    }

    /**
     * Get user registrations
     */
    public Page<Registration> getUserRegistrations(User user, Pageable pageable) {
        log.debug("Fetching registrations for user: {}", user.getEmail());
        return registrationRepository.findByUser(user, pageable);
    }

    /**
     * Get event registrations
     */
    public Page<Registration> getEventRegistrations(Long eventId, Pageable pageable) {
        log.debug("Fetching registrations for event: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        return registrationRepository.findByEvent(event, pageable);
    }

    /**
     * Get registration by ID
     */
    public Optional<Registration> getRegistrationById(Long id) {
        log.debug("Fetching registration by ID: {}", id);
        return registrationRepository.findById(id);
    }

    /**
     * Get upcoming registrations for user
     */
    public List<Registration> getUpcomingRegistrations(User user) {
        log.debug("Fetching upcoming registrations for user: {}", user.getEmail());
        return registrationRepository.findUpcomingRegistrationsForUser(user, LocalDateTime.now());
    }

    /**
     * Get past registrations for user
     */
    public Page<Registration> getPastRegistrations(User user, Pageable pageable) {
        log.debug("Fetching past registrations for user: {}", user.getEmail());
        return registrationRepository.findPastRegistrationsForUser(user, LocalDateTime.now(), pageable);
    }

    /**
     * Get registration statistics for event
     */
    public RegistrationStatistics getEventRegistrationStats(Long eventId) {
        log.debug("Fetching registration statistics for event: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        List<Object[]> stats = registrationRepository.getRegistrationStatsByEvent(event);
        long checkedInCount = registrationRepository.countCheckedInRegistrations(event);
        Double totalRevenue = registrationRepository.getTotalRevenueForEvent(event);
        
        return RegistrationStatistics.builder()
            .eventId(eventId)
            .totalRegistrations(registrationRepository.countByEvent(event))
            .confirmedRegistrations(registrationRepository.countByEventAndStatus(event, Registration.RegistrationStatus.CONFIRMED))
            .checkedInCount(checkedInCount)
            .totalRevenue(totalRevenue != null ? BigDecimal.valueOf(totalRevenue) : BigDecimal.ZERO)
            .build();
    }

    /**
     * Process expired registrations
     */
    @Transactional
    public void processExpiredRegistrations() {
        log.info("Processing expired registrations");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<Registration> expiredRegistrations = registrationRepository
            .findRegistrationsWithPendingPayments(cutoffTime);
        
        for (Registration registration : expiredRegistrations) {
            try {
                // Release tickets
                TicketType ticketType = registration.getTicketType();
                ticketType.releaseTickets(registration.getQuantity());
                ticketTypeRepository.save(ticketType);
                
                // Cancel registration
                registration.setStatus(Registration.RegistrationStatus.CANCELLED);
                registration.setCancelledAt(LocalDateTime.now());
                registrationRepository.save(registration);
                
                // Update event registration count
                Event event = registration.getEvent();
                event.setRegistrationCount(Math.max(0, event.getRegistrationCount() - registration.getQuantity()));
                eventRepository.save(event);
                
                log.info("Expired registration cancelled: {}", registration.getId());
            } catch (Exception e) {
                log.error("Error processing expired registration {}: {}", registration.getId(), e.getMessage());
            }
        }
        
        log.info("Processed {} expired registrations", expiredRegistrations.size());
    }

    // Private helper methods
    
    private String generateConfirmationCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    // Inner class for statistics
    @lombok.Builder
    @lombok.Data
    public static class RegistrationStatistics {
        private Long eventId;
        private long totalRegistrations;
        private long confirmedRegistrations;
        private long checkedInCount;
        private BigDecimal totalRevenue;
    }
}