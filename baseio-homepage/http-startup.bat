cd ../baseio
call mvn clean install -DskipTests

cd ../baseio-sample/baseio-sample-http
call mvn clean package -P run -DskipTests

xcopy target\classes\app ..\..\baseio-homepage\app\ /e /y
xcopy target\baseio-sample-http-*-SNAPSHOT.jar ..\..\baseio-homepage\app\lib\ /y
xcopy ..\..\baseio-all\target\baseio-all-*-SNAPSHOT.jar ..\..\baseio-homepage\lib\ /y
xcopy ..\..\baseio-all\target\baseio-all-*-SNAPSHOT.jar ..\..\baseio-homepage\app\lib\ /y

cd ..\..\baseio-homepage

rem java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n -cp ./lib/*;  com.generallycloud.baseio.container.startup.ApplicationBootstrap %cd% true
rem java -cp ./lib/*;  com.generallycloud.baseio.container.startup.ApplicationBootstrap %cd% true

java -XX:+PrintGCDetails -Xloggc:gc.log -cp ./lib/*; -Dcontainer.runtime=prod -Dcom.generallycloud.baseio.develop.debug=true com.generallycloud.sample.baseio.http11.startup.TestHttpStartup
