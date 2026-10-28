package com.example.spring.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring.common.BizException;
import com.example.spring.common.ErrorCode;
import com.example.spring.common.UserContext;
import com.example.spring.dto.TaskDTO;
import com.example.spring.entity.SysUser;
import com.example.spring.entity.TrainTask;
import com.example.spring.mapper.TrainTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService extends ServiceImpl<TrainTaskMapper, TrainTask> {

    private final UserService userService;
    private final SubmissionService submissionService;

    /**
     * 发布任务,publisherId 从 Token 获取
     */
    public Long create(TaskDTO dto) {
        checkDeadline(dto.getDeadline(), null);
        TrainTask task = new TrainTask();
        BeanUtils.copyProperties(dto, task);
        task.setPublisherId(UserContext.getUserId());
        save(task);
        return task.getId();
    }

    /**
     * 编辑任务,仅发布人本人或 admin
     */
    public void update(Long taskId, TaskDTO dto) {
        TrainTask task = checkOwn(taskId);
        checkDeadline(dto.getDeadline(), task.getDeadline());
        BeanUtils.copyProperties(dto, task);
        updateById(task);
    }

    /**
     * 删除任务(模板按物理删除;如需逻辑删除请在表中加 deleted 字段)
     */
    public void delete(Long taskId) {
        checkOwn(taskId);
        removeById(taskId);
    }

    /**
     * 任务分页;学生仅可见已发布(status>=1)
     */
    public Page<TrainTask> pageTasks(int pageNum, int pageSize, String keyword, Integer status) {
        UserContext.LoginUser login = UserContext.get();
        LambdaQueryWrapper<TrainTask> wrapper = new LambdaQueryWrapper<TrainTask>()
                .like(StringUtils.hasText(keyword), TrainTask::getTitle, keyword)
                .eq(status != null, TrainTask::getStatus, status)
                .ge("student".equals(login.role()), TrainTask::getStatus, 1);
        // 学生仅可见：面向全部班级的任务 + 自己班级的任务
        if ("student".equals(login.role())) {
            String className = userService.getById(login.userId()).getClassName();
            if (StringUtils.hasText(className)) {
                wrapper.and(w -> w.isNull(TrainTask::getClassName)
                        .or().eq(TrainTask::getClassName, className));
            } else {
                wrapper.isNull(TrainTask::getClassName);
            }
        }
        wrapper.orderByDesc(TrainTask::getCreateTime);
        Page<TrainTask> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillPublisherName(page.getRecords());
        return page;
    }

    /**
     * 任务详情;学生端附带本人是否已提交
     */
    public TrainTask detail(Long taskId) {
        TrainTask task = getById(taskId);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        fillPublisherName(List.of(task));
        UserContext.LoginUser login = UserContext.get();
        if ("student".equals(login.role())) {
            task.setSubmitted(submissionService.countByTaskAndStudent(taskId, login.userId()) > 0);
        }
        return task;
    }

    /**
     * 校验截止时间:必须晚于当前时间;
     * 编辑任务时若截止时间未做调整(与原值相同),允许保留,兼容已截止任务修改其他信息
     */
    private void checkDeadline(LocalDateTime deadline, LocalDateTime originalDeadline) {
        if (deadline == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "截止时间不能为空");
        }
        if (originalDeadline != null && deadline.equals(originalDeadline)) {
            return;
        }
        if (!deadline.isAfter(LocalDateTime.now())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "截止时间必须晚于当前时间");
        }
    }

    /**
     * 校验任务存在且当前用户可操作(发布人本人或 admin)
     */
    public TrainTask checkOwn(Long taskId) {
        TrainTask task = getById(taskId);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        UserContext.LoginUser login = UserContext.get();
        if (!login.isAdmin() && !task.getPublisherId().equals(login.userId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "非发布人无权操作");
        }
        return task;
    }

    private void fillPublisherName(List<TrainTask> tasks) {
        List<Long> ids = tasks.stream().map(TrainTask::getPublisherId).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = userService.listByIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
        tasks.forEach(t -> t.setPublisherName(nameMap.get(t.getPublisherId())));
    }
}
