cd ../firenio
call mvn clean install -DskipTests

cd ../firenio-sample/sample-http
call mvn clean package -DskipTests

xcopy target\classes\app ..\..\firenio-homepage\app\ /e /y
xcopy target\sample-http-*-jar-with-dependencies.jar ..\..\firenio-homepage\app\lib\ /y
xcopy ..\..\firenio-all\target\firenio-all-*-SNAPSHOT.jar ..\..\firenio-homepage\lib\ /y

cd ..\..\firenio-homepage

java -XX:+PrintGCDetails -Xloggc:gc.log ^
     -cp ./lib/*; ^
     -Dboot.mode=prod ^
     -Dboot.libPath=/app/lib ^
     -Dboot.class=sample.http11.startup.TestHttpBootstrapEngine ^
     com.firenio.container.Bootstrap
