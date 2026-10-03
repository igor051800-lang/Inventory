/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.RoleRepository;
import com.inventory.deva_inventory.model.Role;
import com.inventory.deva_inventory.service.RoleService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author mntemnte
 */
@Service
public class RoleServiceImpl implements RoleService{
  @Autowired
    private RoleRepository roleDao;

    @Override
    public Role saveRole(Role role) {
        return roleDao.save(role);
    }

    @Override
    public Role updateRole(Integer roleId, Role role) {
        Role rol = roleDao.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        rol.setRoleName(role.getRoleName());
        return roleDao.save(rol);
    }

    @Override
    public void deleteRole(Integer roleId) {
        if (!roleDao.existsById(roleId)) {
            throw new ResourceNotFoundException("Role", "id", roleId);
        }
        roleDao.deleteById(roleId);
    }

    @Override
    public List<Role> listRoles() {
        return roleDao.findAll();
    }

}
