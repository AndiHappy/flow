package com.flow;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SpringBootApplication
@PropertySource("classpath:application-test2.properties")
public class NotifyController {

	@RequestMapping("/notify")
	@ResponseBody
	String reduce(HttpServletRequest req, @RequestParam(value = "msg", required = true) String msg) {
		return "Notify Server receive: "+ msg;
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(NotifyController.class, args);
	}
}
