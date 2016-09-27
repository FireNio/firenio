copy ..\nimbleio-all\target\nimbleio-all*SNAPSHOT.jar ..\nimbleio-release\nimble-io.http\lib
copy ..\nimbleio-all\target\nimbleio-all*SNAPSHOT.jar ..\nimbleio-release\nimble-io.nio\lib

copy ..\nimbleio-all\target\*source.jar ..\nimbleio-release\nimble-io.http\lib
copy ..\nimbleio-all\target\*source.jar ..\nimbleio-release\nimble-io.nio\lib

copy ..\nimbleio-all\target\nimbleio-all*SNAPSHOT.jar .
copy ..\nimbleio-all\target\*source.jar .

xcopy ..\nimbleio-test\src\main\resources\http\* ..\nimbleio-release\nimble-io.http\ /d/e
xcopy ..\nimbleio-test\src\main\resources\nio\* ..\nimbleio-release\nimble-io.nio\ /d/e