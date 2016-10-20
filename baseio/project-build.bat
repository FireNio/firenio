
call cmd /c build-package.bat

cd ..\baseio-all
call cmd /c build-assembly.bat

cd ..\baseio
call cmd /c .\move-resources.bat 

call cmd /c .\install-source.bat 