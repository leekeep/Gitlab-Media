package com.demo.index;

import java.io.File;

import com.demo.player.WavPlayer;
import com.jfinal.core.Path;

import io.jboot.web.controller.JbootController;

/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法
 * 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * IndexController
 */
@Path(value = "/", viewPath = "/index")
public class IndexController extends JbootController {
	public void index() {
		System.out.println(getIPAddress());
		render("index.html");
	}
	
	public void play(){
		String fileUrl = System.getProperty("user.home") + File.separatorChar + "voice" + File.separatorChar + "1016.wav";
		WavPlayer wavePlayer = new WavPlayer(fileUrl);
		wavePlayer.start();
		renderText("success");
	}
}



