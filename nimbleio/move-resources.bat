copy ..\nimbleio-core\target\*SNAPSHOT.jar ..\nimbleio-release\nimble-io.http\lib
copy ..\nimbleio-core\target\*SNAPSHOT.jar ..\nimbleio-release\nimble-io.nio\lib

copy ..\nimbleio-extend\target\*SNAPSHOT.jar ..\nimbleio-release\nimble-io.http\lib
copy ..\nimbleio-extend\target\*SNAPSHOT.jar ..\nimbleio-release\nimble-io.nio\lib
copy ..\nimbleio-all\target\*SNAPSHOT.jar .
copy ..\nimbleio-core\target\*sources.jar .

xcopy ..\nimbleio-test\src\main\resources\http\* ..\nimbleio-release\nimble-io.http\ /d/e
xcopy ..\nimbleio-test\src\main\resources\nio\* ..\nimbleio-release\nimble-io.nio\ /d/e