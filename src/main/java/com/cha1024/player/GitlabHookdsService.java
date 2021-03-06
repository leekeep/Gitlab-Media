package com.cha1024.player;

import java.io.File;

import org.apache.commons.lang3.RandomUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cha1024.airobot.BaiduAiService;
import com.cha1024.ext.plugin.mqtt.MqttKit;
import com.jfinal.aop.Inject;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;

/**
 * gitlab消息钩子服务
 * @author nicolas
 *
 */
public class GitlabHookdsService {
	
	@Inject
	BaiduAiService baiduAiService;
	/**
	 * 用户的home目录
	 */
	private String userHome = System.getProperty("user.home");
	/**
	 * 获得指定目录的随机文件
	 * @return
	 */
	private String getRandomFilePath(String path, Integer startv, Integer endv, String suffix) {
		int intv = RandomUtils.nextInt(startv, endv);
		String filePath = path + File.separatorChar + "voice" + File.separatorChar + intv + suffix;
		return filePath;
	}
	/**
	 * 推送消息给企业微信
	 * @param title
	 * @param username
	 * @param comment
	 * @param codeUrl
	 */
	private void pushToQyWx(String title, String username, String comment, String projectName, String codeUrl, String ref) {
		String url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=0d6925f9-ef86-4f40-bfb7-ee6da2afd39a";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msgtype", "markdown");
		JSONObject markdown = new JSONObject();
		String content = "### " + title + "\n";
		content = content + ">**推送人员**: " + username + "\n";
		content = content + ">**影响仓库**：" + projectName + "\n";
		content = content + ">**仓库版本**：" + ref + "\n";
		content = content + ">**仓库地址**：" + codeUrl + "\n";
		content = content + "**内容注释**：\n" + comment;
		markdown.put("content", content);
		jsonObject.put("markdown", markdown);
		String data = jsonObject.toJSONString();
		HttpKit.post(url, data);
	}
	/**
	 * 推送消息到LCD屏幕
	 * @param username
	 * @param action
	 * @param ref
	 */
	private void pushToLCD(String username, String action, String ref) {
		String topic = "raspberry/topic";
		ref = ref.replace("refs/heads/", "");
		String payload = username + "  " + action + "  " + ref;
		try {
			MqttKit.pub(topic, payload.getBytes(), 0, false, 60);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 处理消息钩子发送的内容
	 * @param jsonBody
	 */
	public String excuteMsgByString(String jsonBody) {
		if(StrKit.isBlank(jsonBody)) {
			return "failure";
		}
//		System.out.println(jsonBody);
		LogKit.info(jsonBody);
		JSONObject jsonObject = JSON.parseObject(jsonBody);
		String eventName = jsonObject.getString("event_name");
		String userName = jsonObject.getString("user_username");
		String ref = jsonObject.getString("ref");
		if(StrKit.isBlank(userName)) {
			userName = jsonObject.getString("user_name");
		}
		String filePath = getRandomFilePath(userHome, 1001, 1041, ".wav");
		String projectName = "示例项目", codeUrl = "无";
		JSONObject project = jsonObject.getJSONObject("project");
		if(project != null) {
			projectName = project.getString("name") + "(" + project.getString("description") + ")";
			codeUrl = project.getString("web_url");
		}
		JSONArray commits = jsonObject.getJSONArray("commits");
		String comment = null;
		if(commits != null && commits.size()>0) {
			comment = commits.getJSONObject(0).getString("message");
		}
		comment = StrKit.defaultIfBlank(comment, "无");
		if("push".equalsIgnoreCase(eventName)) {
			String content = userName + " 提交了代码，注释：" + comment;
			baiduAiService.playAiTextVoice(content, false);
			
			LogKit.info(userName + "提交了代码");
			WavPlayer wavePlayer = new WavPlayer(filePath);
			wavePlayer.start();
//			pushToQyWx("代码提交通知", userName, comment, projectName, codeUrl, ref);
			pushToLCD(userName, eventName, ref);
		}else if("repository_update".equalsIgnoreCase(eventName)) {
			String content = userName + " 合并了仓库，注释：" + comment;
			baiduAiService.playAiTextVoice(content, false);
			
			LogKit.info(userName + " 合并了仓库");
			String fileUrl = System.getProperty("user.home") + File.separatorChar + "voice" + File.separatorChar + "1016.wav";
			WavPlayer wavePlayer = new WavPlayer(fileUrl);
			wavePlayer.start();
			//pushToQyWx("仓库更新通知", userName, comment, projectName, codeUrl, ref);
		}
		return "success";
	}
	
}
