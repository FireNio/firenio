call mvn clean install -P install -DskipTests

cd ..\firenio
copy ..\firenio-all\target\firenio-all*.jar .

cd ..
pause