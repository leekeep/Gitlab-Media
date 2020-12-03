package com.cha1024.player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
/**
 * Mp3格式音乐播放
 * @author nicolas
 *
 */
public class Mp3Player extends Thread {
	Player player;
	File music;

	// 构造方法
	public Mp3Player(String fileName) {
		this.music = new File(fileName);
	}

	// 重写run方法
	@Override
	public void run() {
		super.run();
		try {
			play();
		} catch (FileNotFoundException | JavaLayerException e) {
			e.printStackTrace();
		}
	}

	// 播放方法
	private void play() throws FileNotFoundException, JavaLayerException {
		BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(music));
		player = new Player(buffer);
		player.play();
	}

	public static void main(String[] args) throws MalformedURLException, Exception {
		String fileUrl = "D:\\workspace.idea\\python\\python_pi\\statics\\voice\\finish_success.mp3";
		Mp3Player player = new Mp3Player(fileUrl);
		player.start();
	}
}
