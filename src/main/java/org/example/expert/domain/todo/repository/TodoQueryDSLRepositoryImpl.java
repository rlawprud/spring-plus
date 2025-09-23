package org.example.expert.domain.todo.repository;

// Lv 2 - 3. QueryDSL 추가

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;

import java.util.Optional;

@RequiredArgsConstructor
public class TodoQueryDSLRepositoryImpl implements TodoQueryDSLRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findVyIdWithUser(Long todoId) {

        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchjoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
