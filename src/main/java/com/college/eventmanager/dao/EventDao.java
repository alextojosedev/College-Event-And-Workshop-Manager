package com.college.eventmanager.dao;

import com.college.eventmanager.model.Event;
import com.college.eventmanager.model.EventStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Events and Workshops.
 */
public interface EventDao {
    Optional<Event> findById(int eventId);
    Optional<Event> findByCode(String eventCode);
    List<Event> findAll();
    List<Event> findUpcoming();
    List<Event> search(String query, String category, EventStatus status);
    Event create(Event event);
    boolean update(Event event);
    boolean updateStatus(int eventId, EventStatus status);
    boolean cancelEvent(int eventId);
    boolean delete(int eventId);
    int countTotalEvents();
    int countUpcomingEvents();
    int sumAvailableSeats();
}
