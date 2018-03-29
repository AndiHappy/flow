package com.flow.custom.service;

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

import com.flow.custom.customcmd.BackTaskCmd;
import com.flow.custom.customcmd.BeforeSignCmd;
import com.flow.custom.customcmd.CusTomCompleteTaskCmd;

/**
 * @author zhailz
 * 
 *         扩展task的操作：前进（complete），后退（back）
 * 
 * @version 2018年3月21日 下午4:17:38
 */
@Component
public class CustomTaskServiceImpl extends TaskServiceImpl implements CusTomTaskService {

	/**
	 * @param taskId
	 *            当前任务的taskId
	 * @param afterTaskIsdeleteCurrentTask
	 *            当前的任务是否删除
	 * @param variables
	 *            参数
	 * @param localScope
	 *            范围
	 * @return 后退的过程中，产生新的任务
	 * 
	 */
	public List<TaskEntity> back(String taskId, Map<String, Object> variables, boolean localScope) {
		return commandExecutor.execute(new BackTaskCmd(taskId, variables, localScope));
	}

	public TaskEntity beforeSign(String taskId, Map<String, Object> variables, boolean localScope, String assignee) {
		return commandExecutor.execute(new BeforeSignCmd(taskId, variables, localScope, assignee));
	}

	@Override
	public void complete(String taskId) {
		commandExecutor.execute(new CusTomCompleteTaskCmd(taskId, null));
	}
	
	@Override
	public void complete(String taskId, Map<String, Object> variables) {
		commandExecutor.execute(new CusTomCompleteTaskCmd(taskId, variables));
	}

	@Override
	public void complete(String taskId, Map<String, Object> variables, boolean localScope) {
		commandExecutor.execute(new CusTomCompleteTaskCmd(taskId, variables, localScope));
	}

}
