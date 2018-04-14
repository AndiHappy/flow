package com.flow.custom.customcmd;

import java.io.IOException;
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
import com.flow.util.Charsets;
import com.flow.util.HC;

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
		// find activiti in process definition
		ActivityImpl activi = entity.getActivity();
		String currentActivityId = entity.getActivityId();
		if (currentActivityId != null && activi == null) {
			// construct activiti by variables
			if (entity.getVariables() != null) {
				activi = ActivityManagerUtil.getInstance().getCacheActivity(currentActivityId);
				if (activi == null) {
					String desid = (String) entity.getVariable(ActivityManagerUtil.getDestinationActivityIdName(currentActivityId));
					String assignee = (String) entity.getVariable(ActivityManagerUtil.getCurrentActivityAssigneeName(currentActivityId));
					ProcessDefinitionImpl processDef = entity.getProcessDefinition();
					log.info("clone activity :{}",currentActivityId);
					activi = ActivityManagerUtil.cloneCurrentActivity(currentActivityId, desid, processDef, assignee);
				}
			}

			if (activi != null) {
				entity.setActivity(activi);
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
//		if(task.getName().equals("task1")){
//			try {
//				String json = HC.getInstance().getResponse("http://127.0.0.1:8081/reduce?num=10", null, Charsets.UTF8);
//				log.info("完成任务:{},调用REST接口，返回:{}",task.getName(),json);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
//		
//		if(task.getName().equals("task2")){
//			try {
//				String json = HC.getInstance().getResponse("http://127.0.0.1:8082/notify?msg=success", null, Charsets.UTF8);
//				log.info("完成任务:{},调用REST接口，返回:{}",task.getName(),json);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		return null;
	}

	protected void complete(TaskEntity task, String completeReason, Map<String, Object> variables2, boolean localScope) {
		if (task.getDelegationState() != null && task.getDelegationState().equals(DelegationState.PENDING)) {
			throw new ActivitiException("A delegated task cannot be completed, but should be resolved instead.");
		}

		task.fireEvent(TaskListener.EVENTNAME_COMPLETE);

		if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
			Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityWithVariablesEvent(ActivitiEventType.TASK_COMPLETED, this, variables2, localScope));
		}

		Context.getCommandContext().getTaskEntityManager().deleteTask(task, completeReason, false);

		if (task.getExecutionId() != null) {
			ExecutionEntity execution = task.getExecution();
			execution.removeTask(task);
		}

	}

}
