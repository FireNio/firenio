
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
   
		ClientConnector connector = ClientUtil.getClientConnector();
		
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
   
		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		MessageConsumer consumer = new MessageConsumerImpl(session, "qName");

		consumer.login("admin", "admin100");

		Message message = consumer.revice();

		System.out.println(message);

		consumer.logout();
		
		connector.close();
```

### 示例详见 {src\test\java\test}