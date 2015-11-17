cd ../firenio
mvn clean install -DskipTests

cd ../firenio-sample/sample-http
mvn clean package -DskipTests

cp -r -u -v target/classes/app ../../firenio-homepage
cp -u -v target/sample-http-*-jar-with-dependencies.jar ../../firenio-homepage/app/lib/ 
cp -u -v ../../firenio-all/target/firenio-all-*-SNAPSHOT.jar ../../firenio-homepage/lib/ 

cd ../../firenio-homepage

kill -9 $(ps -ef | grep java | grep -v grep | awk '{print $2}')

CLASSPATH=""
for i in lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

PRG="$0"
PRGDIR=`dirname "$PRG"`

java -XX:+PrintGCDetails -Xloggc:gc.log -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n \
     -cp $CLASSPATH \
     -Dboot.mode=prod \
     -Dboot.libPath=/app/lib \
     -Dboot.class=sample.http11.startup.TestHttpBootstrapEngine \
     com.firenio.container.Bootstrap
