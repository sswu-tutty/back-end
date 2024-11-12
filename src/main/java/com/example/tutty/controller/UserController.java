package com.example.tutty.controller;

import com.example.tutty.dto.user.UserDTO;
import com.example.tutty.exception.InvalidCredentialsException;
import com.example.tutty.security.JwtTokenProvider;
import com.example.tutty.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    // 회원가입 API
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO);

        // 응답 메시지 생성
        Map<String, String> response = new HashMap<>();
        response.put("message", "회원가입 성공");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody UserDTO userDTO) {
        try {
            // 사용자 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getUserId(), userDTO.getPassword())
            );

            // 인증 성공 시 JWT 토큰 생성
            String jwtToken = jwtTokenProvider.createToken(authentication.getName());

            // 응답으로 토큰을 Map 형태로 반환
            Map<String, String> response = new HashMap<>();
            response.put("token", jwtToken);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }
    }
    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        // 사용자 이름 가져오기
        String userName = userService.getUserNameByUserId(userDetails.getUsername());

        // 응답으로 사용자 이름을 반환
        Map<String, String> response = new HashMap<>();
        response.put("name", userName);

        return ResponseEntity.ok(response);
    }

}
