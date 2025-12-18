package com.devsuperior.dscatalog.resources;

import com.devsuperior.dscatalog.TokenUtil;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.factories.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenUtil tokenUtil;

    private long existingId;
    private long nonExistingId;
    private long contTotalProducts;
    private String jsonBody;
    private Product product;
    private ProductDTO productDTO;
    private String username, password, bearerToken;

    @BeforeEach
    void setUp() throws Exception{

        existingId =1L;
        nonExistingId = 1000L;
        contTotalProducts = 25L;

        product = Factory.createProduct();
        productDTO =Factory.createProductDTO();

        jsonBody = objectMapper.writeValueAsString(productDTO);

        username = "maria@gmail.com";
        password= "123456";

        bearerToken = tokenUtil.obtainAccessToken(mockMvc,username,password);
    }

    @Test
    public void findAllShouldReturnSortedPageWhenSortByName() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get("/products?page=0&size=12&sort=name,asc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(contTotalProducts))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("Macbook Pro"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value("PC Gamer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name").value("PC Gamer Alfa"));
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception{

        String expectedName = productDTO.getName();
        String expectedDescription = product.getDescription();

        mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}",existingId)
                        .header("Authorization","Bearer " + bearerToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(existingId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(expectedName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(expectedDescription));
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}",nonExistingId)
                        .header("Authorization","Bearer " + bearerToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
