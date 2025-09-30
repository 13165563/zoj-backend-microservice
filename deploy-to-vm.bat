@echo off
REM ZOJ 微服务系统 Windows → Linux 部署脚本

setlocal enabledelayedexpansion

REM 配置变量
set "VM_IP=192.168.213.128"
set "VM_USER=root"
set "VM_PATH=/opt/zoj-backend-microservice"

REM 设置颜色
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM 打印带颜色的消息
:print_message
echo %GREEN%[INFO]%NC% %~1
goto :eof

:print_warning
echo %YELLOW%[WARNING]%NC% %~1
goto :eof

:print_error
echo %RED%[ERROR]%NC% %~1
goto :eof

:print_header
echo %BLUE%================================%NC%
echo %BLUE%  ZOJ 微服务 Windows→Linux 部署%NC%
echo %BLUE%================================%NC%
goto :eof

REM 检查环境
:check_environment
call :print_message "检查本地环境..."

REM 检查Maven
mvn --version >nul 2>&1
if errorlevel 1 (
    call :print_error "Maven 未安装，请先安装 Maven"
    exit /b 1
)

REM 检查Java
java -version >nul 2>&1
if errorlevel 1 (
    call :print_error "Java 未安装，请先安装 Java 17+"
    exit /b 1
)

call :print_message "本地环境检查通过"
goto :eof

REM 本地打包
:build_locally
call :print_message "开始本地打包..."

REM 清理并编译
call :print_message "清理并编译项目..."
mvn clean compile
if errorlevel 1 (
    call :print_error "项目编译失败"
    exit /b 1
)

REM 打包所有模块
call :print_message "打包所有模块..."
mvn clean package -DskipTests
if errorlevel 1 (
    call :print_error "项目打包失败"
    exit /b 1
)

REM 检查生成的jar文件
call :print_message "检查生成的jar文件..."
set "modules=zoj-backend-gateway zoj-backend-user-service zoj-backend-question-service zoj-backend-judge-service"

for %%m in (%modules%) do (
    if exist "%%m\target\%%m-0.0.1-SNAPSHOT.jar" (
        call :print_message "✓ %%m\target\%%m-0.0.1-SNAPSHOT.jar 生成成功"
    ) else (
        call :print_error "✗ %%m\target\%%m-0.0.1-SNAPSHOT.jar 生成失败"
        exit /b 1
    )
)

call :print_message "本地打包完成"
goto :eof

REM 传输文件到虚拟机
:transfer_to_vm
call :print_message "传输文件到虚拟机 %VM_IP%..."

REM 检查SSH连接
ssh -o ConnectTimeout=5 %VM_USER%@%VM_IP% "echo SSH连接测试成功" >nul 2>&1
if errorlevel 1 (
    call :print_error "无法连接到虚拟机 %VM_IP%，请检查："
    call :print_error "1. 虚拟机是否启动"
    call :print_error "2. SSH服务是否运行"
    call :print_error "3. IP地址是否正确"
    call :print_error "4. 是否已配置SSH密钥认证"
    exit /b 1
)

REM 创建目标目录
ssh %VM_USER%@%VM_IP% "mkdir -p %VM_PATH%"

REM 传输项目文件
call :print_message "传输项目源码..."
scp -r -o "StrictHostKeyChecking=no" . %VM_USER%@%VM_IP%:%VM_PATH%/

call :print_message "文件传输完成"
goto :eof

REM 在虚拟机中构建Docker镜像
:build_docker_images
call :print_message "在虚拟机中构建Docker镜像..."

ssh %VM_USER%@%VM_IP% "cd %VM_PATH% && docker-compose build"

call :print_message "Docker镜像构建完成"
goto :eof

REM 启动服务
:start_services
call :print_message "启动服务..."

ssh %VM_USER%@%VM_IP% "cd %VM_PATH% && docker-compose up -d"

call :print_message "等待服务启动..."
timeout /t 30 /nobreak >nul

ssh %VM_USER%@%VM_IP% "cd %VM_PATH% && docker-compose ps"

call :print_message "服务启动完成"
goto :eof

REM 显示访问信息
:show_access_info
call :print_message "服务访问信息："
echo %BLUE%API Gateway:%NC% http://%VM_IP%:8100
echo %BLUE%API 文档:%NC% http://%VM_IP%:8100/doc.html
echo %BLUE%Nacos 控制台:%NC% http://%VM_IP%:8848/nacos
echo %BLUE%RabbitMQ 管理界面:%NC% http://%VM_IP%:15672
echo.
call :print_message "默认账号信息："
echo %BLUE%Nacos:%NC% nacos/nacos
echo %BLUE%RabbitMQ:%NC% itheima/123321
echo %BLUE%MySQL:%NC% root/123
goto :eof

REM 检查服务状态
:check_services
call :print_message "检查服务状态..."
ssh %VM_USER%@%VM_IP% "cd %VM_PATH% && docker-compose ps"
goto :eof

REM 停止服务
:stop_services
call :print_message "停止服务..."
ssh %VM_USER%@%VM_IP% "cd %VM_PATH% && docker-compose down"
call :print_message "服务已停止"
goto :eof

REM 清理资源
:cleanup
call :print_message "清理Docker资源..."
ssh %VM_USER%@%VM_IP% "cd %VM_PATH% && docker-compose down -v && docker system prune -f"
call :print_message "资源清理完成"
goto :eof

REM 主函数
:main
call :print_header

if "%1"=="" set "1=deploy"

if "%1"=="deploy" (
    call :check_environment
    call :build_locally
    call :transfer_to_vm
    call :build_docker_images
    call :start_services
    call :show_access_info
) else if "%1"=="build" (
    call :check_environment
    call :build_locally
) else if "%1"=="transfer" (
    call :transfer_to_vm
) else if "%1"=="start" (
    call :start_services
    call :show_access_info
) else if "%1"=="stop" (
    call :stop_services
) else if "%1"=="restart" (
    call :stop_services
    timeout /t 5 /nobreak >nul
    call :start_services
    call :show_access_info
) else if "%1"=="status" (
    call :check_services
) else if "%1"=="clean" (
    call :cleanup
) else (
    echo 用法: %0 {deploy^|build^|transfer^|start^|stop^|restart^|status^|clean}
    echo.
    echo 命令说明：
    echo   deploy     - 完整部署流程（默认）
    echo   build      - 仅本地打包
    echo   transfer   - 仅传输文件到虚拟机
    echo   start      - 启动服务
    echo   stop       - 停止服务
    echo   restart    - 重启服务
    echo   status     - 检查服务状态
    echo   clean      - 清理Docker资源
    echo.
    echo 配置说明：
    echo   虚拟机IP: %VM_IP%
    echo   虚拟机用户: %VM_USER%
    echo   部署路径: %VM_PATH%
    echo.
    echo 使用前请确保：
    echo 1. 已配置SSH密钥认证
    echo 2. 虚拟机已安装Docker和Docker Compose
    echo 3. 虚拟机网络可访问
    exit /b 1
)

goto :eof

REM 执行主函数
call :main %*
