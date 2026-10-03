/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.AddressRepository;
import com.inventory.deva_inventory.dao.StoreRepository;
import com.inventory.deva_inventory.model.Address;

import com.inventory.deva_inventory.model.Store;
import com.inventory.deva_inventory.service.AddressService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author best
 */
@Service
public class AddressServiceImpl  implements AddressService{
  @Autowired
  private AddressRepository addressRepo;
  @Autowired
  private StoreRepository  storeRepo;

    @Override
    public Address findAddressById(Integer addressId) {
        return addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
    }

    @Override
    public Address saveStoreAddress(Integer parentId, Address addressData) {
        Store store = storeRepo.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", parentId));
        addressData.setStore(store);
        return addressRepo.save(addressData);
    }

    @Override
    public Address saveCompanyAddress(Integer parentId, Address a) {
        throw new UnsupportedOperationException("Saving company addresses is not implemented");
    }

    @Override
    public void deleteAddress(Integer addressId) {
        if (!addressRepo.existsById(addressId)) {
            throw new ResourceNotFoundException("Address", "id", addressId);
        }
        addressRepo.deleteById(addressId);
    }

    @Override
    public Address editStoreAddress(Integer parrentId, Address a) {
        throw new UnsupportedOperationException("Editing store addresses is not implemented");
    }

    @Override
    public Address editCompanyAddress(Integer parrentId, Address a) {
        throw new UnsupportedOperationException("Editing company addresses is not implemented");
    }

    @Override
    public List<Address> listAllStoreAddress() {
        return addressRepo.findAll().stream()
                .filter(address -> address.getStore() != null)
                .toList();
    }

}
