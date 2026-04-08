package com.planovaai.backend.service;

import com.planovaai.backend.dto.ModelSuggestionDto;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ModelSuggestionService {

    public ModelSuggestionDto suggestModel(String srsText) {

        ModelSuggestionDto dto = new ModelSuggestionDto();

        String text = srsText.toLowerCase();

        if ( text.contains("change") || text.contains("flexible") ) {
            dto.setSuggestedModel("Agile");
            dto.setReason("Requirements are dynamic and changing");
        }
        else if ( text.contains("fixed") || text.contains("clear requirements") ) {
            dto.setSuggestedModel("Waterfall");
            dto.setReason("Requirements are well-defined and stable");
        }
        else if ( text.contains("risk") || text.contains("complex") ) {
            dto.setSuggestedModel("Spiral");
            dto.setReason("Project involves risk analysis and complexity");
        }

        return dto;
    }

    public String extractTextFromDocument(MultipartFile file) throws Exception {
        XWPFDocument doc = new XWPFDocument(file.getInputStream());

        return doc.getParagraphs().stream().map(XWPFParagraph::getText)
                .reduce("", (a,b) -> a + b );
    }

    public ModelSuggestionDto suggestTypeFile(MultipartFile file) throws Exception {
        String content;

        if ( file.getOriginalFilename().endsWith(".txt") ) {
            content = new String(file.getBytes());
        }
        else if ( file.getOriginalFilename().endsWith(".docx") ) {
            content = extractTextFromDocument(file);
        }
        else {
            throw new RuntimeException("Unsupported file format");
        }

        return suggestModel(content);
    }
}
