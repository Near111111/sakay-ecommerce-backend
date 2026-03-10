package com.sakay.ecommerce.repository;

import com.sakay.ecommerce.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    List<ProductVariant> findByProductId(UUID productId);
    List<ProductVariant> findByProductIdAndVariantType(UUID productId, ProductVariant.VariantType variantType);
    Optional<ProductVariant> findBySku(String sku);
}
