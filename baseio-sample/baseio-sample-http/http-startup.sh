mvn clean compile -DskipTests
mvn exec:java -Dexec.mainClass="com.generallycloud.sample.baseio.http11.startup.HttpStartup" -Dexec.args="false"