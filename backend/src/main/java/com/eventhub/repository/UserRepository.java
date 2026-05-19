package com.eventhub.repository;

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
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by email
    Optional<User> findByEmail(String email);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find by role
    Page<User> findByRole(User.UserRole role, Pageable pageable);

    // Find active users
    Page<User> findByIsActiveTrue(Pageable pageable);

    // Find users by email verification status
    Page<User> findByIsEmailVerified(boolean isEmailVerified, Pageable pageable);

    // Search users by name or email
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    // Find users created within date range
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Page<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);

    // Find users by last login date
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :date")
    List<User> findUsersLoggedInSince(@Param("date") LocalDateTime date);

    // Find inactive users (not logged in for a while)
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);

    // Count users by role
    long countByRole(User.UserRole role);

    // Count active users
    long countByIsActiveTrue();

    // Count verified users
    long countByIsEmailVerifiedTrue();

    // Find organizers (users who have created events)
    @Query("SELECT DISTINCT u FROM User u JOIN u.organizedEvents e")
    Page<User> findOrganizers(Pageable pageable);

    // Find users with most registrations
    @Query("SELECT u FROM User u ORDER BY SIZE(u.registrations) DESC")
    Page<User> findMostActiveUsers(Pageable pageable);

    // Find users by location
    @Query("SELECT u FROM User u WHERE LOWER(u.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<User> findByLocationContaining(@Param("location") String location, Pageable pageable);

    // Find users who registered for specific event
    @Query("SELECT DISTINCT u FROM User u JOIN u.registrations r WHERE r.event.id = :eventId")
    List<User> findUsersRegisteredForEvent(@Param("eventId") Long eventId);

    // Find users with pending email verification
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false AND u.emailVerificationToken IS NOT NULL")
    List<User> findUsersWithPendingEmailVerification();

    // Find users by email verification token
    Optional<User> findByEmailVerificationToken(String token);

    // Find users by password reset token
    Optional<User> findByPasswordResetToken(String token);

    // Find users with expired password reset tokens
    @Query("SELECT u FROM User u WHERE u.passwordResetToken IS NOT NULL AND u.passwordResetExpiry < :now")
    List<User> findUsersWithExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    // Get user statistics
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countNewUsersInPeriod(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.registrations r WHERE r.createdAt >= :startDate")
    long countActiveUsersInPeriod(@Param("startDate") LocalDateTime startDate);

    // Find users by multiple criteria
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) " +
           "AND (:isActive IS NULL OR u.isActive = :isActive) " +
           "AND (:isEmailVerified IS NULL OR u.isEmailVerified = :isEmailVerified) " +
           "AND (:location IS NULL OR LOWER(u.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<User> findUsersWithFilters(@Param("role") User.UserRole role,
                                   @Param("isActive") Boolean isActive,
                                   @Param("isEmailVerified") Boolean isEmailVerified,
                                   @Param("location") String location,
                                   Pageable pageable);
}