/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.InventoryRepository;
import com.inventory.deva_inventory.dao.StoreRepository;
import com.inventory.deva_inventory.model.Inventory;
import com.inventory.deva_inventory.model.Store;
import com.inventory.deva_inventory.service.InventoryService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.Date;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class InventoryServiceImpl implements InventoryService{

    @Autowired
    private InventoryRepository inventoryRepo;
    @Autowired
    private StoreRepository storeRepo;

    @Override
    public Inventory saveInventory(Integer storeId, Inventory inventory) {
        Store store = findStore(storeId);
        inventory.setInventoryDate(new Date());
        inventory.setStore(store);
        return inventoryRepo.save(inventory);
    }

    @Override
    public void deleteInventory(Integer inventoryId) {
        if (!inventoryRepo.existsById(inventoryId)) {
            throw new ResourceNotFoundException("Inventory", "id", inventoryId);
        }
        inventoryRepo.deleteById(inventoryId);
    }

    @Override
    public Inventory editInventory(Integer inventoryId,Integer storeId,Inventory inventory) {
        Inventory inv = inventoryRepo.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));
        Store store = findStore(storeId);
        inv.setInventoryName(inventory.getInventoryName());
        inv.setInventoryDate(inventory.getInventoryDate());
        inv.setStore(store);
        return inventoryRepo.save(inv);
    }

    @Override
    public List<Inventory> listAllInventorys() {
        return inventoryRepo.findAll();
    }

    @Transactional
    @Override
    public List<Inventory> listAllInventorysByStoreId(Integer storeId) {
        if (!storeRepo.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", "id", storeId);
        }
        return inventoryRepo.getAllInventoryByStoreId(storeId);
    }

    @Override
    public Inventory findInventoryByInventoryCode(String inventoryCode) {
        Inventory inv = inventoryRepo.getInventoryByInventoryCode(inventoryCode);
        if (inv == null) {
            throw new ResourceNotFoundException("Inventory", "inventoryCode", inventoryCode);
        }
        return inv;
    }

    @Override
    public List<Inventory> findInventoryByReorderLevel(Integer reorderLevel) {
        return inventoryRepo.getInventoryByReorderLevel(reorderLevel);
    }

    @Override
    public List<Inventory> findInventoryByAlertLevel(Integer alertLevel) {
        return inventoryRepo.getInventoryByAlertLevel(alertLevel);
    }

    private Store findStore(Integer storeId) {
        return storeRepo.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));
    }

}
