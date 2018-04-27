lsof -ntP -i:443|xargs kill -9

CLASSPATH=""
for i in lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

PRG="$0"
PRGDIR=`dirname "$PRG"`
java -XX:+PrintGCDetails -Xloggc:gc.log -cp $CLASSPATH -Dcontainer.deployModel=true com.generallycloud.baseio.container.http11.HttpStartup
