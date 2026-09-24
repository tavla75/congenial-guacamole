package org.example.resturang_kassa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RestaurantWebController {

    @GetMapping(value = "/", produces = "text/html")
    public String home() {
        return """
                <!DOCTYPE html>
                <html lang="sv">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Restaurangkassa</title>
                    <style>
                        body {
                            background: #f4f6f8;
                            color: #1f2933;
                            font-family: Arial, sans-serif;
                            margin: 0;
                            padding: 3rem;
                        }
                        main {
                            background: white;
                            border-radius: 12px;
                            margin: auto;
                            max-width: 720px;
                            padding: 2rem;
                            box-shadow: 0 4px 16px rgb(0 0 0 / 10%);
                        }
                        h1 { color: #176b87; }
                    </style>
                </head>
                <body>
                    <main>
                        <h1>Välkommen till restaurangkassan</h1>
                        <p>Webbservern körs och är redo att ta emot beställningar.</p>
                        <p>Status: <a href="/api/status">/api/status</a></p>
                    </main>
                </body>
                </html>
                """;
    }

    @GetMapping("/api/status")
    public Map<String, String> status() {
        return Map.of(
                "application", "Resturang kassa",
                "status", "running"
        );
    }
}
