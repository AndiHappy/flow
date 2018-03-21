package com.flow.custom.service;

import java.util.Map;

import org.activiti.engine.impl.TaskServiceImpl;
import org.springframework.stereotype.Component;

import com.flow.custom.customcmd.BackTaskCmd;

/**
 * @author zhailz
 * 
 * 扩展task的操作：前进（complete），后退（back）
 * 
 * @version 2018年3月21日 下午4:17:38
 */
@Component
public class CustomTaskServiceImpl extends TaskServiceImpl implements CusTomTaskService{
	
	
	
	/**
	 * @param taskId 当前任务的taskId
	 * @param afterTaskIsdeleteCurrentTask 当前的任务是否删除
	 * @param variables 参数
	 * @param localScope 范围
	 * 
	 * */
	public void back(String taskId, boolean afterTaskIsdeleteCurrentTask,Map<String, Object> variables,boolean localScope) {
	  	commandExecutor.execute(new BackTaskCmd(taskId, afterTaskIsdeleteCurrentTask,variables, localScope));
	  }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
