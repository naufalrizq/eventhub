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
import java.util.List;

/**
 * EventSession Entity - Represents individual sessions within an event
 */
@Entity
@Table(name = "event_sessions")
@Data
@EqualsAndHashCode(exclude = {"event", "speakers"})
@ToString(exclude = {"event", "speakers"})
@EntityListeners(AuditingEntityListener.class)
public class EventSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Session title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(length = 2000)
    private String description;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Column(length = 100)
    private String location;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Min(value = 0, message = "Current capacity cannot be negative")
    @Column(name = "current_capacity", nullable = false)
    private Integer currentCapacity = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Size(max = 500, message = "Requirements must not exceed 500 characters")
    @Column(length = 500)
    private String requirements;

    @Size(max = 500, message = "Materials must not exceed 500 characters")
    @Column(length = 500)
    private String materials;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToMany
    @JoinTable(
        name = "session_speakers",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> speakers;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isAvailable() {
        return isActive && 
               (maxCapacity == null || currentCapacity < maxCapacity) &&
               startTime.isAfter(LocalDateTime.now());
    }

    public boolean hasCapacity() {
        return maxCapacity == null || currentCapacity < maxCapacity;
    }

    public void addParticipant() {
        if (!hasCapacity()) {
            throw new IllegalStateException("Session is at full capacity");
        }
        this.currentCapacity++;
    }

    public void removeParticipant() {
        if (currentCapacity > 0) {
            this.currentCapacity--;
        }
    }

    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }

    public boolean isCompleted() {
        return LocalDateTime.now().isAfter(endTime);
    }

    @PreUpdate
    @PrePersist
    public void validateTimes() {
        if (endTime != null && startTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}