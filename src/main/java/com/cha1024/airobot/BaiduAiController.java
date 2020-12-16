package com.cha1024.airobot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cha1024.player.Mp3Player;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.ext.interceptor.GET;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.StrKit;

import io.jboot.web.controller.JbootController;

@Path(value = "/airobot", viewPath = "/airobot")
public class BaiduAiController extends JbootController {

	@Inject
	BaiduAiService baiduAiService;

	@Before(GET.class)
	public void index() {

		render("airobot.html");
	}

	/**
	 * 播放指定文字
	 */
	@Before(POST.class)
	public void playByText() {
		String content = getPara("content");
		boolean playResult = baiduAiService.playAiTextVoice(content);
		if (playResult) {
			renderText("success");
		} else {
			renderError(500);
		}
	}
	/**
	 * 播放随机舔狗语录
	 */
	@Before(POST.class)
	public void playRandomTiangou() {
		String jsonStr = HttpKit.get("https://xiaojieapi.cn/API/tiangou.php");
		if(StrKit.notBlank(jsonStr)) {
			JSONObject jsonObject = JSON.parseObject(jsonStr);
			String content = jsonObject.getString("text");
			boolean playResult = baiduAiService.playAiTextVoice(content);
			if (playResult) {
				renderText("success");
			} else {
				renderError(500);
			}
		} else {
			renderError(500);
		}
	}
}
