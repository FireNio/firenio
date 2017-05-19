@rem >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
rem java -Xms128m -Xmx512m -verbose:gc -Xloggc:gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -cp .;.\lib\*; com.generallycloud.nio.container.http11.startup.HttpServerStartup




set "CURRENT_DIR=%cd%"
@rem java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n -cp ./lib/*;  com.generallycloud.baseio.container.startup.ApplicationBootstrap %CURRENT_DIR% true
java -cp ./lib/*;  com.generallycloud.baseio.container.startup.ApplicationBootstrap %CURRENT_DIR% true
