package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    // Lv 1 - 1. POST /todos 호출 시 오류 수정
    // 오류가 나는 이유 : 해당 클래스에 적용된 @Transactional의 readOnly 설정이 true로 되어있음.
    // 이 설정이 적용된 경우 DB에 저장된 레코드의 생성/수정/삭제가 불가능함.
    // 따라서, 클래스에 적용된 어노테이션을 삭제하고 모든 메서드의 쓰임에 따라 @Transactional 어노테이션을 적용하거나,
    // 해당 메서드에 @Transactional(readOnly = false) 로 설정을 해 주어야 함.
    // @Transactional 어노테이션의 경우, readOnly 설정의 기본 값이 false 이므로, @Transactional 로만 작성하여도 큰 지장은 없으나,
    // 아래의 경우에는 이 문제를 파악하고 수정하였음을 명시하기 위해 readOnly 값을 false로 설정했음을 알림.
    @Transactional(readOnly = false)
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    // Lv1 - 3. null 일 수 있는 값 weather, startDate - endDate 를 기준으로 검색할 수 있도록 수정
    public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDate startDate, LocalDate endDate ) {
        Pageable pageable = PageRequest.of(page - 1, size);

        LocalDateTime startDateTime = (startDate == null) ? null : startDate.atStartOfDay();
        LocalDateTime endDateTime = (endDate == null) ? null : endDate.atTime(23, 59, 59);
        Page<Todo> todos = todoRepository.findSearchTodo(pageable, weather, startDateTime, endDateTime);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }
}
