set CURRENT_DIR=%cd%

call cmd /c "%CURRENT_DIR%\build-clean.bat" 

call cmd /c "%CURRENT_DIR%\build-package.bat" 

call cmd /c "%CURRENT_DIR%\build-source.bat" 

call cmd /c "%CURRENT_DIR%\move-resources.bat" 

call cmd /c "%CURRENT_DIR%\install-jar-all.bat" 

call cmd /c "%CURRENT_DIR%\install-jar-core.bat" 

call cmd /c "%CURRENT_DIR%\install-jar-extend.bat" 