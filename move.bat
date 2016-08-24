copy .\target\*.jar .\release\nimble-io.http\lib
copy .\target\*.jar .\release\nimble-io.nio\lib

xcopy .\src\main\resources\http\* .\release\nimble-io.http\ /d/e
xcopy .\src\main\resources\nio\* .\release\nimble-io.nio\ /d/e

mvn install:install-file -DgroupId=com.gifisan -DartifactId=nimbleio -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -Dfile=.\target\nimbleio-0.0.1-SNAPSHOT.jar