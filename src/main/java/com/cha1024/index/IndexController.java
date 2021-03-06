package com.cha1024.index;

import java.io.File;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.cha1024.ext.plugin.mqtt.MqttKit;
import com.cha1024.player.WavPlayer;
import com.jfinal.aop.Before;
import com.jfinal.core.Path;
import com.jfinal.ext.interceptor.GET;
import com.jfinal.ext.interceptor.POST;

import io.jboot.web.controller.JbootController;

/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法
 * 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * IndexController
 */
@Path(value = "/", viewPath = "/index")
public class IndexController extends JbootController {
	@Before(GET.class)
	public void index() {
		System.out.println(getIPAddress());
		render("index.html");
	}

	@Before(POST.class)
	public void play(){
		String fileUrl = System.getProperty("user.home") + File.separatorChar + "voice" + File.separatorChar + "1016.wav";
		WavPlayer wavePlayer = new WavPlayer(fileUrl);
		wavePlayer.start();
		renderText("success");
	}
	
	@Before(GET.class)
	public void mqtt() {
		render("mqtt.html");
	}

	@Before(POST.class)
	public void sendMqtt() throws MqttPersistenceException, MqttException {
		String topic = getPara("topic");
		String payload = getPara("payload");
		Integer qos = getParaToInt("qos");
		Long timeout = getParaToLong("timeout", 60l);
		Boolean retained = getParaToBoolean("retained", false);
		MqttKit.pub(topic, payload.getBytes(), qos, retained, timeout);
		renderText("success");
	}
}



