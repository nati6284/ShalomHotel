package com.shalom.shalomhotel.Controller;

import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Dto.UpdateUserRequest;
import com.shalom.shalomhotel.Dto.UserDTO;
import com.shalom.shalomhotel.Service.interfac.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllUsers(){
        Response response = userService.getAllUsers();

        if (response.getUserList() != null && !response.getUserList().isEmpty()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }
    }
    @GetMapping("/get-by-id/{userId}")
    public ResponseEntity<Response> getUserById(@PathVariable("userId") String userId) {
        try {
            Response response = userService.getUserById(userId);
            if (response.getUser() != null) {
                return ResponseEntity.ok(response); // HTTP 200
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // HTTP 404
            }
        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setMessage("Error retrieving user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // HTTP 500
        }
    }
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Response> deleteUser(@PathVariable("userId") String userId) {
        try {
            Response response = userService.deleteUser(userId);
            if (response.getMessage() != null && response.getMessage().contains("successfully")) {
                return ResponseEntity.ok(response); // HTTP 200
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // HTTP 404
            }
        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setMessage("Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // HTTP 500
        }
    }
    @GetMapping("/get-logged-in-profile-info")
    public ResponseEntity<Response> getLoggedUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Response response = userService.getMyInfo(email);

            if (response.getUser() != null) {
                return ResponseEntity.ok(response); // HTTP 200
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // HTTP 404
            }
        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setMessage("Error retrieving profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // HTTP 500
        }
    }

    @GetMapping("/get-user-booking/{userId}")
    public ResponseEntity<Response> getUserBookingHistory(@PathVariable("userId") String userId) {
        try {
            Response response = userService.getUserBookingHistory(userId);

            // Check if there are bookings in either place
            boolean hasBookings = (response.getBookingList() != null && !response.getBookingList().isEmpty()) ||
                    (response.getUser() != null &&
                            response.getUser().getBookings() != null &&
                            !response.getUser().getBookings().isEmpty());

            if (hasBookings) {
                return ResponseEntity.ok(response); // HTTP 200
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response); // HTTP 204
            }
        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setMessage("Error retrieving booking history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<Response> updateUser(
            @PathVariable("userId") String userId,
            @RequestBody UpdateUserRequest updateRequest) {

        try {
            // Validate request body
            if (updateRequest == null) {
                Response errorResponse = new Response();
                errorResponse.setMessage("Request body cannot be empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            Response response = userService.updateUser(userId, updateRequest);

            // Check response message to determine appropriate HTTP status
            if (response.getMessage().contains("successfully")) {
                return ResponseEntity.ok(response); // HTTP 200
            } else if (response.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // HTTP 404
            } else if (response.getMessage().contains("Invalid") ||
                    response.getMessage().contains("required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // HTTP 400
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500
            }

        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setMessage("Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/update-my-profile")
    public ResponseEntity<Response> updateMyProfile(@RequestBody UpdateUserRequest updateRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Get current user to get their ID
            Response currentUserResponse = userService.getMyInfo(email);
            if (currentUserResponse.getUser() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(currentUserResponse);
            }

            String userId = String.valueOf(currentUserResponse.getUser().getId());
            Response response = userService.updateUser(userId, updateRequest);

            if (response.getMessage().contains("successfully")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setMessage("Error updating profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}