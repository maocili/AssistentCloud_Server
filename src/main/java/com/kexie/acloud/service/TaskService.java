package com.kexie.acloud.service;

import com.kexie.acloud.dao.ISocietyDao;
import com.kexie.acloud.dao.ITaskDao;
import com.kexie.acloud.domain.Society;
import com.kexie.acloud.domain.SubTask;
import com.kexie.acloud.domain.Task;
import com.kexie.acloud.domain.User;
import com.kexie.acloud.exception.AuthorizedException;
import com.kexie.acloud.exception.TaskException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.Resource;
import javax.naming.AuthenticationException;
import javax.transaction.Transactional;

/**
 * Created : wen
 * DateTime : 2017/4/27 10:00
 * Description :
 */

@Service
@Transactional
public class TaskService implements ITaskService {

    @Autowired
    ITaskDao mTaskDao;

    @Resource
    ISocietyDao mSocietyDao;

    @Override
    public Task getTaskByTaskId(String taskId) {

        Task task = mTaskDao.getTasksByTaskId(taskId);

        if (task == null)
            throw new TaskException("没有找到当前任务");

        return task;
    }

    @Override
    public List<Task> getTaskBySocietyId(int societyId) {
        return mTaskDao.getTasksBySocietyId(societyId);
    }

    @Override
    public List<Task> getTaskByUserId(String userId) {
        return mTaskDao.getTasksByUserId(userId);
    }

    @Override
    public List<Task> getTaskByPublisherId(String publisherId) {
        return mTaskDao.getTasksByPublisherId(publisherId);
    }

    @Override
    public void create(Task task, String userId) throws AuthenticationException {

        Society society = task.getSociety();

        if (!mSocietyDao.isInSociety(society.getId(), userId)) {
            throw new AuthenticationException("用户 " + userId + " 不在当前社团 " + society.getId() + " 中");
        }

        User user = new User();
        user.setUserId(userId);

        task.setPublisher(user);

        mTaskDao.add(task);
    }

    @Override
    public Task update(Task task) {
        if (mTaskDao.getTasksByTaskId(task.getId()) == null)
            throw new TaskException("任务不存在,taskId找不到");
        return mTaskDao.update(task);
    }


    @Override
    public void updateSubTask(List<SubTask> subTasks) {
        // 更新子任务
        subTasks.forEach(task -> mTaskDao.updateSubTask(task));
    }

    @Override
    public void updateSubTask(String taskId, List<SubTask> subTask) {
        Task task = mTaskDao.getTasksByTaskId(taskId);
        task.setSubTask(subTask);
        mTaskDao.update(task);
    }

    @Override
    public void updateExecutor(String taskId, List<User> executors) {
        Task task = mTaskDao.getTasksByTaskId(taskId);
        task.setExecutors(executors);
        mTaskDao.update(task);
    }

    @Override
    public void active(String taskId) {
        Task task = mTaskDao.getTasksByTaskId(taskId);
        task.setTaskType(1);
        mTaskDao.update(task);
    }

    @Override
    public void archive(String taskId, String userId) throws AuthorizedException {

        Task task = mTaskDao.getTasksByTaskId(taskId);

        if (task == null)
            throw new TaskException("没有这条数据");

        // 判断处理人是否在执行者中
        if (!mTaskDao.isInExecutor(userId, taskId))
            throw new AuthorizedException("当前用户不在任务中，没有权限操作当前任务");

        task.setTaskType(2);
        mTaskDao.update(task);
    }

    @Override
    public void delete(String taskId) {
        Task task = mTaskDao.getTasksByTaskId(taskId);

        if (task == null)
            throw new TaskException("没有这条数据");

        task.setTaskType(3);
        mTaskDao.update(task);
    }
}
