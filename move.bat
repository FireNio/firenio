copy .\target\*.jar .\release\nimble-io.http\lib
copy .\target\*.jar .\release\nimble-io.nio\lib

xcopy .\src\main\resources\http\* .\release\nimble-io.http\ /d/e
xcopy .\src\main\resources\nio\* .\release\nimble-io.nio\ /d/e