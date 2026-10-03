/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.CompanyRepository;
import com.inventory.deva_inventory.dao.StoreRepository;
import com.inventory.deva_inventory.model.Company;
import com.inventory.deva_inventory.model.Store;
import com.inventory.deva_inventory.service.StoreService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author best
 */
@Service
public class StoreServiceImpl implements StoreService{
    @Autowired
 private StoreRepository storeRepo;
    @Autowired
   private CompanyRepository compRepo;

    @Override
    public Store saveStore(Integer companyId,Store storeData) {
        Company comp = compRepo.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));
        storeData.setCompany(comp);
        return storeRepo.save(storeData);
    }

    @Override
    public Store updateStore(Integer storeId, Store storeData) {
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));
        store.setStoreName(storeData.getStoreName());
        store.setStoreSize(storeData.getStoreSize());
        store.setBuilding(storeData.getBuilding());
        store.setFloor(storeData.getFloor());
        store.setRoom(storeData.getRoom());
        return storeRepo.save(store);
    }

    @Override
    public List<Store> getAllStores() {
        return storeRepo.findAll();
    }

    @Override
    public void deleteStore(Integer storeId) {
        if (!storeRepo.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", "id", storeId);
        }
        storeRepo.deleteById(storeId);
    }

}
