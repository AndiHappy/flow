package com.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class EngineServiceTest extends BasePare {

	private PropertiesUtil util = new PropertiesUtil();
	private String todotaskId = "todotaskId";

	@Test
	public void testDeployWorkFlow() {
		System.out.println(engineService.getActivityFontName());
	}

	@Test
	public void startWorkFlowInstance() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("para", "aggree");
		
		if(StringUtils.isEmpty(util.getPropertyValue(todotaskId))){
			ExecutionEntity instance = (ExecutionEntity) engineService.getRuntimeService().startProcessInstanceById(workflowDefinitionId);
			Assert.assertNotNull(instance);
			Assert.assertNotNull(instance.getTasks());
			String taskId = instance.getTasks().get(0).getId();
			engineService.getTaskService().complete(taskId, variables);
			List<Task> tasks = engineService.getTodoTaskId(instance.getId(), orgId, "100");
			taskId = tasks.get(0).getId();
			util.setPropertiesValue("todotaskId", taskId);
		}
		String taskId = util.getPropertyValue(todotaskId);
		System.out.println(taskId);
		engineService.getCustomTaskService().back(taskId,false, variables, false);

	}

}
