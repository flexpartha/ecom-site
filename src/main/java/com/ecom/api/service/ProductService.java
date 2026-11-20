package com.ecom.api.service;

import com.ecom.api.dto.ProductRequest;
import com.ecom.api.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts(Long categoryId, Boolean active);

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySku(String sku);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
