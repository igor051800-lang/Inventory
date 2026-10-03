/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.CompanyRepository;
import com.inventory.deva_inventory.model.Company;
import com.inventory.deva_inventory.service.CompanyService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author best
 */
@Service
public class CompanyServiceImpl implements CompanyService{
    @Autowired
 private CompanyRepository compRepo;

    @Override
    public Company saveCompany(Company comData) {
        return compRepo.save(comData);
    }

    @Override
    public Company editCompany(Integer companyId, Company compData) {
        Company comp = compRepo.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));
        comp.setCompanyName(compData.getCompanyName());
        comp.setEmail(compData.getEmail());
        comp.setPhone1(compData.getPhone1());
        comp.setPhone2(compData.getPhone2());
        return compRepo.save(comp);
    }

    @Transactional
    @Override
    public  Company  findCompany() {
        return compRepo.findFirstByOrderByCompanyIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("No company has been registered"));
    }

}
