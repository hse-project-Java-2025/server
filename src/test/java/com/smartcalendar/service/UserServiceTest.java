package com.smartcalendar.service;

import com.smartcalendar.model.User;
import com.smartcalendar.repository.UserRepository;
import com.smartcalendar.repository.StatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(user);
    }

    @Test
    void testCreateUser_UsernameExists() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void testCreateUser_EmailExists() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        assertEquals("Email already exists", ex.getMessage());
    }

    @Test
    void testFindUserById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserById(1L);

        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
        assertEquals("testuser", foundUser.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.findUserById(99L));
    }

    @Test
    void testFindByUsername() {
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByUsername("testuser");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByUsername_NotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        Optional<User> found = userService.findByUsername("nouser");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        when(userRepository.findByEmail("no@mail.com")).thenReturn(Optional.empty());
        Optional<User> found = userService.findByEmail("no@mail.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertTrue(userService.existsByUsername("testuser"));
    }

    @Test
    void testExistsByEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(userService.existsByEmail("test@example.com"));
    }

    @Test
    void testUpdateEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("old@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updated = userService.updateEmail(1L, "new@example.com");
        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    void testUpdateEmail_NotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.updateEmail(2L, "new@example.com"));
    }

    @Test
    void testFindAllUsers() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<User> users = userService.findAllUsers();
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getUsername());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteAllUsersAndStatistics() {
        doNothing().when(statisticsRepository).deleteAll();
        doNothing().when(userRepository).deleteAll();
        userService.deleteAllUsersAndStatistics();
        verify(statisticsRepository).deleteAll();
        verify(userRepository).deleteAll();
    }

    @Test
    void testUserDetailsService() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("pass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        UserDetails details = userService.userDetailsService().loadUserByUsername("testuser");
        assertEquals("testuser", details.getUsername());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nouser"));
    }

    @Test
    void testLoadUserByEmail() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("pass");
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        UserDetails details = userService.loadUserByEmail("test@example.com");
        assertEquals("testuser", details.getUsername());
    }

    @Test
    void testLoadUserByEmail_NotFound() {
        when(userRepository.findByEmail("no@mail.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByEmail("no@mail.com"));
    }

    @Test
    void testChangeCredentials_Success() {
        User user = new User();
        user.setUsername("olduser");
        user.setPassword("oldpass");
        when(userRepository.findByUsername("olduser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", "oldpass")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = userService.changeCredentials("olduser", "oldpass", "newuser", "newpass");
        assertTrue(result);
        assertEquals("newuser", user.getUsername());
    }

    @Test
    void testChangeCredentials_Fail_WrongPassword() {
        User user = new User();
        user.setUsername("olduser");
        user.setPassword("oldpass");
        when(userRepository.findByUsername("olduser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "oldpass")).thenReturn(false);

        boolean result = userService.changeCredentials("olduser", "wrong", "newuser", "newpass");
        assertFalse(result);
    }

    @Test
    void testChangeCredentials_UserNotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.changeCredentials("nouser", "pass", "new", "new"));
    }

    @Test
    void testFindByLoginOrEmail_ByUsername() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Optional<User> found = userService.findByLoginOrEmail("testuser");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByLoginOrEmail_ByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        Optional<User> found = userService.findByLoginOrEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByLoginOrEmail_NotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nouser")).thenReturn(Optional.empty());
        Optional<User> found = userService.findByLoginOrEmail("nouser");
        assertFalse(found.isPresent());
    }
}