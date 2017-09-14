cd ../../baseio
mvn clean install -DskipTests

cd ../baseio-sample/baseio-sample-http
mvn clean package -P run -DskipTests

cp -r -u -v target/classes/app ../../baseio-release/http-container/
cp -u -v target/baseio-sample-http-*-SNAPSHOT.jar ../../baseio-release/http-container/app/lib/ 
cp -u -v ../../baseio-all/target/baseio-all-*-SNAPSHOT.jar ../../baseio-release/http-container/lib/ 

cd ../../baseio-release/http-container

lsof -ntP -i:443|xargs kill -9

CLASSPATH=""
for i in lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

PRG="$0"
PRGDIR=`dirname "$PRG"`
# java -cp $CLASSPATH com.generallycloud.baseio.container.startup.ApplicationBootstrap $PRGDIR true

java -cp $CLASSPATH com.generallycloud.baseio.container.startup.ApplicationBootstrap true
