/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.BrandRepository;
import com.inventory.deva_inventory.model.Brand;
import com.inventory.deva_inventory.service.BrandService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author best
 */
@Service
public class BrandServiceImpl implements BrandService{

    @Autowired
    private BrandRepository brandRepo;

    @Override
    public Brand findBrandById(Integer brandId) {
        return brandRepo.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", brandId));
    }

    @Override
    public Brand findBrandByName(String brandName) {
        Brand brand = brandRepo.findByBrandName(brandName);
        if (brand == null) {
            throw new ResourceNotFoundException("Brand", "brandName", brandName);
        }
        return brand;
    }

    @Override
    public Brand saveBrand(Brand brandData) {
        return brandRepo.save(brandData);
    }

    @Override
    public void deleteBrand(Integer brandId) {
        if (!brandRepo.existsById(brandId)) {
            throw new ResourceNotFoundException("Brand", "id", brandId);
        }
        brandRepo.deleteById(brandId);
    }

    @Override
    public Brand editBrand(Integer brandId, Brand brandDetail) {
        Brand brand = findBrandById(brandId);
        brand.setBrandName(brandDetail.getBrandName());
        brand.setBrandDescription(brandDetail.getBrandDescription());
        return brandRepo.save(brand);
    }

    @Override
    public List<Brand> listAllBrand() {
        return brandRepo.findAll();
    }
}
