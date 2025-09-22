package org.example.expert.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        // Lv1 - 2. User 정보에 nickname 추가 및 JWT 페이로드에 nickname 정보 추가
        // nickname 정보를 request로 받아 새로운 유저 정보를 저장합니다.
        User newUser = new User(
                signupRequest.getEmail(),
                encodedPassword,
                userRole,
                signupRequest.getNickname()
        );
        User savedUser = userRepository.save(newUser);

        // Lv1 - 2. User 정보에 nickname 추가 및 JWT 페이로드에 nickname 정보 추가
        // 저장한 유저 정보를 기준으로 새 토큰을 발급합니다. (회원가입 후 자동 로그인 기능)
        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), userRole, savedUser.getNickname());

        return new SignupResponse(bearerToken);
    }

    public SigninResponse signin(SigninRequest signinRequest) {
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new InvalidRequestException("가입되지 않은 유저입니다."));

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        // Lv1 - 2. User 정보에 nickname 추가 및 JWT 페이로드에 nickname 정보 추가
        // 로그인 시 발급되는 토큰의 값에 닉네임을 추가함.
        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());

        return new SigninResponse(bearerToken);
    }
}
