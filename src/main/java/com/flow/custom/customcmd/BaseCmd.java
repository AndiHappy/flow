package com.flow.custom.customcmd;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.task.DelegationState;

import com.flow.custom.behavior.CustomUserTaskActivityBehavior;

/**
 * @author zhailz
 *
 * @version 2018年3月27日 下午4:11:35
 */
public abstract class BaseCmd {

	protected void complete(TaskEntity task,String completeReason, Map<String, Object> variables2, boolean localScope) {
		if (task.getDelegationState() != null && task.getDelegationState().equals(DelegationState.PENDING)) {
	  		throw new ActivitiException("A delegated task cannot be completed, but should be resolved instead.");
	  	}
	  	
		task.fireEvent(TaskListener.EVENTNAME_COMPLETE);
	    
	    if(Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
	    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
	    	    ActivitiEventBuilder.createEntityWithVariablesEvent(ActivitiEventType.TASK_COMPLETED, this, variables2, localScope));
	    }
	 
	    Context.getCommandContext().getTaskEntityManager().deleteTask(task, completeReason, false);
	    
	    if (task.getExecutionId()!=null) {
	      ExecutionEntity execution = task.getExecution();
	      execution.removeTask(task);
	    }
		
	}
	
	/***
	 * 根据活动标识（activityId）克隆出来一个活动定义
	 * */
	protected ActivityImpl onlyCloneActivity(String activityId,ProcessDefinitionImpl processDefinition) {
		ActivityImpl tmp = processDefinition.createActivity(activityId);
		TaskDefinition definition = new TaskDefinition(null);
		definition.setKey(activityId);
		Expression nameExpression = new FixedValue(activityId);
		definition.setNameExpression(nameExpression);
		tmp.setActivityBehavior(new CustomUserTaskActivityBehavior(new TaskDefinition(null)));
		return tmp;
	}
	
	protected String getSuspendedTaskException() {
		return "Cannot execute operation: task is suspended";
	}
}
