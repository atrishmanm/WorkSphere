package com.worksphere.service;

import com.worksphere.dao.UserDAO;
import com.worksphere.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
public class UserServiceTest {
    
    @Mock
    private UserDAO userDAO;
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userDAO);
    }
    
    @Test
    void testCreateUser_Success() throws SQLException {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String fullName = "Test User";
        
        User expectedUser = new User(username, email, fullName);
        expectedUser.setId(1);
        
        when(userDAO.usernameExists(username)).thenReturn(false);
        when(userDAO.emailExists(email)).thenReturn(false);
        when(userDAO.createUser(any(User.class))).thenReturn(expectedUser);
        
        // Act
        User result = userService.createUser(username, email, fullName);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(fullName, result.getFullName());
        
        verify(userDAO).usernameExists(username);
        verify(userDAO).emailExists(email);
        verify(userDAO).createUser(any(User.class));
    }
    
    @Test
    void testCreateUser_DuplicateUsername() throws SQLException {
        // Arrange
        String username = "existinguser";
        String email = "test@example.com";
        String fullName = "Test User";
        
        when(userDAO.usernameExists(username)).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(username, email, fullName)
        );
        
        assertTrue(exception.getMessage().contains("Username"));
        assertTrue(exception.getMessage().contains("already exists"));
        
        verify(userDAO).usernameExists(username);
        verify(userDAO, never()).createUser(any(User.class));
    }
    
    @Test
    void testCreateUser_DuplicateEmail() throws SQLException {
        // Arrange
        String username = "testuser";
        String email = "existing@example.com";
        String fullName = "Test User";
        
        when(userDAO.usernameExists(username)).thenReturn(false);
        when(userDAO.emailExists(email)).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(username, email, fullName)
        );
        
        assertTrue(exception.getMessage().contains("Email"));
        assertTrue(exception.getMessage().contains("already exists"));
        
        verify(userDAO).usernameExists(username);
        verify(userDAO).emailExists(email);
        verify(userDAO, never()).createUser(any(User.class));
    }
    
    @Test
    void testCreateUser_InvalidUsername() throws SQLException {
        // Arrange
        String invalidUsername = "a"; // too short
        String email = "test@example.com";
        String fullName = "Test User";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(invalidUsername, email, fullName)
        );
        
        assertTrue(exception.getMessage().contains("Username must be 3-20 characters"));
        
        verify(userDAO, never()).usernameExists(anyString());
        verify(userDAO, never()).createUser(any(User.class));
    }
    
    @Test
    void testCreateUser_InvalidEmail() throws SQLException {
        // Arrange
        String username = "testuser";
        String invalidEmail = "invalid-email";
        String fullName = "Test User";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(username, invalidEmail, fullName)
        );
        
        assertTrue(exception.getMessage().contains("Invalid email format"));
        
        verify(userDAO, never()).usernameExists(anyString());
        verify(userDAO, never()).createUser(any(User.class));
    }
    
    @Test
    void testCreateUser_EmptyFullName() throws SQLException {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String emptyFullName = "";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(username, email, emptyFullName)
        );
        
        assertTrue(exception.getMessage().contains("Full name cannot be null or empty"));
        
        verify(userDAO, never()).usernameExists(anyString());
        verify(userDAO, never()).createUser(any(User.class));
    }
    
    @Test
    void testFindUserById_Success() throws SQLException {
        // Arrange
        int userId = 1;
        User expectedUser = new User("testuser", "test@example.com", "Test User");
        expectedUser.setId(userId);
        
        when(userDAO.findById(userId)).thenReturn(Optional.of(expectedUser));
        
        // Act
        Optional<User> result = userService.findUserById(userId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        
        verify(userDAO).findById(userId);
    }
    
    @Test
    void testFindUserById_NotFound() throws SQLException {
        // Arrange
        int userId = 999;
        
        when(userDAO.findById(userId)).thenReturn(Optional.empty());
        
        // Act
        Optional<User> result = userService.findUserById(userId);
        
        // Assert
        assertFalse(result.isPresent());
        
        verify(userDAO).findById(userId);
    }
    
    @Test
    void testFindUserById_InvalidId() throws SQLException {
        // Arrange
        int invalidId = -1;
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.findUserById(invalidId)
        );
        
        assertTrue(exception.getMessage().contains("User ID must be positive"));
        
        verify(userDAO, never()).findById(anyInt());
    }
}
