package com.cha1024.ext.plugin.mqtt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import com.jfinal.kit.LogKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
/**
 * Jfinal MQTT 插件
 * 使用.eclipse.paho.client.mqttv3作为客户端
 * @author Dean
 *
 */
public class MqttPlugin implements IPlugin{	
	private ScheduledExecutorService scheduler;//自动重连的任务调度器
	private MqttAsyncClient client;//异步MQTT Client
	private MqttConnectOptions options;//MQTT Cleint连接配置
	
	private String brokerURL;//MQTT服务器连接地址
	private String clientId;//当前客户端ID
	private String userName;//连接MQTT额用户名
	private String password;//连接MQTT额密码
	private boolean manualAcks = false;//是否手动应答    //默认为false
	private boolean automaticReconnection = false; //是否自动重连 默认诶false
	private boolean cleanSession = true;//是否保持session，true无法接受离线消息， false可以接受离线消息
	private int reConnectionTimeInterval = 5;//重试重连的时间间隔
	private int connectionTimeout = 30;//连接超时时间
	private int keepAliveInterval = 60;//心跳时间
	private String version = "3.1.1";//可选版本3.0 3.1.1
	private int maxConnections = 10;//最大连接数  业务量大的话 建议增加这个设置
	private Properties sslProperties;//MQTT SSL链接配置
	//对于消息质量为1或者2的消息必须在client和server进行存储，以保证消息服务质量
	//Paho-mqtt-client 默认存储在内存中，如果设置了stroageDir，则可以将mqtt的消息序列化指定的目录之下
	private String stroageDir;
	//下面是遗嘱消息的设置
	private String willTopic;//将遗嘱消息发布到那个主题
	private String willPayload;//遗嘱消息内容
	private int willQos = 2;//遗嘱消息内容
	private boolean willretained = false;//是否保留这个遗嘱消息
	
	public MqttPlugin(String configFile){
		Prop prop = PropKit.use(configFile);
		this.brokerURL = prop.get("mqtt.brokerURL", "tcp://127.0.0.1:1883");
		this.clientId = prop.get("mqtt.clientId", "jf_mq_p_"+System.nanoTime());
		this.userName = prop.get("mqtt.userName", null);
		this.password = prop.get("mqtt.password",null);
		this.manualAcks = prop.getBoolean("mqtt.manualAcks",false);
		this.automaticReconnection = prop.getBoolean("mqtt.automaticReconnection", false);
		this.cleanSession = prop.getBoolean("mqtt.cleanSession", true);
		this.connectionTimeout = prop.getInt("mqtt.connectionTimeout", 30);
		this.keepAliveInterval = prop.getInt("mqtt.keepAliveInterval", 60);
		this.version = prop.get("mqtt.version", "3.1.1");
		this.maxConnections = prop.getInt("mqtt.maxConnections", 10);
		this.stroageDir = prop.get("mqtt.stroageDir", null);
		this.reConnectionTimeInterval = prop.getInt("mqtt.reConnectionTimeInterval", 5);
		if(StrKit.notBlank(prop.get("mqtt.sslProperties"))){
			Properties p = new Properties();
			try {
				p.load(new FileInputStream(prop.get("mqtt.sslProperties")));
				this.sslProperties = p;
			} catch (FileNotFoundException e) {
				LogKit.error("MQTT SSL配置文件未找到,请检查文件["+prop.get("mqtt.sslProperties")+"]是否存在",e);
			} catch (IOException e) {
				LogKit.error("MQTT SSL配置文件["+prop.get("mqtt.sslProperties")+"]读取失败",e);
			}
		}
		
	}
	
	public MqttPlugin(String brokerURL,String clientId){
		this.brokerURL = brokerURL;
		this.clientId = clientId;
	}
	
