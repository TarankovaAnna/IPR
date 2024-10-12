#!/bin/bash
# Скрипт принудительно завершает выполнение JMeter

#
# Выводится на экран список процессов Java


for /f "tokens=1 delims= " %%a in ('jps ^| findstr /c:"ApacheJMeter.jar"') do set PID=%%a
echo Process ID: %PID%


if defined PID (
    echo Process ApacheJMeter found, killing...
    taskkill /F /PID %PID%
    echo Process killed successfully
) else (
    echo No ApacheJMeter process found
)