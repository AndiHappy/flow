package com.flow.custom.customcmd;

import java.util.Map;

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
		//因为是前加签的操作，所以下一个任务：需要跳转回来，即是当前的Task对应的活动定义
		ActivityImpl desactiviti = findDestinationActivity(task, processDefinition);
		
		leave(execution, processDefinition, desactiviti);

		if(execution.getTasks() != null && !execution.getTasks().isEmpty()){
			return execution.getTasks().get(0);
		}else{
			return null;
		}
	}
	
	/**
	 * 当前任务的对应的下一个活动定义
	 * */
	private ActivityImpl findDestinationActivity(TaskEntity task,ProcessDefinitionImpl processDefinition) {
		String definitionKey = task.getTaskDefinitionKey();
		//insert before task action ,this task refter to activity is  destination activity
		//查找当前的任务对应的任务的定义
		ActivityImpl desactiviti = processDefinition.findActivity(definitionKey);
		if(desactiviti == null){
			//如果当前的任务定义为null，说明这个任务本身有可能是加签产生的任务，或者临时生成的任务
			//直接的克隆一个活动定义出来
			desactiviti =  onlyCloneActivity(definitionKey,processDefinition);
		}
		return desactiviti;
	}

	private void leave(ExecutionEntity execution, ProcessDefinitionImpl processDefinition, ActivityImpl desactiviti) {
		//construct new activiti as the insert task which refer to
		ActivityImpl newactivity= cloneBeforeSign(desactiviti,processDefinition);
		if(newactivity != null){
			log.info("clone activiti:{}",newactivity.toString());
			execution.setActivity(newactivity);
			//this taskentity refter to destination activity
			execution.setVariable(ActivityManagerUtil.getDestinationActivityIdName(newactivity.getId()), desactiviti.getId());
			//this taskentity refter to current activity
			execution.setVariable(ActivityManagerUtil.getCurrentActivityIdName(newactivity.getId()), newactivity.getId());
			//this para store activiti`s assigen
			execution.setVariable(ActivityManagerUtil.getCurrentActivityAssigneeName(newactivity.getId()), newactivity.getId());
			execution.performOperation(AtomicOperation.ACTIVITY_START);
		}
	}

	// temporary construct sign activiti，
	private ActivityImpl cloneBeforeSign(ActivityImpl desactiviti, ProcessDefinitionImpl processDefinition) {
		String beforeId = desactiviti.getId();
		String activityId = "IBT@"+beforeId+"@"+System.currentTimeMillis();
		ActivityImpl tmp = processDefinition.createActivity(activityId);
		TaskDefinition definitions = new TaskDefinition(null);
		definitions.setKey(activityId);
		Expression nameExpression = new FixedValue(beforeId+"@IBT");
		definitions.setNameExpression(nameExpression);
		tmp.setActivityBehavior(new SignUserTaskBehavior(definitions,this.assignee));
		TransitionImpl transition = tmp.createOutgoingTransition();
		transition.setDestination(desactiviti);
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
