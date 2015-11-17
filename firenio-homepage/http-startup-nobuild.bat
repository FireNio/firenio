java -XX:+PrintGCDetails -Xloggc:gc.log -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n ^
     -cp ./lib/*; ^
     -Dboot.mode=prod ^
     -Dboot.libPath=/app/lib ^
     -Dcom.firenio.develop.debug=true ^
     -Dboot.class=sample.http11.startup.TestHttpBootstrapEngine ^
     com.firenio.container.Bootstrap
