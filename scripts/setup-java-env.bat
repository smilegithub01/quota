@echo off
REM JDK环境变量配置脚本
REM 需要以管理员身份运行

echo ========================================
echo   JDK多版本环境变量配置
echo ========================================
echo.

REM 设置JAVA_HOME_8
setx JAVA_HOME_8 "C:\Program Files\Java\jdk1.8.0_251" /M
if %errorlevel% equ 0 (
    echo [成功] JAVA_HOME_8 已设置
) else (
    echo [失败] 设置JAVA_HOME_8失败，请以管理员身份运行
)

REM 设置JAVA_HOME_17
setx JAVA_HOME_17 "C:\Program Files\Java\jdk-17" /M
if %errorlevel% equ 0 (
    echo [成功] JAVA_HOME_17 已设置
) else (
    echo [失败] 设置JAVA_HOME_17失败，请以管理员身份运行
)

REM 显示当前配置
echo.
echo ========================================
echo   当前环境变量配置
echo ========================================
echo.
echo JAVA_HOME_8  = %JAVA_HOME_8%
echo JAVA_HOME_17 = %JAVA_HOME_17%
echo JAVA_HOME    = %JAVA_HOME%
echo.

echo 配置完成！请关闭所有命令行窗口并重新打开以使配置生效。
echo.
pause
