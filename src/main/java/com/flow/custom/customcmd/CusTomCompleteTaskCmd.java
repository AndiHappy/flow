package com.flow.custom.customcmd;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cmd.CompleteTaskCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.task.DelegationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flow.util.ActivityManagerUtil;

/**
 * @author zhailz
 *
 * @version 2018年3月28日 上午11:07:58
 */
public class CusTomCompleteTaskCmd extends CompleteTaskCmd {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger("CusTomCompleteTaskCmd");

	
	public CusTomCompleteTaskCmd(String taskId, Map<String, Object> variables) {
		super(taskId, variables);
	}

	public CusTomCompleteTaskCmd(String taskId, Map<String, Object> variables, boolean localScope) {
		super(taskId, variables, localScope);
	}

	/**
	 * custom complete action for custom action
	 */
	@Override
	protected Void execute(CommandContext commandContext, TaskEntity task) {

		ExecutionEntity entity = task.getExecution();
		//find activiti in process definition
		ActivityImpl activi = entity.getActivity();
		String currentActivityId = entity.getActivityId();
		if(currentActivityId != null && activi == null){
			//construct activiti by variables
			if(entity.getVariables() != null && entity.getVariable(ActivityManagerUtil.getCurrentActivityIdName(currentActivityId)) != null){
				String currentid = (String) entity.getVariable(ActivityManagerUtil.getCurrentActivityIdName(currentActivityId));
				activi =  ActivityManagerUtil.getInstance().getCacheActivity(currentid);
				if(activi == null){
					String desid = (String) entity.getVariable(ActivityManagerUtil.getDestinationActivityIdName(currentActivityId));
					ProcessDefinitionImpl processDef = entity.getProcessDefinition();
					activi = ActivityManagerUtil.clone(currentid,desid,processDef,task.getAssignee());
				}
				
				if(activi != null){
					entity.setActivity(activi);
				}
			}
			
		}
		task.setExecution(entity);

		if (variables != null) {
			if (localScope) {
				task.setVariablesLocal(variables);
			} else {
				task.setExecutionVariables(variables);
			}
		}

		task.complete(variables, localScope);
		return null;
	}
	
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
