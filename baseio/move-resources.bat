copy ..\baseio-all\target\baseio-all*SNAPSHOT.jar ..\baseio-release\baseio.http\lib
copy ..\baseio-all\target\baseio-all*SNAPSHOT.jar ..\baseio-release\baseio.nio\lib

copy ..\baseio-all\target\*source.jar ..\baseio-release\baseio.http\lib
copy ..\baseio-all\target\*source.jar ..\baseio-release\baseio.nio\lib

copy ..\baseio-all\target\baseio-all*SNAPSHOT.jar .
copy ..\baseio-all\target\*source.jar .

xcopy ..\baseio-test\src\main\resources\http\* ..\baseio-release\baseio.http\ /d/e
xcopy ..\baseio-test\src\main\resources\nio\* ..\baseio-release\baseio.nio\ /d/e