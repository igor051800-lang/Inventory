package com.inventory.deva_inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventory.deva_inventory.model.Inventory;
import com.inventory.deva_inventory.service.InventoryService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryContorller.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryContorllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService invService;

    @Test
    void inventoriesForUnknownStoreReturns404() throws Exception {
        when(invService.listAllInventorysByStoreId(42))
                .thenThrow(new ResourceNotFoundException("Store", "id", 42));

        mockMvc.perform(get("/api/inventories/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Store not found with id '42'"));
    }

    @Test
    void updateReturns404WhenInventoryMissing() throws Exception {
        when(invService.editInventory(eq(1), eq(2), any(Inventory.class)))
                .thenThrow(new ResourceNotFoundException("Inventory", "id", 1));

        mockMvc.perform(put("/api/inventories/1/2").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteReturns404WhenInventoryMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Inventory", "id", 9)).when(invService).deleteInventory(9);

        mockMvc.perform(delete("/api/inventories/9"))
                .andExpect(status().isNotFound());
    }
}
