<#
.SYNOPSIS
    JDK多版本环境配置脚本
    
.DESCRIPTION
    配置JDK 8和JDK 17的多版本共存环境，设置环境变量
    
.NOTES
    File Name      : setup-java-env.ps1
    Author         : Quota System Team
    Prerequisite   : PowerShell 5.1 or later
    Execution Policy: 需要以管理员身份运行
#>

param(
    [switch]$Force = $false
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK多版本环境配置脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "[警告] 建议以管理员身份运行此脚本，否则只能设置用户级环境变量" -ForegroundColor Yellow
    Write-Host ""
}

# 定义JDK路径
$javaBasePath = "C:\Program Files\Java"
$jdk8Path = "$javaBasePath\jdk1.8.0_251"
$jdk17Path = "$javaBasePath\jdk-17"

# 检查JDK 8是否存在
Write-Host "[检查] JDK 8安装状态..." -ForegroundColor Yellow
if (Test-Path $jdk8Path) {
    Write-Host "  ✓ JDK 8 已安装: $jdk8Path" -ForegroundColor Green
} else {
    Write-Host "  ✗ JDK 8 未找到: $jdk8Path" -ForegroundColor Red
    Write-Host "    请先安装JDK 8" -ForegroundColor Yellow
}

# 检查JDK 17是否存在
Write-Host "[检查] JDK 17安装状态..." -ForegroundColor Yellow
if (Test-Path $jdk17Path) {
    Write-Host "  ✓ JDK 17 已安装: $jdk17Path" -ForegroundColor Green
} else {
    Write-Host "  ✗ JDK 17 未找到: $jdk17Path" -ForegroundColor Red
    Write-Host ""
    Write-Host "请先安装JDK 17，可以选择以下方式：" -ForegroundColor Yellow
    Write-Host "  1. 访问 https://www.oracle.com/java/technologies/downloads/#java17 下载安装" -ForegroundColor White
    Write-Host "  2. 使用Chocolatey: choco install openjdk17 -y" -ForegroundColor White
    Write-Host "  3. 使用winget: winget install Oracle.JDK.17" -ForegroundColor White
    Write-Host ""
    
    $installChoice = Read-Host "是否现在下载JDK 17安装包? (Y/N)"
    if ($installChoice -eq 'Y' -or $installChoice -eq 'y') {
        Write-Host ""
        Write-Host "[下载] 正在打开JDK 17下载页面..." -ForegroundColor Yellow
        Start-Process "https://www.oracle.com/java/technologies/downloads/#java17"
        Write-Host "请在浏览器中完成下载和安装，然后重新运行此脚本" -ForegroundColor Yellow
        exit 0
    }
    
    if (-not $Force) {
        Write-Host "安装JDK 17后请重新运行此脚本" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host ""

# 设置环境变量
Write-Host "[配置] 设置环境变量..." -ForegroundColor Yellow

# 设置JAVA_HOME_8
Write-Host "  设置 JAVA_HOME_8 = $jdk8Path" -ForegroundColor White
[Environment]::SetEnvironmentVariable("JAVA_HOME_8", $jdk8Path, "Machine")

# 设置JAVA_HOME_17
Write-Host "  设置 JAVA_HOME_17 = $jdk17Path" -ForegroundColor White
[Environment]::SetEnvironmentVariable("JAVA_HOME_17", $jdk17Path, "Machine")

# 获取当前JAVA_HOME
$currentJavaHome = [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
if ([string]::IsNullOrEmpty($currentJavaHome)) {
    Write-Host "  设置 JAVA_HOME = %JAVA_HOME_8% (默认使用JDK 8)" -ForegroundColor White
    [Environment]::SetEnvironmentVariable("JAVA_HOME", "%JAVA_HOME_8%", "Machine")
} else {
    Write-Host "  JAVA_HOME 已存在: $currentJavaHome" -ForegroundColor White
    Write-Host "  如需修改，请手动更改或使用 -Force 参数" -ForegroundColor Yellow
}

# 更新PATH变量
Write-Host "  更新 PATH 变量..." -ForegroundColor White
$machinePath = [Environment]::GetEnvironmentVariable("Path", "Machine")

# 移除旧的Java路径
$newPath = ($machinePath -split ';' | Where-Object { 
    $_ -notmatch 'Java\\jdk' -and $_ -notmatch 'Java\\jre' -and $_ -ne ''
}) -join ';'

# 添加新的Java路径到最前面
$newPath = "%JAVA_HOME%\bin;$newPath"
[Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")

Write-Host ""
Write-Host "[完成] 环境变量配置完成!" -ForegroundColor Green
Write-Host ""

# 显示配置结果
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  配置结果" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "环境变量:" -ForegroundColor Yellow
Write-Host "  JAVA_HOME_8  = $([Environment]::GetEnvironmentVariable('JAVA_HOME_8', 'Machine'))" -ForegroundColor White
Write-Host "  JAVA_HOME_17 = $([Environment]::GetEnvironmentVariable('JAVA_HOME_17', 'Machine'))" -ForegroundColor White
Write-Host "  JAVA_HOME    = $([Environment]::GetEnvironmentVariable('JAVA_HOME', 'Machine'))" -ForegroundColor White
Write-Host ""

# 验证JDK版本
Write-Host "JDK版本验证:" -ForegroundColor Yellow

if (Test-Path "$jdk8Path\bin\java.exe") {
    Write-Host "  JDK 8:" -ForegroundColor White
    & "$jdk8Path\bin\java.exe" -version 2>&1 | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
}

if (Test-Path "$jdk17Path\bin\java.exe") {
    Write-Host "  JDK 17:" -ForegroundColor White
    & "$jdk17Path\bin\java.exe" -version 2>&1 | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  后续操作" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 关闭所有命令行窗口并重新打开" -ForegroundColor White
Write-Host "2. 使用 switch-java.ps1 脚本切换JDK版本:" -ForegroundColor White
Write-Host "   .\scripts\switch-java.ps1 -Version 17" -ForegroundColor Gray
Write-Host "   .\scripts\switch-java.ps1 -Version 8" -ForegroundColor Gray
Write-Host "3. 或手动设置环境变量:" -ForegroundColor White
Write-Host "   `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'" -ForegroundColor Gray
Write-Host ""

# 刷新当前会话的环境变量
Write-Host "[刷新] 正在刷新当前会话的环境变量..." -ForegroundColor Yellow
$env:JAVA_HOME_8 = [Environment]::GetEnvironmentVariable("JAVA_HOME_8", "Machine")
$env:JAVA_HOME_17 = [Environment]::GetEnvironmentVariable("JAVA_HOME_17", "Machine")
$env:JAVA_HOME = [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
$env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [Environment]::GetEnvironmentVariable("Path", "User")

Write-Host "[完成] 当前会话环境变量已刷新!" -ForegroundColor Green
