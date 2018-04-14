package com.flow.custom.customcmd;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flow.util.ActivityManagerUtil;

/**
 * @author zhailz
 *
 * @version 2018年4月10日 下午4:25:54
 */
public class AfterSignCmd extends BaseCmd implements Command<TaskEntity> {

	private Logger log = LoggerFactory.getLogger("AfterSignCmd");

	private String taskId;
	private Map<String, Object> variables;
	private boolean localScope;
	private String assignee;

	public static final String DELETE_REASON_DELETED = "after_sign_action";


	public AfterSignCmd(String taskId, Map<String, Object> variables, boolean localScope, String assignee) {
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
		//当前任务执行的过程总，对应的下一个活动的定义，下一个任务，应该是task活动定义的下一个的活动定义，或者是excution中
		//带有的下一个节点
		ActivityImpl desactiviti =findDestinationActivity(task,execution,processDefinition);
		leave(execution, processDefinition, desactiviti);

		if(execution.getTasks() != null && !execution.getTasks().isEmpty()){
			return execution.getTasks().get(0);
		}else{
			return null;
		}
	}

	private ActivityImpl findDestinationActivity(TaskEntity task,ExecutionEntity execution, ProcessDefinitionImpl processDefinition) {
		String currentActivityId = task.getTaskDefinitionKey();
		//insert before task action ,this task refter to activity is  destination activity
		//查找当前的任务对应的任务的定义
		ActivityImpl currentActiviti = processDefinition.findActivity(currentActivityId);
		if(currentActiviti != null){
			List<PvmTransition> outgoings = currentActiviti.getOutgoingTransitions();
			if(outgoings != null && !outgoings.isEmpty()){
				return (ActivityImpl) outgoings.get(0).getDestination();
			}
		}
		
		//如果当前的任务定义为null，说明这个任务本身有可能是加签产生的任务，或者临时生成的任务
		//需要找到当前任务对应的下一个活动的定义
		String desid = (String) execution.getVariable(ActivityManagerUtil.getDestinationActivityIdName(currentActivityId));
		log.info("clone activity :{}",currentActivityId);
		ActivityImpl desitination  = onlyCloneActivity(desid, processDefinition);
		return desitination;
	}

	private void leave(ExecutionEntity execution, ProcessDefinitionImpl processDefinition, ActivityImpl desactiviti) {
		// TODO Auto-generated method stub
		
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
