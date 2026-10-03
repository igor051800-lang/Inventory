/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.OrderProductRepository;
import com.inventory.deva_inventory.dao.OrderRepository;
import com.inventory.deva_inventory.model.Order;
import com.inventory.deva_inventory.model.OrderProduct;
import com.inventory.deva_inventory.service.OrderProductService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author mntemnte
 */
@Service
public class OrderProductServiceImpl implements OrderProductService{
@Autowired
private OrderProductRepository orderProductRepo;
@Autowired 
private  OrderRepository orderRepo;

    @Override
    public OrderProduct saveOrderProduct( Integer orderId, OrderProduct orderPro) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        orderPro.setOrder(order);
        return orderProductRepo.save(orderPro);
    }

    @Override
    public void deleteOrderProduct(Integer orderProductId) {
        if (!orderProductRepo.existsById(orderProductId)) {
            throw new ResourceNotFoundException("OrderProduct", "id", orderProductId);
        }
        orderProductRepo.deleteById(orderProductId);
    }

    @Override
    public OrderProduct editOrderProduct(Integer orderProductId, OrderProduct orderProduct) {
        OrderProduct orderPro = orderProductRepo.findById(orderProductId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderProduct", "id", orderProductId));
        orderPro.setOrderProductName(orderProduct.getOrderProductName());
        orderPro.setOrderProductQuantity(orderProduct.getOrderProductQuantity());
        orderPro.setOrderProductPrice(orderProduct.getOrderProductPrice());
        return orderProductRepo.save(orderPro);
    }

    @Override
    public List<OrderProduct> listAllOrderProduct() {
        return orderProductRepo.findAll();
    }

    @Transactional
    @Override
    public List<OrderProduct> listAllOrderProductById(Integer orderId) {
        if (!orderRepo.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }
        return orderProductRepo.getAllProductById(orderId);
    }

}
