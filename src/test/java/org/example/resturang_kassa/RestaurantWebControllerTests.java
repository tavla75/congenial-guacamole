package org.example.resturang_kassa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantWebControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageIsAvailable() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Fortsätt")));
    }

    @Test
    void productGridIsAvailable() throws Exception {
        mockMvc.perform(get("/grid"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Lägg till produkt")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Administrera priser")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pris för")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Betala")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("restaurant-prices")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("restaurant-products")));
    }

    @Test
    void statusEndpointIsAvailable() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "application": "Resturang kassa",
                          "status": "running"
                        }
                        """));
    }
}
