package com.flow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.flow.context.FlowEngine;
import com.flow.util.ActivityManagerUtil;
import com.flow.util.Charsets;
import com.flow.util.HC;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class ExcuteTimeTest {
	
	private Logger log = LoggerFactory.getLogger("ExcuteTimeTest");

	@Resource
	protected FlowEngine engineService;

	protected String orgId = "001";
	protected String prodefName = "001";
	protected String resourceName = "./bpmn/test1.bpmn";
	protected String workFlowDefinitionText = null;
	protected String workflowDefinitionId = null;

	@Test
	public void iniEngine() {
		// 准备流程定义，加载到测试用例中
		try {
			engineService.buildProcessEngine();
			log.info("初始化引擎");
			ProcessDefinition def = engineService.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("test1").processDefinitionName("测试1").processDefinitionTenantId(orgId).singleResult();
			if (def != null && def.getDeploymentId() != null) {
				workflowDefinitionId = def.getId();
			} else {
				File file = new File(resourceName);
				System.out.println(file.getAbsolutePath());
				InputStream inputStream = new FileInputStream(new File(resourceName));
				StringBuilder workFlowDefinitionTextStringBuilder = new StringBuilder();
				byte[] b = new byte[1024];
				int length = 0;
				while ((length = inputStream.read(b)) != -1) {
					workFlowDefinitionTextStringBuilder.append(new String(b, 0, length));
				}
				log.info(workFlowDefinitionTextStringBuilder.toString());
				inputStream.close();

				if (workFlowDefinitionText == null) {
					workFlowDefinitionText = workFlowDefinitionTextStringBuilder.toString();
				}

				Deployment delpoy = engineService.getRepositoryService().createDeployment().tenantId(orgId).name(prodefName).addString(resourceName, workFlowDefinitionText).deploy();
				
				String deploymentId = delpoy.getId();
				def = engineService.getRepositoryService().createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
				workflowDefinitionId = def.getId();
				log.info("workflowDefinitionId:{}",workflowDefinitionId);
//				String json = HC.getInstance().getResponse("http://127.0.0.1:8081/reduce?num=10", null, Charsets.UTF8);
//				log.info("调用REST接口，返回:{}",json);
				
				
				long time = System.currentTimeMillis();
				ProcessInstance instance = engineService.getRuntimeService().startProcessInstanceById(workflowDefinitionId);
				long time2 = System.currentTimeMillis();
				log.info("启动流程实例花费:{}  ms",time2-time);
				ExecutionEntity instanceEntity = (ExecutionEntity) instance;
				engineService.getCustomTaskService().complete(instanceEntity.getTasks().get(0).getId());
				long time3 = System.currentTimeMillis();
				log.info("完成第一个任务花费:{} ms",time3-time2);
				String nextId = engineService.getTaskService().createTaskQuery().processInstanceId(instance.getId()).singleResult().getId();
				engineService.getCustomTaskService().complete(nextId);
				long time4 = System.currentTimeMillis();
				log.info("完成第二个任务花费:{} ms",time4-time3);
				log.info("总的花费是:{} ms",time4-time);
				
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	protected void rebuildApplicationContext()
	{
		ClassPathXmlApplicationContext tx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		FlowEngine engine = tx.getBean(com.flow.context.FlowEngine.class);
		Assert.assertNotNull(engine);
		engineService = engine;
		engineService.buildProcessEngine();
		ActivityManagerUtil.getInstance().clearCache();
	}
}
