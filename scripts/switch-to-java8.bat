@echo off
REM JDK版本切换脚本 - 切换到JDK 8

echo ========================================
echo   切换到 JDK 8
echo ========================================
echo.

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_251
set PATH=%JAVA_HOME%\bin;%PATH%

echo [完成] 已切换到 JDK 8
echo.
echo JAVA_HOME = %JAVA_HOME%
echo.

REM 验证版本
echo 验证Java版本:
java -version
echo.

echo [提示] 此切换仅在当前命令行窗口有效
echo        如需永久切换，请修改系统环境变量
echo.
