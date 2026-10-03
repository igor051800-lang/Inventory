/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.CategoryRepository;

import com.inventory.deva_inventory.model.Category;
import com.inventory.deva_inventory.service.CategoryService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author best
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository catRepo;

    @Override
    public Category AddCategory(Category cat) {
        return catRepo.save(cat);
    }

    @Override
    public Category searchByCategoryId(Integer categoryId) {
        return findCategory(categoryId);
    }

    @Override
    public List<Category> ListAllCategory() {
        return catRepo.findAll();
    }

    @Transactional
    @Override
    public Category editCategory(Integer catId,Category catDetail) {
        Category cat = findCategory(catId);
        cat.setCategoryName(catDetail.getCategoryName());
        cat.setCategoryDescription(catDetail.getCategoryDescription());
        return catRepo.save(cat);
    }

    @Override
    public Category findCategoryByName(String categoryName) {
        Category cat = catRepo.findBycategoryName(categoryName);
        if (cat == null) {
            throw new ResourceNotFoundException("Category", "categoryName", categoryName);
        }
        return cat;
    }

    @Override
    public void deleteCategory(Integer catId) {
        if (!catRepo.existsById(catId)) {
            throw new ResourceNotFoundException("Category", "id", catId);
        }
        catRepo.deleteById(catId);
    }

    private Category findCategory(Integer categoryId) {
        return catRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

}
