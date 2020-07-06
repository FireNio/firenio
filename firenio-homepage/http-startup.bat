cd ../firenio
call mvn clean install -DskipTests

cd ../firenio-sample/sample-http
call mvn clean package -DskipTests

rmdir ..\..\firenio-homepage\app /q /s
del ..\..\firenio-homepage\lib\*.jar /q /s

xcopy target\classes\app ..\..\firenio-homepage\app\ /e /y
xcopy target\sample-http-*-jar-with-dependencies.jar ..\..\firenio-homepage\app\lib\ /y
xcopy ..\..\firenio-boot\target\firenio-boot-*-SNAPSHOT.jar ..\..\firenio-homepage\lib\ /y

cd ..\..\firenio-homepage

java -XX:+PrintGCDetails -Xloggc:gc.log ^
     -cp ./lib/*; ^
     -Dboot.prodMode=true ^
     -Dboot.libPath=/app/lib ^
     -Dboot.className=sample.http11.startup.TestHttpBootstrapEngine ^
     com.firenio.boot.Bootstrap
