package com.ecom.api.service;

import com.ecom.api.dto.CategoryRequest;
import com.ecom.api.dto.CategoryResponse;
import com.ecom.api.entity.Category;
import com.ecom.api.exception.DuplicateResourceException;
import com.ecom.api.exception.ResourceNotFoundException;
import com.ecom.api.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void should_returnAllCategories_when_categoriesExist() {
        // Arrange
        Category category1 = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .description("Electronic gadgets")
                .createdAt(LocalDateTime.now())
                .build();
        Category category2 = Category.builder()
                .id(2L)
                .name("Books")
                .slug("books")
                .description("Printed books")
                .createdAt(LocalDateTime.now())
                .build();
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        // Act
        List<CategoryResponse> responses = categoryService.getAllCategories();

        // Assert
        assertThat(responses)
                .hasSize(2)
                .extracting(CategoryResponse::getName)
                .containsExactly("Electronics", "Books");
    }

    @Test
    void should_returnCategory_when_categoryFoundById() {
        // Arrange
        Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .description("Electronic gadgets")
                .build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        CategoryResponse response = categoryService.getCategoryById(1L);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(1L);
                    assertThat(res.getName()).isEqualTo("Electronics");
                    assertThat(res.getSlug()).isEqualTo("electronics");
                    assertThat(res.getDescription()).isEqualTo("Electronic gadgets");
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_categoryNotFoundById() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }

    @Test
    void should_returnCategory_when_categoryFoundBySlug() {
        // Arrange
        Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .description("Electronic gadgets")
                .build();
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(category));

        // Act
        CategoryResponse response = categoryService.getCategoryBySlug("electronics");

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(1L);
                    assertThat(res.getName()).isEqualTo("Electronics");
                    assertThat(res.getSlug()).isEqualTo("electronics");
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_categoryNotFoundBySlug() {
        // Arrange
        when(categoryRepository.findBySlug("unknown-slug")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryBySlug("unknown-slug"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with slug: unknown-slug");
    }

    @Test
    void should_createCategory_when_validRequest() {
        // Arrange
        CategoryRequest request = CategoryRequest.builder()
                .name("Home Appliances")
                .slug("home-appliances")
                .description("Appliances for home")
                .build();

        Category savedCategory = Category.builder()
                .id(10L)
                .name("Home Appliances")
                .slug("home-appliances")
                .description("Appliances for home")
                .build();

        when(categoryRepository.existsByName("Home Appliances")).thenReturn(false);
        when(categoryRepository.existsBySlug("home-appliances")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // Act
        CategoryResponse response = categoryService.createCategory(request);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(10L);
                    assertThat(res.getName()).isEqualTo("Home Appliances");
                    assertThat(res.getSlug()).isEqualTo("home-appliances");
                    assertThat(res.getDescription()).isEqualTo("Appliances for home");
                });
    }

    @Test
    void should_throwDuplicateResourceException_when_createCategoryWithDuplicateName() {
        // Arrange
        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .slug("new-electronics")
                .description("Duplicate name")
                .build();

        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Category already exists with name: Electronics");
    }

    @Test
    void should_throwDuplicateResourceException_when_createCategoryWithDuplicateSlug() {
        // Arrange
        CategoryRequest request = CategoryRequest.builder()
                .name("New Category")
                .slug("electronics")
                .description("Duplicate slug")
                .build();

        when(categoryRepository.existsByName("New Category")).thenReturn(false);
        when(categoryRepository.existsBySlug("electronics")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Category already exists with slug: electronics");
    }

    @Test
    void should_updateCategory_when_validRequest() {
        // Arrange
        Category existingCategory = Category.builder()
                .id(1L)
                .name("Old Electronics")
                .slug("old-electronics")
                .description("Old description")
                .build();

        CategoryRequest updateRequest = CategoryRequest.builder()
                .name("Updated Electronics")
                .slug("updated-electronics")
                .description("Updated description")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByName("Updated Electronics")).thenReturn(false);
        when(categoryRepository.existsBySlug("updated-electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CategoryResponse response = categoryService.updateCategory(1L, updateRequest);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getName()).isEqualTo("Updated Electronics");
                    assertThat(res.getSlug()).isEqualTo("updated-electronics");
                    assertThat(res.getDescription()).isEqualTo("Updated description");
                });
    }

    @Test
    void should_updateCategory_when_nameAndSlugUnchanged() {
        // Arrange
        Category existingCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .description("Old description")
                .build();

        CategoryRequest updateRequest = CategoryRequest.builder()
                .name("Electronics")
                .slug("electronics")
                .description("New description")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CategoryResponse response = categoryService.updateCategory(1L, updateRequest);

        // Assert
        assertThat(response.getDescription()).isEqualTo("New description");
    }

    @Test
    void should_throwResourceNotFoundException_when_updateCategoryNotFound() {
        // Arrange
        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .slug("electronics")
                .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }

    @Test
    void should_throwDuplicateResourceException_when_updateCategoryWithDuplicateName() {
        // Arrange
        Category existingCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .build();

        CategoryRequest request = CategoryRequest.builder()
                .name("Gadgets")
                .slug("electronics")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByName("Gadgets")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Category already exists with name: Gadgets");
    }

    @Test
    void should_throwDuplicateResourceException_when_updateCategoryWithDuplicateSlug() {
        // Arrange
        Category existingCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .build();

        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .slug("gadgets")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsBySlug("gadgets")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Category already exists with slug: gadgets");
    }

    @Test
    void should_deleteCategory_when_categoryExists() {
        // Arrange
        when(categoryRepository.existsById(1L)).thenReturn(true);

        // Act
        categoryService.deleteCategory(1L);

        // Assert
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void should_throwResourceNotFoundException_when_deleteCategoryNotFound() {
        // Arrange
        when(categoryRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }
}
