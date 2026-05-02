package com.planovaai.backend.controller;

import com.planovaai.backend.dto.GanttTaskDto;
import com.planovaai.backend.dto.TimeDebtDto;
import com.planovaai.backend.service.TimeDebtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TimeDebtController {

    private final TimeDebtService timeDebtService;

    public TimeDebtController(TimeDebtService timeDebtService) {
        this.timeDebtService = timeDebtService;
    }

//    @GetMapping("/project/{projectId}")
//    public List<TimeDebtDto> getTimeDebt(@PathVariable Long projectId) {
//        return timeDebtService.calculateTimeDebt(projectId);
//    }

    @PostMapping("/time-debt")
    public ResponseEntity<?> calculateTimeDebt(@RequestBody List<GanttTaskDto> tasks) {
        TimeDebtDto result = timeDebtService.calculate(tasks);
        return ResponseEntity.ok(result);
    }
}
