call mvn clean compile -DskipTests
call mvn exec:java -Dexec.mainClass="com.generallycloud.sample.baseio.http11.startup.HttpStartup" -Dexec.args="false"