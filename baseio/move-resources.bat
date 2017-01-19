rem copy ..\baseio-all\target\baseio-all*SNAPSHOT.jar ..\baseio-release\baseio.http\lib
rem copy ..\baseio-all\target\baseio-all*SNAPSHOT.jar ..\baseio-release\baseio.nio\lib

rem copy ..\baseio-all\target\*source.jar ..\baseio-release\baseio.http\lib
rem copy ..\baseio-all\target\*source.jar ..\baseio-release\baseio.nio\lib

rem xcopy ..\baseio-test\src\main\resources\http\* ..\baseio-release\baseio.http\ /d/e
rem xcopy ..\baseio-test\src\main\resources\nio\* ..\baseio-release\baseio.nio\ /d/e

copy ..\baseio-all\target\baseio-all*.jar .
rem copy ..\baseio-all\target\*sources.jar .