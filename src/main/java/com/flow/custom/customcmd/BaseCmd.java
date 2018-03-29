package com.flow.custom.customcmd;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.DelegationState;

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
}
