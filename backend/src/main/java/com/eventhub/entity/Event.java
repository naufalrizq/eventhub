package com.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Event entity representing events in the system
 */
@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_event_organizer", columnList = "organizer_id"),
    @Index(name = "idx_event_category", columnList = "category"),
    @Index(name = "idx_event_status", columnList = "status"),
    @Index(name = "idx_event_start_date", columnList = "start_date"),
    @Index(name = "idx_event_location", columnList = "city, country"),
    @Index(name = "idx_event_featured", columnList = "is_featured")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Event title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Event description is required")
    @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "short_description")
    @Size(max = 300, message = "Short description must not exceed 300 characters")
    private String shortDescription;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "registration_start_date")
    private LocalDateTime registrationStartDate;

    @Column(name = "registration_end_date")
    private LocalDateTime registrationEndDate;

    @NotBlank(message = "Venue name is required")
    @Column(name = "venue_name", nullable = false)
    private String venueName;

    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "Country is required")
    @Column(nullable = false)
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type = EventType.IN_PERSON;

    @Column(name = "online_meeting_url")
    private String onlineMeetingUrl;

    @Column(name = "meeting_id")
    private String meetingId;

    @Column(name = "meeting_password")
    private String meetingPassword;

    @Positive(message = "Capacity must be positive")
    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "current_registrations", nullable = false)
    private Integer currentRegistrations = 0;

    @Column(name = "waitlist_enabled", nullable = false)
    private Boolean waitlistEnabled = false;

    @Column(name = "max_waitlist_size")
    private Integer maxWaitlistSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = false;

    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    @ElementCollection
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    private Set<String> imageUrls = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "event_tags", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "cancellation_policy", columnDefinition = "TEXT")
    private String cancellationPolicy;

    @Column(name = "refund_policy", columnDefinition = "TEXT")
    private String refundPolicy;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(name = "seo_title")
    private String seoTitle;

    @Column(name = "seo_description")
    private String seoDescription;

    @Column(name = "seo_keywords")
    private String seoKeywords;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Registration> registrations = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TicketType> ticketTypes = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EventSession> sessions = new HashSet<>();

    // Helper methods
    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        return status == Status.PUBLISHED &&
               (registrationStartDate == null || now.isAfter(registrationStartDate)) &&
               (registrationEndDate == null || now.isBefore(registrationEndDate)) &&
               currentRegistrations < capacity;
    }

    public boolean isWaitlistAvailable() {
        return waitlistEnabled && 
               currentRegistrations >= capacity &&
               (maxWaitlistSize == null || getWaitlistCount() < maxWaitlistSize);
    }

    public int getAvailableSpots() {
        return Math.max(0, capacity - currentRegistrations);
    }

    public int getWaitlistCount() {
        return (int) registrations.stream()
                .filter(r -> r.getStatus() == Registration.Status.WAITLISTED)
                .count();
    }

    public boolean isPastEvent() {
        return endDate.isBefore(LocalDateTime.now());
    }

    public boolean isUpcoming() {
        return startDate.isAfter(LocalDateTime.now());
    }

    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return startDate.isBefore(now) && endDate.isAfter(now);
    }

    public double getAverageRating() {
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // Enums
    public enum Category {
        TECHNOLOGY,
        BUSINESS,
        HEALTH_WELLNESS,
        EDUCATION,
        ARTS_CULTURE,
        SPORTS_FITNESS,
        FOOD_DRINK,
        MUSIC,
        NETWORKING,
        CHARITY,
        GOVERNMENT,
        SPIRITUALITY,
        FAMILY,
        FASHION,
        HOME_LIFESTYLE,
        AUTO_BOAT_AIR,
        HOBBIES,
        TRAVEL_OUTDOOR,
        COMMUNITY,
        OTHER
    }

    public enum EventType {
        IN_PERSON,
        ONLINE,
        HYBRID
    }

    public enum Status {
        DRAFT,
        PUBLISHED,
        CANCELLED,
        POSTPONED,
        COMPLETED
    }
}