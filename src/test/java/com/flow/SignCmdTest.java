package com.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class SignCmdTest extends BasePare {
	
	private Logger log = LoggerFactory.getLogger("SignCmdTest");

	
	private PropertiesUtil util = new PropertiesUtil();
	private String todotaskId = "todotaskId";
	private String instanceId = "instanceId";

	@Test
	public void testDeployWorkFlow() {
		System.out.println(engineService.getActivityFontName());
	}
	
	@Test
	public void startWorkFlowInstance() {
		ExecutionEntity instance = (ExecutionEntity) engineService.getRuntimeService().startProcessInstanceById(workflowDefinitionId);
		Assert.assertNotNull(instance);
		Assert.assertNotNull(instance.getTasks());
		String taskId = instance.getTasks().get(0).getId();
		log.info("todotask：{}",taskId);
		log.info("instanceId:{}",instance.getId());
		util.setPropertiesValue("todotaskId", taskId);
		util.setPropertiesValue(instanceId, instance.getId());
	}
	
	@Test
	public void complete() {
		Map<String, Object> variables = new HashMap<String, Object>();
		String taskId = util.getPropertyValue(todotaskId);
		engineService.getCustomTaskService().complete(taskId, variables, false);
		String preinstanceId = util.getPropertyValue(instanceId);
		//查询未完成的任务
		List<Task> tasks = engineService.getTodoTaskId(preinstanceId);
		if(tasks != null && !tasks.isEmpty()){
			util.setPropertiesValue("todotaskId", tasks.get(0).getId());
		}
	}

	
	@Test
	public void beforesign() {
		Map<String, Object> variables = new HashMap<String, Object>();
		String taskId = util.getPropertyValue(todotaskId);
		System.out.println(taskId);
		TaskEntity task = engineService.getCustomTaskService().beforeSign(taskId, variables, false,"200");
		if(task != null){
			util.setPropertiesValue("todotaskId", task.getId());
		}
	}
	
	@Test
	public void getTodoTask(){
		String preinstanceId = util.getPropertyValue(instanceId);
		//查询未完成的任务
		List<Task> tasks = engineService.getTodoTaskId(preinstanceId);
		for (Task task : tasks) {
			log.info("未完成的任务:{},{},{}",task.getName(),task.getId(),task.getCreateTime());
		}
	}
	
	@Test
	public void gethisTask(){
		String preinstanceId = util.getPropertyValue(instanceId);
		//查询未完成的任务
		List<HistoricTaskInstance> tasks = engineService.gethisTasks(preinstanceId);
		for (HistoricTaskInstance task : tasks) {
			log.info("histask: name:{},endTime:{}, Assignee:{} reson:{}",task.getName(),task.getEndTime(),task.getAssignee(),task.getDeleteReason());
		}
	}
	
	
	@Test
	public void geTasks(){
		String preinstanceId = util.getPropertyValue(instanceId);
		//查询未完成的任务
		List<Task> tasks = engineService.getTasks(preinstanceId);
		for (Task task : tasks) {
			log.info("任务:{},{},{},{}",task.getId(),task.getName(),task.getOwner(),task.getAssignee());
		}
	}
	

	@Test
	public void getHissByInstanceId(){
		String preinstanceId = util.getPropertyValue(instanceId);
		List<HistoricActivityInstance> hiss = engineService.getHistoryService().createHistoricActivityInstanceQuery()
				.processInstanceId(preinstanceId).orderByHistoricActivityInstanceEndTime().asc().list();
		if(hiss != null && !hiss.isEmpty()){
			for (HistoricActivityInstance historicIdentityLink : hiss) {
				log.info(historicIdentityLink.getTaskId() + " "+ historicIdentityLink.getActivityName() + " "+ historicIdentityLink.getAssignee());
			}
		}
	}
}
