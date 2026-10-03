/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.deva_inventory.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.deva_inventory.security.JwtService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 *
 * @author mntemnte
 */
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
 
    private final  AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, JwtService jwtService){
        super(authenticationManager);
        this.authenticationManager =authenticationManager;
        this.jwtService = jwtService;
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
     String username = request.getParameter("userName");
     String password = request.getParameter("password");
     return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
             username == null ? "" : username.trim(),
             password == null ? "" : password));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, Authentication auth) throws IOException, ServletException {
        User user = (User) auth.getPrincipal();
        String access_token = jwtService.createAccessToken(user.getUsername(),
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        String refresh_token = jwtService.createRefreshToken(user.getUsername());

        response.setHeader("access_token", access_token);
        response.setHeader("refresh_token", refresh_token);
        Map<String,String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }
}
