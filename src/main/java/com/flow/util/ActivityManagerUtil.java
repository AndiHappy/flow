package com.flow.util;

import java.util.WeakHashMap;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;

import com.flow.custom.behavior.CustomUserTaskActivityBehavior;
import com.flow.custom.behavior.SignUserTaskBehavior;

/**
 * @author zhailz
 *
 * @version 2018年3月28日 下午5:54:39
 */
public class ActivityManagerUtil {

	public  static final String  sourceActivityIdName = "sourceActivityId";
	
	public  static final String  destinationActivityIdName = "destinationActivityId";
	
	public  static final String  currentActivityIdName = "currentActivityId";
	
	public  static final String  currentActivityAssigenName = "currentActivityAssigen";

	
	public static final String spit = "@";

	private WeakHashMap<String, ActivityImpl> cacheActivity = new WeakHashMap<String, ActivityImpl>(100);
	
	private static class ActivityManagerUtilHoler{
		private static ActivityManagerUtil instance = new ActivityManagerUtil();
	}
	
	private ActivityManagerUtil(){}
	
	public static ActivityManagerUtil getInstance(){
		return ActivityManagerUtilHoler.instance;
	}
	
	public  void cache(ActivityImpl impl){
		cacheActivity.put(impl.getId(), impl);
	}

	public ActivityImpl getCacheActivity(String activityId) {
		return cacheActivity.get(activityId);
	}

	/**
	 * 构建的是当前的任务节点并且确定了指向：desid的活动定义
	 * */
	public static ActivityImpl cloneCurrentActivity(String currentid, String destinationActivityid, ProcessDefinitionImpl processDef, String assignee) {
		ActivityImpl des = processDef.findActivity(destinationActivityid);
		if(des == null){
			des = processDef.createActivity(destinationActivityid);
			TaskDefinition definitions = new TaskDefinition(null);
			definitions.setKey(des.getId());
			Expression nameExpression = new FixedValue(des.getId());
			definitions.setNameExpression(nameExpression);
			des.setActivityBehavior(new CustomUserTaskActivityBehavior(definitions));
		}
		ActivityImpl tmp = processDef.createActivity(currentid);
		tmp.setActivityBehavior(new SignUserTaskBehavior(new TaskDefinition(null),assignee));
		TransitionImpl transition = tmp.createOutgoingTransition();
		transition.setDestination(des);
		ActivityManagerUtil.getInstance().cache(tmp);
		return tmp;
	}

	public static String getDestinationActivityIdName(String activityId) {
		return activityId+spit+destinationActivityIdName;
	}

	public static String getCurrentActivityIdName(String activityId) {
		return activityId+spit+currentActivityIdName;
	}

	public static String getCurrentActivityAssigneeName(String activityId) {
		return activityId+spit+currentActivityAssigenName;
	}

	public void clearCache() {
		if(cacheActivity != null){
			cacheActivity.clear();
		}
	}
}
