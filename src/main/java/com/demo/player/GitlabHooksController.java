package com.demo.player;

import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.HttpKit;

import io.jboot.web.controller.JbootController;

@Path(value = "/gitlabhooks", viewPath = "/hooks")
public class GitlabHooksController extends JbootController {

	@Inject
	GitlabHookdsService gitlabHookdsService;
	
	public void index() {
		String jsonBody = HttpKit.readData(getRequest());
		String result = gitlabHookdsService.excuteMsgByString(jsonBody);
		renderText(result);
	}
}
