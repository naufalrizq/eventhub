package com.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Review Entity - Represents user reviews for events
 */
@Entity
@Table(name = "reviews", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"}))
@Data
@EqualsAndHashCode(exclude = {"event", "user"})
@ToString(exclude = {"event", "user"})
@EntityListeners(AuditingEntityListener.class)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Column(length = 1000)
    private String comment;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = true;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public boolean canBeModified(User currentUser) {
        return user.equals(currentUser) && 
               createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    public void approve() {
        this.isApproved = true;
    }

    public void reject() {
        this.isApproved = false;
    }

    public void verify() {
        this.isVerified = true;
    }
}