package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;

import java.util.Optional;

// Lv 2 - 3. QueryDSL 추가
public interface TodoQueryDSLRepository {

    Optional<Todo> findByIdWithUser(Long todoId);
}
