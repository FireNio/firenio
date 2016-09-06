copy ..\nimbleio-core\target\*SNAPSHOT.jar ..\release\nimble-io.http\lib
copy ..\nimbleio-core\target\*SNAPSHOT.jar ..\release\nimble-io.nio\lib

copy ..\nimbleio-extend\target\*SNAPSHOT.jar ..\release\nimble-io.http\lib
copy ..\nimbleio-extend\target\*SNAPSHOT.jar ..\release\nimble-io.nio\lib

xcopy ..\nimbleio-extend\src\main\resources\http\* ..\release\nimble-io.http\ /d/e
xcopy ..\nimbleio-extend\src\main\resources\nio\* ..\release\nimble-io.nio\ /d/e

mvn install:install-file -DgroupId=com.generallycloud -DartifactId=nimbleio-core -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -Dfile=..\nimbleio-core\target\nimbleio-core-0.0.1-SNAPSHOT.jar

mvn install:install-file -DgroupId=com.generallycloud -DartifactId=nimbleio-extend -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -Dfile=..\nimbleio-extend\target\nimbleio-extend-0.0.1-SNAPSHOT.jar

mvn install:install-file -DgroupId=com.generallycloud -DartifactId=nimbleio-all -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -Dfile=..\nimbleio-all\target\nimbleio-all-0.0.1-SNAPSHOT.jar