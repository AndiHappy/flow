package com.flow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
public class ActivityFindProdefTimeTest {
	
	private Logger log = LoggerFactory.getLogger("ActivityTimeTest");

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
			ArrayList<Integer> value = new ArrayList<Integer>();
			double startTime =0;
			double num = 50;
			for (int i = 0; i <num; i++) {
				long time = System.currentTimeMillis();
				ProcessDefinition def = engineService.getRepositoryService().createProcessDefinitionQuery().processDefinitionId("test1:51:2667503").singleResult();
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
				}
				long time2 = System.currentTimeMillis();
				log.info("查询一个流程定义的时间是:{} ms",time2-time);
				startTime = startTime+ (time2-time);
				value.add((int) (time2-time));
			}

			log.info("------------------------------------运行"+ num+"次的耗时-----------------------------------------------------");
			log.info("{}",value.toString());
			log.info("---寻找到一次流程:{}  ms,平均耗时:{} ",startTime,startTime/num);
			
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
