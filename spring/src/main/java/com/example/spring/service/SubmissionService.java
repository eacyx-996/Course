package com.example.spring.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring.common.BizException;
import com.example.spring.common.ErrorCode;
import com.example.spring.common.UserContext;
import com.example.spring.entity.SysUser;
import com.example.spring.entity.TaskSubmission;
import com.example.spring.entity.TrainTask;
import com.example.spring.mapper.SysUserMapper;
import com.example.spring.mapper.TaskSubmissionMapper;
import com.example.spring.mapper.TrainTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService extends ServiceImpl<TaskSubmissionMapper, TaskSubmission> {

    private static final Set<String> ALLOW_EXT = Set.of("zip", "rar", "pdf", "doc", "docx", "md");
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    // 直接注入 Mapper,避免与 TaskService 产生循环依赖
    private final TrainTaskMapper trainTaskMapper;
    private final SysUserMapper sysUserMapper;

    @Value("${app.upload-dir}")
    private String uploadDir;

    /**
     * 学生提交作业;同一任务重复提交时覆盖原记录
     */
    public Long submit(Long taskId, String content, MultipartFile file) {
        TrainTask task = trainTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        if (task.getDeadline() != null && LocalDateTime.now().isAfter(task.getDeadline())) {
            throw new BizException(ErrorCode.TASK_CLOSED);
        }

        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            attachmentUrl = saveFile(file);
        }

        Long studentId = UserContext.getUserId();
        TaskSubmission exist = getOne(new LambdaQueryWrapper<TaskSubmission>()
                .eq(TaskSubmission::getTaskId, taskId)
                .eq(TaskSubmission::getStudentId, studentId));
        if (exist != null) {
            // 重复提交:覆盖内容,重置批阅状态
            exist.setContent(content);
            if (attachmentUrl != null) {
                exist.setAttachmentUrl(attachmentUrl);
            }
            exist.setSubmitTime(LocalDateTime.now());
            exist.setStatus(0);
            exist.setScore(null);
            exist.setComment(null);
            exist.setReviewerId(null);
            exist.setReviewTime(null);
            updateById(exist);
            return exist.getId();
        }

        TaskSubmission submission = new TaskSubmission();
        submission.setTaskId(taskId);
        submission.setStudentId(studentId);
        submission.setContent(content);
        submission.setAttachmentUrl(attachmentUrl);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setStatus(0);
        save(submission);
        return submission.getId();
    }

    /**
     * 我的提交记录(学生)
     */
    public Page<TaskSubmission> myPage(int pageNum, int pageSize) {
        Long studentId = UserContext.getUserId();
        Page<TaskSubmission> page = page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TaskSubmission>()
                        .eq(TaskSubmission::getStudentId, studentId)
                        .orderByDesc(TaskSubmission::getSubmitTime));
        fillExtra(page.getRecords());
        return page;
    }

    /**
     * 任务提交列表(教师/管理员批阅入口),可按学生班级筛选
     */
    public Page<TaskSubmission> pageByTask(Long taskId, Integer status, String className, int pageNum, int pageSize) {
        if (trainTaskMapper.selectById(taskId) == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        LambdaQueryWrapper<TaskSubmission> wrapper = new LambdaQueryWrapper<TaskSubmission>()
                .eq(TaskSubmission::getTaskId, taskId)
                .eq(status != null, TaskSubmission::getStatus, status)
                .orderByDesc(TaskSubmission::getSubmitTime);
        if (org.springframework.util.StringUtils.hasText(className)) {
            List<Long> studentIds = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getClassName, className)
                            .select(SysUser::getId))
                    .stream().map(SysUser::getId).toList();
            if (studentIds.isEmpty()) {
                return new Page<>(pageNum, pageSize);
            }
            wrapper.in(TaskSubmission::getStudentId, studentIds);
        }
        Page<TaskSubmission> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillExtra(page.getRecords());
        return page;
    }

    public long countByTaskAndStudent(Long taskId, Long studentId) {
        return count(new LambdaQueryWrapper<TaskSubmission>()
                .eq(TaskSubmission::getTaskId, taskId)
                .eq(TaskSubmission::getStudentId, studentId));
    }

    /**
     * 保存附件,返回访问路径
     */
    private String saveFile(MultipartFile file) {
        if (file.getSize() > MAX_SIZE) {
            throw new BizException(ErrorCode.FILE_TOO_LARGE);
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOW_EXT.contains(ext)) {
            throw new BizException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName = UUID.randomUUID() + "." + ext;
        try {
            Path dir = Paths.get(uploadDir, datePath);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(fileName));
        } catch (IOException e) {
            throw new BizException(ErrorCode.SERVER_ERROR, "文件保存失败");
        }
        return "/upload/" + datePath + "/" + fileName;
    }

    /**
     * 填充任务标题/学生姓名等非表字段
     */
    private void fillExtra(List<TaskSubmission> list) {
        if (list.isEmpty()) {
            return;
        }
        List<Long> taskIds = list.stream().map(TaskSubmission::getTaskId).distinct().toList();
        Map<Long, String> taskMap = trainTaskMapper.selectByIds(taskIds).stream()
                .collect(Collectors.toMap(TrainTask::getId, TrainTask::getTitle));
        List<Long> studentIds = list.stream().map(TaskSubmission::getStudentId).distinct().toList();
        Map<Long, SysUser> userMap = sysUserMapper.selectByIds(studentIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        list.forEach(s -> {
            s.setTaskTitle(taskMap.get(s.getTaskId()));
            SysUser u = userMap.get(s.getStudentId());
            if (u != null) {
                s.setStudentName(u.getRealName());
                s.setUsername(u.getUsername());
            }
        });
    }
}
