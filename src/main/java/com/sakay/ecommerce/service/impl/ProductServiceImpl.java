package com.sakay.ecommerce.service.impl;

import com.sakay.ecommerce.dto.request.CreateProductRequest;
import com.sakay.ecommerce.dto.request.CreateVariantRequest;
import com.sakay.ecommerce.dto.response.ProductResponse;
import com.sakay.ecommerce.entity.Product;
import com.sakay.ecommerce.entity.ProductVariant;
import com.sakay.ecommerce.exception.ResourceNotFoundException;
import com.sakay.ecommerce.repository.ProductRepository;
import com.sakay.ecommerce.repository.ProductVariantRepository;
import com.sakay.ecommerce.service.ProductService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket:products}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Override
    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(ProductResponse::from);
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        return ProductResponse.from(findProduct(id));
    }

    @Override
    public ProductResponse getProductBySlug(String slug) {
        return ProductResponse.from(productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
    }

    @Override
    public Page<ProductResponse> searchProducts(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name, pageable)
                .map(ProductResponse::from);
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .category(request.getCategory())
                .basePrice(request.getBasePrice())
                .images(request.getImages() != null ? request.getImages() : List.of())
                .build();
        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProduct(UUID id, CreateProductRequest request) {
        Product product = findProduct(id);
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setBasePrice(request.getBasePrice());
        if (request.getImages() != null) product.setImages(request.getImages());
        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        Product product = findProduct(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public List<ProductVariant> getVariants(UUID productId) {
        return variantRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public ProductVariant addVariant(UUID productId, CreateVariantRequest request) {
        Product product = findProduct(productId);
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .variantType(request.getVariantType())
                .variantValue(request.getVariantValue())
                .stockQty(request.getStockQty())
                .priceModifier(request.getPriceModifier())
                .sku(request.getSku())
                .build();
        return variantRepository.save(variant);
    }

    @Override
    public String uploadImage(UUID productId, MultipartFile file) {
        try {
            String filename = productId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(filename)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            String url = minioEndpoint + "/" + bucket + "/" + filename;
            Product product = findProduct(productId);
            product.getImages().add(url);
            productRepository.save(product);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    private Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}