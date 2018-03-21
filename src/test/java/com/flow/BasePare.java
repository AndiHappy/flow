package com.flow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.flow.context.FlowEngine;

/**
 * @author zhailz 测试前的准备工作
 * @version 2018年3月20日 下午5:58:37
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class BasePare {

	private Logger logger = LoggerFactory.getLogger("basetest");
	@Resource
	protected FlowEngine engineService;

	protected String orgId = "001";
	protected String prodefName = "001";
	protected String resourceName = "./bpmn/test2.bpmn";
	protected String workFlowDefinitionText = null;
	protected String workflowDefinitionId = null;

	@Before
	public void iniEngine() {
		// 准备流程定义，加载到测试用例中

		try {
			engineService.buildProcessEngine();
			ProcessDefinition def = engineService.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("test2").processDefinitionName("测试2").processDefinitionTenantId(orgId).singleResult();
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
				logger.info(workFlowDefinitionTextStringBuilder.toString());
				inputStream.close();

				if (workFlowDefinitionText == null) {
					workFlowDefinitionText = workFlowDefinitionTextStringBuilder.toString();
				}

				Deployment delpoy = engineService.getRepositoryService().createDeployment().tenantId(orgId).name(prodefName).addString(resourceName, workFlowDefinitionText).deploy();
				
				String deploymentId = delpoy.getId();
				def = engineService.getRepositoryService().createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
				workflowDefinitionId = def.getId();
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
