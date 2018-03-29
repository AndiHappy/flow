package com.flow.custom.customcmd;

import java.util.Map;
import java.util.UUID;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flow.custom.behavior.SignUserTaskBehavior;
import com.flow.util.ActivityManagerUtil;

/**
 * @author zhailz
 *
 * @version 2018年3月27日 下午3:58:43
 */
public class BeforeSignCmd extends BaseCmd implements Command<TaskEntity> {
	
	private Logger log = LoggerFactory.getLogger("BeforeSignCmd");
	
	private String taskId;
	private Map<String, Object> variables;
	private boolean localScope;
	private String assignee;
	
	public static final String DELETE_REASON_DELETED = "before_sign_action";

	public BeforeSignCmd(String taskId, Map<String, Object> variables, boolean localScope,String assignee) {
		this.taskId = taskId;
		this.variables = variables;
		this.localScope = localScope;
		this.assignee = assignee;
	}

	@Override
	public TaskEntity execute(CommandContext commandContext) {
		log.info("BeforeSignCmd:{},", this.taskId);
		TaskEntity task = commandContext.getTaskEntityManager().findTaskById(taskId);

		if (task == null) {
			throw new ActivitiObjectNotFoundException("Cannot find task with id " + taskId, Task.class);
		}

		if (task.isSuspended()) {
			throw new ActivitiException(getSuspendedTaskException());
		}
		
		log.info("find current task taskId:{},taskKey:{}",task.getId(),task.getName());
		
		ExecutionEntity execution = task.getExecution();
		complete(task,DELETE_REASON_DELETED,this.getVariables(),this.isLocalScope());
		
		//first:taskEntity refer to Activiti Definition
		//second:clone the activiti definition,set assigen 
		ProcessDefinitionImpl processDefinition = task.getExecution().getProcessDefinition();
		String definitionKey = task.getTaskDefinitionKey();
		//insert before task action ,this task refter to activity is  destination activity
		ActivityImpl desactiviti = processDefinition.findActivity(definitionKey);
		if(desactiviti != null){
			ActivityImpl activititmp = clone(desactiviti,processDefinition);
			if(activititmp != null){
				log.info("clone activiti:{}",activititmp.toString());
				execution.setActivity(activititmp);
				//this taskentity refter to destination activity
				execution.setVariable(ActivityManagerUtil.getDestinationActivityIdName(activititmp.getId()), desactiviti.getId());
				//this taskentity refter to current activity
				execution.setVariable(ActivityManagerUtil.getCurrentActivityIdName(activititmp.getId()), activititmp.getId());
				execution.performOperation(AtomicOperation.ACTIVITY_START);
			}
		}else{
			String currentid = task.getExecution().getActivityId();
			String desid = (String) execution.getVariable(ActivityManagerUtil.getDestinationActivityIdName(task.getId()));
			desactiviti = ActivityManagerUtil.clone(currentid, desid, processDefinition, task.getAssignee());
//			ActivityImpl activititmp = clone(activiti,processDefinition);
//			if(activititmp != null){
//				log.info("clone activiti:{}",activititmp.toString());
//				execution.setActivity(activititmp);
//				execution.setVariable(ActivityManagerUtil.destinationActivityIdName, activiti.getId());
//				execution.setVariable(ActivityManagerUtil.currentActivityIdName, activititmp.getId());
//				execution.performOperation(AtomicOperation.ACTIVITY_START);
//			}
		}

		if(execution.getTasks() != null && !execution.getTasks().isEmpty()){
			return execution.getTasks().get(0);
		}else{
			return null;
		}
	}

	// temporary construct sign activiti，
	private ActivityImpl clone(ActivityImpl activiti, ProcessDefinitionImpl processDefinition) {
		String beforeId = activiti.getId();
		String activityId = "IBT@"+beforeId+"@"+System.currentTimeMillis();
		ActivityImpl tmp = processDefinition.createActivity(activityId);
		TaskDefinition definitions = new TaskDefinition(null);
		Expression nameExpression = new FixedValue(beforeId+"@BeforeTaskInsert");
		definitions.setNameExpression(nameExpression);
		tmp.setActivityBehavior(new SignUserTaskBehavior(definitions,this.assignee));
		TransitionImpl transition = tmp.createOutgoingTransition();
		transition.setDestination(activiti);
		ActivityManagerUtil.getInstance().cache(tmp);
		return tmp;
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

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	
}
