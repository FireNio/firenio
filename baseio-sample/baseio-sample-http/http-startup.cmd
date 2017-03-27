cd ../../baseio
call build-package.bat

cd ../baseio-sample/baseio-sample-http
call mvn clean package -P run -DskipTests

rem copy ..\baseio-sample\target\baseio-sample*.jar ..\baseio-test\target\classes\http\app\_java_lib\

rem cd ../baseio-test

rem set MAVEN_OPTS=-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n

rem call mvn exec:java -Dexec.mainClass="com.generallycloud.baseio.sample.http11.startup.HttpBootstrap" -Dexec.args="http"  

call mvn exec:java -Dexec.mainClass="com.generallycloud.baseio.container.startup.ApplicationBootstrap"