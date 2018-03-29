package com.flow.custom.behavior;

import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.task.TaskDefinition;

import com.flow.custom.service.CustomUserServer;

/**
 * @author zhailz
 *
 * @version 2018年3月21日 下午8:43:14
 */
public class CustomActivityBehaviorFactory extends DefaultActivityBehaviorFactory implements ActivityBehaviorFactory
{
	
	private CustomUserServer customUserServer = new CustomUserServer();
	
	@Override
	public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask, TaskDefinition taskDefinition)
	{
		return new CustomUserTaskActivityBehavior(taskDefinition,customUserServer);
	}
}