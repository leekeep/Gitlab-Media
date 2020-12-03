package com.cha1024.wxrobot;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.cha1024.wxrobot.dto.PassiveGroupMsgDto;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.StrKit;

import io.jboot.web.controller.JbootController;

@Path(value = "/wxrobot", viewPath = "/wxrobot")
public class WxRobotController extends JbootController {

	@Inject
	WxRobotGroupService wxRobotGroupService;

	/**
	 * 主动群消息
	 */
	public void activeGroupMsg() {
		String jsonBody = HttpKit.readData(getRequest());
		System.out.println(getIPAddress());
		System.out.println(jsonBody);
//		String text = "{\"rs\":1,\"tip\":\"这里是返回的内容，utf-8格式中文，不需要转码[结束][img]http://www.dijiu.com/upload/2009/2/24/2009022479639361.gif[/img][结束][img]http://www.dijiu.com/upload/2009/2/24/2009022479639361.gif[/img]\",\"wxid\":\"wxid_i0mvlqzxj25h11\",\"end\":0}";
//		renderText(text);
		renderNull();
	}

	/**
	 * 被动群消息
	 */
	public void passiveGroupMsg() {
		String jsonBody = HttpKit.readData(getRequest());
		System.out.println(getIPAddress());
		System.out.println(jsonBody);
		PassiveGroupMsgDto groupMsg = wxRobotGroupService.convertPassiveGroupMsgDtoByString(jsonBody);
//		PassiveGroupMsgDto groupMsg = JSON.parseObject(jsonBody, PassiveGroupMsgDto.class);
		if (StrKit.notBlank(groupMsg.getContent())) {
			Map<String, Object> rs = new LinkedHashMap<>();
			rs.put("rs", 1);
			rs.put("tip", "我是复读机：" + groupMsg.getContent());
			if (groupMsg.getContent().contains("骂人")) {
				rs.put("wxuin", groupMsg.getOwn());
				rs.put("wxuin_tip", groupMsg.getNickname() + " 骂人： " + groupMsg.getContent());
			}
			rs.put("end", 0);
			System.out.println(JSON.toJSONString(rs));
			renderJson(rs);
		} else {
			renderNull();
		}
	}
}
