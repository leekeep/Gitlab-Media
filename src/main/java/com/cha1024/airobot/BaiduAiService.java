package com.cha1024.airobot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONObject;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.baidu.aip.util.Util;
import com.cha1024.player.Mp3Player;
import com.jfinal.kit.StrKit;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
/**
 * 百度AI平台服务
 * @author nicolas
 *
 */
public class BaiduAiService {
	private static final String APP_ID = "23029072";
    private static final String API_KEY = "2dnaNyHnNTys9MsHtx789C0Q";
    private static final String SECRET_KEY = "Ps1jEgE4DhAwwmnMGzqQjVeq8Ej5kpGf";
    /**
     * 百度AIP授权客户端对象
     */
    private static AipSpeech client;

	/**
	 * 用户的home目录
	 */
	private String userHome = System.getProperty("user.home");
    /**
     * 获取AipSpeech
     * @return
     */
	public static AipSpeech getAipSpeech() {
		if(client != null) {
			return client;
		}
		client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
		// 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

//        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理
		return client;
	}
	/**
	 * 获得指定目录的随机文件
	 * @return
	 */
	private String getRandomFilePath() {
		String filePath = userHome + File.separatorChar + "robot_voice";
		FileUtil.mkdir(filePath);
		return (filePath + File.separatorChar + IdUtil.fastSimpleUUID() + ".mp3");
	}
	/**
	 * 文本转语音，并返回语音文件的本地路径
	 * @param content
	 * @return
	 */
	public String aiText2Voice(String content) {
		// 设置可选参数
		HashMap<String, Object> options = new HashMap<String, Object>();
	    options.put("spd", "5");//语速，取值0-9，默认为5中语速
	    options.put("pit", "5");//音调，取值0-9，默认为5中语调
	    options.put("vol", "6");//音量，取值0-15，默认为5中音量
	    options.put("per", "0");//发音人选择, 0为女声，1为男声，3为情感合成-度逍遥，4为情感合成-度丫丫，默认为普通女
		TtsResponse res = getAipSpeech().synthesis(content, "zh", 1, options);
		byte[] data = res.getData();
		JSONObject res1 = res.getResult();
		if (data != null) {
			String outputPath = getRandomFilePath();
			try {
				Util.writeBytesToFileSystem(data, outputPath);
				return outputPath;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(res1 != null) {
			System.out.println(res1.toString());
		}
		return null;
	}
	/**
	 * 播放指定文本
	 * @param content
	 * @return
	 */
	public boolean playAiTextVoice(String content) {
		String voiceFilePath = aiText2Voice(content);
		if (StrKit.notBlank(voiceFilePath)) {
			Thread thread = new Thread(new Mp3Player(voiceFilePath));
			thread.start();
			return true;
		}
		return false;
	}
}
