# JDK 17 安装与多版本配置指南

## 一、当前环境状态

- **已安装JDK版本**: JDK 1.8.0_251
- **安装路径**: `C:\Program Files\Java\jdk1.8.0_251`
- **JAVA_HOME**: `C:\Program Files\Java\jdk1.8.0_251`

## 二、下载JDK 17

### 方式一：Oracle官网下载（推荐）

1. 访问Oracle JDK下载页面：
   ```
   https://www.oracle.com/java/technologies/downloads/#java17
   ```

2. 选择Windows x64 Installer (jdk-17_windows-x64_bin.exe)

3. 下载并运行安装程序

### 方式二：使用PowerShell下载（需要Oracle账号）

```powershell
# 下载JDK 17安装包（示例URL，请从官网获取实际下载链接）
$downloadUrl = "https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.exe"
$outputPath = "$env:USERPROFILE\Downloads\jdk-17_windows-x64_bin.exe"

# 使用Invoke-WebRequest下载
Invoke-WebRequest -Uri $downloadUrl -OutFile $outputPath -UseBasicParsing
```

### 方式三：使用Chocolatey包管理器（推荐）

```powershell
# 如果已安装Chocolatey，直接执行：
choco install openjdk17 -y

# 或者安装Oracle JDK 17：
choco install oraclejdk17 -y
```

## 三、安装步骤

### 步骤1：运行安装程序

1. 双击下载的 `jdk-17_windows-x64_bin.exe`
2. 点击"Next"继续
3. 选择安装路径（建议保持默认或选择 `C:\Program Files\Java\jdk-17`）
4. 完成安装

### 步骤2：验证安装

```powershell
# 检查JDK 17是否安装成功
Test-Path "C:\Program Files\Java\jdk-17"
```

## 四、环境变量配置

### 自动配置方式

运行项目根目录下的配置脚本：

```powershell
# 以管理员身份运行PowerShell
Set-ExecutionPolicy Bypass -Scope Process -Force
.\scripts\setup-java-env.ps1
```

### 手动配置方式

1. 打开系统环境变量设置：
   - 右键"此电脑" → 属性 → 高级系统设置 → 环境变量

2. 添加新的系统变量：
   ```
   变量名：JAVA_HOME_8
   变量值：C:\Program Files\Java\jdk1.8.0_251
   
   变量名：JAVA_HOME_17
   变量值：C:\Program Files\Java\jdk-17
   
   变量名：JAVA_HOME
   变量值：%JAVA_HOME_8%（默认使用JDK 8）
   ```

3. 修改PATH变量：
   - 在PATH变量开头添加：`%JAVA_HOME%\bin`

## 五、版本切换方法

### 方式一：使用切换脚本

```powershell
# 切换到JDK 8
.\scripts\switch-java.ps1 -Version 8

# 切换到JDK 17
.\scripts\switch-java.ps1 -Version 17

# 查看当前版本
.\scripts\switch-java.ps1 -Status
```

### 方式二：手动切换

```powershell
# 切换到JDK 17（当前PowerShell会话）
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# 验证
java -version
```

### 方式三：修改系统环境变量

1. 打开环境变量设置
2. 修改 `JAVA_HOME` 变量值为：
   - JDK 8: `C:\Program Files\Java\jdk1.8.0_251`
   - JDK 17: `C:\Program Files\Java\jdk-17`
3. 重新打开命令行窗口

## 六、验证安装结果

```powershell
# 验证JDK 8
$env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_251"
& "$env:JAVA_HOME\bin\java.exe" -version

# 验证JDK 17
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
& "$env:JAVA_HOME\bin\java.exe" -version

# 查看所有已安装的JDK
Get-ChildItem "C:\Program Files\Java" | Select-Object Name
```

## 七、IDE配置（IDEA）

### 配置项目JDK

1. 打开IDEA → File → Project Structure
2. 在SDKs中添加JDK 17：
   - 点击"+" → Add JDK
   - 选择 `C:\Program Files\Java\jdk-17`
3. 在Project Settings → Project中：
   - SDK选择JDK 17
   - Language level选择17

### 配置Maven Runner

1. Settings → Build, Execution, Deployment → Build Tools → Maven → Runner
2. JRE选择JDK 17

## 八、常见问题

### Q1: 安装后命令行仍显示旧版本？

**解决方案**：
- 关闭所有命令行窗口并重新打开
- 或刷新环境变量：
  ```powershell
  $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
  ```

### Q2: 如何在脚本中指定JDK版本？

**解决方案**：
```powershell
# 在脚本开头指定JDK版本
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "$env:JAVA_HOME\bin;" + ($env:Path -replace '[^;]*Java[^;]*;', '')
```

### Q3: Maven编译时报错？

**解决方案**：
- 检查JAVA_HOME是否正确指向JDK 17
- 在pom.xml中确认java.version为17
- 运行 `mvn -version` 验证Maven使用的JDK版本

## 九、附录

### 环境变量配置脚本

见项目根目录：`scripts\setup-java-env.ps1`

### 版本切换脚本

见项目根目录：`scripts\switch-java.ps1`

### 快速验证命令

```powershell
# 一键验证脚本
java -version
javac -version
mvn -version
```
