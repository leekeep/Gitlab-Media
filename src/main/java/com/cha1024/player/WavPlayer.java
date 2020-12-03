package com.cha1024.player;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
/**
 * Wav格式音乐播放
 * @author nicolas
 *
 */
public class WavPlayer extends Thread {

	private String name;

	public WavPlayer(String name) {
		this.name = name;
	}

	@Override
	public void run() {
		File file = new File(name);
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		AudioFormat format = audioInputStream.getFormat();
		SourceDataLine auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(format);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		auline.start();
		int nBytesRead = 0;
		byte[] abbytes = new byte[512];
		try {
			while ((nBytesRead = audioInputStream.read(abbytes, 0, abbytes.length)) != -1) {
				if (nBytesRead >= 0) {
					auline.write(abbytes, 0, nBytesRead);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			auline.drain();
			auline.close();
		}
	}
	
	public static void main(String[] args) {
		String fileUrl = "D:\\workspace.idea\\python\\python_pi\\statics\\voice\\jiayou.wav";
		WavPlayer wavePlayer = new WavPlayer(fileUrl);
		wavePlayer.start();
	}
}