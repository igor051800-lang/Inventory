package com.inventory.deva_inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inventory.deva_inventory.model.Product;
import com.inventory.deva_inventory.service.ProductService;
import com.inventory.deva_inventory.service.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService prodService;

    @Test
    void getByProductNumberReturns404WhenMissing() throws Exception {
        when(prodService.getProductByProductNumber("P-1"))
                .thenThrow(new ResourceNotFoundException("Product", "productNumber", "P-1"));

        mockMvc.perform(get("/api/products/product-number/P-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with productNumber 'P-1'"))
                .andExpect(jsonPath("$.path").value("/api/products/product-number/P-1"));
    }

    @Test
    void updateReturns404WhenProductMissing() throws Exception {
        when(prodService.editProduct(eq(99), any(Product.class)))
                .thenThrow(new ResourceNotFoundException("Product", "id", 99));

        mockMvc.perform(put("/api/products/99").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id '99'"));
    }

    @Test
    void deleteReturns404WhenProductMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Product", "id", 5)).when(prodService).deleteProduct(5);

        mockMvc.perform(delete("/api/products/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listByOrderReturnsProducts() throws Exception {
        Product product = new Product();
        product.setProductName("Milk");
        when(prodService.listAllProductById(1)).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Milk"));
    }

    @Test
    void saveReturns409OnConstraintViolation() throws Exception {
        when(prodService.saveProduct(eq(1), eq(2), eq(3), eq(4), any(Product.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        mockMvc.perform(post("/api/products/1/2/3/4").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/products/category/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void malformedBodyReturns400() throws Exception {
        mockMvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
    }
}
