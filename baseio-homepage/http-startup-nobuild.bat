java -XX:+PrintGCDetails -Xloggc:gc.log -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n ^
     -cp ./lib/*; ^
     -Dboot.mode=prod ^
     -Dcom.generallycloud.baseio.develop.debug=true ^
     -Dboot.class=sample.baseio.http11.startup.TestHttpBootstrapEngine ^
     com.firenio.baseio.container.Bootstrap
