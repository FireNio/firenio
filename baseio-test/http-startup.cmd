rem call build-package.bat
cd ../baseio
call build-package.bat

rem call mvn clean compile
cd ../baseio-test
call mvn clean compile

rem call mvn exec:java -Dexec.mainClass="com.generallycloud.test.nio.http11.TestHTTPServer"

set MAVEN_OPTS=-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n

call mvn exec:java -Dexec.mainClass="com.generallycloud.test.nio.http11.TestHTTPServer"