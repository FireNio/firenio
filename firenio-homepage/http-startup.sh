#!/bin/bash

cd ../firenio
mvn clean install -DskipTests

cd ../firenio-sample/sample-http
mvn clean package -DskipTests

rm -rf ../../firenio-homepage/app 
rm -rf ../../firenio-homepage/lib/*.jar 

cp -r -u -v target/classes/app ../../firenio-homepage
cp -u -v target/sample-http-*-jar-with-dependencies.jar ../../firenio-homepage/app/lib/ 
cp -u -v ../../firenio-boot/target/firenio-boot-*-SNAPSHOT.jar ../../firenio-homepage/lib/ 

cd ../../firenio-homepage

kill -9 $(ps -ef | grep com.firenio.boot.Bootstrap | grep -v grep | awk '{print $2}')

CLASSPATH=""
for i in lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

PRG="$0"
PRGDIR=`dirname "$PRG"`

(java -XX:+PrintGCDetails -Xloggc:gc.log -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n \
     -cp $CLASSPATH \
     -Dboot.prodMode=true \
     -Dboot.libPath=/app/lib \
     -Dboot.className=sample.http11.startup.TestHttpBootstrapEngine \
     com.firenio.boot.Bootstrap) &

## echo -17 > /proc/$(ps -ef | grep java | grep -v grep | awk '{print $2}')/oom_adj && cat /proc/$(ps -ef | grep java | grep -v grep | awk '{print $2}')/oom_adj

