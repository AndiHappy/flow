package com.flow.custom.service;

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
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
	 * @return  后退的过程中，产生新的任务
	 * 
	 * */
	public List<TaskEntity> back(String taskId,Map<String, Object> variables,boolean localScope) {
	   return commandExecutor.execute(new BackTaskCmd(taskId,variables, localScope));
	  }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
