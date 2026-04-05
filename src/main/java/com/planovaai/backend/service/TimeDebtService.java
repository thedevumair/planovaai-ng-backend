package com.planovaai.backend.service;

import com.planovaai.backend.dto.TimeDebtDto;
import com.planovaai.backend.entity.Task;
import com.planovaai.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TimeDebtService {

    private final TaskRepository taskRepository;

    public TimeDebtService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TimeDebtDto> calculateTimeDebt(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        List<TimeDebtDto> timeDebtDtoList = new ArrayList<>();

        for (Task task : tasks) {
            TimeDebtDto dto = new TimeDebtDto();

            int planned = task.getDuration();
            int actual = task.getActualDuration();

            int delay = actual - planned;

            dto.setTitle(task.getTitle());
            dto.setPlanned(planned);
            dto.setActual(actual);
            dto.setDelay(delay);

            timeDebtDtoList.add(dto);
        }
        return timeDebtDtoList;
    }
}
