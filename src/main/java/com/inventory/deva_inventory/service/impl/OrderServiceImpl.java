/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.OrderRepository;
import com.inventory.deva_inventory.dao.SupplierRepository;
import com.inventory.deva_inventory.model.Order;
import com.inventory.deva_inventory.model.Supplier;
import com.inventory.deva_inventory.service.OrderService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author mntemnte
 */
@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private SupplierRepository supplierRepo;

    @Override
    public Order saveOrder(Integer supplierId,Order order) {
        Supplier sup = supplierRepo.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));
        order.setSupplier(sup);
        return orderRepo.save(order);
    }

    @Override
    public void deleteOrder(Integer orderId) {
        if (!orderRepo.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }
        orderRepo.deleteById(orderId);
    }

    @Override
    public Order editOrder(Integer orderId, Order order) {
        Order or = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        or.setOrderName(order.getOrderName());
        or.setOrderType(order.getOrderType());
        or.setDescription(order.getDescription());
        or.setOrderNumber(order.getOrderNumber());
        return orderRepo.save(or);
    }

    @Override
    public List<Order> listAllOrder() {
        return orderRepo.findAll();
    }

    @Override
    public List<Order> listAllOrderBySupplierId(Integer supplierId) {
        if (!supplierRepo.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier", "id", supplierId);
        }
        return orderRepo.getOrderBySupplierId(supplierId);
    }

}
