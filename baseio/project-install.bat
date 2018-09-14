call mvn clean install -DskipTests

cd ..\baseio
copy ..\baseio-all\target\baseio-all*.jar .

set version=3.2.8
call mvn install:install-file -Dfile=baseio-all-%version%-SNAPSHOT-sources.jar -DgroupId=com.generallycloud -DartifactId=baseio-all -Dversion=%version%-SNAPSHOT -Dpackaging=jar -Dclassifier=sources

cd ..
pause