package com.shalom.shalomhotel.Controller;

import com.shalom.shalomhotel.Dto.LoginRequest;
import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Service.interfac.IUserService;
import com.shalom.shalomhotel.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody User user) {
        try {
            Response response = userService.register(user);

            // Determine HTTP status based on the response content
            if (response.getUser() != null && response.getToken() != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 for successful registration
            } else if (response.getMessage() != null && response.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 for conflict
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 for other errors
            }
        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setMessage("Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 for server errors
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody LoginRequest loginRequest) {
        try {
            Response response = userService.login(loginRequest);

            // Determine HTTP status based on the response content
            if (response.getToken() != null && response.getUser() != null) {
                return ResponseEntity.ok(response); // 200 for successful login
            } else if (response.getMessage() != null &&
                    (response.getMessage().contains("Invalid") ||
                            response.getMessage().contains("not found"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 for authentication failure
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 for other errors
            }
        } catch (Exception e) {
            Response errorResponse = new Response();
            errorResponse.setMessage("Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 for server errors
        }
    }
} 