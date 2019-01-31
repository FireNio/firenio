cd ../baseio
mvn clean install -DskipTests

cd ../baseio-sample/baseio-sample-http
mvn clean package -DskipTests

cp -r -u -v target/classes/app ../../baseio-homepage
cp -u -v target/baseio-sample-http-*-jar-with-dependencies.jar ../../baseio-homepage/app/lib/ 
cp -u -v ../../baseio-all/target/baseio-all-*-SNAPSHOT.jar ../../baseio-homepage/lib/ 

cd ../../baseio-homepage

kill -9 $(ps -ef | grep java | grep -v grep | awk '{print $2}')

CLASSPATH=""
for i in lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

PRG="$0"
PRGDIR=`dirname "$PRG"`
# java -cp $CLASSPATH com.generallycloud.baseio.container.startup.ApplicationBootstrap $PRGDIR true

java -XX:+PrintGCDetails -Xloggc:gc.log -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n \
     -cp $CLASSPATH \
     -Dboot.mode=prod \
     -Dboot.libPath=/app/lib \
     -Dcom.generallycloud.baseio.develop.debug=true \
     -Dboot.class=sample.baseio.http11.startup.TestHttpBootstrapEngine \
     com.firenio.baseio.container.Bootstrap
