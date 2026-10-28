package com.example.spring.controller;

import com.example.spring.common.PageVO;
import com.example.spring.common.Result;
import com.example.spring.common.UserContext;
import com.example.spring.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 作业提交模块 /submission
 */
@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * 提交作业(学生)
     */
    @PostMapping
    public Result<Object> submit(
            @RequestParam Long taskId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile file) {
        UserContext.requireRole("student".equals(UserContext.get().role()));
        Long submissionId = submissionService.submit(taskId, content, file);
        return Result.ok("提交成功", Map.of(
                "submissionId", submissionId,
                "submitTime", java.time.LocalDateTime.now()));
    }

    /**
     * 我的提交记录(学生)
     */
    @GetMapping("/my")
    public Result<Object> my(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        UserContext.requireRole("student".equals(UserContext.get().role()));
        return Result.ok(PageVO.of(submissionService.myPage(pageNum, pageSize)));
    }

    /**
     * 任务提交列表(教师/管理员批阅入口)
     */
    @GetMapping("/page")
    public Result<Object> pageByTask(
            @RequestParam Long taskId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String className,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        UserContext.requireRole(UserContext.get().isTeacherOrAdmin());
        return Result.ok(PageVO.of(submissionService.pageByTask(taskId, status, className, pageNum, pageSize)));
    }
}
