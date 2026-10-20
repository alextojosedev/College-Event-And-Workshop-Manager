package com.college.eventmanager.service;

import com.college.eventmanager.dao.RegistrationDao;
import com.college.eventmanager.dao.RegistrationDaoImpl;
import com.college.eventmanager.exception.ValidationException;
import com.college.eventmanager.model.Registration;

import java.util.List;

/**
 * Service managing student registrations, cancellations, and registration reports.
 */
public class RegistrationService {
    private final RegistrationDao registrationDao;

    public RegistrationService() {
        this(new RegistrationDaoImpl());
    }

    public RegistrationService(RegistrationDao registrationDao) {
        this.registrationDao = registrationDao;
    }

    /**
     * Registers a student for an event using transactional concurrency control.
     */
    public Registration registerStudent(int userId, int eventId, String notes) {
        if (userId <= 0) {
            throw new ValidationException("Invalid student ID.");
        }
        if (eventId <= 0) {
            throw new ValidationException("Invalid event ID.");
        }
        return registrationDao.registerStudentInTransaction(userId, eventId, notes != null ? notes.trim() : "");
    }

    /**
     * Cancels a student registration in a transaction, refunding the seat.
     */
    public boolean cancelRegistration(int registrationId, int userId) {
        if (registrationId <= 0) {
            throw new ValidationException("Invalid registration ID.");
        }
        return registrationDao.cancelRegistrationInTransaction(registrationId, userId);
    }

    public List<Registration> getStudentRegistrations(int userId) {
        return registrationDao.findByUserId(userId);
    }

    public List<Registration> getEventRegistrations(int eventId) {
        return registrationDao.findByEventId(eventId);
    }

    public List<Registration> getAllRegistrations() {
        return registrationDao.findAll();
    }

    public boolean isStudentRegistered(int userId, int eventId) {
        return registrationDao.isUserRegistered(userId, eventId);
    }
}
