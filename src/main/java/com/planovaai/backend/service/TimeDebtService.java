package com.planovaai.backend.service;

import com.planovaai.backend.dto.GanttTaskDto;
import com.planovaai.backend.dto.TimeDebtDto;
import com.planovaai.backend.dto.TimeDebtTaskDto;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeDebtService {

    private final TaskRepository taskRepository;

    public TimeDebtService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

//    public List<TimeDebtDto> calculateTimeDebt(Long projectId) {
//        List<Task> tasks = taskRepository.findByProjectId(projectId);
//
//        List<TimeDebtDto> timeDebtDtoList = new ArrayList<>();
//
//        for (Task task : tasks) {
//            TimeDebtDto dto = new TimeDebtDto();
//
//            int planned = task.getDuration();
//            int actual = task.getActualDuration();
//
//            int delay = actual - planned;
//
//            dto.setTitle(task.getTitle());
//            dto.setPlanned(planned);
//            dto.setActual(actual);
//            dto.setDelay(delay);
//
//            timeDebtDtoList.add(dto);
//        }
//        return timeDebtDtoList;
//    }

    public TimeDebtDto calculate(List<GanttTaskDto> tasks) {

        LocalDate today = LocalDate.now();
        int totalDebtDays = 0;
        int delayedCount = 0;
        List<TimeDebtTaskDto> debtTasks = new ArrayList<>();
        LocalDate recoveryDate = today;

        for (GanttTaskDto task : tasks) {
            TimeDebtTaskDto debtTask = new TimeDebtTaskDto();
            debtTask.setId(task.getId());
            debtTask.setTitle(task.getTitle());
            debtTask.setPlannedStart(task.getStartDate());
            debtTask.setPlannedEnd(task.getEndDate());
            debtTask.setProgress(task.getProgress());
            debtTask.setStatus(task.getStatus());
            debtTask.setDuration(task.getDuration());

            int debtDays = 0;

            // ✅ Debt Type 1: Missed deadline (task not done but end date passed)
            if (task.getEndDate().isBefore(today) &&
                    !"Done".equalsIgnoreCase(task.getStatus())) {
                long overdueDays = ChronoUnit.DAYS.between(task.getEndDate(), today);
                debtDays += (int) overdueDays;
                debtTask.setDelayed(true);
                debtTask.setOverdueDays((int) overdueDays);
            }

            // ✅ Debt Type 2: Planned vs actual (progress based)
            if (task.getStartDate().isBefore(today) &&
                    task.getEndDate().isAfter(today)) {
                // How much should be done by today
                long totalDays = ChronoUnit.DAYS.between(task.getStartDate(), task.getEndDate());
                long elapsedDays = ChronoUnit.DAYS.between(task.getStartDate(), today);
                int expectedProgress = totalDays > 0
                        ? (int) ((elapsedDays * 100) / totalDays)
                        : 0;
                int actualProgress = task.getProgress();
                int gap = expectedProgress - actualProgress;

                if (gap > 20) { // more than 20% behind
                    int progressDebt = (int) (totalDays * gap / 100);
                    debtDays += progressDebt;
                    debtTask.setProgressGap(gap);
                    debtTask.setBehindSchedule(true);
                }
            }

            if (debtDays > 0) {
                totalDebtDays += debtDays;
                delayedCount++;
                debtTask.setDebtDays(debtDays);

                // ✅ Recovery: reschedule task end date
                LocalDate newEnd = recoveryDate.plusDays(task.getDuration());
                debtTask.setRecoveryEnd(newEnd);
                recoveryDate = newEnd;
            } else {
                debtTask.setDebtDays(0);
                debtTask.setDelayed(false);
                debtTask.setBehindSchedule(false);
            }

            debtTasks.add(debtTask);
        }

        // ✅ Risk Level
        String riskLevel;
        if (totalDebtDays == 0) riskLevel = "None";
        else if (totalDebtDays <= 7) riskLevel = "Low";
        else if (totalDebtDays <= 21) riskLevel = "Medium";
        else riskLevel = "High";

        TimeDebtDto result = new TimeDebtDto();
        result.setTotalDebtDays(totalDebtDays);
        result.setDelayedTaskCount(delayedCount);
        result.setRiskLevel(riskLevel);
        result.setRecoveryDate(recoveryDate);
        result.setTasks(debtTasks);

        return result;
    }
}
