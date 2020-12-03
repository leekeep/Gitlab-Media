package com.cha1024.ext.plugin.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;


/**
 * MQTT 消息处理工具
 * @author Dean
 *
 */
public class MqttKit {
    /**
	 * QOS_AT_MOST_ONCE 最多一次，有可能重复或丢失
	 * MQTT 消息质量
	 */
	public static final int QOS_AT_MOST_ONCE = 0;
	/**
	 * QOS_AT_LEAST_ONCE 至少一次，有可能重复
	 * 应答过程: Client[Qos=1,DUP=0/*重复次数*\/,MessageId=x --->PUBLISH--> Server收到后，存储Message，发布，删除，向Client回发PUBACK
	 * 如果你需要向MQTT broker确认你已经处理的消息，建议将消息设为质量设为QOS_AT_LEAST_ONCE 然后手动应答消息
	 * MQTT 消息质量
	 */
	public static final int QOS_AT_LEAST_ONCE = 1;
	/**
	 * QOS_EXACTLY_ONCE 只有一次，确保消息只到达一次（用于比较严格的计费系统）
	 * MQTT 消息质量
	 */
	public static final int QOS_EXACTLY_ONCE = 2;
	
	//静态连接
	private static MqttAsyncClient client;
	private static boolean manualAcks;
	
	/**
	 * 初始化当前工具所需要的对象
	 * @param _client MQTT client对象
	 * @param _manualAcks 是否需要手动应答消息 
	 */
	static void init(MqttAsyncClient _client,boolean _manualAcks){
		client = _client;
		manualAcks = _manualAcks;
	}
	/**
	 * 订阅主题
	 * @param topic String 主题
	 * @param qos int 消息质量 
	 * @param messageListener 收到消息是时IMqttMessageListener回调接口
	 * @throws MqttException 订阅失败时 触发该异常
	 */
	public static boolean sub(String topic,int qos,IMqttMessageListener  messageListener) throws MqttException{
		return sub(topic, qos, messageListener, 0);
	}
	
	/**
	 * 订阅主题
	 * @param topic String 主题
	 * @param qos int 消息质量 
	 * @param messageListener 收到消息是时IMqttMessageListener回调接口
	 * @param timeout 回调接口 耗时时间 单位:毫秒
	 * @throws MqttException 订阅失败时 或订阅超时触发该异常
	 */
	public static boolean sub(String topic,int qos,IMqttMessageListener  messageListener,long timeout) throws MqttException{
		if(timeout <= -1 || timeout > Long.MAX_VALUE){
			throw new IllegalArgumentException("订阅回调函数超时时间不能小于0 且 大于 Long.MAX_VALUE");
		}
		IMqttToken token = client.subscribe(topic,qos,  messageListener);
		if(timeout <= 0){
			token.waitForCompletion();
		}else{
			token.waitForCompletion(timeout);
		}
		return token.isComplete();
	}
	
	/**
	 * 取消订阅
	 * @param topic String 需要被取消主题
	 * @return boolean  true:取消成功 false:取消失败
	 * @throws MqttException
	 */
	public static boolean unsub(String topic) throws MqttException{
		IMqttToken token = client.unsubscribe(topic);
		token.waitForCompletion();
		return token.isComplete();
	}
	
	/**
	 * 发布主题
	 * @param topic  String 主题
	 * @param payload byte[] 消息内容
	 * @param qos int 消息质量
	 * @param retained 是否持久化 如果设为true 服务器会将该消息发送给当前的订阅者，还会降这个消息推送给新订阅这个题注的订阅者 
	 * @throws MqttException 
	 * @throws MqttPersistenceException 
	 */
	public static boolean pub(String topic,byte[] payload,int qos,boolean retained) throws MqttPersistenceException, MqttException{
		return pub(topic, payload, qos, retained, 0);
	}
	
	/**
	 * 发布主题
	 * @param topic  String 主题
	 * @param payload byte[] 消息内容
	 * @param qos int 消息质量
	 * @param retained 是否持久化 如果设为true 服务器会将该消息发送给当前的订阅者，还会降这个消息推送给新订阅这个题注的订阅者
	 * @param timeout 发布超时时间 
	 * @throws MqttException 
	 * @throws MqttPersistenceException 
	 */
	public static boolean pub(String topic,byte[] payload,int qos,boolean retained,long timeout) throws MqttPersistenceException, MqttException{
		if(timeout <= -1 || timeout > Long.MAX_VALUE){
			throw new IllegalArgumentException("发布回调函数超时时间不能小于0 且 大于 Long.MAX_VALUE");
		}
		IMqttToken token = client.publish(topic, payload, qos, retained);
		if(timeout <= 0){
			token.waitForCompletion();
		}else{
			token.waitForCompletion(timeout);
		}
		return token.isComplete();
	}
	
	/**
	 * 告诉 MQTT Broker 我已经收到这条消息
	 * 关于这个方法我得多说两句，使用这个的前提条件:
	 * 1、MqttPlugin.manualAcks 必须为true
	 * 2、消息质量 Qos 必须为  1
	 * 
	 * 只有满足这两个条件时，调用该方法才有用，不满足这两个条件，调也是白调
	 * 不是我限制，paho-mqtt-client 底层就是这么限制的，我也没辙
	 * @param message 消息体
	 * @throws MqttException 
	 */
	public static void ack(MqttMessage message) throws MqttException{
		if(manualAcks && message.getQos() == MqttKit.QOS_AT_LEAST_ONCE){
			client.messageArrivedComplete(message.getId(), message.getQos());
		}
	}
	
}