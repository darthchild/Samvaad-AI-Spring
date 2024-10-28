package com.en.samvaad.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequest {
    private String message;
    private boolean isImage;
    private String language = "English";
    private String output;
    private String base64Image;
}
