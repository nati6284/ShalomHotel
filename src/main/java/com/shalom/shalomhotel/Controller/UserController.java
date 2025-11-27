package com.shalom.shalomhotel.Controller;

import com.shalom.shalomhotel.Dto.Response;
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
        // Check if user list exists and is not empty
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
            if (response.getBookingList() != null && !response.getBookingList().isEmpty()) {
                return ResponseEntity.ok(response); // HTTP 200
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response); // HTTP 204
            }
        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setMessage("Error retrieving booking history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // HTTP 500
        }
    }
}