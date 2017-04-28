cd ../../baseio
mvn clean install -DskipTests

cd ../baseio-sample/baseio-sample-http
mvn clean package -P run -DskipTests

mvn exec:java -Dexec.mainClass="com.generallycloud.baseio.container.startup.ApplicationBootstrap"