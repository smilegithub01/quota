@echo off
REM 批量更新实体类中的BigDecimal精度定义
REM 将 precision = 20, scale = 4 更新为 precision = 24, scale = 6

echo 正在更新实体类精度定义...

REM 使用PowerShell进行批量替换
powershell -Command "Get-ChildItem -Path 'd:\IdeaProjects\credit-N\quota-system\quota-core\src\main\java\com\bank\quota\core\domain\*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace 'precision = 20, scale = 4', 'precision = 24, scale = 6' | Set-Content $_.FullName }"

echo 实体类精度定义更新完成！
pause
