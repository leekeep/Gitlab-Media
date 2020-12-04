package com.cha1024.wxrobot;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cha1024.wxrobot.dto.PassiveGroupMsgDto;
import com.cha1024.wxrobot.dto.Proverbs;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import io.jboot.web.controller.JbootController;

@Path(value = "/wxrobot", viewPath = "/wxrobot")
public class WxRobotController extends JbootController {

	@Inject
	WxRobotGroupService wxRobotGroupService;

	private TimedCache<String, String> tenMinTimeoutCache = CacheUtil.newTimedCache(10 * 60 * 1000);
	/**
	 * 主动群消息
	 */
	public void activeGroupMsg() {
//		String jsonBody = HttpKit.readData(getRequest());
//		System.out.println(getIPAddress());
//		System.out.println(jsonBody);
		String key = "activeGroupMsg";
		String proverbsContent = tenMinTimeoutCache.get(key);
		if(StrKit.isBlank(proverbsContent)) {
			Proverbs proverbs = ProverbsKit.getRandomProverbs();
			proverbsContent = proverbs.getContent();
			tenMinTimeoutCache.put(key, proverbsContent);
			Map<String, Object> rs = new LinkedHashMap<>();
			rs.put("rs", 1);
			rs.put("tip", proverbsContent);
			rs.put("end", 0);
			renderJson(rs);
		}else {
			LogKit.info("不发送消息，缓存内容还在: " + proverbsContent);
			renderNull();
		}
	}

	/**
	 * 被动群消息
	 */
	public void passiveGroupMsg() {
		String jsonBody = HttpKit.readData(getRequest());
		LogKit.info(getIPAddress());
		LogKit.warn(jsonBody);
		PassiveGroupMsgDto groupMsg = wxRobotGroupService.convertPassiveGroupMsgDtoByString(jsonBody);
//		PassiveGroupMsgDto groupMsg = JSON.parseObject(jsonBody, PassiveGroupMsgDto.class);
		if (StrKit.notBlank(groupMsg.getContent())) {
			Map<String, Object> rs = new LinkedHashMap<>();
			rs.put("rs", 1);
			rs.put("tip", "我是复读机：" + groupMsg.getContent());
			if (groupMsg.getContent().contains("机器")) {
				rs.put("wxuin", groupMsg.getOwn());
				rs.put("wxuin_tip", groupMsg.getNickname() + "识别出机器人，他说：\n" + groupMsg.getContent());
			}
			rs.put("end", 0);
//			System.out.println(JSON.toJSONString(rs));
			renderJson(rs);
		} else {
			renderNull();
		}
	}
}