	public boolean start() {
		//获取实例化mqtt client
		try {
			if(StrKit.isBlank(this.stroageDir)){
				this.client = new MqttAsyncClient(this.brokerURL, this.clientId);
			}else{
				this.client = new MqttAsyncClient(this.brokerURL, this.clientId, new MqttDefaultFilePersistence(this.stroageDir));
			}
		} catch (MqttException e) {
			LogKit.error("MQTT Client Plugin 初始化Client失败",e);
			return false;
		}
		this.client.setManualAcks(this.manualAcks);
		this.options = new MqttConnectOptions();
		this.options.setAutomaticReconnect(this.automaticReconnection);
		this.options.setCleanSession(this.cleanSession);
		this.options.setConnectionTimeout(this.connectionTimeout);
		this.options.setKeepAliveInterval(this.keepAliveInterval);
		
		//设置mqtt版本信息
		if(StrKit.notBlank(this.version)){
			if("3.1.1".equals(this.version)){
				this.options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
			}else if("3.1".equals(this.version)){
				this.options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
			}else{
				this.options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
			}
		}else{
			this.options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
		}
		
		//设置用户名密码
		if(StrKit.notBlank(this.userName) && StrKit.notBlank(this.password)){
			this.options.setUserName(this.userName);
			this.options.setPassword(this.password.toCharArray());
		}
		
		//设置遗嘱消息
		if(StrKit.notBlank(this.willTopic) && StrKit.notBlank(this.willPayload)){
			this.options.setWill(this.willTopic, this.willPayload.getBytes(), this.willQos, this.willretained);
		}
		
		//设置SSL配置文件
		if(this.sslProperties != null && !this.sslProperties.isEmpty() && this.sslProperties.values().size() > 0){
			this.options.setSSLProperties(this.sslProperties);
		}
		
		try {
			IMqttToken token = this.client.connect(this.options);
			token.waitForCompletion(this.connectionTimeout * 1000);
			MqttKit.init(this.client,this.manualAcks);
			if(this.automaticReconnection){
				this.reConnection();
			}
		} catch (MqttSecurityException e) {
			LogKit.error("MQTT Clinet连接MQTT Broker连接信息验证失败,请检查连接配置信息",e);
			return false;
		} catch (MqttException e) {
			LogKit.error("MQTT Clinet连接MQTT Broker连接失败",e);
			return false;
		}
		return true;
	}

	public boolean stop() {
		if(this.automaticReconnection && !this.scheduler.isShutdown()){
			this.scheduler.shutdownNow();
		}
		if(this.client != null && this.client.isConnected()){
			try {
				this.client.disconnect();
			} catch (MqttException e) {
				LogKit.error("释放MQTT Client连接失败",e);
				return false;
			}
		}
		return true;
	}
	/**
	 * 使用线程池定时检查client连接状态并重连client
	 */
	private void reConnection(){
		this.scheduler = Executors.newSingleThreadScheduledExecutor();
		this.scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				if(!client.isConnected()){
					try {
						client.reconnect();
						LogKit.debug("MQTT Client 尝试重连结果"+(client.isConnected()?"重连成功":"重连失败，继续尝试"));
					} catch (MqttException e) {
						LogKit.error("MQTT Client重连失败",e);
					}finally {}
				}
			}
		}, 0 , this.reConnectionTimeInterval * 1000, TimeUnit.MILLISECONDS);
	}
	
	//设置遗嘱消息
	public void setWill(String topic,String payload,int qos,boolean retainedd){
		this.willTopic = topic;
		this.willPayload = payload;
		this.willQos = qos;
		this.willretained = retainedd;
	}
	
	/***********************************************************************************************/
	/************************************下面一大堆的get set 我就不说了*********************************************/
	/***********************************************************************************************/

	public String getBrokerURL() {
		return brokerURL;
	}

	public void setBrokerRUL(String brokerURL) {
		this.brokerURL = brokerURL;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean getManualAcks() {
		return manualAcks;
	}

	public void setManualAcks(boolean manualAcks) {
		this.manualAcks = manualAcks;
	}

	public boolean getAutomaticReconnection() {
		return automaticReconnection;
	}

	public void setAutomaticReconnection(boolean automaticReconnection) {
		this.automaticReconnection = automaticReconnection;
	}

	public boolean getCleanSession() {
		return cleanSession;
	}

	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getKeepAliveInterval() {
		return keepAliveInterval;
	}

	public void setKeepAliveInterval(int keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public Properties getSslProperties() {
		return sslProperties;
	}

	public void setSslProperties(Properties sslProperties) {
		this.sslProperties = sslProperties;
	}

	public String getWillTopic() {
		return willTopic;
	}

	public String getWillPayload() {
		return willPayload;
	}

	public int getWillQos() {
		return willQos;
	}


	public boolean getWillretained() {
		return willretained;
	}
	public String getStroageDir() {
		return stroageDir;
	}

	public void setStroageDir(String stroageDir) {
		this.stroageDir = stroageDir;
	}

	public int getReConnectionTimeInterval() {
		return reConnectionTimeInterval;
	}

	public void setReConnectionTimeInterval(int reConnectionTimeInterval) {
		if(reConnectionTimeInterval < 0 || reConnectionTimeInterval > Integer.MAX_VALUE){
			throw new IllegalArgumentException("MQTT Client重连时间间隔不能小于0 且 大于 Integer.MAX_VALUE");
		}
		this.reConnectionTimeInterval = reConnectionTimeInterval;
	}
	
}