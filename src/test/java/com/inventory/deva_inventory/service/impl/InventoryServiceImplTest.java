package com.inventory.deva_inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.deva_inventory.dao.InventoryRepository;
import com.inventory.deva_inventory.dao.StoreRepository;
import com.inventory.deva_inventory.model.Inventory;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock private InventoryRepository inventoryRepo;
    @Mock private StoreRepository storeRepo;

    @InjectMocks
    private InventoryServiceImpl service;

    @Test
    void saveInventoryThrowsWhenStoreMissing() {
        when(storeRepo.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveInventory(1, new Inventory()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Store not found with id '1'");
        verify(inventoryRepo, never()).save(any());
    }

    @Test
    void editInventoryThrowsWhenInventoryMissing() {
        when(inventoryRepo.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editInventory(5, 1, new Inventory()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Inventory not found with id '5'");
    }

    @Test
    void findByInventoryCodeThrowsWhenMissing() {
        when(inventoryRepo.getInventoryByInventoryCode("C-1")).thenReturn(null);

        assertThatThrownBy(() -> service.findInventoryByInventoryCode("C-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByStoreThrowsWhenStoreMissing() {
        when(storeRepo.existsById(8)).thenReturn(false);

        assertThatThrownBy(() -> service.listAllInventorysByStoreId(8))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
