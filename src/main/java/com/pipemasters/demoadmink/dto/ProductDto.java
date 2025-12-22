package com.pipemasters.demoadmink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для продукта")
public class ProductDto {

    @Schema(description = "Уникальный идентификатор продукта", example = "1")
    private Long id;

    @Schema(description = "Название продукта", example = "Laptop")
    private String name;

    @Schema(description = "Описание продукта", example = "High-performance laptop")
    private String description;

    @Schema(description = "Цена продукта", example = "999.99")
    private BigDecimal price;

    @Schema(description = "Количество на складе", example = "50")
    private Integer quantity;
}
