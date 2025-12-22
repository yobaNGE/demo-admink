package com.pipemasters.demoadmink.service;

import com.pipemasters.demoadmink.dto.ProductDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class ProductService {

    private final Map<Long, ProductDto> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Counter productCreatedCounter;
    private final Counter productDeletedCounter;

    public ProductService(MeterRegistry meterRegistry) {
        this.productCreatedCounter = Counter.builder("products.created")
                .description("Количество созданных продуктов")
                .register(meterRegistry);
        this.productDeletedCounter = Counter.builder("products.deleted")
                .description("Количество удаленных продуктов")
                .register(meterRegistry);

        // Инициализация тестовых данных
        createProduct(ProductDto.builder()
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .quantity(50)
                .build());
        createProduct(ProductDto.builder()
                .name("Smartphone")
                .description("Latest smartphone model")
                .price(new BigDecimal("699.99"))
                .quantity(100)
                .build());
    }

    public List<ProductDto> getAllProducts() {
        log.info("Получение всех продуктов");
        return new ArrayList<>(products.values());
    }

    public Optional<ProductDto> getProductById(Long id) {
        log.info("Получение продукта с id: {}", id);
        return Optional.ofNullable(products.get(id));
    }

    public ProductDto createProduct(ProductDto productDto) {
        Long id = idGenerator.getAndIncrement();
        productDto.setId(id);
        products.put(id, productDto);
        productCreatedCounter.increment();
        log.info("Создан продукт: {}", productDto);
        return productDto;
    }

    public Optional<ProductDto> updateProduct(Long id, ProductDto productDto) {
        log.info("Обновление продукта с id: {}", id);
        if (products.containsKey(id)) {
            productDto.setId(id);
            products.put(id, productDto);
            return Optional.of(productDto);
        }
        return Optional.empty();
    }

    public boolean deleteProduct(Long id) {
        log.info("Удаление продукта с id: {}", id);
        ProductDto removed = products.remove(id);
        if (removed != null) {
            productDeletedCounter.increment();
            return true;
        }
        return false;
    }
}
