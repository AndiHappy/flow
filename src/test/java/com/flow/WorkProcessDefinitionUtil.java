package com.flow;

import org.activiti.bpmn.model.BpmnModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author zhailz
 *
 * @version 2018年3月26日 下午8:23:08
 * 
 * 流程的定义，原来本是xml根式的文件，但是xml格式的文件，比较的难易形成，使用的时候，比较的笨重
 * 1.使用json定义，然后定义完成以后，转化为xml文档
 * 2.自定义一个模板，然后在模板的基础上面，根据传输过来的参数修改
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class WorkProcessDefinitionUtil extends BasePare {

	@Before
	public void iniEngine() {
		// 准备流程定义，加载到测试用例中
		engineService.buildProcessEngine();
	}
	
	@Test
	public void testDeployWorkFlow() {
		String resourceName = "manual";
		BpmnModel bpmnModel = new BpmnModel();
		engineService.getRepositoryService().createDeployment().addBpmnModel(resourceName, bpmnModel);
	}
}
