package com.flow.custom.customcmd;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.behavior.GatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flow.util.exception.BaseIllegalException;

/**
 * @author zhailz
 * 
 *
 * @version 2018年3月21日 下午4:20:35
 */
public class BackTaskCmd implements Command<List<TaskEntity>> {

	private Logger log = LoggerFactory.getLogger("BackTaskCmd");

	private String taskId;

	private Map<String, Object> variables;

	private boolean localScope;
	
	 public static final String DELETE_REASON_DELETED = "back action delete";

	public BackTaskCmd(String taskId, Map<String, Object> variables, boolean localScope) {
		this.taskId = taskId;
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
	public List<TaskEntity> execute(CommandContext commandContext) {

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
		ProcessDefinitionImpl processDefinition = task.getExecution().getProcessDefinition();
		ActivityImpl activiti = task.getExecution().getProcessDefinition().findActivity(definitionKey);
		if(activiti != null){
			ActivityImpl back = getBackActiviti(activiti,processDefinition);
			if(back != null){
				log.info("find back activiti:{}",back.toString());
				ExecutionEntity execution = task.getExecution();
				complete(task,this.getVariables(),this.isLocalScope());
				execution.setActivity(back);
				execution.performOperation(AtomicOperation.ACTIVITY_START);
				List<TaskEntity> tasks = task.getExecution().getTasks();
				return tasks;
			}
		}

		return null;
	}

	
	private void complete(TaskEntity task, Map<String, Object> variables2, boolean localScope2) {
		if (task.getDelegationState() != null && task.getDelegationState().equals(DelegationState.PENDING)) {
	  		throw new ActivitiException("A delegated task cannot be completed, but should be resolved instead.");
	  	}
	  	
		task.fireEvent(TaskListener.EVENTNAME_COMPLETE);
	    
	    if(Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
	    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
	    	    ActivitiEventBuilder.createEntityWithVariablesEvent(ActivitiEventType.TASK_COMPLETED, this, variables2, localScope));
	    }
	 
	    Context.getCommandContext().getTaskEntityManager().deleteTask(task, "backAction", false);
	    
	    if (task.getExecutionId()!=null) {
	      ExecutionEntity execution = task.getExecution();
	      execution.removeTask(task);
//	      execution.signal(null, null);
	    }
		
	}

	private ActivityImpl getBackActiviti(ActivityImpl currentActiviti, ProcessDefinitionImpl processDefinition) {
		List<PvmTransition> incomings = currentActiviti.getIncomingTransitions();
		if(incomings != null && incomings.size() > 1){
			throw BaseIllegalException.tooManyBackActivities;
		}
		
		if(incomings != null && !incomings.isEmpty()){
			ActivityImpl sources = (ActivityImpl) incomings.get(0).getSource();
			
			if(sources.getActivityBehavior() instanceof UserTaskActivityBehavior || sources.getActivityBehavior() instanceof NoneStartEventActivityBehavior){
				return sources;
			}
			
			//如果是网关，回退的过程中碰到是网关的情况下，这中情况下也不能回退
			if(sources.getActivityBehavior() instanceof GatewayActivityBehavior){
				throw BaseIllegalException.backActivitiIsWrongType;
			}
		}else{
			throw BaseIllegalException.noBackActivitiIs;
		}
		
		return null;
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
