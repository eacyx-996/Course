package com.example.spring.controller;

import com.example.spring.common.PageVO;
import com.example.spring.common.Result;
import com.example.spring.common.UserContext;
import com.example.spring.dto.TaskDTO;
import com.example.spring.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 实训任务模块 /task
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 发布任务(admin/teacher)
     */
    @PostMapping
    public Result<Object> create(@Valid @RequestBody TaskDTO dto) {
        UserContext.requireRole(UserContext.get().isTeacherOrAdmin());
        return Result.ok("发布成功", java.util.Map.of("taskId", taskService.create(dto)));
    }

    /**
     * 编辑任务(admin/teacher 本人)
     */
    @PutMapping("/{taskId}")
    public Result<Void> update(@PathVariable Long taskId, @Valid @RequestBody TaskDTO dto) {
        UserContext.requireRole(UserContext.get().isTeacherOrAdmin());
        taskService.update(taskId, dto);
        return Result.ok("修改成功", null);
    }

    /**
     * 删除任务(admin/teacher 本人)
     */
    @DeleteMapping("/{taskId}")
    public Result<Void> delete(@PathVariable Long taskId) {
        UserContext.requireRole(UserContext.get().isTeacherOrAdmin());
        taskService.delete(taskId);
        return Result.ok("删除成功", null);
    }

    /**
     * 任务分页列表;学生仅可见已发布(status>=1)
     */
    @GetMapping("/page")
    public Result<Object> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.ok(PageVO.of(taskService.pageTasks(pageNum, pageSize, keyword, status)));
    }

    /**
     * 任务详情
     */
    @GetMapping("/{taskId}")
    public Result<Object> detail(@PathVariable Long taskId) {
        return Result.ok(taskService.detail(taskId));
    }
}
