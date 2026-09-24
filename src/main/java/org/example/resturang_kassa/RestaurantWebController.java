package org.example.resturang_kassa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RestaurantWebController {

    @GetMapping("/api/status")
    public Map<String, String> status() {
        return Map.of(
                "application", "Resturang kassa",
                "status", "running"
        );
    }
}
