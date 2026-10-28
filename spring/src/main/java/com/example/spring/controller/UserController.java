package com.example.spring.controller;

import com.example.spring.common.ErrorCode;
import com.example.spring.common.PageVO;
import com.example.spring.common.Result;
import com.example.spring.common.UserContext;
import com.example.spring.dto.LoginDTO;
import com.example.spring.dto.RegisterDTO;
import com.example.spring.entity.SysUser;
import com.example.spring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户模块 /user
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 注册(公开)
     */
    @PostMapping("/register")
    public Result<Object> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok("注册成功", userService.register(dto));
    }

    /**
     * 登录(公开)
     */
    @PostMapping("/login")
    public Result<Object> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok("登录成功", userService.login(dto));
    }

    /**
     * 当前登录用户信息
     */
    @GetMapping("/info")
    public Result<Object> info() {
        SysUser user = userService.getById(UserContext.getUserId());
        if (user == null) {
            return Result.fail(ErrorCode.UNAUTHORIZED);
        }
        return Result.ok(user);
    }

    /**
     * 用户分页列表(教师/管理员)
     */
    @GetMapping("/page")
    public Result<Object> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String keyword) {
        UserContext.requireRole(UserContext.get().isTeacherOrAdmin());
        return Result.ok(PageVO.of(userService.pageUsers(pageNum, pageSize, role, className, keyword)));
    }

    /**
     * 启用/禁用(仅 admin)
     */
    @PutMapping("/{userId}/status")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestBody StatusBody body) {
        UserContext.requireRole(UserContext.get().isAdmin());
        userService.updateStatus(userId, body.getStatus());
        return Result.ok("操作成功", null);
    }

    // 局部请求体,避免多建一个 DTO 类
    public static class StatusBody {
        private Integer status;
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }
}
