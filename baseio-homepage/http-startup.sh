cd ../baseio
mvn clean install -DskipTests

cd ../baseio-sample/baseio-sample-http
mvn clean package -P run -DskipTests

cp -r -u -v target/classes/app ../../baseio-homepage
cp -u -v target/baseio-sample-http-*-SNAPSHOT.jar ../../baseio-homepage/app/lib/ 
cp -u -v ../../baseio-all/target/baseio-all-*-SNAPSHOT.jar ../../baseio-homepage/lib/ 
cp -u -v ../../baseio-all/target/baseio-all-*-SNAPSHOT.jar ../../baseio-homepage/app/lib/ 

cd ../../baseio-homepage

kill -9 $(ps -ef | grep java | grep -v grep | awk '{print $2}')

CLASSPATH=""
for i in lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

PRG="$0"
PRGDIR=`dirname "$PRG"`
# java -cp $CLASSPATH com.generallycloud.baseio.container.startup.ApplicationBootstrap $PRGDIR true

java -XX:+PrintGCDetails -Xloggc:gc.log -cp $CLASSPATH -Dcontainer.runtime=prod -Dcom.generallycloud.baseio.develop.debug=true com.generallycloud.sample.baseio.http11.startup.TestHttpStartup
