package com.example.spring.controller;

import com.example.spring.common.Result;
import com.example.spring.common.UserContext;
import com.example.spring.dto.ReviewDTO;
import com.example.spring.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 批阅模块 /review
 */
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 批阅作业(教师/管理员)
     */
    @PostMapping
    public Result<Object> review(@Valid @RequestBody ReviewDTO dto) {
        UserContext.requireRole(UserContext.get().isTeacherOrAdmin());
        reviewService.review(dto);
        return Result.ok("批阅成功");
    }

    /**
     * 查看批阅结果
     */
    @GetMapping("/submission/{submissionId}")
    public Result<Object> getBySubmission(@PathVariable Long submissionId) {
        return Result.ok(reviewService.getBySubmission(submissionId));
    }

    /**
     * 成绩统计(教师/管理员)
     */
    @GetMapping("/statistics")
    public Result<Object> statistics(@RequestParam Long taskId,
                                     @RequestParam(required = false) String className) {
        UserContext.requireRole(UserContext.get().isTeacherOrAdmin());
        return Result.ok(reviewService.statistics(taskId, className));
    }
}
