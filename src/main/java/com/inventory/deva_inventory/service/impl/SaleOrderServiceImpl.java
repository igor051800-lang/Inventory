
package com.inventory.deva_inventory.service.impl;

import com.inventory.deva_inventory.dao.OrderRepository;
import com.inventory.deva_inventory.dao.SaleOrderRepository;
import com.inventory.deva_inventory.dao.SupplierRepository;
import com.inventory.deva_inventory.model.Order;
import com.inventory.deva_inventory.model.SaleOrder;
import com.inventory.deva_inventory.service.SaleOrderService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SaleOrderServiceImpl implements SaleOrderService{

    @Autowired
    private SaleOrderRepository saleOrderRepository;
    @Autowired 
    private  OrderRepository   orderRepository;
    @Autowired
    private SupplierRepository supplierRepository;

    @Transactional
    @Override
    public SaleOrder saveSaleOrder(Integer orderId, SaleOrder saleOrder) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setOrderStatus("supplier_spproved");
        order = orderRepository.save(order);
        saleOrder.setOrder(order);
        saleOrder.setSaleOrderStatus("supplier_created");
        return saleOrderRepository.save(saleOrder);
    }

    @Override
    public SaleOrder updateSaleOrder(Integer saleOrderId, SaleOrder sOrder) {
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", saleOrderId));
        saleOrder.setSaleOrderName(sOrder.getSaleOrderName());
        saleOrder.setSaleOrderNumber(sOrder.getSaleOrderNumber());
        saleOrder.setDescription(sOrder.getDescription());
        return saleOrderRepository.save(saleOrder);
    }

    @Override
    public void deleteSaleOrder(Integer saleOrderId) {
        if (!saleOrderRepository.existsById(saleOrderId)) {
            throw new ResourceNotFoundException("SaleOrder", "id", saleOrderId);
        }
        saleOrderRepository.deleteById(saleOrderId);
    }

    @Override
    public List<SaleOrder> listSaleOrder() {
        return saleOrderRepository.findAll();
    }

    @Override
    public SaleOrder listSaleOrderByOrder(Integer orderId) {
        SaleOrder saleOrder = saleOrderRepository.getAllSaleOrderByOrderId(orderId);
        if (saleOrder == null) {
            throw new ResourceNotFoundException("SaleOrder", "orderId", orderId);
        }
        return saleOrder;
    }

    @Override
    public List<SaleOrder> listAllSaleOrderBySupplier(Integer supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier", "id", supplierId);
        }
        return saleOrderRepository.getSaleOrderBySupplierId(supplierId);
    }

}
