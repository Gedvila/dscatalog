package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.factories.Factory;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private PageImpl page;
    private Product product;
    private ProductDTO productDTO;
    private Category category;

    @BeforeEach
    void setUp() throws Exception{
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 3L;
        product = Factory.createProduct();
        category = Factory.createCategory();
        page = new PageImpl<>(List.of(product));
        productDTO = Factory.createProductDTO();

        /// Simulação delete
        Mockito.doNothing().when(productRepository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);

        /// Simulação existsById
        Mockito.when(productRepository.existsById(existingId)).thenReturn(true);
        Mockito.when(productRepository.existsById(nonExistingId)).thenReturn(false);
        Mockito.when(productRepository.existsById(dependentId)).thenReturn(true);

        /// Simulação findAll
        Mockito.when(productRepository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        /// Simulação save
        Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);

        /// Simulação findById
        Mockito.when(productRepository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        /// Simulação getReferenceById
        Mockito.when(productRepository.getReferenceById(existingId)).thenReturn(product);
        Mockito.when(productRepository.getReferenceById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
        Mockito.when(categoryRepository.getReferenceById(existingId)).thenReturn(category);
        Mockito.when(categoryRepository.getReferenceById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
    }

    /// Testes delete
    @Test
    public void deleteShouldDoNothingWhenIdExist(){

        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingId);
        });
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenInDoesNotExist(){
        Assertions.assertThrows(ResourceNotFoundException.class,() -> {
            service.delete(nonExistingId);
        });
    }

    @Test
    public void deleteShouldThrowDataIntegrityViolationExceptionWhenDependentId(){

        Assertions.assertThrows(DatabaseException.class, () ->{
            service.delete(dependentId);
        });
    }

    /// Testes findAll
    @Test
    public void findAllPagedShouldReturnPage(){
        Pageable pageable = PageRequest.of(0,10);

        Page<ProductDTO> result = service.findAllPaged(pageable);

        Assertions.assertNotNull(result);
        Mockito.verify(productRepository).findAll(pageable);
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExist(){
        ProductDTO result = service.findById(existingId);

        Assertions.assertNotNull(result);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdNonExist(){
        Assertions.assertThrows(ResourceNotFoundException.class,()->{
            service.findById(nonExistingId);
        });
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExist(){

        ProductDTO result = service.update(existingId,productDTO);

        Assertions.assertNotNull(result);
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdNonExist(){

        Assertions.assertThrows(ResourceNotFoundException.class,()->{
            service.update(nonExistingId,productDTO);
        });
    }
}
