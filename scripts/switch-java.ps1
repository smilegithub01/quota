<#
.SYNOPSIS
    JDK版本切换脚本
    
.DESCRIPTION
    在JDK 8和JDK 17之间快速切换
    
.PARAMETER Version
    指定要切换的JDK版本 (8 或 17)
    
.PARAMETER Status
    显示当前JDK配置状态
    
.PARAMETER List
    列出所有已安装的JDK版本
    
.EXAMPLE
    .\switch-java.ps1 -Version 17
    切换到JDK 17
    
.EXAMPLE
    .\switch-java.ps1 -Version 8
    切换到JDK 8
    
.EXAMPLE
    .\switch-java.ps1 -Status
    显示当前JDK状态
    
.NOTES
    File Name      : switch-java.ps1
    Author         : Quota System Team
#>

param(
    [ValidateSet(8, 17)]
    [int]$Version,
    
    [switch]$Status,
    
    [switch]$List
)

# 定义JDK路径
$javaBasePath = "C:\Program Files\Java"
$jdk8Path = "$javaBasePath\jdk1.8.0_251"
$jdk17Path = "$javaBasePath\jdk-17"

function Show-Status {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  当前JDK配置状态" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    # 显示环境变量
    Write-Host "环境变量:" -ForegroundColor Yellow
    Write-Host "  JAVA_HOME    = $env:JAVA_HOME" -ForegroundColor White
    Write-Host "  JAVA_HOME_8  = $env:JAVA_HOME_8" -ForegroundColor White
    Write-Host "  JAVA_HOME_17 = $env:JAVA_HOME_17" -ForegroundColor White
    Write-Host ""
    
    # 显示当前Java版本
    Write-Host "当前Java版本:" -ForegroundColor Yellow
    try {
        $javaVersion = & java -version 2>&1 | Select-Object -First 1
        Write-Host "  $javaVersion" -ForegroundColor Green
    } catch {
        Write-Host "  未找到Java" -ForegroundColor Red
    }
    Write-Host ""
    
    # 显示Java路径
    Write-Host "Java可执行文件路径:" -ForegroundColor Yellow
    try {
        $javaPath = (Get-Command java -ErrorAction SilentlyContinue).Source
        Write-Host "  $javaPath" -ForegroundColor White
    } catch {
        Write-Host "  未找到" -ForegroundColor Red
    }
    Write-Host ""
}

function Show-List {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  已安装的JDK版本" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    $javaDir = Get-ChildItem $javaBasePath -ErrorAction SilentlyContinue
    
    if ($null -eq $javaDir) {
        Write-Host "未找到已安装的JDK" -ForegroundColor Red
        return
    }
    
    foreach ($dir in $javaDir) {
        $javaExe = Join-Path $dir.FullName "bin\java.exe"
        if (Test-Path $javaExe) {
            Write-Host "  $($dir.Name)" -ForegroundColor White
            $version = & $javaExe -version 2>&1 | Select-Object -First 1
            Write-Host "    $version" -ForegroundColor Gray
            Write-Host "    路径: $($dir.FullName)" -ForegroundColor Gray
            Write-Host ""
        }
    }
}

function Switch-JavaVersion {
    param([int]$TargetVersion)
    
    $targetPath = $null
    $versionName = ""
    
    switch ($TargetVersion) {
        8 {
            $targetPath = $jdk8Path
            $versionName = "JDK 8"
        }
        17 {
            $targetPath = $jdk17Path
            $versionName = "JDK 17"
        }
    }
    
    # 检查目标JDK是否存在
    if (-not (Test-Path $targetPath)) {
        Write-Host "[错误] $versionName 未安装: $targetPath" -ForegroundColor Red
        Write-Host "请先运行 setup-java-env.ps1 脚本安装JDK" -ForegroundColor Yellow
        return
    }
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  切换JDK版本" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Host "[切换] 正在切换到 $versionName ..." -ForegroundColor Yellow
    
    # 设置当前会话的环境变量
    $env:JAVA_HOME = $targetPath
    
    # 更新PATH，移除其他Java路径并添加新路径
    $pathArray = $env:Path -split ';'
    $newPathArray = $pathArray | Where-Object { 
        $_ -notmatch 'Java\\jdk' -and $_ -notmatch 'Java\\jre' -and $_ -ne ''
    }
    $env:Path = "$targetPath\bin;" + ($newPathArray -join ';')
    
    Write-Host "[完成] 已切换到 $versionName" -ForegroundColor Green
    Write-Host ""
    
    # 验证切换结果
    Write-Host "验证结果:" -ForegroundColor Yellow
    Write-Host "  JAVA_HOME = $env:JAVA_HOME" -ForegroundColor White
    
    try {
        $javaVersion = & java -version 2>&1 | Select-Object -First 1
        Write-Host "  $javaVersion" -ForegroundColor Green
    } catch {
        Write-Host "  验证失败" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "[提示] 此切换仅在当前PowerShell会话中有效" -ForegroundColor Yellow
    Write-Host "       如需永久切换，请修改系统环境变量或运行以下命令:" -ForegroundColor Yellow
    Write-Host "       [Environment]::SetEnvironmentVariable('JAVA_HOME', '$targetPath', 'Machine')" -ForegroundColor Gray
    Write-Host ""
}

# 主逻辑
if ($List) {
    Show-List
} elseif ($Status) {
    Show-Status
} elseif ($Version) {
    Switch-JavaVersion -TargetVersion $Version
} else {
    # 默认显示状态
    Show-Status
    Write-Host "使用方法:" -ForegroundColor Yellow
    Write-Host "  .\switch-java.ps1 -Version 17    # 切换到JDK 17" -ForegroundColor White
    Write-Host "  .\switch-java.ps1 -Version 8     # 切换到JDK 8" -ForegroundColor White
    Write-Host "  .\switch-java.ps1 -Status        # 显示当前状态" -ForegroundColor White
    Write-Host "  .\switch-java.ps1 -List          # 列出所有JDK" -ForegroundColor White
    Write-Host ""
}
