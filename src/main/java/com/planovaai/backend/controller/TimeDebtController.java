package com.planovaai.backend.controller;

import com.planovaai.backend.dto.TimeDebtDto;
import com.planovaai.backend.service.TimeDebtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/time-debt")
public class TimeDebtController {

    private final TimeDebtService timeDebtService;

    public TimeDebtController(TimeDebtService timeDebtService) {
        this.timeDebtService = timeDebtService;
    }

    @GetMapping("/project/{projectId}")
    public List<TimeDebtDto> getTimeDebt(@PathVariable Long projectId) {
        return timeDebtService.calculateTimeDebt(projectId);
    }
}
