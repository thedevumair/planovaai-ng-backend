package com.planovaai.backend.service;

import com.planovaai.backend.dto.ModelSuggestionDto;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@Service
public class ModelSuggestionService {


    public String extractTextFromDocument(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();

        if (fileName == null) throw new RuntimeException("File name is missing");

        if (fileName.endsWith(".txt")) {
            return new String(file.getBytes());
        }

        if (fileName.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                return doc.getParagraphs()
                        .stream()
                        .map(XWPFParagraph::getText)
                        .filter(s -> s != null && !s.isBlank())
                        .collect(Collectors.joining("\n")); // ✅ preserve newlines
            }
        }

        throw new RuntimeException("Unsupported file type. Only .txt and .docx allowed.");
    }

    private String extractRelevantText(String text) {
        if (text == null || text.isEmpty()) return "";

        // Clean up whitespace
        text = text.replaceAll("\\s+", " ").trim();

        // Keywords that indicate relevant SRS sections
        String[] relevantKeywords = {
                "functional requirement",
                "non-functional requirement",
                "use case",
                "system feature",
                "user story",
                "module",
                "component",
                "interface",
                "authentication",
                "authorization",
                "database",
                "api",
                "security",
                "performance",
                "notification",
                "report",
                "payment",
                "upload",
                "search",
                "admin"
        };

        // ✅ Split into sentences and keep only relevant ones
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder relevant = new StringBuilder();

        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();
            for (String keyword : relevantKeywords) {
                if (lower.contains(keyword)) {
                    relevant.append(sentence).append(" ");
                    break;
                }
            }

            // ✅ Stop at 5000 chars — enough for analysis
            if (relevant.length() >= 5000) break;
        }

        // ✅ If nothing matched, fallback to first 5000 chars
        if (relevant.length() == 0) {
            return text.substring(0, Math.min(text.length(), 5000));
        }

        return relevant.toString().trim();
    }
}
