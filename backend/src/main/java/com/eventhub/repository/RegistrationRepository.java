package com.eventhub.repository;

import com.eventhub.entity.Event;
import com.eventhub.entity.Registration;
import com.eventhub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Registration entity operations
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Find registrations by user
    Page<Registration> findByUser(User user, Pageable pageable);

    // Find registrations by event
    Page<Registration> findByEvent(Event event, Pageable pageable);

    // Find registrations by status
    Page<Registration> findByStatus(Registration.RegistrationStatus status, Pageable pageable);

    // Find registration by user and event
    Optional<Registration> findByUserAndEvent(User user, Event event);

    // Check if user is registered for event
    boolean existsByUserAndEvent(User user, Event event);

    // Find registrations by user and status
    Page<Registration> findByUserAndStatus(User user, Registration.RegistrationStatus status, Pageable pageable);

    // Find registrations by event and status
    Page<Registration> findByEventAndStatus(Event event, Registration.RegistrationStatus status, Pageable pageable);

    // Count registrations by event
    long countByEvent(Event event);

    // Count registrations by event and status
    long countByEventAndStatus(Event event, Registration.RegistrationStatus status);

    // Count registrations by user
    long countByUser(User user);

    // Find registrations created within date range
    @Query("SELECT r FROM Registration r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    Page<Registration> findRegistrationsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      Pageable pageable);

    // Find upcoming event registrations for user
    @Query("SELECT r FROM Registration r WHERE r.user = :user AND r.event.startDate > :now " +
           "AND r.status = 'CONFIRMED' ORDER BY r.event.startDate ASC")
    List<Registration> findUpcomingRegistrationsForUser(@Param("user") User user, @Param("now") LocalDateTime now);

    // Find past event registrations for user
    @Query("SELECT r FROM Registration r WHERE r.user = :user AND r.event.endDate < :now " +
           "ORDER BY r.event.endDate DESC")
    Page<Registration> findPastRegistrationsForUser(@Param("user") User user, @Param("now") LocalDateTime now, Pageable pageable);

    // Find registrations by confirmation code
    Optional<Registration> findByConfirmationCode(String confirmationCode);

    // Find registrations requiring check-in
    @Query("SELECT r FROM Registration r WHERE r.event = :event AND r.status = 'CONFIRMED' " +
           "AND r.checkedInAt IS NULL")
    List<Registration> findRegistrationsRequiringCheckIn(@Param("event") Event event);

    // Find checked-in registrations for event
    @Query("SELECT r FROM Registration r WHERE r.event = :event AND r.checkedInAt IS NOT NULL")
    List<Registration> findCheckedInRegistrations(@Param("event") Event event);

    // Count checked-in registrations for event
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event = :event AND r.checkedInAt IS NOT NULL")
    long countCheckedInRegistrations(@Param("event") Event event);

    // Find registrations with pending payments
    @Query("SELECT r FROM Registration r WHERE r.status = 'PENDING_PAYMENT' " +
           "AND r.createdAt < :cutoffTime")
    List<Registration> findRegistrationsWithPendingPayments(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find registrations by payment status
    @Query("SELECT r FROM Registration r WHERE r.paymentStatus = :paymentStatus")
    Page<Registration> findByPaymentStatus(@Param("paymentStatus") Registration.PaymentStatus paymentStatus, Pageable pageable);

    // Get registration statistics for event
    @Query("SELECT r.status, COUNT(r) FROM Registration r WHERE r.event = :event GROUP BY r.status")
    List<Object[]> getRegistrationStatsByEvent(@Param("event") Event event);

    // Get registration statistics for organizer
    @Query("SELECT r.status, COUNT(r) FROM Registration r WHERE r.event.organizer = :organizer GROUP BY r.status")
    List<Object[]> getRegistrationStatsByOrganizer(@Param("organizer") User organizer);

    // Find registrations by ticket type
    @Query("SELECT r FROM Registration r WHERE r.ticketType.id = :ticketTypeId")
    Page<Registration> findByTicketType(@Param("ticketTypeId") Long ticketTypeId, Pageable pageable);

    // Find registrations with special requirements
    @Query("SELECT r FROM Registration r WHERE r.specialRequirements IS NOT NULL AND r.specialRequirements != ''")
    Page<Registration> findRegistrationsWithSpecialRequirements(Pageable pageable);

    // Find registrations by event organizer
    @Query("SELECT r FROM Registration r WHERE r.event.organizer = :organizer")
    Page<Registration> findByEventOrganizer(@Param("organizer") User organizer, Pageable pageable);

    // Get revenue statistics
    @Query("SELECT SUM(r.totalAmount) FROM Registration r WHERE r.event = :event AND r.paymentStatus = 'COMPLETED'")
    Double getTotalRevenueForEvent(@Param("event") Event event);

    @Query("SELECT SUM(r.totalAmount) FROM Registration r WHERE r.event.organizer = :organizer AND r.paymentStatus = 'COMPLETED'")
    Double getTotalRevenueForOrganizer(@Param("organizer") User organizer);

    // Find registrations expiring soon
    @Query("SELECT r FROM Registration r WHERE r.status = 'PENDING_PAYMENT' " +
           "AND r.paymentDeadline BETWEEN :now AND :deadline")
    List<Registration> findRegistrationsExpiringSoon(@Param("now") LocalDateTime now, @Param("deadline") LocalDateTime deadline);

    // Advanced search with filters
    @Query("SELECT r FROM Registration r WHERE " +
           "(:eventId IS NULL OR r.event.id = :eventId) " +
           "AND (:userId IS NULL OR r.user.id = :userId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:paymentStatus IS NULL OR r.paymentStatus = :paymentStatus) " +
           "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.createdAt <= :endDate)")
    Page<Registration> findRegistrationsWithFilters(@Param("eventId") Long eventId,
                                                   @Param("userId") Long userId,
                                                   @Param("status") Registration.RegistrationStatus status,
                                                   @Param("paymentStatus") Registration.PaymentStatus paymentStatus,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);
}