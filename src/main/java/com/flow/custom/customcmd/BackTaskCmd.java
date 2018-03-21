package com.flow.custom.customcmd;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhailz
 * 
 *
 * @version 2018年3月21日 下午4:20:35
 */
public class BackTaskCmd implements Command<Object> {

	private Logger log = LoggerFactory.getLogger("BackTaskCmd");

	private String taskId;

	private boolean afterTaskIsdeleteCurrentTask;

	private Map<String, Object> variables;

	private boolean localScope;

	public BackTaskCmd(String taskId, boolean afterTaskIsdeleteCurrentTask, Map<String, Object> variables, boolean localScope) {
		this.taskId = taskId;
		this.afterTaskIsdeleteCurrentTask = afterTaskIsdeleteCurrentTask;
		this.variables = variables;
		this.localScope = localScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.
	 * impl.interceptor.CommandContext)
	 */
	@Override
	public Object execute(CommandContext commandContext) {

		TaskEntity task = commandContext.getTaskEntityManager().findTaskById(taskId);

		if (task == null) {
			throw new ActivitiObjectNotFoundException("Cannot find task with id " + taskId, Task.class);
		}

		if (task.isSuspended()) {
			throw new ActivitiException(getSuspendedTaskException());
		}
		
		log.info("find current task taskId:{},taskKey:{}",task.getId(),task.getName());
		
		//找到back的上一级的task
		
		String definitionKey = task.getTaskDefinitionKey();
		System.out.println(definitionKey);
		
		//
		
		System.out.println();

		return commandContext;
	}

	protected String getSuspendedTaskException() {
		return "Cannot execute operation: task is suspended";
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public boolean isAfterTaskIsdeleteCurrentTask() {
		return afterTaskIsdeleteCurrentTask;
	}

	public void setAfterTaskIsdeleteCurrentTask(boolean afterTaskIsdeleteCurrentTask) {
		this.afterTaskIsdeleteCurrentTask = afterTaskIsdeleteCurrentTask;
	}

	public Map<String, Object> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, Object> variables) {
		this.variables = variables;
	}

	public boolean isLocalScope() {
		return localScope;
	}

	public void setLocalScope(boolean localScope) {
		this.localScope = localScope;
	}

}
