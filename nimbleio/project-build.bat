set CURRENT_DIR=%cd%

call cmd /c "%CURRENT_DIR%\package.bat" 

call cmd /c "%CURRENT_DIR%\source.bat" 

call cmd /c "%CURRENT_DIR%\move.bat" 