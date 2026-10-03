/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.RoleRepository;
import com.inventory.deva_inventory.dao.UserRepository;
import com.inventory.deva_inventory.model.Role;
import com.inventory.deva_inventory.model.User;
import com.inventory.deva_inventory.service.UserService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

/**
 *e
 * @author best
 */
@Service("userDetailService")
public class UserServiceImpl implements UserService,UserDetailsService{
    @Autowired
    private UserRepository userDao;
    @Autowired
    private RoleRepository roleDao;
    @Autowired
private PasswordEncoder encoder;
    @Override
    public User saveUser(Integer roleId,User user) {
        
        try {
              Role role = roleDao.findById(roleId).orElse(null);
              if (role == null || user.getUserName() == null || user.getUserName().isBlank()
                      || user.getPassword() == null || user.getPassword().isEmpty()) {
                  return null;
              }
              // The bootstrap (first) user must be an Admin, otherwise nobody could manage users afterwards.
              if (!hasUsers() && !ADMIN_ROLE.equals(role.getRoleName())) {
                  return null;
              }
              if (userDao.findByUserName(user.getUserName()) != null) {
                  return null;
              }
              user.setUserId(null);
              user.setPassword(encoder.encode(user.getPassword()));
              user.setUserStatus("enabled");
              user.setRoles(null);
              user.addRole(role);
              user = userDao.save(user);
        } catch (Exception e) {
            user =null;
        }
        return user;
    }

    @Override
    public User updatUser(Integer userId, User user) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void deleteUser(Integer userId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<User> listUsers() {
      List<User > listUser = null;
        try {
             listUser = userDao.findAll();
        } catch (Exception e) {
        }
  return  listUser;
    }    

    @Override
    public boolean hasUsers() {
        return userDao.count() > 0;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userDao.findByUserName(username);
    if(user == null){
        throw  new UsernameNotFoundException("no user with this user name found");
    }
    else{
           Collection<SimpleGrantedAuthority> authList =  new ArrayList<>();
           user.getRoles().forEach(role -> {
            authList.add(new SimpleGrantedAuthority(role.getRoleName()));
           });
        return new org.springframework.security.core.userdetails.User(
					user.getUserName(),
                                        user.getPassword(),
					authList);
    }
  
    }
//    
}
