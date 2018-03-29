package com.flow.util;

import java.util.WeakHashMap;

import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;

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

	public static ActivityImpl clone(String currentid, String desid, ProcessDefinitionImpl processDef, String assignee) {
		ActivityImpl des = processDef.findActivity(desid);
		if(des != null){
			ActivityImpl tmp = processDef.createActivity(currentid);
			tmp.setActivityBehavior(new SignUserTaskBehavior(new TaskDefinition(null),assignee));
			TransitionImpl transition = tmp.createOutgoingTransition();
			transition.setDestination(des);
			ActivityManagerUtil.getInstance().cache(tmp);
			return tmp;
		}
		return null;
	}

	public static String getDestinationActivityIdName(String taskId) {
		return taskId+spit+destinationActivityIdName;
	}

	public static String getCurrentActivityIdName(String taskId) {
		return taskId+spit+currentActivityIdName;
	}
}
