cd ../../baseio
mvn clean install -DskipTests

cd ../baseio-sample/baseio-sample-http
mvn clean package -P run -DskipTests

cp -r -u -v target/classes/app ../../baseio-release/http-container/app/ 
cp -u -v target/baseio-sample-http-*-SNAPSHOT.jar ../../baseio-release/http-container/app/lib/ 
cp -u -v ../../baseio-all/target/baseio-all-*-SNAPSHOT.jar ../../baseio-release/http-container/lib/ 

cd ../../baseio-release/http-container

PRG="$0"
PRGDIR=`dirname "$PRG"`
java -cp lib/baseio-all-3.1.9-SNAPSHOT.jar:lib/fastjson-1.1.41.jar:lib/log4j-1.2.14.jar:lib/slf4j-api-1.7.2.jar:lib/slf4j-log4j12-1.7.2.jar com.generallycloud.baseio.container.startup.ApplicationBootstrap $PRGDIR true
