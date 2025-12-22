package com.pipemasters.demoadmink.controller;

import com.pipemasters.demoadmink.dto.ProductDto;
import com.pipemasters.demoadmink.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "API для управления продуктами")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Получить все продукты", description = "Возвращает список всех продуктов")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка продуктов")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить продукт по ID", description = "Возвращает продукт по указанному ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Продукт найден"),
        @ApiResponse(responseCode = "404", description = "Продукт не найден")
    })
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "ID продукта") @PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Создать продукт", description = "Создает новый продукт")
    @ApiResponse(responseCode = "201", description = "Продукт успешно создан")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        ProductDto created = productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить продукт", description = "Обновляет существующий продукт")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Продукт успешно обновлен"),
        @ApiResponse(responseCode = "404", description = "Продукт не найден")
    })
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "ID продукта") @PathVariable Long id,
            @RequestBody ProductDto productDto) {
        return productService.updateProduct(id, productDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить продукт", description = "Удаляет продукт по ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Продукт успешно удален"),
        @ApiResponse(responseCode = "404", description = "Продукт не найден")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID продукта") @PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
