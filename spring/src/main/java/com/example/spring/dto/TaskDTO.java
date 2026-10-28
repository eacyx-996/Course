package com.example.spring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务发布/编辑请求
 */
@Data
public class TaskDTO {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 100, message = "任务标题不超过100字")
    private String title;

    private String knowledgePoint;

    private String content;

    /** 目标班级,不传=全部班级 */
    private String className;

    @NotNull(message = "截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    /** 0 保存草稿 / 1 立即发布 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
