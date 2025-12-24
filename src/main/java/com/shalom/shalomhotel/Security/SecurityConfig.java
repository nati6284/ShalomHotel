package com.shalom.shalomhotel.Security;

import com.shalom.shalomhotel.Service.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailService customUserDetailsService;
    @Autowired
    private JWTAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request

                        .requestMatchers(
                                "/auth/**",
                                "/api/bookings/confirmation/**",
                                "/api/rooms/alltypes",
                                "/api/rooms/types/names",
                                "/api/rooms/types/**",
                                "/api/rooms/availability",
                                "/api/rooms/search",
                                "/api/rooms/all",
                                "/api/rooms/{id}"
                        ).permitAll()

                        .requestMatchers(
                                "/api/rooms/addtypes",
                                "/api/rooms/updatetypes/**",
                                "/api/rooms/deletetypes/**",
                                "/api/rooms/addroom",
                                "/api/rooms/updateRoom/**",
                                "/api/rooms/updatestatus/**",
                                "/api/rooms/delete/**",
                                "/api/rooms/inventory",
                                "/user/all"
                        ).authenticated()

                        .requestMatchers(
                                "/api/bookings/**",
                                "/user/get-by-id/**",
                                "/user/delete/**",
                                "/user/get-logged-in-profile-info",
                                "/user/get-user-booking/**",
                                "/user/update/**",
                                "/user/update-my-profile"
                        ).authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}