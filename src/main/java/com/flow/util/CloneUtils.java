package com.flow.util;

import java.lang.reflect.Field;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class CloneUtils
{
	private static Logger logger = LoggerFactory.getLogger("CustomUserTaskActivityBehavior");

	//只是简单的成员变量之间的考本
	public static void copyFields(Object source, Object target, String... fieldNames)
	{
		Assert.assertNotNull(source);
		Assert.assertNotNull(target);
		Assert.assertSame(source.getClass(), target.getClass());

		for (String fieldName : fieldNames)
		{
			try
			{
				Field field = FieldUtils.getField(source.getClass(), fieldName, true);
				field.setAccessible(true);
				field.set(target, field.get(source));
			}
			catch (Exception e)
			{
				logger.error("clone error:{}",e);
				
			}
		}
	}
}
