package com.ecom.api.service;

import com.ecom.api.dto.ProductRequest;
import com.ecom.api.dto.ProductResponse;
import com.ecom.api.entity.Category;
import com.ecom.api.entity.Product;
import com.ecom.api.exception.DuplicateResourceException;
import com.ecom.api.exception.ResourceNotFoundException;
import com.ecom.api.repository.CategoryRepository;
import com.ecom.api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void should_returnAllProducts_when_noFilterProvided() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").build();
        Product product1 = Product.builder()
                .id(1L)
                .name("Keyboard")
                .sku("KEY-001")
                .price(new BigDecimal("49.99"))
                .stockQuantity(20)
                .active(true)
                .category(category)
                .build();
        Product product2 = Product.builder()
                .id(2L)
                .name("Mouse")
                .sku("MOU-001")
                .price(new BigDecimal("29.99"))
                .stockQuantity(15)
                .active(false)
                .category(category)
                .build();
        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        // Act
        List<ProductResponse> responses = productService.getAllProducts(null, null);

        // Assert
        assertThat(responses)
                .hasSize(2)
                .extracting(ProductResponse::getSku)
                .containsExactly("KEY-001", "MOU-001");
    }

    @Test
    void should_returnProducts_when_filteredByCategoryIdAndActiveTrue() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").build();
        Product product = Product.builder()
                .id(1L)
                .name("Keyboard")
                .sku("KEY-001")
                .price(new BigDecimal("49.99"))
                .stockQuantity(20)
                .active(true)
                .category(category)
                .build();
        when(productRepository.findByCategoryIdAndActiveTrue(1L)).thenReturn(List.of(product));

        // Act
        List<ProductResponse> responses = productService.getAllProducts(1L, true);

        // Assert
        assertThat(responses)
                .hasSize(1)
                .first()
                .satisfies(p -> {
                    assertThat(p.getSku()).isEqualTo("KEY-001");
                    assertThat(p.getActive()).isTrue();
                    assertThat(p.getCategoryId()).isEqualTo(1L);
                });
    }

    @Test
    void should_returnProducts_when_filteredByCategoryIdAndActiveFalse() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").build();
        Product productActive = Product.builder().id(1L).active(true).category(category).build();
        Product productInactive = Product.builder().id(2L).active(false).category(category).build();
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(productActive, productInactive));

        // Act
        List<ProductResponse> responses = productService.getAllProducts(1L, false);

        // Assert
        assertThat(responses)
                .hasSize(1)
                .extracting(ProductResponse::getId)
                .containsExactly(2L);
    }

    @Test
    void should_returnProducts_when_filteredByCategoryIdOnly() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").build();
        Product product = Product.builder().id(1L).name("Keyboard").category(category).build();
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(product));

        // Act
        List<ProductResponse> responses = productService.getAllProducts(1L, null);

        // Assert
        assertThat(responses)
                .hasSize(1)
                .first()
                .satisfies(p -> assertThat(p.getCategoryId()).isEqualTo(1L));
    }

    @Test
    void should_returnProducts_when_filteredByActiveTrueOnly() {
        // Arrange
        Product product = Product.builder().id(1L).name("Active Keyboard").active(true).build();
        when(productRepository.findByActiveTrue()).thenReturn(List.of(product));

        // Act
        List<ProductResponse> responses = productService.getAllProducts(null, true);

        // Assert
        assertThat(responses)
                .hasSize(1)
                .first()
                .satisfies(p -> assertThat(p.getActive()).isTrue());
    }

    @Test
    void should_returnProducts_when_filteredByActiveFalseOnly() {
        // Arrange
        Product productActive = Product.builder().id(1L).active(true).build();
        Product productInactive = Product.builder().id(2L).active(false).build();
        when(productRepository.findAll()).thenReturn(List.of(productActive, productInactive));

        // Act
        List<ProductResponse> responses = productService.getAllProducts(null, false);

        // Assert
        assertThat(responses)
                .hasSize(1)
                .first()
                .satisfies(p -> assertThat(p.getId()).isEqualTo(2L));
    }

    @Test
    void should_returnProduct_when_productFoundById() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").build();
        Product product = Product.builder()
                .id(10L)
                .name("Wireless Mouse")
                .sku("MOU-001")
                .price(new BigDecimal("29.99"))
                .stockQuantity(50)
                .active(true)
                .category(category)
                .build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        // Act
        ProductResponse response = productService.getProductById(10L);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(10L);
                    assertThat(res.getName()).isEqualTo("Wireless Mouse");
                    assertThat(res.getSku()).isEqualTo("MOU-001");
                    assertThat(res.getPrice()).isEqualByComparingTo(new BigDecimal("29.99"));
                    assertThat(res.getStockQuantity()).isEqualTo(50);
                    assertThat(res.getCategoryId()).isEqualTo(1L);
                    assertThat(res.getCategoryName()).isEqualTo("Electronics");
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_productNotFoundById() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 999");
    }

    @Test
    void should_returnProduct_when_productFoundBySku() {
        // Arrange
        Product product = Product.builder()
                .id(10L)
                .name("Wireless Mouse")
                .sku("MOU-001")
                .price(new BigDecimal("29.99"))
                .build();
        when(productRepository.findBySku("MOU-001")).thenReturn(Optional.of(product));

        // Act
        ProductResponse response = productService.getProductBySku("MOU-001");

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(10L);
                    assertThat(res.getSku()).isEqualTo("MOU-001");
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_productNotFoundBySku() {
        // Arrange
        when(productRepository.findBySku("NON-EXISTENT")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductBySku("NON-EXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with SKU: NON-EXISTENT");
    }

    @Test
    void should_createProduct_when_validRequest() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").build();
        ProductRequest request = ProductRequest.builder()
                .name("Mechanical Keyboard")
                .description("RGB backlit mechanical keyboard")
                .price(new BigDecimal("89.99"))
                .sku("KEY-RGB-01")
                .stockQuantity(30)
                .active(true)
                .categoryId(1L)
                .build();

        Product savedProduct = Product.builder()
                .id(100L)
                .name("Mechanical Keyboard")
                .description("RGB backlit mechanical keyboard")
                .price(new BigDecimal("89.99"))
                .sku("KEY-RGB-01")
                .stockQuantity(30)
                .active(true)
                .category(category)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySku("KEY-RGB-01")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(100L);
                    assertThat(res.getName()).isEqualTo("Mechanical Keyboard");
                    assertThat(res.getSku()).isEqualTo("KEY-RGB-01");
                    assertThat(res.getPrice()).isEqualByComparingTo(new BigDecimal("89.99"));
                    assertThat(res.getCategoryId()).isEqualTo(1L);
                    assertThat(res.getCategoryName()).isEqualTo("Electronics");
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_createProductCategoryNotFound() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Keyboard")
                .sku("KEY-001")
                .price(new BigDecimal("50.00"))
                .stockQuantity(10)
                .categoryId(999L)
                .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }

    @Test
    void should_throwDuplicateResourceException_when_createProductWithDuplicateSku() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").build();
        ProductRequest request = ProductRequest.builder()
                .name("Keyboard")
                .sku("KEY-DUPLICATE")
                .price(new BigDecimal("50.00"))
                .stockQuantity(10)
                .categoryId(1L)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySku("KEY-DUPLICATE")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Product already exists with SKU: KEY-DUPLICATE");
    }

    @Test
    void should_updateProduct_when_validRequest() {
        // Arrange
        Category oldCategory = Category.builder().id(1L).name("Electronics").build();
        Category newCategory = Category.builder().id(2L).name("Peripherals").build();
        Product existingProduct = Product.builder()
                .id(10L)
                .name("Old Keyboard")
                .sku("KEY-OLD")
                .price(new BigDecimal("40.00"))
                .stockQuantity(10)
                .active(true)
                .category(oldCategory)
                .build();

        ProductRequest updateRequest = ProductRequest.builder()
                .name("Updated Keyboard")
                .description("Updated description")
                .price(new BigDecimal("60.00"))
                .sku("KEY-NEW")
                .stockQuantity(25)
                .active(true)
                .categoryId(2L)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productRepository.existsBySku("KEY-NEW")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.updateProduct(10L, updateRequest);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getName()).isEqualTo("Updated Keyboard");
                    assertThat(res.getSku()).isEqualTo("KEY-NEW");
                    assertThat(res.getPrice()).isEqualByComparingTo(new BigDecimal("60.00"));
                    assertThat(res.getStockQuantity()).isEqualTo(25);
                    assertThat(res.getCategoryId()).isEqualTo(2L);
                    assertThat(res.getCategoryName()).isEqualTo("Peripherals");
                });
    }

    @Test
    void should_updateProduct_when_skuUnchanged() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").build();
        Product existingProduct = Product.builder()
                .id(10L)
                .name("Keyboard")
                .sku("KEY-SAME")
                .price(new BigDecimal("40.00"))
                .stockQuantity(10)
                .active(true)
                .category(category)
                .build();

        ProductRequest updateRequest = ProductRequest.builder()
                .name("Keyboard Revised")
                .description("New desc")
                .price(new BigDecimal("45.00"))
                .sku("KEY-SAME")
                .stockQuantity(15)
                .active(false)
                .categoryId(1L)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.updateProduct(10L, updateRequest);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getName()).isEqualTo("Keyboard Revised");
                    assertThat(res.getActive()).isFalse();
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_updateProductNotFound() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Keyboard")
                .sku("KEY-001")
                .categoryId(1L)
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 999");
    }

    @Test
    void should_throwResourceNotFoundException_when_updateProductCategoryNotFound() {
        // Arrange
        Product existingProduct = Product.builder().id(10L).sku("KEY-001").build();
        ProductRequest request = ProductRequest.builder()
                .name("Keyboard")
                .sku("KEY-001")
                .categoryId(999L)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(10L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }

    @Test
    void should_throwDuplicateResourceException_when_updateProductWithDuplicateSku() {
        // Arrange
        Category category = Category.builder().id(1L).name("Electronics").build();
        Product existingProduct = Product.builder().id(10L).sku("KEY-ORIGINAL").build();
        ProductRequest request = ProductRequest.builder()
                .name("Keyboard")
                .sku("KEY-TAKEN")
                .categoryId(1L)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySku("KEY-TAKEN")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(10L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Product already exists with SKU: KEY-TAKEN");
    }

    @Test
    void should_deleteProduct_when_productExists() {
        // Arrange
        when(productRepository.existsById(10L)).thenReturn(true);

        // Act
        productService.deleteProduct(10L);

        // Assert
        verify(productRepository).deleteById(10L);
    }

    @Test
    void should_throwResourceNotFoundException_when_deleteProductNotFound() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 999");
    }
}
