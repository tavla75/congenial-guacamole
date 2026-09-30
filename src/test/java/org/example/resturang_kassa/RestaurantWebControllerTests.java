package org.example.resturang_kassa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantWebControllerTests {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    @Transactional
    void categoriesCanBeAddedAndSharedThroughTheApi() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Web and desktop"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Web and desktop"));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        ["Mat", "Dryck", "Web and desktop"]
                        """, false));
    }
}
