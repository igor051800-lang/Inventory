package com.inventory.deva_inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.deva_inventory.dao.OrderRepository;
import com.inventory.deva_inventory.dao.SupplierRepository;
import com.inventory.deva_inventory.model.Order;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepo;
    @Mock private SupplierRepository supplierRepo;

    @InjectMocks
    private OrderServiceImpl service;

    @Test
    void saveOrderThrowsWhenSupplierMissing() {
        when(supplierRepo.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveOrder(1, new Order()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Supplier not found with id '1'");
        verify(orderRepo, never()).save(any());
    }

    @Test
    void editOrderThrowsWhenMissing() {
        when(orderRepo.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editOrder(2, new Order()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteOrderThrowsWhenMissing() {
        when(orderRepo.existsById(3)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteOrder(3))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(orderRepo, never()).deleteById(anyInt());
    }
}
