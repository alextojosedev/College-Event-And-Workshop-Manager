package com.college.eventmanager.dao;

import com.college.eventmanager.model.Registration;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Registrations.
 * Includes transaction-backed methods for concurrent seat reservation and cancellation.
 */
public interface RegistrationDao {
    Optional<Registration> findById(int registrationId);
    Optional<Registration> findByCode(String registrationCode);
    Optional<Registration> findByUserAndEvent(int userId, int eventId);
    List<Registration> findByUserId(int userId);
    List<Registration> findByEventId(int eventId);
    List<Registration> findAll();
    boolean isUserRegistered(int userId, int eventId);
    int countTotalRegistrations();
    int countActiveRegistrations();

    /**
     * Executes registration creation and seat decrement atomically in a JDBC transaction.
     */
    Registration registerStudentInTransaction(int userId, int eventId, String notes);

    /**
     * Executes registration cancellation and seat increment atomically in a JDBC transaction.
     */
    boolean cancelRegistrationInTransaction(int registrationId, int userId);

    /**
     * Atomically cancels an event and updates all associated registrations in a transaction.
     */
    boolean cancelEventAndRegistrationsInTransaction(int eventId);
}
