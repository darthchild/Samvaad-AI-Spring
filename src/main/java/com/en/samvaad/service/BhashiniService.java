package com.en.samvaad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;

import java.util.Map;

@Service
public class BhashiniService {

    private static final String BASE_URL = "https://bhasa-api.onrender.com";

    @Autowired
    private RestTemplate restTemplate;

    public String translateText(String text, String sourceLang, String targetLang) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = Map.of(
                "sourceLang", sourceLang,
                "targetLang", targetLang,
                "text", text
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        Map<String, String> response = restTemplate.postForObject(BASE_URL + "/nmt", request, Map.class);

        return response.get("translatedText");
    }
}
