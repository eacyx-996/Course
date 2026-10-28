package com.example.spring.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring.common.BizException;
import com.example.spring.common.ErrorCode;
import com.example.spring.common.UserContext;
import com.example.spring.dto.ReviewDTO;
import com.example.spring.entity.SysUser;
import com.example.spring.entity.TaskSubmission;
import com.example.spring.mapper.SysUserMapper;
import com.example.spring.mapper.TaskSubmissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService extends ServiceImpl<TaskSubmissionMapper, TaskSubmission> {

    private final SysUserMapper sysUserMapper;

    /**
     * 批阅作业:打分+评语,状态置为已批阅
     */
    public void review(ReviewDTO dto) {
        TaskSubmission submission = getById(dto.getSubmissionId());
        if (submission == null) {
            throw new BizException(ErrorCode.SUBMISSION_NOT_FOUND);
        }
        submission.setScore(dto.getScore());
        submission.setComment(dto.getComment());
        submission.setReviewerId(UserContext.getUserId());
        submission.setReviewTime(LocalDateTime.now());
        submission.setStatus(1);
        updateById(submission);
    }

    /**
     * 查看批阅结果;学生仅可查看本人提交
     */
    public TaskSubmission getBySubmission(Long submissionId) {
        TaskSubmission submission = getById(submissionId);
        if (submission == null) {
            throw new BizException(ErrorCode.SUBMISSION_NOT_FOUND);
        }
        UserContext.LoginUser login = UserContext.get();
        if ("student".equals(login.role()) && !submission.getStudentId().equals(login.userId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "不能查看他人成绩");
        }
        if (submission.getStatus() == null || submission.getStatus() != 1) {
            throw new BizException(ErrorCode.NOT_FOUND, "尚未批阅");
        }
        if (submission.getReviewerId() != null) {
            SysUser reviewer = sysUserMapper.selectById(submission.getReviewerId());
            if (reviewer != null) {
                submission.setReviewerName(reviewer.getRealName());
            }
        }
        return submission;
    }

    /**
     * 成绩统计:提交率/平均分/分数段分布,可按班级筛选
     */
    public Map<String, Object> statistics(Long taskId, String className) {
        List<TaskSubmission> all = list(new LambdaQueryWrapper<TaskSubmission>()
                .eq(TaskSubmission::getTaskId, taskId));

        List<Long> classStudentIds = null;
        if (org.springframework.util.StringUtils.hasText(className)) {
            classStudentIds = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getRole, "student")
                            .eq(SysUser::getClassName, className)
                            .select(SysUser::getId))
                    .stream().map(SysUser::getId).toList();
            List<Long> ids = classStudentIds;
            all = all.stream().filter(s -> ids.contains(s.getStudentId())).toList();
        }

        List<TaskSubmission> reviewed = all.stream()
                .filter(s -> s.getStatus() != null && s.getStatus() == 1 && s.getScore() != null)
                .toList();

        long totalStudents;
        if (classStudentIds != null) {
            totalStudents = classStudentIds.size();
        } else {
            totalStudents = sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "student"));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("totalStudents", totalStudents);
        data.put("submittedCount", all.size());
        data.put("reviewedCount", reviewed.size());
        data.put("submissionRate", totalStudents == 0 ? 0
                : BigDecimal.valueOf((double) all.size() / totalStudents).setScale(2, RoundingMode.HALF_UP));

        if (!reviewed.isEmpty()) {
            data.put("avgScore", reviewed.stream()
                    .map(TaskSubmission::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(reviewed.size()), 2, RoundingMode.HALF_UP));
            data.put("maxScore", reviewed.stream().map(TaskSubmission::getScore).max(BigDecimal::compareTo).orElse(null));
            data.put("minScore", reviewed.stream().map(TaskSubmission::getScore).min(BigDecimal::compareTo).orElse(null));
        }

        // 分数段分布 90-100 / 80-89 / 70-79 / 60-69 / 0-59
        int[] ranges = {90, 80, 70, 60, 0};
        List<Map<String, Object>> distribution = new java.util.ArrayList<>();
        for (int low : ranges) {
            int high = low == 90 ? 100 : low + 9;
            long count = reviewed.stream()
                    .filter(s -> s.getScore().compareTo(BigDecimal.valueOf(low)) >= 0
                            && s.getScore().compareTo(BigDecimal.valueOf(high)) <= 0)
                    .count();
            distribution.add(Map.of("range", low + "-" + high, "count", count));
        }
        data.put("scoreDistribution", distribution);
        return data;
    }
}
