package com.inventory.deva_inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.deva_inventory.dao.BrandRepository;
import com.inventory.deva_inventory.dao.CategoryRepository;
import com.inventory.deva_inventory.dao.InventoryRepository;
import com.inventory.deva_inventory.dao.OrderRepository;
import com.inventory.deva_inventory.dao.ProductRepository;
import com.inventory.deva_inventory.dao.SuppliedProductRepository;
import com.inventory.deva_inventory.dao.SupplierRepository;
import com.inventory.deva_inventory.model.Product;
import com.inventory.deva_inventory.model.SuppliedProduct;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepo;
    @Mock private OrderRepository orderRepo;
    @Mock private SupplierRepository supplierRepo;
    @Mock private SuppliedProductRepository supProductRepository;
    @Mock private CategoryRepository catRepo;
    @Mock private BrandRepository brandRepo;
    @Mock private InventoryRepository invRepo;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void editProductThrowsWhenMissing() {
        when(productRepo.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editProduct(1, new Product()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id '1'");
        verify(productRepo, never()).save(any());
    }

    @Test
    void getProductByProductNumberThrowsWhenMissing() {
        when(productRepo.getProductByProductNumber("X")).thenReturn(null);

        assertThatThrownBy(() -> service.getProductByProductNumber("X"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProductThrowsWhenMissing() {
        when(productRepo.existsById(3)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteProduct(3))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepo, never()).deleteById(anyInt());
    }

    @Test
    void addProductToInventoryThrowsWhenInventoryCodeUnknown() {
        when(invRepo.getInventoryByInventoryCode("INV-X")).thenReturn(null);

        assertThatThrownBy(() -> service.addProductToInventory(1, "INV-X"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("inventoryCode");
        verify(productRepo, never()).save(any());
    }

    @Test
    void saveProductDoesNotMarkSuppliedProductWhenCategoryMissing() {
        SuppliedProduct supplied = new SuppliedProduct();
        supplied.setSuppliedProductStatus("send");
        when(supProductRepository.findById(2)).thenReturn(Optional.of(supplied));
        when(catRepo.findById(3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveProduct(1, 2, 3, 4, new Product()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id '3'");
        assertThat(supplied.getSuppliedProductStatus()).isEqualTo("send");
        verify(supProductRepository, never()).save(any());
        verify(productRepo, never()).save(any());
    }
}
