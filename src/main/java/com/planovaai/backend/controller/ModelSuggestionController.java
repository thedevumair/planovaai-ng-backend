package com.planovaai.backend.controller;

import com.planovaai.backend.dto.ModelSuggestionDto;
import com.planovaai.backend.service.ModelSuggestionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/model")
public class ModelSuggestionController {

    private final ModelSuggestionService modelSuggestionService;

    public ModelSuggestionController(ModelSuggestionService modelSuggestionService) {
        this.modelSuggestionService = modelSuggestionService;
    }

    @PostMapping("/suggest")
    public ModelSuggestionDto suggestModel(@RequestBody String text) {
        return modelSuggestionService.suggestModel(text);
    }

    @PostMapping("/upload")
    public ModelSuggestionDto uploadSrsFile
            (@RequestParam("file")MultipartFile file)  throws Exception {

        return modelSuggestionService.suggestTypeFile(file);
    }
}
