
# NimbleIO使用指南

本项目目前还在测试阶段，请大家多提issue，共同完善。

## 项目意义

学习研究

## 功能列表

### 启动服务

``` java
   
		ServerLauncher launcher = new ServerLauncher();
		
		launcher.launch();
```

### 发送消息

``` java
   
		ClientConnector connector = new ClientConnector("localhost", 8300);
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		MessageProducer producer = new MessageProducerImpl(session);

		producer.login("admin", "admin100");

		TextMessage message = new TextMessage("msgID", "qName", "hello world!");

		producer.offer(message);

		producer.logout();
		
		connector.close();
```

### 收取消息

``` java
   
		ClientConnector connector = new ClientConnector("localhost", 8300);
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		MessageConsumer consumer = new MessageConsumerImpl(session, "qName");

		consumer.login("admin", "admin100");

		Message message = consumer.revice();

		System.out.println(message);

		consumer.logout();
		
		connector.close();
```

### 免费的服务
``` java
   
		ClientConnector connector = new ClientConnector("wkapp.wicp.net", 11990);
```


### 案例
	(感谢android-chat-starter@https://github.com/madhur/android-chat-starter)
![](https://raw.githubusercontent.com/NimbleIO/NimbleIO/master/images/TEST-1.png)


### 示例详见 {src\test\java\test}
