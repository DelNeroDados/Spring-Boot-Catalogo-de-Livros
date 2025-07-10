package com.BookAffix.Book_Affix.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GutendexClient {

    @Value("${gutendex.api.base-url}")
    private String gutendexApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public JsonNode buscarLivroPorTitulo(String titulo) {
        String url = gutendexApiBaseUrl + "?search=" + titulo;
        return restTemplate.getForObject(url, JsonNode.class);
    }
}
