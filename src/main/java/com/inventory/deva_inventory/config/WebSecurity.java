/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.deva_inventory.config;

import com.inventory.deva_inventory.filter.CustomAuthenticationFilter;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 *
 * @author mntemnte
 */
@Configuration
@EnableWebSecurity
public class WebSecurity {

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            @Qualifier("userDetailService") UserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        CustomAuthenticationFilter customAuthentication = new CustomAuthenticationFilter(authenticationManager);
        customAuthentication.setFilterProcessesUrl("/api/login");
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/login/**", "/api/roles/**").permitAll()
//                        .requestMatchers(GET, "/api/courses/**").permitAll()
//                        .requestMatchers("/api/checktoken/**").permitAll()
//                        .requestMatchers(GET, "/api/categories/**").permitAll()
//                        .requestMatchers(GET, "/api/users/**").hasAnyAuthority("Admin")
//                        .requestMatchers(GET, "/api/requests/**").hasAnyAuthority("Admin")
//                        .requestMatchers(GET, "/api/your/courses/**").hasAnyAuthority("Teacher")
//                        .requestMatchers(GET, "/api/course/requests/**").hasAnyAuthority("Admin")
//                        .requestMatchers(POST, "/api/requests/approve/**").hasAnyAuthority("Admin")
//                        .requestMatchers(POST, "/api/users/**").hasAnyAuthority("ADMIN")
                        .anyRequest().permitAll())
                .addFilter(customAuthentication);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOriginPattern("*");
//      configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
