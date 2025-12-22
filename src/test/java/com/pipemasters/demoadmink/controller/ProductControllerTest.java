package com.pipemasters.demoadmink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.demoadmink.dto.ProductDto;
import com.pipemasters.demoadmink.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void getAllProducts_shouldReturnAllProducts() throws Exception {
        // Given
        List<ProductDto> products = List.of(
                ProductDto.builder().id(1L).name("Laptop").price(new BigDecimal("999.99")).quantity(10).build(),
                ProductDto.builder().id(2L).name("Phone").price(new BigDecimal("699.99")).quantity(20).build()
        );
        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Phone"));
    }

    @Test
    void getProductById_shouldReturnProduct_whenExists() throws Exception {
        // Given
        ProductDto product = ProductDto.builder()
                .id(1L)
                .name("Laptop")
                .description("High-end laptop")
                .price(new BigDecimal("999.99"))
                .quantity(10)
                .build();
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void getProductById_shouldReturn404_whenNotExists() throws Exception {
        // Given
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_shouldReturn201AndCreatedProduct() throws Exception {
        // Given
        ProductDto inputProduct = ProductDto.builder()
                .name("New Product")
                .description("New description")
                .price(new BigDecimal("199.99"))
                .quantity(5)
                .build();
        ProductDto createdProduct = ProductDto.builder()
                .id(3L)
                .name("New Product")
                .description("New description")
                .price(new BigDecimal("199.99"))
                .quantity(5)
                .build();
        when(productService.createProduct(any(ProductDto.class))).thenReturn(createdProduct);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct_whenExists() throws Exception {
        // Given
        ProductDto inputProduct = ProductDto.builder()
                .name("Updated Product")
                .price(new BigDecimal("1299.99"))
                .quantity(15)
                .build();
        ProductDto updatedProduct = ProductDto.builder()
                .id(1L)
                .name("Updated Product")
                .price(new BigDecimal("1299.99"))
                .quantity(15)
                .build();
        when(productService.updateProduct(eq(1L), any(ProductDto.class))).thenReturn(Optional.of(updatedProduct));

        // When & Then
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    void updateProduct_shouldReturn404_whenNotExists() throws Exception {
        // Given
        ProductDto inputProduct = ProductDto.builder().name("Updated").build();
        when(productService.updateProduct(eq(999L), any(ProductDto.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_shouldReturn204_whenExists() throws Exception {
        // Given
        when(productService.deleteProduct(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_shouldReturn404_whenNotExists() throws Exception {
        // Given
        when(productService.deleteProduct(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }
}
