package com.flow.custom.customcmd;

import java.util.Map;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhailz
 * 
 *
 * @version 2018年3月21日 下午4:20:35
 */
public class BackTaskCmd implements Command<Object> {
	
	private Logger log = LoggerFactory.getLogger("BackTaskCmd");

	
	public BackTaskCmd(String taskId, boolean afterTaskIsdeleteCurrentTask, Map<String, Object> variables, boolean localScope) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
	 */
	@Override
	public Object execute(CommandContext commandContext) {
		log.info(commandContext.toString());
		throw new IllegalAccessError("回退的逻辑");
//		return null;
	}

}
