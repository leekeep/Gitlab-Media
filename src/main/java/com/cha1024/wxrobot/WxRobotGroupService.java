package com.cha1024.wxrobot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import com.cha1024.wxrobot.dto.PassiveGroupMsgDto;
import com.jfinal.kit.StrKit;

/**
 * 微信群服务
 * @author nicolas
 *
 */
public class WxRobotGroupService {

	/**
	 * 把字符串转成被动微信群消息对象
	 * @param jsonBody
	 * @return
	 */
	public PassiveGroupMsgDto convertPassiveGroupMsgDtoByString(String jsonBody) {
		PassiveGroupMsgDto dto = new PassiveGroupMsgDto();
		if (StrKit.notBlank(jsonBody)) {
			String[] params = jsonBody.split("\\&");
			for (int i = 0; i < params.length; i++) {
				String param = params[i];
				String[] paramPare = param.split("=");
				if (paramPare.length >= 2) {
					String key = paramPare[0];
					String value = paramPare[1];
					if ("u".equals(key)) {
						dto.setU(NumberUtils.toLong(value));
					} else if ("gid".equals(key)) {
						dto.setGid(NumberUtils.toLong(value));
					} else if ("robotid".equals(key)) {
						dto.setRobotid(value);
					} else if ("mid".equals(key)) {
						dto.setMid(value);
					} else if ("nickname".equals(key)) {
						value = unicodeDecode(value);
						dto.setNickname(value);
					} else if ("displayname".equals(key)) {
						value = unicodeDecode(value);
						dto.setDisplayname(value);
					} else if ("skw".equals(key)) {
						dto.setSkw(NumberUtils.toLong(value));
					} else if ("gname".equals(key)) {
						value = unicodeDecode(value);
						dto.setGname(value);
					} else if ("content".equals(key)) {
						value = unicodeDecode(value);
						if(value.startsWith("<msg><emoji fromusername")) {
							int idx = value.indexOf("cdnurl=");
							if(idx != -1) {
								value = value.substring(idx + 8);
								idx = value.indexOf("\"");
								if(idx != -1) {
									value = value.substring(0, idx);
								}
							}
						}
						dto.setContent(value);
					} else if ("own".equals(key)) {
						dto.setOwn(value);
					} else if ("ownname".equals(key)) {
						dto.setOwnname(value);
					} else if ("msgid".equals(key)) {
						dto.setMsgid(value);
					} else if ("msgtype".equals(key)) {
						dto.setMsgtype(NumberUtils.toInt(value));
					} else if ("isadmin".equals(key)) {
						dto.setIsadmin(NumberUtils.toInt(value));
					} else if ("gadmin".equals(key)) {
						String[] gadmin = value.split(",");
						dto.setGadmin(gadmin);
					} else if ("username".equals(key)) {
						dto.setUsername(value);
					} else if ("gusername".equals(key)) {
						dto.setGusername(value);
					} else if ("vol".equals(key)) {
						dto.setVol(value);
					} else if ("atlist".equals(key)) {
						dto.setAtlist(value);
					} else if ("voltype".equals(key)) {
						dto.setVoltype(value);
					} else if ("robotnickname".equals(key)) {
						value = unicodeDecode(value);
						dto.setVoltype(value);
					} else if ("atmod".equals(key)) {
						dto.setAtmod(NumberUtils.toInt(value));
					} else {
						System.out.println(key + " = " + value);
					}
				}
			}
		}
		return dto;
	}
	
	/**
	 * @Title: unicodeDecode
	 * @Description: unicode解码
	 * @param str
	 * @return
	 */
	private String unicodeDecode(String string) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(string);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			string = string.replace(matcher.group(1), ch + "");
		}
		return string;
	}
	/**
     * @Title: unicodeEncode 
     * @Description: unicode编码
     * @param string
     * @return
     */
    private String unicodeEncode(String string) {
        char[] utfBytes = string.toCharArray();
        String unicodeBytes = "";
        for (int i = 0; i < utfBytes.length; i++) {
            String hexB = Integer.toHexString(utfBytes[i]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        return unicodeBytes;
    }
}
