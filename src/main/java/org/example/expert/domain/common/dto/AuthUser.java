package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final UserRole userRole;
    // Lv1 - 2. User 정보에 nickname 추가 및 JWT 페이로드에 nickname 정보 추가
    public String nickname;

    public AuthUser(Long id, String email, UserRole userRole, String nickname) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
        this.nickname =  nickname;
    }
}
