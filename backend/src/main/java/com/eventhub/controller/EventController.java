package com.eventhub.controller;

import com.eventhub.entity.Event;
import com.eventhub.entity.User;
import com.eventhub.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Event management
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class EventController {

    private final EventService eventService;

    /**
     * Get all events with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<Page<Event>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.debug("GET /api/events - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Event> events = eventService.getAllEvents(pageable);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Get event by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        log.debug("GET /api/events/{}", id);
        
        return eventService.getEventById(id)
            .map(event -> ResponseEntity.ok(event))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get event by slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Event> getEventBySlug(@PathVariable String slug) {
        log.debug("GET /api/events/slug/{}", slug);
        
        return eventService.getEventBySlug(slug)
            .map(event -> ResponseEntity.ok(event))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new event
     */
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody Event event,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("POST /api/events - Creating event: {}", event.getTitle());
        
        // Set the organizer to current user
        event.setOrganizer(currentUser);
        
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Update existing event
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @eventService.getEventById(#id).orElse(null)?.organizer?.id == authentication.principal.id)")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody Event eventDetails) {
        
        log.info("PUT /api/events/{} - Updating event", id);
        
        try {
            Event updatedEvent = eventService.updateEvent(id, eventDetails);
            return ResponseEntity.ok(updatedEvent);
        } catch (RuntimeException e) {
            log.error("Error updating event {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete event
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @eventService.getEventById(#id).orElse(null)?.organizer?.id == authentication.principal.id)")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.info("DELETE /api/events/{}", id);
        
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting event {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get events by current user (organizer)
     */
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Event>> getMyEvents(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/events/my-events for user: {}", currentUser.getEmail());
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Event> events = eventService.getEventsByOrganizer(currentUser, pageable);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Get upcoming events
     */
    @GetMapping("/upcoming")
    public ResponseEntity<Page<Event>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/events/upcoming");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventService.getUpcomingEvents(pageable);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Get featured events
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Event>> getFeaturedEvents() {
        log.debug("GET /api/events/featured");
        
        List<Event> events = eventService.getFeaturedEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Get popular events
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<Event>> getPopularEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/events/popular");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventService.getPopularEvents(pageable);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Search events
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Event>> searchEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/events/search?keyword={}", keyword);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<Event> events = eventService.searchEvents(keyword, pageable);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Get events by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Event>> getEventsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/events/category/{}", category);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<Event> events = eventService.getEventsByCategory(category, pageable);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Get events by location
     */
    @GetMapping("/location")
    public ResponseEntity<Page<Event>> getEventsByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/events/location?location={}", location);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<Event> events = eventService.getEventsByLocation(location, pageable);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Get events with available tickets
     */
    @GetMapping("/available-tickets")
    public ResponseEntity<Page<Event>> getEventsWithAvailableTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/events/available-tickets");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<Event> events = eventService.getEventsWithAvailableTickets(pageable);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Advanced search with multiple filters
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<Page<Event>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.debug("GET /api/events/advanced-search with filters");
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Event> events = eventService.searchEventsWithFilters(
            keyword, category, location, startDate, endDate, pageable);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Publish event
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @eventService.getEventById(#id).orElse(null)?.organizer?.id == authentication.principal.id)")
    public ResponseEntity<Event> publishEvent(@PathVariable Long id) {
        log.info("POST /api/events/{}/publish", id);
        
        try {
            Event publishedEvent = eventService.publishEvent(id);
            return ResponseEntity.ok(publishedEvent);
        } catch (RuntimeException e) {
            log.error("Error publishing event {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel event
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ORGANIZER') and @eventService.getEventById(#id).orElse(null)?.organizer?.id == authentication.principal.id)")
    public ResponseEntity<Event> cancelEvent(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        log.info("POST /api/events/{}/cancel", id);
        
        try {
            Event cancelledEvent = eventService.cancelEvent(id, reason);
            return ResponseEntity.ok(cancelledEvent);
        } catch (RuntimeException e) {
            log.error("Error cancelling event {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get event statistics for organizer
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<EventService.EventStatistics> getEventStatistics(
            @AuthenticationPrincipal User currentUser) {
        
        log.debug("GET /api/events/statistics for user: {}", currentUser.getEmail());
        
        EventService.EventStatistics statistics = eventService.getEventStatistics(currentUser);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get events by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Event>> getEventsByStatus(
            @PathVariable Event.EventStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/events/status/{}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Event> events = eventService.getEventsByStatus(status, pageable);
        
        return ResponseEntity.ok(events);
    }
}