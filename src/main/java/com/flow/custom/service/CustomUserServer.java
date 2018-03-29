package com.flow.custom.service;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author zhailz
 *
 * @version 2018年3月26日 下午8:03:38
 */

@Service("customUserServer")
public class CustomUserServer {

	private Logger logger = LoggerFactory.getLogger("CustomUserServer");

	public String findUserId(String expressionValue) {
		logger.debug("调用CustomUserServer findUserId :{}" , expressionValue);
		return expressionValue;
	}

	public String findOwnerUserId(String expressionValue) {
		logger.debug("调用CustomUserServer findOwnerUserId :{}" , expressionValue);
		return expressionValue;
	}

	public List<String> findCandidateGroups(String value) {
		logger.debug("调用CustomUserServer findCandidateGroups :{}" , value);
		return Arrays.asList(value);
	}
	
	

}
