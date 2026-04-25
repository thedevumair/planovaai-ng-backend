package com.planovaai.backend.controller;

import com.planovaai.backend.dto.TimeDebtDto;
import com.planovaai.backend.service.TimeDebtService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-debt")
@CrossOrigin(origins = "*")
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
