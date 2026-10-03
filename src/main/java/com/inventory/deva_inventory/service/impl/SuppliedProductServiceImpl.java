/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.OrderRepository;
import com.inventory.deva_inventory.dao.SaleOrderRepository;
import com.inventory.deva_inventory.dao.SuppliedProductRepository;
import com.inventory.deva_inventory.model.SaleOrder;
import com.inventory.deva_inventory.model.SuppliedProduct;
import com.inventory.deva_inventory.service.SuppliedProductService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuppliedProductServiceImpl implements SuppliedProductService {

    @Autowired
    private SuppliedProductRepository supProductRepo;
    @Autowired
    private SaleOrderRepository saleOrderRepo;
    @Autowired
    private OrderRepository orderRepo;

    @Override
    public SuppliedProduct saveSuppliedProduct(Integer saleOrderId, SuppliedProduct suppliedProduct) {
        SaleOrder saleOrder = saleOrderRepo.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", saleOrderId));
        suppliedProduct.setSaleOrder(saleOrder);
        suppliedProduct.setSuppliedProductStatus("send");
        return supProductRepo.save(suppliedProduct);
    }

    @Override
    public void deleteSuppliedProduct(Integer suppliedProductId) {
        if (!supProductRepo.existsById(suppliedProductId)) {
            throw new ResourceNotFoundException("SuppliedProduct", "id", suppliedProductId);
        }
        supProductRepo.deleteById(suppliedProductId);
    }

    @Override
    public SuppliedProduct editSuppliedProduct(Integer suppliedProductId, SuppliedProduct suppliedProduct) {
        SuppliedProduct supProduct = supProductRepo.findById(suppliedProductId)
                .orElseThrow(() -> new ResourceNotFoundException("SuppliedProduct", "id", suppliedProductId));
        supProduct.setSuppliedProductName(suppliedProduct.getSuppliedProductName());
        supProduct.setSuppliedProductPrice(suppliedProduct.getSuppliedProductPrice());
        supProduct.setSuppliedProductQuantity(suppliedProduct.getSuppliedProductQuantity());
        return supProductRepo.save(supProduct);
    }

    @Override
    public List<SuppliedProduct> listAllSuppliedProduct() {
        return supProductRepo.findAll();
    }

    @Override
    public List<SuppliedProduct> listAllSuppliedProductBySaleOrderId(Integer saleOrderId) {
        if (!saleOrderRepo.existsById(saleOrderId)) {
            throw new ResourceNotFoundException("SaleOrder", "id", saleOrderId);
        }
        return supProductRepo.getAllSuppliedProductBySaleOrderId(saleOrderId);
    }

    @Override
    public List<SuppliedProduct> listAllSuppliedProductByOrderId(Integer orderId) {
        if (!orderRepo.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }
        return supProductRepo.getSuppliedProductByOrderId(orderId);
    }

    @Override
    public List<SuppliedProduct> listAllSuppliedProductBySuppliedStatus() {
        return supProductRepo.getAllSuppliedProductByStatus("send");
    }

    @Override
    public List<SuppliedProduct> listAllSuppliedProductBySuppliedRecieved() {
        return supProductRepo.getAllSuppliedProductByStatus("recieved");
    }

}
