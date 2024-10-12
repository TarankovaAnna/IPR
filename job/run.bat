@echo off

REM Экспорт и проверка данных
set COMMON=C:\jenkins\tools\common.ini
if not exist "%COMMON%" (
    echo INI FILE: %COMMON% does not exist
    exit /b 1
)

REM Загрузка переменных из INI файла
for /f "tokens=*" %%A in ('type "%COMMON%"') do set "%%A"


set SCRIPTS_PATH=C:\jenkins
set PROJECT_DIR=C:
set SCRIPT=%1
set LAUNCHED_STATIONS=%2
set DATETIME=%DATE:~-4%_%DATE:~-7,2%_%DATE:~-10,2%_%TIME:~0,2%_%TIME:~3,2%
set LOG_NAME=%DATETIME%.log
set UNIQUE_TEST_NAME=%SCRIPT%_%LAUNCHED_STATIONS%_%DATETIME%
set ARTIFACT_DIR=%PROJECT_DIR%\%UNIQUE_TEST_NAME%

REM Вывод информации по запущенному тесту
echo [INFO] PROJECT_DIR: %PROJECT_DIR%
echo [INFO] ARTIFACT_DIR: %ARTIFACT_DIR%
echo [INFO] Log name: %LOG_NAME%
echo [INFO] SCRIPT: %SCRIPT%
echo [INFO] JAVA arguments: %JAVA_ARGS%
echo [INFO] JMETER parameters: %JMETER_PARAMS%
echo [INFO] JMETER_FLEXIBLE_PARAMS: %JMETER_FLEXIBLE_PARAMS%

echo [ACTION] Run test at %DATE:~-10,8%_%TIME:~0,8%

REM Запуск теста
cd "%JMETER_JAR%"
java -jar ApacheJMeter.jar -n -j "%ARTIFACT_DIR%\%LOG_NAME%" -t "%SCRIPTS_PATH%\%SCRIPT%.jmx" -Jdata="%ARTIFACT_DIR%" -JtestName="%UNIQUE_TEST_NAME%"

echo [ACTION] Archiving artifact
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

