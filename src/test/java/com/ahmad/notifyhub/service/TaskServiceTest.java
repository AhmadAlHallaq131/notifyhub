package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.dto.response.PageResponse;
import com.ahmad.notifyhub.dto.response.TaskResponse;
import com.ahmad.notifyhub.entity.Task;
import com.ahmad.notifyhub.entity.enums.TaskPriority;
import com.ahmad.notifyhub.entity.enums.TaskStatus;
import com.ahmad.notifyhub.mapper.TaskMapper;
import com.ahmad.notifyhub.repository.TagRepository;
import com.ahmad.notifyhub.repository.TaskListRepository;
import com.ahmad.notifyhub.repository.TaskRepository;
import com.ahmad.notifyhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final String USER_EMAIL = "user@example.com";

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskListRepository taskListRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskMapper taskMapper;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(
                taskRepository, taskListRepository, tagRepository, userRepository, taskMapper
        );
    }

    @Test
    void searchTasksReturnsEmptyListForBlankQuery() {
        assertEquals(List.of(), taskService.searchTasks("   ", USER_EMAIL));

        verify(taskRepository, never()).searchByUserEmail(any(), any());
    }

    @Test
    void getTaskPageAppliesFiltersAndMapsContent() {
        Task task = org.mockito.Mockito.mock(Task.class);
        TaskResponse response = new TaskResponse(
                10L, "Write tests", null, TaskPriority.HIGH, TaskStatus.PENDING,
                null, null, null, null, null, null, List.of()
        );
        when(taskRepository.findPageByFilters(
                eq(USER_EMAIL), eq(TaskStatus.PENDING), eq(TaskPriority.HIGH), eq(3L), eq("tests"), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(task)));
        when(taskMapper.toResponse(task)).thenReturn(response);

        PageResponse<TaskResponse> result = taskService.getTaskPage(
                USER_EMAIL, 0, 20, "dueDate", Sort.Direction.ASC,
                TaskStatus.PENDING, TaskPriority.HIGH, 3L, " tests "
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findPageByFilters(
                eq(USER_EMAIL), eq(TaskStatus.PENDING), eq(TaskPriority.HIGH), eq(3L), eq("tests"), pageableCaptor.capture()
        );
        assertEquals("dueDate", pageableCaptor.getValue().getSort().getOrderFor("dueDate").getProperty());
        assertEquals(response, result.content().getFirst());
        assertEquals(1, result.totalElements());
    }

    @Test
    void getTaskPageRejectsUnsupportedSortField() {
        assertThrows(IllegalArgumentException.class, () -> taskService.getTaskPage(
                USER_EMAIL, 0, 20, "passwordHash", Sort.Direction.ASC,
                null, null, null, null
        ));
    }
}
