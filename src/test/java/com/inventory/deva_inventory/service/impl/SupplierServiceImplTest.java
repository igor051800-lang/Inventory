package com.inventory.deva_inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.deva_inventory.dao.RoleRepository;
import com.inventory.deva_inventory.dao.SupplierRepository;
import com.inventory.deva_inventory.dao.UserRepository;
import com.inventory.deva_inventory.model.Role;
import com.inventory.deva_inventory.model.Supplier;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock private SupplierRepository supRepo;
    @Mock private RoleRepository roleRepo;
    @Mock private UserRepository userDao;
    @Mock private PasswordEncoder encoder;

    @InjectMocks
    private SupplierServiceImpl service;

    @Test
    void findSupplierByUserThrowsWhenMissing() {
        when(supRepo.getSupplierByUserName("ghost")).thenReturn(null);

        assertThatThrownBy(() -> service.findSupplierByUser("ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Supplier not found with userName 'ghost'");
    }

    @Test
    void approveSupplierReturnsPersistedSupplierNotRequestBody() {
        Supplier stored = new Supplier();
        stored.setSupplierId(1);
        Supplier request = new Supplier();
        request.setUserName("bob");
        request.setPassword("secret");
        when(supRepo.findById(1)).thenReturn(Optional.of(stored));
        when(roleRepo.findByRoleName("Supplier")).thenReturn(new Role());
        when(supRepo.save(stored)).thenReturn(stored);
        when(encoder.encode("secret")).thenReturn("hashed");

        Supplier result = service.approveSupplier(1, request);

        assertThat(result).isSameAs(stored);
        assertThat(result.getSupplierStatus()).isEqualTo("approved");
    }

    @Test
    void approveSupplierThrowsWhenSupplierRoleMissing() {
        when(supRepo.findById(1)).thenReturn(Optional.of(new Supplier()));
        when(roleRepo.findByRoleName("Supplier")).thenReturn(null);

        assertThatThrownBy(() -> service.approveSupplier(1, new Supplier()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(supRepo, never()).save(any());
        verify(userDao, never()).save(any());
    }
}
