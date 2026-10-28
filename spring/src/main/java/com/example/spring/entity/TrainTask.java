package com.example.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实训任务表
 */
@Data
@TableName("train_task")
public class TrainTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /* 所属知识点 */
    private String knowledgePoint;

    /* 任务详情 */
    private String content;

    /* 任务附件 */
    private String attachmentUrl;

    /* 目标班级(NULL=全部班级) */
    private String className;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    /* 0-未开始(草稿) 1-进行中 2-已截止 */
    private Integer status;

    private Long publisherId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /* 发布人姓名(非表) */
    @TableField(exist = false)
    private String publisherName;

    /* 学生端:本人是否已提交(非表) */
    @TableField(exist = false)
    private Boolean submitted;
}
