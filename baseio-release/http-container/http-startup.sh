PRG="$0"
PRGDIR=`dirname "$PRG"`
java -cp lib/baseio-all-3.1.8-SNAPSHOT.jar:lib/fastjson-1.1.41.jar:lib/log4j-1.2.14.jar:lib/slf4j-api-1.7.2.jar:lib/slf4j-log4j12-1.7.2.jar com.generallycloud.baseio.container.startup.ApplicationBootstrap "PRGDIR"
