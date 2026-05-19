package com.eventhub.service;

import com.eventhub.entity.Event;
import com.eventhub.entity.User;
import com.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Event-related business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Get all events with pagination
     */
    @Cacheable(value = "events", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Event> getAllEvents(Pageable pageable) {
        log.debug("Fetching all events with pagination: {}", pageable);
        return eventRepository.findAll(pageable);
    }

    /**
     * Get event by ID
     */
    @Cacheable(value = "event", key = "#id")
    public Optional<Event> getEventById(Long id) {
        log.debug("Fetching event by ID: {}", id);
        return eventRepository.findById(id);
    }

    /**
     * Get event by slug
     */
    @Cacheable(value = "event", key = "#slug")
    public Optional<Event> getEventBySlug(String slug) {
        log.debug("Fetching event by slug: {}", slug);
        return eventRepository.findBySlug(slug);
    }

    /**
     * Create new event
     */
    @Transactional
    @CacheEvict(value = {"events", "featuredEvents", "upcomingEvents"}, allEntries = true)
    public Event createEvent(Event event) {
        log.info("Creating new event: {}", event.getTitle());
        
        // Generate slug if not provided
        if (event.getSlug() == null || event.getSlug().isEmpty()) {
            event.setSlug(generateSlug(event.getTitle()));
        }
        
        // Ensure slug is unique
        event.setSlug(ensureUniqueSlug(event.getSlug()));
        
        // Set default status if not provided
        if (event.getStatus() == null) {
            event.setStatus(Event.EventStatus.DRAFT);
        }
        
        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with ID: {}", savedEvent.getId());
        return savedEvent;
    }

    /**
     * Update existing event
     */
    @Transactional
    @CacheEvict(value = {"event", "events", "featuredEvents", "upcomingEvents"}, allEntries = true)
    public Event updateEvent(Long id, Event eventDetails) {
        log.info("Updating event with ID: {}", id);
        
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
        
        // Update fields
        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setStartDate(eventDetails.getStartDate());
        event.setEndDate(eventDetails.getEndDate());
        event.setLocation(eventDetails.getLocation());
        event.setCategory(eventDetails.getCategory());
        event.setMaxCapacity(eventDetails.getMaxCapacity());
        event.setIsFeatured(eventDetails.getIsFeatured());
        event.setImageUrl(eventDetails.getImageUrl());
        
        // Update slug if title changed
        if (!event.getTitle().equals(eventDetails.getTitle())) {
            String newSlug = generateSlug(eventDetails.getTitle());
            if (!newSlug.equals(event.getSlug())) {
                event.setSlug(ensureUniqueSlug(newSlug));
            }
        }
        
        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully: {}", updatedEvent.getId());
        return updatedEvent;
    }

    /**
     * Delete event
     */
    @Transactional
    @CacheEvict(value = {"event", "events", "featuredEvents", "upcomingEvents"}, allEntries = true)
    public void deleteEvent(Long id) {
        log.info("Deleting event with ID: {}", id);
        
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
        
        // Check if event can be deleted (no confirmed registrations)
        if (event.getRegistrationCount() > 0) {
            throw new RuntimeException("Cannot delete event with existing registrations");
        }
        
        eventRepository.delete(event);
        log.info("Event deleted successfully: {}", id);
    }

    /**
     * Get events by organizer
     */
    public Page<Event> getEventsByOrganizer(User organizer, Pageable pageable) {
        log.debug("Fetching events by organizer: {}", organizer.getEmail());
        return eventRepository.findByOrganizer(organizer, pageable);
    }

    /**
     * Get events by status
     */
    @Cacheable(value = "eventsByStatus", key = "#status + '-' + #pageable.pageNumber")
    public Page<Event> getEventsByStatus(Event.EventStatus status, Pageable pageable) {
        log.debug("Fetching events by status: {}", status);
        return eventRepository.findByStatus(status, pageable);
    }

    /**
     * Get upcoming events
     */
    @Cacheable(value = "upcomingEvents", key = "#pageable.pageNumber")
    public Page<Event> getUpcomingEvents(Pageable pageable) {
        log.debug("Fetching upcoming events");
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable);
    }

    /**
     * Get featured events
     */
    @Cacheable(value = "featuredEvents")
    public List<Event> getFeaturedEvents() {
        log.debug("Fetching featured events");
        return eventRepository.findFeaturedEvents();
    }

    /**
     * Search events
     */
    public Page<Event> searchEvents(String keyword, Pageable pageable) {
        log.debug("Searching events with keyword: {}", keyword);
        return eventRepository.searchEvents(keyword, pageable);
    }

    /**
     * Get events by category
     */
    @Cacheable(value = "eventsByCategory", key = "#category + '-' + #pageable.pageNumber")
    public Page<Event> getEventsByCategory(String category, Pageable pageable) {
        log.debug("Fetching events by category: {}", category);
        return eventRepository.findByCategory(category, pageable);
    }

    /**
     * Get events by location
     */
    public Page<Event> getEventsByLocation(String location, Pageable pageable) {
        log.debug("Fetching events by location: {}", location);
        return eventRepository.findByLocationContainingIgnoreCase(location, pageable);
    }

    /**
     * Get events with available tickets
     */
    public Page<Event> getEventsWithAvailableTickets(Pageable pageable) {
        log.debug("Fetching events with available tickets");
        return eventRepository.findEventsWithAvailableTickets(LocalDateTime.now(), pageable);
    }

    /**
     * Get popular events
     */
    @Cacheable(value = "popularEvents", key = "#pageable.pageNumber")
    public Page<Event> getPopularEvents(Pageable pageable) {
        log.debug("Fetching popular events");
        return eventRepository.findPopularEvents(pageable);
    }

    /**
     * Publish event
     */
    @Transactional
    @CacheEvict(value = {"event", "events", "upcomingEvents"}, allEntries = true)
    public Event publishEvent(Long id) {
        log.info("Publishing event with ID: {}", id);
        
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
        
        // Validate event can be published
        validateEventForPublishing(event);
        
        event.setStatus(Event.EventStatus.PUBLISHED);
        Event publishedEvent = eventRepository.save(event);
        
        log.info("Event published successfully: {}", publishedEvent.getId());
        return publishedEvent;
    }

    /**
     * Cancel event
     */
    @Transactional
    @CacheEvict(value = {"event", "events", "upcomingEvents"}, allEntries = true)
    public Event cancelEvent(Long id, String reason) {
        log.info("Cancelling event with ID: {}", id);
        
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
        
        event.setStatus(Event.EventStatus.CANCELLED);
        // You might want to add a cancellation reason field to the Event entity
        
        Event cancelledEvent = eventRepository.save(event);
        
        // TODO: Send cancellation notifications to registered users
        
        log.info("Event cancelled successfully: {}", cancelledEvent.getId());
        return cancelledEvent;
    }

    /**
     * Get event statistics for organizer
     */
    public EventStatistics getEventStatistics(User organizer) {
        log.debug("Fetching event statistics for organizer: {}", organizer.getEmail());
        
        long totalEvents = eventRepository.getTotalEventsByOrganizer(organizer);
        Long totalRegistrations = eventRepository.getTotalRegistrationsByOrganizer(organizer);
        
        return EventStatistics.builder()
            .totalEvents(totalEvents)
            .totalRegistrations(totalRegistrations != null ? totalRegistrations : 0L)
            .build();
    }

    /**
     * Advanced event search with filters
     */
    public Page<Event> searchEventsWithFilters(String keyword, String category, String location,
                                              LocalDateTime startDate, LocalDateTime endDate,
                                              Pageable pageable) {
        log.debug("Advanced event search with filters");
        return eventRepository.findEventsWithFilters(keyword, category, location, startDate, endDate, pageable);
    }

    // Private helper methods
    
    private String generateSlug(String title) {
        return title.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }

    private String ensureUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int counter = 1;
        
        while (eventRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }

    private void validateEventForPublishing(Event event) {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Event title is required for publishing");
        }
        
        if (event.getStartDate() == null) {
            throw new RuntimeException("Event start date is required for publishing");
        }
        
        if (event.getStartDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot publish event with past start date");
        }
        
        if (event.getEndDate() != null && event.getEndDate().isBefore(event.getStartDate())) {
            throw new RuntimeException("Event end date must be after start date");
        }
        
        if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Event location is required for publishing");
        }
    }

    // Inner class for statistics
    @lombok.Builder
    @lombok.Data
    public static class EventStatistics {
        private long totalEvents;
        private long totalRegistrations;
    }
}