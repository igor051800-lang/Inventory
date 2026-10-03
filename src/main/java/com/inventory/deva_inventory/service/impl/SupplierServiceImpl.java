/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.RoleRepository;
import com.inventory.deva_inventory.dao.SupplierRepository;
import com.inventory.deva_inventory.dao.UserRepository;
import com.inventory.deva_inventory.model.Role;
import com.inventory.deva_inventory.model.Supplier;
import com.inventory.deva_inventory.model.User;
import com.inventory.deva_inventory.service.SupplierService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author best
 */
@Service
public class SupplierServiceImpl implements SupplierService{

    @Autowired
    private SupplierRepository supRepo;
    @Autowired
    private RoleRepository roleRepo;
    @Autowired
    private  UserRepository userDao;
        @Autowired
private PasswordEncoder encoder;

    @Override
    public Supplier saveSupplier(Supplier sup) {
        sup.setSupplierStatus("waiting");
        return supRepo.save(sup);
    }

    @Override
    public Supplier updateSupplier(Integer supId, Supplier sup) {
        Supplier supplier = findSupplier(supId);
        supplier.setSupplierName(sup.getSupplierName());
        supplier.setEmail(sup.getEmail());
        supplier.setPhone1(sup.getPhone1());
        supplier.setPhone2(sup.getPhone2());
        return supRepo.save(supplier);
    }

    @Override
    public void deleteSupplier(Integer supId) {
        if (!supRepo.existsById(supId)) {
            throw new ResourceNotFoundException("Supplier", "id", supId);
        }
        supRepo.deleteById(supId);
    }

    @Override
    public List<Supplier> getAllSupplier() {
        return supRepo.findAll();
    }

    @Override
    public Supplier changeSupplierStatus(Integer supId, String supplierStatus) {
        Supplier sup = findSupplier(supId);
        sup.setSupplierStatus(supplierStatus);
        return supRepo.save(sup);
    }

    @Transactional
    @Override
    public Supplier approveSupplier(Integer supId ,Supplier sup) {
        Supplier supplier = findSupplier(supId);
        Role role = roleRepo.findByRoleName("Supplier");
        if (role == null) {
            throw new ResourceNotFoundException("Role", "roleName", "Supplier");
        }
        supplier.setSupplierStatus("approved");
        supplier = supRepo.save(supplier);
        User user = new User();
        user.setSupplier(supplier);
        user.addRole(role);
        user.setUserName(sup.getUserName());
        user.setPassword(encoder.encode(sup.getPassword()));
        user.setUserStatus("enabled");
        userDao.save(user);
        return supplier;
    }

    @Override
    public Supplier declineSupplier(Integer supId) {
        Supplier sup = findSupplier(supId);
        sup.setSupplierStatus("decline");
        return supRepo.save(sup);
    }

    @Override
    public Supplier findSupplierByUser(String userName) {
        Supplier sup = supRepo.getSupplierByUserName(userName);
        if (sup == null) {
            throw new ResourceNotFoundException("Supplier", "userName", userName);
        }
        return sup;
    }

    private Supplier findSupplier(Integer supId) {
        return supRepo.findById(supId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supId));
    }

}
