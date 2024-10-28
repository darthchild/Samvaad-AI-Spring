package com.en.samvaad.controller;

import com.en.samvaad.models.ConversationRequest;
import com.en.samvaad.models.ConversationResponse;
import com.en.samvaad.service.LlmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConversationController {
    
    private final LlmService llmService;

    public ConversationController(LlmService llmService) {
        this.llmService = llmService;
    }

    /*
    TODO
    FIX : Bhashini Service not running
    UNDER : how does routing work, what & why "/chat" & why doest work on reload, how to map it to main
     */

    @PostMapping("/backend-api/v2/conversation")
    public ResponseEntity<ConversationResponse> conversation(@RequestBody ConversationRequest request) {
        try {
            String response;

            if (request.getBase64Image() != null && !request.getBase64Image().isEmpty()) {
                if ("txt".equals(request.getOutput())) {
                    response = llmService.imageToText(
                            request.getBase64Image(),
                            request.getMessage(),
                            request.getLanguage()
                    );
                } else {
                    response = "This feature is under development, feel free to contribute to our Open-Source Project";
                }
            } else {
                switch (request.getOutput()) {
                    case "txt":
                        response = llmService.textToText(request.getLanguage(), request.getMessage());
                        System.out.println("controller response : " + response);
                        break;
                    case "img":
                        response = llmService.textToImage(request.getLanguage(), request.getMessage());
                        break;
                    default:
                        response = llmService.textToImage(request.getLanguage(), request.getMessage());
                }
            }

            return ResponseEntity.ok(new ConversationResponse(true, response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ConversationResponse(false, e.getMessage()));
        }
    }
}
