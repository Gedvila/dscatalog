package com.devsuperior.dscatalog.resources;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.factories.Factory;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService service;


    private ProductDTO productDTO;
    private PageImpl<ProductDTO> page;
    private long existingId;
    private long nonExitingId;
    private long dependentId;
    private String jsonBody;

    @BeforeEach
    void setUp() throws Exception{

        existingId = 1L;
        nonExitingId = 1000L;
        dependentId = 3L;
        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(productDTO));
        jsonBody = objectMapper.writeValueAsString(productDTO);

        /// Simulação findAll
        Mockito.when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);

        ///  Simulação findById
        Mockito.when(service.findById(existingId)).thenReturn(productDTO);
        Mockito.when(service.findById(nonExitingId)).thenThrow(ResourceNotFoundException.class);

        /// Simulação update
        Mockito.when(service.update(ArgumentMatchers.eq(existingId),ArgumentMatchers.any())).thenReturn(productDTO);
        Mockito.when(service.update(ArgumentMatchers.eq(nonExitingId),ArgumentMatchers.any())).thenThrow(ResourceNotFoundException.class);

        /// Simulação delete
        Mockito.doNothing().when(service).delete(existingId);
        Mockito.doThrow(ResourceNotFoundException.class).when(service).delete(nonExitingId);
        Mockito.doThrow(DatabaseException.class).when(service).delete(dependentId);

        /// Simulação insert
        Mockito.when(service.insert(ArgumentMatchers.any())).thenReturn(productDTO);
    }

    @Test
    public void findAllShouldReturnPage() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExists() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get("/products/{id}",existingId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get("/products{id}",nonExitingId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}",existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").exists());
    }

    @Test
    public void updateShouldReturnNotFoundExceptionWhenIdNotExist() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}",nonExitingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExist() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}",existingId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void deleteShouldReturnReturnNotFoundWhenIdNonExist() throws  Exception{

        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}",nonExitingId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void insertShouldReturnProductDTOAndCreated() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").exists());
    }
}
