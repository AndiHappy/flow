package com.flow.context;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.task.Task;
import org.activiti.spring.SpringProcessEngineConfiguration;

import com.flow.custom.behavior.CustomActivityBehaviorFactory;
import com.flow.custom.service.CustomTaskServiceImpl;

public class FlowEngine extends SpringProcessEngineConfiguration {

	// 引擎启动的时候，承接activiti的Behavior的行为模式
	@Override
	public ProcessEngine buildProcessEngine() {
		if (!org.activiti.engine.ProcessEngines.isInitialized()) {
			// 替换掉活动的Behavior的工程类
			setActivityBehaviorFactory(new CustomActivityBehaviorFactory());
		} else {
			ProcessEngines.setInitialized(false);
			// 替换掉活动的Behavior的工程类
			setActivityBehaviorFactory(new CustomActivityBehaviorFactory());

		}
		return super.buildProcessEngine();
	}

	/**
	 * 获取代办的事件
	 * */
	public List<Task> getTodoTaskId(String instanceId,String orgId,String userId){
		List<Task> tasks = this.getTaskService().createTaskQuery().processInstanceId(instanceId).taskTenantId(orgId).taskAssignee(userId).active().list();
		return tasks;
	}
	
	public CustomTaskServiceImpl getCustomTaskService() {
	    return (CustomTaskServiceImpl) taskService;
	}
}
