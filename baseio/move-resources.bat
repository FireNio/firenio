copy ..\baseio-all\target\baseio-all*SNAPSHOT.jar ..\baseio-release\base-io.http\lib
copy ..\baseio-all\target\baseio-all*SNAPSHOT.jar ..\baseio-release\base-io.nio\lib

copy ..\baseio-all\target\*source.jar ..\baseio-release\base-io.http\lib
copy ..\baseio-all\target\*source.jar ..\baseio-release\base-io.nio\lib

copy ..\baseio-all\target\baseio-all*SNAPSHOT.jar .
copy ..\baseio-all\target\*source.jar .

xcopy ..\baseio-test\src\main\resources\http\* ..\baseio-release\base-io.http\ /d/e
xcopy ..\baseio-test\src\main\resources\nio\* ..\baseio-release\base-io.nio\ /d/e