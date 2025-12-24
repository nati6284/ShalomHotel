package com.shalom.shalomhotel.Service.impl;

import com.shalom.shalomhotel.Dto.*;
import com.shalom.shalomhotel.Exception.OurException;
import com.shalom.shalomhotel.Service.interfac.IUserService;
import com.shalom.shalomhotel.entity.User;
import com.shalom.shalomhotel.repository.UserRepository;
import com.shalom.shalomhotel.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService {

    @Autowired
    private  UserRepository userRepository;
    @Autowired

    private  PasswordEncoder passwordEncoder;
    @Autowired

    private  JWTUtils jwtUtils;
    @Autowired

    private  AuthenticationManager authenticationManager;



    @Override
    @Transactional
    public Response register(User user) {
        Response response = new Response();

        try {
            // Validate input
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                response.setMessage("Email is required");
                return response;
            }

            if (user.getPassword() == null || user.getPassword().isBlank()) {
                response.setMessage("Password is required");
                return response;
            }

            // Check if user already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                response.setMessage("Email already exists");
                return response;
            }

            // Set default role if not provided
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("USER");
            }

            // Validate role
            if (!user.getRole().equals("USER") && !user.getRole().equals("ADMIN")) {
                response.setMessage("Invalid role. Must be USER or ADMIN");
                return response;
            }

            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Save user
            User savedUser = userRepository.save(user);

            // Generate token
            String token = jwtUtils.generateToken(savedUser);

            // Map to DTO
            UserDTO userDTO = mapToUserDTO(savedUser);

            // Prepare response
            response.setMessage("User registered successfully");
            response.setToken(token);
            response.setRole(savedUser.getRole());
            response.setUser(userDTO);

        } catch (Exception e) {

            response.setMessage("Registration failed: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response login(LoginRequest loginRequest) {
        Response response = new Response();

        try {
            // Validate input
            if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()) {
                response.setMessage("Email is required");
                return response;
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
                response.setMessage("Password is required");
                return response;
            }

            // Authenticate
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Find user
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new OurException("Invalid credentials"));

            // Generate token
            String token = jwtUtils.generateToken(user);

            // Map to DTO
            UserDTO userDTO = mapToUserDTO(user);

            // Prepare response
            response.setMessage("Login successful");
            response.setToken(token);
            response.setRole(user.getRole());
            response.setUser(userDTO);

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Login failed: " + e.getMessage());
        }

        return response;
    }


    @Override
    @Transactional
    public Response updateUser(String userId, UpdateUserRequest updateRequest) {
        Response response = new Response();

        try {
            Long id = Long.parseLong(userId);

            // Find existing user
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new OurException("User not found"));

            // Update fields if provided
            if (updateRequest.getName() != null && !updateRequest.getName().isBlank()) {
                existingUser.setName(updateRequest.getName());
            }

            if (updateRequest.getPhoneNumber() != null && !updateRequest.getPhoneNumber().isBlank()) {
                existingUser.setPhoneNumber(updateRequest.getPhoneNumber());
            }

            // Update email if provided
            if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
                // Check if email already exists (if changing to a different email)
                if (!updateRequest.getEmail().equals(existingUser.getEmail())) {
                    if (userRepository.existsByEmail(updateRequest.getEmail())) {
                        response.setMessage("Email already exists");
                        return response;
                    }
                    existingUser.setEmail(updateRequest.getEmail());
                }
            }

            // Update password if provided
            if (updateRequest.getPassword() != null && !updateRequest.getPassword().isBlank()) {
                // Validate password length
                if (updateRequest.getPassword().length() < 6) {
                    response.setMessage("Password must be at least 6 characters long");
                    return response;
                }
                existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            }



            // Save updated user
            User updatedUser = userRepository.save(existingUser);

            // Generate new token if password or email was changed
            String token = null;
            if (updateRequest.getPassword() != null || updateRequest.getEmail() != null) {
                token = jwtUtils.generateToken(updatedUser);
            }

            // Map to DTO
            UserDTO userDTO = mapToUserDTO(updatedUser);

            response.setMessage("User updated successfully");
            response.setUser(userDTO);
            if (token != null) {
                response.setToken(token);
            }

        } catch (NumberFormatException e) {
            response.setMessage("Invalid user ID format");
        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to update user");
        }

        return response;
    }

    @Override
    public Response getAllUsers() {
        Response response = new Response();

        try {
            List<User> users = userRepository.findAll();

            if (users.isEmpty()) {
                response.setMessage("No users found");
                return response;
            }

            List<UserDTO> userDTOs = users.stream()
                    .map(this::mapToUserDTO)
                    .collect(Collectors.toList());

            response.setMessage("Users retrieved successfully");
            response.setUserList(userDTOs);

        } catch (Exception e) {

            response.setMessage("Failed to retrieve users");
        }

        return response;
    }

    @Override
    public Response getUserById(String userId) {
        Response response = new Response();

        try {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new OurException("User not found"));

            UserDTO userDTO = mapToUserDTO(user);

            response.setMessage("User retrieved successfully");
            response.setUser(userDTO);

        } catch (NumberFormatException e) {
            response.setMessage("Invalid user ID format");
        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to retrieve user");
        }

        return response;
    }

    @Override
    public Response getUserBookingHistory(String userId) {
        Response response = new Response();

        try {
            Long id = Long.parseLong(userId);

            // First, get the user
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new OurException("User not found"));

            UserDTO userDTO = mapToUserDTO(user);

            // Map bookings from user entity (if eager loading is configured)
            // Note: This only works if bookings are eagerly loaded or you fetch them
            List<BookingDTO> bookingDTOs = user.getBookings().stream()
                    .map(this::mapToBookingDTO)
                    .collect(Collectors.toList());

            // ALTERNATIVE: If you have a BookingService, use it instead:
            // List<BookingDTO> bookingDTOs = bookingService.getUserBookings(id);

            response.setMessage("Booking history retrieved successfully");
            response.setUser(userDTO);
            response.setBookingList(bookingDTOs);

        } catch (NumberFormatException e) {
            response.setMessage("Invalid user ID format");
        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to retrieve booking history");
        }

        return response;
    }

    @Override
    @Transactional
    public Response deleteUser(String userId) {
        Response response = new Response();

        try {
            Long id = Long.parseLong(userId);

            // Check if user exists
            if (!userRepository.existsById(id)) {
                response.setMessage("User not found");
                return response;
            }

            userRepository.deleteById(id);
            response.setMessage("User deleted successfully");

        } catch (NumberFormatException e) {
            response.setMessage("Invalid user ID format");
        } catch (Exception e) {

            response.setMessage("Failed to delete user");
        }

        return response;
    }

    @Override
    public Response getMyInfo(String email) {
        Response response = new Response();

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            UserDTO userDTO = mapToUserDTO(user);

            response.setMessage("User information retrieved successfully");
            response.setUser(userDTO);

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to retrieve user information");
        }

        return response;
    }

    // Helper methods
    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        return dto;
    }

    private BookingDTO mapToBookingDTO(com.shalom.shalomhotel.entity.Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingConfirmationCode(booking.getBookingConfirmationCode());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setBookingDate(booking.getBookingDate());

        // Map room info
        if (booking.getRoom() != null) {
            dto.setRoomId(booking.getRoom().getId());
            dto.setRoomNumber(booking.getRoom().getRoomNumber());
            if (booking.getRoom().getRoomType() != null) {
                dto.setRoomType(booking.getRoom().getRoomType().getTypeName());
            }
        }

        return dto;
    }
}