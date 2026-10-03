/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.deva_inventory.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.inventory.deva_inventory.model.User;
import com.inventory.deva_inventory.response.TokenResponse;
import com.inventory.deva_inventory.security.JwtService;
import com.inventory.deva_inventory.service.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mntemnte
 */
@RestController
@RequestMapping("/api")
public class UserController {
     
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    
     
    @PostMapping("/users/{roleId}")
public ResponseEntity<User>  saveUser(@PathVariable Integer roleId,@RequestBody User user){
      User  ur = userService.saveUser(roleId,user);
        if (ur == null) {
            return ResponseEntity.badRequest().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Responded", "CategoryController");
        return ResponseEntity.accepted().headers(headers).body(ur);
    }
    
        @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Integer userId,@RequestBody User user){
        User ur = userService.updatUser(userId, user);
      return ResponseEntity.ok(ur);
    }
 @GetMapping("/users")
 public  ResponseEntity<List<User>> ListAllUser(){
   List<User> urList = userService.listUsers();
   return  ResponseEntity.ok().body(urList);
 }


   @DeleteMapping("/users/{userId}")
 public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable  Integer userId){
          
    userService.deleteUser(userId);
     
     Map<String,Boolean> response = new HashMap<>();
      response.put("deleted", Boolean.TRUE);
       return ResponseEntity.ok(response);
 
}
   @PostMapping("/checktoken/{token}")
 public  ResponseEntity<TokenResponse> validateToken(@PathVariable String  token){
        DecodedJWT decodedJwt;
        try {
            decodedJwt = jwtService.verifyAccessToken(token);
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setUserName(decodedJwt.getSubject());
        tokenResponse.setRoles(jwtService.getRoles(decodedJwt));
        return  ResponseEntity.ok().body(tokenResponse);
 }
}
