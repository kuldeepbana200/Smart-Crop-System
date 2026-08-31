package com.smartcrop.ai.dto;

import java.util.List;
import java.util.Map;

public record AiTestResponse(
        String prompt,
        String response,
        List<Map<String, String>> citations) {
}
