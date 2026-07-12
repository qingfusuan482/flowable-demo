package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.Result;
import org.example.model.dto.LoginRequest;
import org.example.model.vo.LoginVO;
import org.example.model.vo.UserInfoVO;
import org.example.security.SecurityUtils;
import org.example.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/user-info")
    public Result<UserInfoVO> userInfo() {
        String username = SecurityUtils.getCurrentUsername();
        return Result.ok(authService.getUserInfo(username));
    }
}
