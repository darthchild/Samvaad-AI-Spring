package com.en.samvaad.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private final RestTemplate restTemplate;
    private final BhashiniService bhashiniService;

    public LlmService(RestTemplate restTemplate, BhashiniService bhashiniService) {
        this.restTemplate = restTemplate;
        this.bhashiniService = bhashiniService;
    }

    @Value("${fireworks.api.key}")
    private String fireworksApiKey;

    @Value("${runpod.api.key}")
    private String runpodApiKey;

    @Value("${fireworks.url}")
    private String fireworksUrl;

    @Value("${runpod.api.key}")
    private String runpodUrl;
    
    private static final String IMG_PROMPT = "135mm IMAX UHD, 8k, f10, dslr, CANON/NIKON/SONY XXmm/XXXmm, ISO xxx, 1/250, 1/500, 1/2000 etc, f1.4, f2.8, f4.0??";
    private static final String SYSTEM_PROMPT = "SYSTEM PROMPT:You are a personal assistant who's name is Samvaad, who gives very accurate and long in detail answer, your goal is to democratize generative ai for billions of people by offering your generative AI services, USER PROMPT : ";


    private String generateText(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(fireworksApiKey);

        Map<String, Object> payload = Map.of(
                "model", "accounts/fireworks/models/mixtral-8x7b-instruct",
                "max_tokens", 4096,
                "top_p", 1,
                "top_k", 40,
                "presence_penalty", 0,
                "frequency_penalty", 0,
                "temperature", 0.6,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", SYSTEM_PROMPT + userMessage + "Assistant : "
                ))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        Map<String, Object> response = restTemplate.postForObject(fireworksUrl, request, Map.class);

//        System.out.println("Service response : " + ((Map<String, Object>)((List<Map<String, Object>>)response.get("choices")).get(0)
//                .get("message")).get("content").toString().replace("\\n", ""));

        return ((Map<String, Object>)((List<Map<String, Object>>)response.get("choices")).get(0)
                .get("message")).get("content").toString().replace("\\n", "");
    }

    public String textToText(String inputLanguage, String userMessage) {
        if ("en".equals(inputLanguage)) {
            System.out.println("Service response : " + generateText(userMessage));
            return generateText(userMessage);
        } else {
            String translatedText = bhashiniService.translateText(userMessage, inputLanguage, "en");
            String llmResponse = generateText(translatedText);
            return bhashiniService.translateText(llmResponse, "en", inputLanguage);
        }
    }

    public String textToImage(String inputLanguage, String userMessage) {
        String prompt = "en".equals(inputLanguage) ? userMessage :
                bhashiniService.translateText(userMessage, inputLanguage, "en");

        return generateImage(prompt + IMG_PROMPT);
    }

    public String imageToText(String base64Image, String userMessage, String language) {
        if ("en".equals(language)) {
            return generateTextFromImage(base64Image, userMessage);
        } else {
            String translatedMessage = bhashiniService.translateText(userMessage, language, "en");
            String llmResponse = generateTextFromImage(base64Image, translatedMessage);
            return bhashiniService.translateText(llmResponse, "en", language);
        }
    }

    private String generateImage(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(runpodApiKey);

        Map<String, Object> payload = Map.of(
                "input", Map.of(
                        "prompt", prompt,
                        "num_inference_steps", 40,
                        "refiner_inference_steps", 30,
                        "width", 1024,
                        "height", 1024,
                        "guidance_scale", 7,
                        "strength", 0.5,
                        "num_images", 1,
                        "negative_prompt", "bad anatomy, bad hands, three hands, three legs..."
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        Map<String, Object> response = restTemplate.postForObject(runpodUrl, request, Map.class);

        return ((Map<String, String>)response.get("output")).get("image_url");
    }

    private String generateTextFromImage(String base64Image, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(fireworksApiKey);

        Map<String, Object> payload = Map.of(
                "model", "accounts/fireworks/models/firellava-13b",
                "max_tokens", 2048,
                "top_p", 1,
                "top_k", 40,
                "presence_penalty", 0,
                "frequency_penalty", 0,
                "temperature", 0.6,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", SYSTEM_PROMPT + userMessage + "Assistant (Give your answer in 2000 words) : "),
                                Map.of("type", "image_url", "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image))
                        )
                ))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        Map<String, Object> response = restTemplate.postForObject(fireworksUrl, request, Map.class);

        return ((Map<String, Object>)((List<Map<String, Object>>)response.get("choices")).get(0)
                .get("message")).get("content").toString().replace("\\n", "");
    }
}