copy ..\nimbleio-core\target\*SNAPSHOT.jar ..\release\nimble-io.http\lib
copy ..\nimbleio-core\target\*SNAPSHOT.jar ..\release\nimble-io.nio\lib

copy ..\nimbleio-extend\target\*SNAPSHOT.jar ..\release\nimble-io.http\lib
copy ..\nimbleio-extend\target\*SNAPSHOT.jar ..\release\nimble-io.nio\lib
copy ..\nimbleio-all\target\nimbleio-all-0.0.1-SNAPSHOT.jar .
copy ..\nimbleio-core\target\nimbleio-core-0.0.1-SNAPSHOT-sources.jar .

xcopy ..\nimbleio-extend\src\main\resources\http\* ..\release\nimble-io.http\ /d/e
xcopy ..\nimbleio-extend\src\main\resources\nio\* ..\release\nimble-io.nio\ /d/e