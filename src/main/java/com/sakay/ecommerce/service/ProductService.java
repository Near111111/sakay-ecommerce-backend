package com.sakay.ecommerce.service;

import com.sakay.ecommerce.dto.request.CreateProductRequest;
import com.sakay.ecommerce.dto.request.CreateVariantRequest;
import com.sakay.ecommerce.dto.response.ProductResponse;
import com.sakay.ecommerce.dto.response.ProductVariantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Page<ProductResponse> getProducts(Pageable pageable);
    ProductResponse getProductById(UUID id);
    ProductResponse getProductBySlug(String slug);
    Page<ProductResponse> searchProducts(String name, Pageable pageable);
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(UUID id, CreateProductRequest request);
    void deleteProduct(UUID id);
    List<ProductVariantResponse> getVariants(UUID productId);           // ✅ changed
    ProductVariantResponse addVariant(UUID productId, CreateVariantRequest request); // ✅ changed
    String uploadImage(UUID productId, MultipartFile file);
}