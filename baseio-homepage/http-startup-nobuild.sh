kill -9 $(ps -ef | grep java | grep -v grep | awk '{print $2}')

CLASSPATH=""
for i in lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

PRG="$0"
PRGDIR=`dirname "$PRG"`
java -XX:+PrintGCDetails -Xloggc:gc.log -cp $CLASSPATH -Dcontainer.runtime=prod com.generallycloud.sample.baseio.http11.startup.TestHttpStartup
