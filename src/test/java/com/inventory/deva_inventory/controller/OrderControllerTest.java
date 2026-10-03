package com.inventory.deva_inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventory.deva_inventory.model.Order;
import com.inventory.deva_inventory.service.OrderService;
import com.inventory.deva_inventory.service.SupplierService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private SupplierService supService;

    @Test
    void ordersForUnknownSupplierUserReturns404() throws Exception {
        when(supService.findSupplierByUser("ghost"))
                .thenThrow(new ResourceNotFoundException("Supplier", "userName", "ghost"));

        mockMvc.perform(get("/api/orders/ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Supplier not found with userName 'ghost'"));
        verify(orderService, never()).listAllOrderBySupplierId(anyInt());
    }

    @Test
    void updateReturns404WhenOrderMissing() throws Exception {
        when(orderService.editOrder(eq(7), any(Order.class)))
                .thenThrow(new ResourceNotFoundException("Order", "id", 7));

        mockMvc.perform(put("/api/orders/7").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void saveReturns404WhenSupplierMissing() throws Exception {
        when(orderService.saveOrder(eq(3), any(Order.class)))
                .thenThrow(new ResourceNotFoundException("Supplier", "id", 3));

        mockMvc.perform(post("/api/orders/3").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }
}
