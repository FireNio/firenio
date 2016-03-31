set CURRENT_DIR=%cd% 

call cmd /c "%CURRENT_DIR%\package-notests.bat" 

call cmd /c "%CURRENT_DIR%\source.bat" 

call cmd /c "%CURRENT_DIR%\move.bat" 