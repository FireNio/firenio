cd ../../baseio
call build-package.bat

cd ../baseio-sample/baseio-sample-http
call mvn clean package -P run -DskipTests

xcopy target\classes\app ..\..\baseio-release\http-container\app\ /e /y
xcopy target\baseio-sample-http-*-SNAPSHOT.jar ..\..\baseio-release\http-container\app\lib\ /y
xcopy ..\..\baseio-all\target\baseio-all-*-SNAPSHOT.jar ..\..\baseio-release\http-container\lib\ /y

cd ..\..\baseio-release\http-container

rem java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n -cp ./lib/*;  com.generallycloud.baseio.container.startup.ApplicationBootstrap %cd% true
rem java -cp ./lib/*;  com.generallycloud.baseio.container.startup.ApplicationBootstrap %cd% true

java -cp ./lib/*;  com.generallycloud.baseio.container.startup.ApplicationBootstrap true
