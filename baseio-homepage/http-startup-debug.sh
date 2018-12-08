kill -9 $(ps -ef | grep java | grep -v grep | awk '{print $2}')

CLASSPATH=""
for i in lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

PRG="$0"
PRGDIR=`dirname "$PRG"`
# java -cp $CLASSPATH com.generallycloud.baseio.container.startup.ApplicationBootstrap $PRGDIR true

java -XX:+PrintGCDetails -Xloggc:gc.log -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n -cp $CLASSPATH -Dcontainer.runtime=prod -Dcom.generallycloud.baseio.develop.debug=true sample.baseio.http11.startup.TestHttpStartup
