echo [ACTION] Archiving artifact
set ARTIFACT_DIR=%1
set SCRIPTS_PATH=%2
set SCRIPT=%3

REM копируем скрипт в папку с логами
copy "%SCRIPTS_PATH%\%SCRIPT%.jmx" "%ARTIFACT_DIR%\%SCRIPT%.jmx"

REM создаем архив
"C:\Program Files\WinRAR\rar.exe" a -r "%ARTIFACT_DIR%.rar" "%ARTIFACT_DIR%"

REM копируем архив на другой диск
xcopy /E /H "%ARTIFACT_DIR%.rar" "Z:\"

REM удаляем архив с нагрузочной станции
del /s /q "%ARTIFACT_DIR%.rar"

REM удаляем исходную папку с логами с нагрузочной станции
rmdir /s /q "%ARTIFACT_DIR%"

echo [ACTION] Stopped at %DATE:~-10,8%_%TIME:~0,8%
