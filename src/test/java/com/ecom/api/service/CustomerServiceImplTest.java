package com.ecom.api.service;

import com.ecom.api.dto.CustomerRequest;
import com.ecom.api.dto.CustomerResponse;
import com.ecom.api.entity.Customer;
import com.ecom.api.exception.DuplicateResourceException;
import com.ecom.api.exception.ResourceNotFoundException;
import com.ecom.api.repository.CustomerRepository;
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
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void should_returnAllCustomers_when_customersExist() {
        // Arrange
        Customer customer1 = Customer.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .createdAt(LocalDateTime.now())
                .build();
        Customer customer2 = Customer.builder()
                .id(2L)
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@example.com")
                .phone("0987654321")
                .address("456 Elm St")
                .createdAt(LocalDateTime.now())
                .build();
        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));

        // Act
        List<CustomerResponse> responses = customerService.getAllCustomers();

        // Assert
        assertThat(responses)
                .hasSize(2)
                .extracting(CustomerResponse::getEmail)
                .containsExactly("alice@example.com", "bob@example.com");
    }

    @Test
    void should_returnCustomer_when_customerFoundById() {
        // Arrange
        Customer customer = Customer.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // Act
        CustomerResponse response = customerService.getCustomerById(1L);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(1L);
                    assertThat(res.getFirstName()).isEqualTo("Alice");
                    assertThat(res.getLastName()).isEqualTo("Smith");
                    assertThat(res.getEmail()).isEqualTo("alice@example.com");
                    assertThat(res.getPhone()).isEqualTo("1234567890");
                    assertThat(res.getAddress()).isEqualTo("123 Main St");
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_customerNotFoundById() {
        // Arrange
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomerById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: 999");
    }

    @Test
    void should_createCustomer_when_validRequest() {
        // Arrange
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .build();

        Customer savedCustomer = Customer.builder()
                .id(10L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .build();

        when(customerRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // Act
        CustomerResponse response = customerService.createCustomer(request);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getId()).isEqualTo(10L);
                    assertThat(res.getFirstName()).isEqualTo("Alice");
                    assertThat(res.getLastName()).isEqualTo("Smith");
                    assertThat(res.getEmail()).isEqualTo("alice@example.com");
                    assertThat(res.getPhone()).isEqualTo("1234567890");
                    assertThat(res.getAddress()).isEqualTo("123 Main St");
                });
    }

    @Test
    void should_throwDuplicateResourceException_when_createCustomerWithDuplicateEmail() {
        // Arrange
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("duplicate@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .build();

        when(customerRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Customer already exists with email: duplicate@example.com");
    }

    @Test
    void should_updateCustomer_when_validRequest() {
        // Arrange
        Customer existingCustomer = Customer.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("old@example.com")
                .phone("1111111111")
                .address("Old Address")
                .build();

        CustomerRequest updateRequest = CustomerRequest.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .email("new@example.com")
                .phone("2222222222")
                .address("New Address")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CustomerResponse response = customerService.updateCustomer(1L, updateRequest);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getLastName()).isEqualTo("Johnson");
                    assertThat(res.getEmail()).isEqualTo("new@example.com");
                    assertThat(res.getPhone()).isEqualTo("2222222222");
                    assertThat(res.getAddress()).isEqualTo("New Address");
                });
    }

    @Test
    void should_updateCustomer_when_emailUnchanged() {
        // Arrange
        Customer existingCustomer = Customer.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phone("1111111111")
                .address("Old Address")
                .build();

        CustomerRequest updateRequest = CustomerRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phone("9999999999")
                .address("New Address")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CustomerResponse response = customerService.updateCustomer(1L, updateRequest);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.getPhone()).isEqualTo("9999999999");
                    assertThat(res.getAddress()).isEqualTo("New Address");
                });
    }

    @Test
    void should_throwResourceNotFoundException_when_updateCustomerNotFound() {
        // Arrange
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .build();

        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.updateCustomer(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: 999");
    }

    @Test
    void should_throwDuplicateResourceException_when_updateCustomerWithDuplicateEmail() {
        // Arrange
        Customer existingCustomer = Customer.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .build();

        CustomerRequest request = CustomerRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("taken@example.com")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> customerService.updateCustomer(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Customer already exists with email: taken@example.com");
    }

    @Test
    void should_deleteCustomer_when_customerExists() {
        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);

        // Act
        customerService.deleteCustomer(1L);

        // Assert
        verify(customerRepository).deleteById(1L);
    }

    @Test
    void should_throwResourceNotFoundException_when_deleteCustomerNotFound() {
        // Arrange
        when(customerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> customerService.deleteCustomer(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: 999");
    }
}
