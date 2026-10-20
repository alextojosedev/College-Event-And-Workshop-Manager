package com.college.eventmanager.dao;

import com.college.eventmanager.model.Student;
import com.college.eventmanager.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entities.
 * Demonstrates Abstraction in OOP.
 */
public interface UserDao {
    Optional<User> findById(int userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User create(User user);
    boolean update(User user);
    boolean delete(int userId);
    List<Student> findAllStudents();
    int countStudents();
}
