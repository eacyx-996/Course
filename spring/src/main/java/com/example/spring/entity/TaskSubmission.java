package com.example.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业提交表
 */
@Data
@TableName("task_submission")
public class TaskSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long studentId;

    /* 作业文本 */
    private String content;

    /* 附件路径 */
    private String attachmentUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /* 分数(NULL=未批阅) */
    private BigDecimal score;

    /* 评语 */
    private String comment;

    private Long reviewerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    /* 0-待批阅 1-已批阅 */
    private Integer status;

    /* 任务标题(非表) */
    @TableField(exist = false)
    private String taskTitle;

    /* 学生姓名(非表) */
    @TableField(exist = false)
    private String studentName;

    /* 学号(非表) */
    @TableField(exist = false)
    private String username;

    /* 批阅人姓名(非表) */
    @TableField(exist = false)
    private String reviewerName;
}
