package com.planovaai.backend.dto;

import lombok.Data;

@Data
public class ModelSuggestionDto {

    private String suggestedModel;
    private String reason;
}
