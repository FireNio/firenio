call mvn clean package -DskipTests

cd ..\baseio
copy ..\baseio-all\target\baseio-all*.jar .

cd ..
pause