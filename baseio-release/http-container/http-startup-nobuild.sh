lsof -ntP -i:443|xargs kill -9

CLASSPATH=""
for i in lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

PRG="$0"
PRGDIR=`dirname "$PRG"`
java -cp $CLASSPATH com.generallycloud.baseio.container.startup.ApplicationBootstrap true
