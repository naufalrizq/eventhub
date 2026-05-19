package com.eventhub.repository;

import com.eventhub.entity.Event;
import com.eventhub.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for TicketType entity operations
 */
@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    // Find ticket types by event
    List<TicketType> findByEvent(Event event);

    // Find active ticket types by event
    List<TicketType> findByEventAndIsActiveTrue(Event event);

    // Find available ticket types
    @Query("SELECT tt FROM TicketType tt WHERE tt.event = :event AND tt.isActive = true " +
           "AND tt.availableQuantity > 0 " +
           "AND (tt.saleStartDate IS NULL OR tt.saleStartDate <= :now) " +
           "AND (tt.saleEndDate IS NULL OR tt.saleEndDate >= :now)")
    List<TicketType> findAvailableTicketTypes(@Param("event") Event event, @Param("now") LocalDateTime now);

    // Count ticket types by event
    long countByEvent(Event event);

    // Find ticket types with low availability
    @Query("SELECT tt FROM TicketType tt WHERE tt.isActive = true " +
           "AND tt.availableQuantity <= :threshold AND tt.availableQuantity > 0")
    List<TicketType> findTicketTypesWithLowAvailability(@Param("threshold") int threshold);

    // Find sold out ticket types
    @Query("SELECT tt FROM TicketType tt WHERE tt.isActive = true AND tt.availableQuantity = 0")
    List<TicketType> findSoldOutTicketTypes();

    // Get total revenue for event
    @Query("SELECT SUM(tt.price * (tt.totalQuantity - tt.availableQuantity)) FROM TicketType tt WHERE tt.event = :event")
    Double getTotalRevenueForEvent(@Param("event") Event event);

    // Get total tickets sold for event
    @Query("SELECT SUM(tt.totalQuantity - tt.availableQuantity) FROM TicketType tt WHERE tt.event = :event")
    Long getTotalTicketsSoldForEvent(@Param("event") Event event);
}