call mvn clean package -DskipTests

cd ..\firenio
copy ..\firenio-all\target\firenio-all*.jar .

cd ..
pause