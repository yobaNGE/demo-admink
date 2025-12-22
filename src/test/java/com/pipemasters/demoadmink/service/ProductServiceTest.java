package com.pipemasters.demoadmink.service;

import com.pipemasters.demoadmink.dto.ProductDto;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        productService = new ProductService(meterRegistry);
    }

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        // Given: initial data is created in constructor (2 products)

        // When
        List<ProductDto> products = productService.getAllProducts();

        // Then
        assertEquals(2, products.size());
    }

    @Test
    void getProductById_shouldReturnProduct_whenExists() {
        // Given
        Long existingId = 1L;

        // When
        Optional<ProductDto> result = productService.getProductById(existingId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getName());
    }

    @Test
    void getProductById_shouldReturnEmpty_whenNotExists() {
        // Given
        Long nonExistingId = 999L;

        // When
        Optional<ProductDto> result = productService.getProductById(nonExistingId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void createProduct_shouldCreateAndReturnProduct() {
        // Given
        ProductDto newProduct = ProductDto.builder()
                .name("New Product")
                .description("New description")
                .price(new BigDecimal("199.99"))
                .quantity(10)
                .build();

        // When
        ProductDto created = productService.createProduct(newProduct);

        // Then
        assertNotNull(created.getId());
        assertEquals("New Product", created.getName());
        assertEquals(new BigDecimal("199.99"), created.getPrice());
    }

    @Test
    void updateProduct_shouldUpdateAndReturnProduct_whenExists() {
        // Given
        Long existingId = 1L;
        ProductDto updatedProduct = ProductDto.builder()
                .name("Updated Product")
                .description("Updated description")
                .price(new BigDecimal("1299.99"))
                .quantity(25)
                .build();

        // When
        Optional<ProductDto> result = productService.updateProduct(existingId, updatedProduct);

        // Then
        assertTrue(result.isPresent());
        assertEquals(existingId, result.get().getId());
        assertEquals("Updated Product", result.get().getName());
    }

    @Test
    void updateProduct_shouldReturnEmpty_whenNotExists() {
        // Given
        Long nonExistingId = 999L;
        ProductDto updatedProduct = ProductDto.builder().name("Updated").build();

        // When
        Optional<ProductDto> result = productService.updateProduct(nonExistingId, updatedProduct);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteProduct_shouldReturnTrue_whenExists() {
        // Given
        Long existingId = 1L;

        // When
        boolean result = productService.deleteProduct(existingId);

        // Then
        assertTrue(result);
        assertTrue(productService.getProductById(existingId).isEmpty());
    }

    @Test
    void deleteProduct_shouldReturnFalse_whenNotExists() {
        // Given
        Long nonExistingId = 999L;

        // When
        boolean result = productService.deleteProduct(nonExistingId);

        // Then
        assertFalse(result);
    }

    @Test
    void createProduct_shouldIncrementCounter() {
        // Given
        ProductDto newProduct = ProductDto.builder()
                .name("Test")
                .price(new BigDecimal("99.99"))
                .quantity(5)
                .build();
        double initialCount = meterRegistry.counter("products_created_total").count();

        // When
        productService.createProduct(newProduct);

        // Then
        assertEquals(initialCount + 1, meterRegistry.counter("products_created_total").count());
    }

    @Test
    void deleteProduct_shouldIncrementCounter_whenSuccess() {
        // Given
        Long existingId = 1L;
        double initialCount = meterRegistry.counter("products_deleted_total").count();

        // When
        productService.deleteProduct(existingId);

        // Then
        assertEquals(initialCount + 1, meterRegistry.counter("products_deleted_total").count());
    }
}
