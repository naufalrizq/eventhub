package com.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
 * Registration entity representing event registrations
 */
@Entity
@Table(name = "registrations", indexes = {
    @Index(name = "idx_registration_user", columnList = "user_id"),
    @Index(name = "idx_registration_event", columnList = "event_id"),
    @Index(name = "idx_registration_status", columnList = "status"),
    @Index(name = "idx_registration_date", columnList = "registration_date")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "confirmation_code", unique = true)
    private String confirmationCode;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "check_in_date")
    private LocalDateTime checkInDate;

    @Column(name = "is_checked_in", nullable = false)
    private Boolean isCheckedIn = false;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    @Column(name = "dietary_restrictions")
    private String dietaryRestrictions;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;

    @Column(name = "waitlist_notified_date")
    private LocalDateTime waitlistNotifiedDate;

    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData; // JSON string for custom fields

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Ticket> tickets = new HashSet<>();

    // Helper methods
    public boolean canCheckIn() {
        return status == Status.CONFIRMED && 
               paymentStatus == PaymentStatus.COMPLETED &&
               !isCheckedIn;
    }

    public boolean canCancel() {
        LocalDateTime now = LocalDateTime.now();
        return status == Status.CONFIRMED &&
               event.getStartDate().isAfter(now.plusHours(24)); // 24 hours before event
    }

    public boolean isRefundable() {
        return canCancel() && totalAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public void generateConfirmationCode() {
        this.confirmationCode = "REG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void generateQRCode() {
        this.qrCode = "QR-" + id.toString() + "-" + confirmationCode;
    }

    // Enums
    public enum Status {
        PENDING,
        CONFIRMED,
        WAITLISTED,
        CANCELLED,
        REJECTED,
        EXPIRED
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED,
        PARTIALLY_REFUNDED
    }
}