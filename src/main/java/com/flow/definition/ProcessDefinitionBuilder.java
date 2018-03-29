package com.flow.definition;

import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;

/**
 * @author zhailz
 * 
 * 流程定义制造器
 *
 * @version 2018年3月26日 下午8:25:07
 */
public class ProcessDefinitionBuilder {
	
	private ProcessDefinitionImpl model ;
	
	private ProcessDefinitionBuilder(){
		this.model = new ProcessDefinitionImpl("212");
	}

	public static void main(String[] args){
	}

}
