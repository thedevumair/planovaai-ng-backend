package com.planovaai.backend.service;

import com.planovaai.backend.dto.ExtractedTaskDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaskExtractionService {

    public List<ExtractedTaskDto> extractTasks(String text) {

        List<ExtractedTaskDto> tasks = new ArrayList<>();

        // Match "Use case: Something" — stop at next "Use case:" or end
        // Lookahead stops match before next "Use case:" entry
        Pattern pattern = Pattern.compile(
                "Use case:\\s*([^\\n]{3,60}?)(?=\\s+Use case:|\\s+\\d+\\.|\\s+Diagram:|$)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String taskName = matcher.group(1).trim();

            // Skip if too short or too long
            if (taskName.length() < 3 || taskName.length() > 60) continue;

            // Skip if it's just numbers or symbols
            if (taskName.matches("[^a-zA-Z]+")) continue;

            // Clean the title
            String cleanedTitle = taskName
                    .replaceAll("\\s+", " ")
                    .replaceAll("[^a-zA-Z0-9 :\\-]", "")
                    .trim();

            ExtractedTaskDto dto = new ExtractedTaskDto();
            dto.setTitle(cleanedTitle);
            dto.setEstimatedDays(estimateDuration(cleanedTitle));

            tasks.add(dto);

            // Max 10 tasks
            if (tasks.size() >= 10) break;
        }

        return tasks;
    }

    private int estimateDuration(String task) {
        String t = task.toLowerCase();

        if (t.contains("design") || t.contains("architecture")) return 7;
        if (t.contains("develop") || t.contains("implement")) return 14;
        if (t.contains("test") || t.contains("review")) return 5;
        if (t.contains("deploy") || t.contains("release")) return 3;
        if (t.contains("submit") || t.contains("send")) return 2;
        if (t.contains("publish") || t.contains("remove")) return 2;
        if (t.contains("assign") || t.contains("check")) return 1;
        if (t.contains("analysis") || t.contains("requirement")) return 6;

        return 3;
    }
}
