echo [ACTION] Running test

REM Экспорт и проверка данных
set COMMON=C:\jenkins\tools\common.ini
if not exist "%COMMON%" (
    echo INI FILE: %COMMON% does not exist
    exit /b 1
)

REM Загрузка переменных из INI файла
for /f "tokens=*" %%A in ('type "%COMMON%"') do set "%%A"



set SCRIPT=%1
set LAUNCHED_STATIONS=%2
set ARTIFACT_DIR=%3
set LOG_NAME=%4
set UNIQUE_TEST_NAME=%5
set DATETIME=%6
set PROJECT_DIR=%7


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
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to run JMeter test.
    exit /b 1
)