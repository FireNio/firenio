call mvn clean install -DskipTests

cd ..\baseio
copy ..\baseio-all\target\baseio-all*.jar .

cd ..
pause