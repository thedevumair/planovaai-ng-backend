package com.planovaai.backend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {

    public Map<String, Object> predictModel(String srsText) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:5000/predict";

        Map<String, String> request = new HashMap<>();
        request.put("text", srsText);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        Map body = response.getBody();

        String model = (String) body.get("model");
        Double confidence = (Double) body.get("top_confidence");

        System.out.println("🤖 Predicted: " + model + " (" + confidence + "% confidence)");

        return Map.of(
                "model", model,                                          // clean → "Waterfall"
                "modelLabel", model + " (" + confidence + "% confident)" // display → "Waterfall (94.2% confident)"
        );
    }
}
