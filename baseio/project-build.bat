
call cmd /c build-package.bat

cd ..\nimbleio-all
call cmd /c build-assembly.bat

cd ..\nimbleio
call cmd /c .\move-resources.bat 

call cmd /c .\install-source.bat 