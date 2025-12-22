package com.pipemasters.demoadmink.service;

import com.pipemasters.demoadmink.dto.ProductDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
    private final Counter productUpdatedCounter;
    private final Counter productViewsCounter;
    private final Timer productOperationTimer;

    public ProductService(MeterRegistry meterRegistry) {
        this.productCreatedCounter = Counter.builder("products_created_total")
                .description("Total number of products created")
                .register(meterRegistry);
        this.productDeletedCounter = Counter.builder("products_deleted_total")
                .description("Total number of products deleted")
                .register(meterRegistry);
        this.productUpdatedCounter = Counter.builder("products_updated_total")
                .description("Total number of products updated")
                .register(meterRegistry);
        this.productViewsCounter = Counter.builder("products_views_total")
                .description("Total number of product views")
                .register(meterRegistry);
        this.productOperationTimer = Timer.builder("products_operation_duration")
                .description("Duration of product operations")
                .register(meterRegistry);

        Gauge.builder("products_total", products, Map::size)
                .description("Current total number of products")
                .register(meterRegistry);

        Gauge.builder("products_total_quantity", this, ProductService::getTotalQuantity)
                .description("Total quantity of all products in stock")
                .register(meterRegistry);

        Gauge.builder("products_total_value", this, ProductService::getTotalValue)
                .description("Total value of all products in stock")
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
        return productOperationTimer.record(() -> {
            log.info("Получение всех продуктов");
            productViewsCounter.increment(products.size());
            return new ArrayList<>(products.values());
        });
    }

    public Optional<ProductDto> getProductById(Long id) {
        return productOperationTimer.record(() -> {
            log.info("Получение продукта с id: {}", id);
            Optional<ProductDto> product = Optional.ofNullable(products.get(id));
            if (product.isPresent()) {
                productViewsCounter.increment();
            }
            return product;
        });
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
        return productOperationTimer.record(() -> {
            log.info("Обновление продукта с id: {}", id);
            if (products.containsKey(id)) {
                productDto.setId(id);
                products.put(id, productDto);
                productUpdatedCounter.increment();
                return Optional.of(productDto);
            }
            return Optional.empty();
        });
    }

    public boolean deleteProduct(Long id) {
        return productOperationTimer.record(() -> {
            log.info("Удаление продукта с id: {}", id);
            ProductDto removed = products.remove(id);
            if (removed != null) {
                productDeletedCounter.increment();
                return true;
            }
            return false;
        });
    }

    private double getTotalQuantity() {
        return products.values().stream()
                .mapToInt(ProductDto::getQuantity)
                .sum();
    }

    private double getTotalValue() {
        return products.values().stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }
}
