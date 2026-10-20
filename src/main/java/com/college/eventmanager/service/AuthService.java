package com.college.eventmanager.service;

import com.college.eventmanager.dao.UserDao;
import com.college.eventmanager.dao.UserDaoImpl;
import com.college.eventmanager.exception.AuthenticationException;
import com.college.eventmanager.exception.ValidationException;
import com.college.eventmanager.model.Student;
import com.college.eventmanager.model.User;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service handling authentication, password checks, and student self-registration.
 */
public class AuthService {
    private final UserDao userDao;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public AuthService() {
        this(new UserDaoImpl());
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticates a user against database records with hashed password verification.
     */
    public User authenticate(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new ValidationException("Password cannot be empty.");
        }

        Optional<User> userOpt = userDao.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = userOpt.get();
        if (!PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password.");
        }

        return user;
    }

    /**
     * Registers a new Student account with validation checks.
     */
    public Student registerStudent(String username, String rawPassword, String fullName, 
                                   String email, String phone, String studentIdNumber, 
                                   String department) {
        // Validations
        if (username == null || username.trim().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name is required.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("A valid email address is required.");
        }
        if (studentIdNumber == null || studentIdNumber.trim().isEmpty()) {
            throw new ValidationException("Student College ID / Roll Number is required.");
        }

        // Check uniqueness
        if (userDao.findByUsername(username.trim()).isPresent()) {
            throw new ValidationException("Username '" + username + "' is already taken.");
        }
        if (userDao.findByEmail(email.trim()).isPresent()) {
            throw new ValidationException("An account with email '" + email + "' already exists.");
        }

        Student student = new Student();
        student.setUsername(username.trim());
        student.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
        student.setFullName(fullName.trim());
        student.setEmail(email.trim().toLowerCase());
        student.setPhone(phone != null ? phone.trim() : "");
        student.setStudentIdNumber(studentIdNumber.trim());
        student.setDepartment(department != null ? department.trim() : "");

        User created = userDao.create(student);
        return (Student) created;
    }
}
