package com.flow.custom.behavior;

import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhailz
 *
 * @version 2018年3月27日 下午5:52:14
 */
public class SignUserTaskBehavior extends UserTaskActivityBehavior {

	private Logger log = LoggerFactory.getLogger("SignUserTaskBehavior");

	
	private String assignee;
	
	public SignUserTaskBehavior(TaskDefinition taskDefinition, String assignee) {
		super(taskDefinition);
		this.assignee = assignee;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.pvm.delegate.ActivityBehavior#execute(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
	 */
	@Override
	public void execute(ActivityExecution execution) throws Exception {
		    TaskEntity task = TaskEntity.createAndInsert(execution);
		    task.setExecution(execution);
		    task.setAssignee(assignee);
		    task.setTaskDefinition(taskDefinition);
		    if (taskDefinition.getNameExpression() != null) {
		      String name = (String) taskDefinition.getNameExpression().getValue(execution);
		      task.setName(name);
		    }
		    if (taskDefinition.getDescriptionExpression() != null) {
		      String description = (String) taskDefinition.getDescriptionExpression().getValue(execution);
		      task.setDescription(description);
		    }
		    
		    handleAssignments(task, execution);
		   
		    // All properties set, now firing 'create' events
		    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
		      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
		        ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_CREATED, task));
		    }

		    task.fireEvent(TaskListener.EVENTNAME_CREATE);

	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

}
