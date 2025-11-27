package com.shalom.shalomhotel.Service.impl;

import com.shalom.shalomhotel.Dto.LoginRequest;
import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Dto.UserDTO;
import com.shalom.shalomhotel.Exception.OurException;
import com.shalom.shalomhotel.Service.interfac.IUserService;
import com.shalom.shalomhotel.entity.User;
import com.shalom.shalomhotel.repository.UserRepository;
import com.shalom.shalomhotel.utils.JWTUtils;
import com.shalom.shalomhotel.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Response register(User user) {
        Response response = new Response();
        try {
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("USER");
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new OurException(user.getEmail() + " already exists");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(savedUser);

            response.setUser(userDTO);
            response.setMessage("User registered successfully");

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error occurred during user registration: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response login(LoginRequest loginRequest) {
        Response response = new Response();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            var user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new OurException("User not found"));

            var token = jwtUtils.generateToken(user);
            response.setToken(token);
            response.setRole(user.getRole());
            response.setExpirationTime("7 Days");
            response.setMessage("Login successful");
            response.setUser(Utils.mapUserEntityToUserDTO(user));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error occurred during user login: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAllUsers() {
        Response response = new Response();

        try {
            List<User> userList = userRepository.findAll();
            List<UserDTO> userDTOList = Utils.mapUserListEntityToUserListDTO(userList);

            if (userDTOList.isEmpty()) {
                response.setMessage("No users found");
            } else {
                response.setMessage("Users retrieved successfully");
                response.setUserList(userDTOList);
            }

        } catch (Exception e) {
            response.setMessage("Error getting all users: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getUserBookingHistory(String userId) {
        Response response = new Response();

        try {
            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new OurException("User not found"));
            UserDTO userDTO = Utils.mapUserEntityToUserDTOPlusUserBookingsAndRoom(user);

            response.setUser(userDTO);
            if (userDTO.getBookings() == null || userDTO.getBookings().isEmpty()) {
                response.setMessage("No booking history found for user");
            } else {
                response.setMessage("User booking history retrieved successfully");
                response.setUser(userDTO);
            }
            System.out.println("User bookings: " + user.getBookings());

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error getting user booking history: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response deleteUser(String userId) {
        Response response = new Response();

        try {
            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new OurException("User not found"));
            userRepository.deleteById(Long.valueOf(userId));
            response.setMessage("User deleted successfully");

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error deleting user: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getUserById(String userId) {
        Response response = new Response();

        try {
            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new OurException("User not found"));
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);

            response.setMessage("User retrieved successfully");
            response.setUser(userDTO);

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error getting user by ID: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getMyInfo(String email) {
        Response response = new Response();

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);

            response.setMessage("User information retrieved successfully");
            response.setUser(userDTO);

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error getting user information: " + e.getMessage());
        }
        return response;
    }
}