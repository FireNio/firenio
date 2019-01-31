cd ../baseio
call mvn clean install -DskipTests

cd ../baseio-sample/baseio-sample-http
call mvn clean package -DskipTests

xcopy target\classes\app ..\..\baseio-homepage\app\ /e /y
xcopy target\baseio-sample-http-*-jar-with-dependencies.jar ..\..\baseio-homepage\app\lib\ /y
xcopy ..\..\baseio-all\target\baseio-all-*-SNAPSHOT.jar ..\..\baseio-homepage\lib\ /y

cd ..\..\baseio-homepage

rem java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n -cp ./lib/*;  com.generallycloud.baseio.container.startup.ApplicationBootstrap %cd% true
rem java -cp ./lib/*;  com.generallycloud.baseio.container.startup.ApplicationBootstrap %cd% true

java -XX:+PrintGCDetails -Xloggc:gc.log ^
     -cp ./lib/*; ^
     -Dboot.mode=prod ^
     -Dboot.libPath=/app/lib ^
     -Dcom.generallycloud.baseio.develop.debug=true ^
     -Dboot.class=sample.baseio.http11.startup.TestHttpBootstrapEngine ^
     com.firenio.baseio.container.Bootstrap
