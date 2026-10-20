package com.college.eventmanager.service;

import com.college.eventmanager.dao.EventDao;
import com.college.eventmanager.dao.EventDaoImpl;
import com.college.eventmanager.dao.RegistrationDao;
import com.college.eventmanager.dao.RegistrationDaoImpl;
import com.college.eventmanager.dao.UserDao;
import com.college.eventmanager.dao.UserDaoImpl;
import com.college.eventmanager.exception.ValidationException;
import com.college.eventmanager.model.DashboardStats;
import com.college.eventmanager.model.Event;
import com.college.eventmanager.model.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing events and workshops, business validation, and dashboard analytics.
 */
public class EventService {
    private final EventDao eventDao;
    private final RegistrationDao registrationDao;
    private final UserDao userDao;

    public EventService() {
        this(new EventDaoImpl(), new RegistrationDaoImpl(), new UserDaoImpl());
    }

    public EventService(EventDao eventDao, RegistrationDao registrationDao, UserDao userDao) {
        this.eventDao = eventDao;
        this.registrationDao = registrationDao;
        this.userDao = userDao;
    }

    public List<Event> getAllEvents() {
        return eventDao.findAll();
    }

    public List<Event> getUpcomingEvents() {
        return eventDao.findUpcoming();
    }

    public List<Event> searchEvents(String query, String category, EventStatus status) {
        return eventDao.search(query, category, status);
    }

    public Optional<Event> getEventById(int eventId) {
        return eventDao.findById(eventId);
    }

    /**
     * Validates and creates a new Event or Workshop.
     */
    public Event createEvent(Event event) {
        validateEvent(event, true);
        event.setAvailableSeats(event.getMaxCapacity());
        if (event.getStatus() == null) {
            event.setStatus(EventStatus.UPCOMING);
        }
        return eventDao.create(event);
    }

    /**
     * Validates and updates an existing Event.
     */
    public boolean updateEvent(Event event) {
        validateEvent(event, false);
        Optional<Event> existingOpt = eventDao.findById(event.getEventId());
        if (existingOpt.isEmpty()) {
            throw new ValidationException("Event does not exist.");
        }
        Event existing = existingOpt.get();

        // Calculate booked seats
        int booked = existing.getMaxCapacity() - existing.getAvailableSeats();
        if (event.getMaxCapacity() < booked) {
            throw new ValidationException("Cannot reduce maximum capacity to " + event.getMaxCapacity() +
                ". There are already " + booked + " confirmed student registrations.");
        }

        // Adjust available seats based on change in capacity
        int capacityDiff = event.getMaxCapacity() - existing.getMaxCapacity();
        event.setAvailableSeats(existing.getAvailableSeats() + capacityDiff);

        return eventDao.update(event);
    }

    /**
     * Cancels an event and automatically cancels associated registrations in a transaction.
     */
    public boolean cancelEvent(int eventId) {
        Optional<Event> eventOpt = eventDao.findById(eventId);
        if (eventOpt.isEmpty()) {
            throw new ValidationException("Event does not exist.");
        }
        return registrationDao.cancelEventAndRegistrationsInTransaction(eventId);
    }

    /**
     * Deletes an event.
     */
    public boolean deleteEvent(int eventId) {
        return eventDao.delete(eventId);
    }

    /**
     * Aggregates stats for display in the Admin Dashboard.
     */
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalEvents(eventDao.countTotalEvents());
        stats.setUpcomingEvents(eventDao.countUpcomingEvents());
        stats.setTotalRegistrations(registrationDao.countActiveRegistrations());
        stats.setTotalAvailableSeats(eventDao.sumAvailableSeats());
        stats.setTotalStudents(userDao.countStudents());
        return stats;
    }

    private void validateEvent(Event event, boolean isNew) {
        if (event == null) {
            throw new ValidationException("Event data cannot be null.");
        }
        if (event.getEventCode() == null || event.getEventCode().trim().isEmpty()) {
            throw new ValidationException("Event Code is required (e.g., EVT-101).");
        }
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new ValidationException("Event Title is required.");
        }
        if (event.getCategory() == null) {
            throw new ValidationException("Event Category is required.");
        }
        if (event.getDateTime() == null) {
            throw new ValidationException("Date and Time is required.");
        }
        if (event.getVenue() == null || event.getVenue().trim().isEmpty()) {
            throw new ValidationException("Venue is required.");
        }
        if (event.getMaxCapacity() <= 0) {
            throw new ValidationException("Maximum capacity must be greater than zero.");
        }

        if (isNew) {
            if (eventDao.findByCode(event.getEventCode().trim()).isPresent()) {
                throw new ValidationException("Event code '" + event.getEventCode() + "' is already in use.");
            }
            if (event.getDateTime().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Event date and time must be in the future.");
            }
        }
    }
}
