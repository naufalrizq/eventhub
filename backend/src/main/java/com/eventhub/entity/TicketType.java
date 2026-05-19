package com.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TicketType Entity - Represents different types of tickets for events
 */
@Entity
@Table(name = "ticket_types")
@Data
@EqualsAndHashCode(exclude = {"event", "registrations"})
@ToString(exclude = {"event", "registrations"})
@EntityListeners(AuditingEntityListener.class)
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ticket type name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Total quantity is required")
    @Min(value = 1, message = "Total quantity must be at least 1")
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Min(value = 0, message = "Available quantity cannot be negative")
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Min(value = 1, message = "Max per user must be at least 1")
    @Column(name = "max_per_user")
    private Integer maxPerUser;

    @Column(name = "sale_start_date")
    private LocalDateTime saleStartDate;

    @Column(name = "sale_end_date")
    private LocalDateTime saleEndDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToMany(mappedBy = "ticketType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Registration> registrations;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               availableQuantity > 0 &&
               (saleStartDate == null || now.isAfter(saleStartDate)) &&
               (saleEndDate == null || now.isBefore(saleEndDate));
    }

    public void reserveTickets(int quantity) {
        if (availableQuantity < quantity) {
            throw new IllegalStateException("Not enough tickets available");
        }
        this.availableQuantity -= quantity;
    }

    public void releaseTickets(int quantity) {
        this.availableQuantity = Math.min(totalQuantity, availableQuantity + quantity);
    }

    @PrePersist
    public void prePersist() {
        if (availableQuantity == null) {
            availableQuantity = totalQuantity;
        }
    }
}