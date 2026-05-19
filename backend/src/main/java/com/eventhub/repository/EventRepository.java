package com.eventhub.repository;

import com.eventhub.entity.Event;
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
 * Repository interface for Event entity operations
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find events by status
    Page<Event> findByStatus(Event.EventStatus status, Pageable pageable);

    // Find events by organizer
    Page<Event> findByOrganizer(User organizer, Pageable pageable);

    // Find events by category
    Page<Event> findByCategory(String category, Pageable pageable);

    // Find events by location containing
    Page<Event> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    // Find upcoming events
    @Query("SELECT e FROM Event e WHERE e.startDate > :now AND e.status = 'PUBLISHED' ORDER BY e.startDate ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

    // Find events by date range
    @Query("SELECT e FROM Event e WHERE e.startDate >= :startDate AND e.endDate <= :endDate AND e.status = 'PUBLISHED'")
    Page<Event> findEventsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate, 
                                     Pageable pageable);

    // Search events by title or description
    @Query("SELECT e FROM Event e WHERE (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND e.status = 'PUBLISHED'")
    Page<Event> searchEvents(@Param("keyword") String keyword, Pageable pageable);

    // Find featured events
    @Query("SELECT e FROM Event e WHERE e.isFeatured = true AND e.status = 'PUBLISHED' ORDER BY e.createdAt DESC")
    List<Event> findFeaturedEvents();

    // Find events with available tickets
    @Query("SELECT DISTINCT e FROM Event e JOIN e.ticketTypes tt " +
           "WHERE tt.availableQuantity > 0 AND tt.isActive = true " +
           "AND e.status = 'PUBLISHED' AND e.startDate > :now")
    Page<Event> findEventsWithAvailableTickets(@Param("now") LocalDateTime now, Pageable pageable);

    // Find popular events (by registration count)
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' ORDER BY e.registrationCount DESC")
    Page<Event> findPopularEvents(Pageable pageable);

    // Find events by organizer and status
    Page<Event> findByOrganizerAndStatus(User organizer, Event.EventStatus status, Pageable pageable);

    // Count events by organizer
    long countByOrganizer(User organizer);

    // Count events by status
    long countByStatus(Event.EventStatus status);

    // Find events ending soon (for notifications)
    @Query("SELECT e FROM Event e WHERE e.endDate BETWEEN :now AND :endTime AND e.status = 'PUBLISHED'")
    List<Event> findEventsEndingSoon(@Param("now") LocalDateTime now, @Param("endTime") LocalDateTime endTime);

    // Find events starting soon (for notifications)
    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :now AND :startTime AND e.status = 'PUBLISHED'")
    List<Event> findEventsStartingSoon(@Param("now") LocalDateTime now, @Param("startTime") LocalDateTime startTime);

    // Check if event slug exists
    boolean existsBySlug(String slug);

    // Find by slug
    Optional<Event> findBySlug(String slug);

    // Find events by multiple categories
    @Query("SELECT e FROM Event e WHERE e.category IN :categories AND e.status = 'PUBLISHED'")
    Page<Event> findByCategories(@Param("categories") List<String> categories, Pageable pageable);

    // Advanced search with multiple filters
    @Query("SELECT e FROM Event e WHERE " +
           "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR e.category = :category) " +
           "AND (:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:startDate IS NULL OR e.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR e.endDate <= :endDate) " +
           "AND e.status = 'PUBLISHED'")
    Page<Event> findEventsWithFilters(@Param("keyword") String keyword,
                                     @Param("category") String category,
                                     @Param("location") String location,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);

    // Get event statistics
    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer = :organizer")
    long getTotalEventsByOrganizer(@Param("organizer") User organizer);

    @Query("SELECT SUM(e.registrationCount) FROM Event e WHERE e.organizer = :organizer")
    Long getTotalRegistrationsByOrganizer(@Param("organizer") User organizer);
}