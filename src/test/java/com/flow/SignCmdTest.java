package com.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
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
	public void signActionInOneMethod() {

		ExecutionEntity instance = (ExecutionEntity) engineService.getRuntimeService().startProcessInstanceById(workflowDefinitionId);
		Assert.assertNotNull(instance);
		Assert.assertNotNull(instance.getTasks());
		String taskId = instance.getTasks().get(0).getId();
		log.info("todotask：{}", taskId);
		log.info("instanceId:{}", instance.getId());
		util.setPropertiesValue("todotaskId", taskId);
		util.setPropertiesValue(instanceId, instance.getId());
		Map<String, Object> variables = new HashMap<String, Object>();
		TaskEntity task = engineService.getCustomTaskService().beforeSign(taskId, variables, false, "200");
		if (task != null) {
			util.setPropertiesValue("todotaskId", task.getId());
		}

		String preinstanceId = util.getPropertyValue(instanceId);
		// 查询未完成的任务
		List<HistoricTaskInstance> tasks = engineService.gethisTasks(preinstanceId);
		for (HistoricTaskInstance task1 : tasks) {
			log.info("histask: name:{},endTime:{}, Assignee:{} reson:{}", task1.getName(), task1.getEndTime(), task1.getAssignee(), task1.getDeleteReason());
		}

		taskId = util.getPropertyValue(todotaskId);
		engineService.getCustomTaskService().complete(taskId, variables, false);
		preinstanceId = util.getPropertyValue(instanceId);
		// 查询未完成的任务
		List<Task> tasks1 = engineService.getTodoTaskId(preinstanceId);
		if (tasks1 != null && !tasks1.isEmpty()) {
			util.setPropertiesValue("todotaskId", tasks1.get(0).getId());
		}

		taskId = util.getPropertyValue(todotaskId);
		engineService.getCustomTaskService().complete(taskId, variables, false);

		// 查询未完成的任务
		List<HistoricTaskInstance> tasks2 = engineService.gethisTasks(preinstanceId);
		for (HistoricTaskInstance task1 : tasks2) {
			log.info("histask: name:{},endTime:{}, Assignee:{} reson:{}", task1.getName(), task1.getEndTime(), task1.getAssignee(), task1.getDeleteReason());
		}

	}

	@Test
	public void signActionManyTimesInOneMethod() {

		ExecutionEntity instance = (ExecutionEntity) engineService.getRuntimeService().startProcessInstanceById(workflowDefinitionId);
		Assert.assertNotNull(instance);
		Assert.assertNotNull(instance.getTasks());
		String taskId = instance.getTasks().get(0).getId();
		log.info("todotask：{}", taskId);
		log.info("instanceId:{}", instance.getId());
		util.setPropertiesValue("todotaskId", taskId);
		util.setPropertiesValue(instanceId, instance.getId());

		Map<String, Object> variables = new HashMap<String, Object>();
		TaskEntity task = engineService.getCustomTaskService().beforeSign(taskId, variables, false, "200");
		if (task != null) {
			util.setPropertiesValue("todotaskId", task.getId());
		}

		String preinstanceId = util.getPropertyValue(instanceId);
		// 查询未完成的任务
		List<HistoricTaskInstance> tasks = engineService.gethisTasks(preinstanceId);
		for (HistoricTaskInstance task1 : tasks) {
			log.info("histask: name:{},endTime:{}, Assignee:{} reson:{}", task1.getName(), task1.getEndTime(), task1.getId(), task1.getDeleteReason());
		}
		
		rebuildApplicationContext();
		taskId = util.getPropertyValue(todotaskId);
		task = engineService.getCustomTaskService().beforeSign(taskId, variables, false, "300");
		if (task != null) {
			util.setPropertiesValue("todotaskId", task.getId());
		}

		// 查询未完成的任务
		List<HistoricTaskInstance> tasks3 = engineService.gethisTasks(preinstanceId);
		for (HistoricTaskInstance task1 : tasks3) {
			log.info("histask: name:{},endTime:{}, Assignee:{} reson:{}", task1.getName(), task1.getEndTime(), task1.getId(), task1.getDeleteReason());
		}

		rebuildApplicationContext();
		taskId = util.getPropertyValue(todotaskId);
		log.info("taskId:{}",taskId);
		engineService.getCustomTaskService().complete(taskId, variables, false);
		preinstanceId = util.getPropertyValue(instanceId);
		// 查询未完成的任务
		List<Task> tasks1 = engineService.getTodoTaskId(preinstanceId);
		if (tasks1 != null && !tasks1.isEmpty()) {
			util.setPropertiesValue("todotaskId", tasks1.get(0).getId());
		}

		rebuildApplicationContext();
		taskId = util.getPropertyValue(todotaskId);
		engineService.getCustomTaskService().complete(taskId, variables, false);

		// 查询未完成的任务
		List<HistoricTaskInstance> tasks2 = engineService.gethisTasks(preinstanceId);
		for (HistoricTaskInstance task1 : tasks2) {
			log.info("histask: name:{},endTime:{}, Assignee:{} reson:{}", task1.getName(), task1.getEndTime(), task1.getAssignee(), task1.getDeleteReason());
		}

	}

	@Test
	public void startWorkFlowInstance() {
		ExecutionEntity instance = (ExecutionEntity) engineService.getRuntimeService().startProcessInstanceById(workflowDefinitionId);
		Assert.assertNotNull(instance);
		Assert.assertNotNull(instance.getTasks());
		String taskId = instance.getTasks().get(0).getId();
		log.info("todotask：{}", taskId);
		log.info("instanceId:{}", instance.getId());
		util.setPropertiesValue("todotaskId", taskId);
		util.setPropertiesValue(instanceId, instance.getId());
	}

	@Test
	public void complete() {
		Map<String, Object> variables = new HashMap<String, Object>();
		String taskId = util.getPropertyValue(todotaskId);
		engineService.getCustomTaskService().complete(taskId, variables, false);
		String preinstanceId = util.getPropertyValue(instanceId);
		// 查询未完成的任务
		List<Task> tasks = engineService.getTodoTaskId(preinstanceId);
		if (tasks != null && !tasks.isEmpty()) {
			util.setPropertiesValue("todotaskId", tasks.get(0).getId());
		}
	}

	@Test
	public void beforesign() {
		Map<String, Object> variables = new HashMap<String, Object>();
		String taskId = util.getPropertyValue(todotaskId);
		System.out.println(taskId);
		TaskEntity task = engineService.getCustomTaskService().beforeSign(taskId, variables, false, "400");
		if (task != null) {
			util.setPropertiesValue("todotaskId", task.getId());
		}
	}

	@Test
	public void getTodoTask() {
		String preinstanceId = util.getPropertyValue(instanceId);
		// 查询未完成的任务
		List<Task> tasks = engineService.getTodoTaskId(preinstanceId);
		for (Task task : tasks) {
			log.info("未完成的任务:{},{},{}", task.getName(), task.getId(), task.getCreateTime());
		}
	}

	@Test
	public void gethisTask() {
		String preinstanceId = util.getPropertyValue(instanceId);
		// 查询未完成的任务
		List<HistoricTaskInstance> tasks = engineService.gethisTasks(preinstanceId);
		for (HistoricTaskInstance task : tasks) {
			log.info("histask: name:{},endTime:{}, Assignee:{} reson:{}", task.getName(), task.getEndTime(), task.getAssignee(), task.getDeleteReason());
		}
	}

	@Test
	public void geTasks() {
		String preinstanceId = util.getPropertyValue(instanceId);
		// 查询未完成的任务
		List<Task> tasks = engineService.getTasks(preinstanceId);
		for (Task task : tasks) {
			log.info("任务:{},{},{},{}", task.getId(), task.getName(), task.getOwner(), task.getAssignee());
		}
	}

	@Test
	public void getHissByInstanceId() {
		String preinstanceId = util.getPropertyValue(instanceId);
		List<HistoricActivityInstance> hiss = engineService.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(preinstanceId).orderByHistoricActivityInstanceEndTime().asc().list();
		if (hiss != null && !hiss.isEmpty()) {
			for (HistoricActivityInstance historicIdentityLink : hiss) {
				log.info(historicIdentityLink.getTaskId() + " " + historicIdentityLink.getActivityName() + " " + historicIdentityLink.getAssignee());
			}
		}
	}
}
