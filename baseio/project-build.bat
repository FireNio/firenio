
call cmd /c build-package.bat

rem cd ..\baseio-all
rem call cmd /c build-assembly.bat

cd ..\baseio
call cmd /c .\move-resources.bat 

call cmd /c .\install-source.bat 