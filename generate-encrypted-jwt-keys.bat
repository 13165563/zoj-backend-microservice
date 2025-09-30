@echo off
echo 开始生成加密JWT密钥文件...

REM 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到Java，请先安装Java
    pause
    exit /b 1
)

REM 编译并运行Java程序
echo 正在编译加密密钥生成程序...
javac generate-encrypted-jwt-keys.java

if %errorlevel% neq 0 (
    echo 错误：编译失败
    pause
    exit /b 1
)

echo 正在生成加密密钥文件...
java generate-encrypted-jwt-keys

if %errorlevel% neq 0 (
    echo 错误：生成加密密钥文件失败
    pause
    exit /b 1
)

echo 加密密钥文件生成完成！
echo 请检查 zoj-backend-common\src\main\resources\jwt\ 目录
echo 密钥文件已加密，需要密码才能解密使用

REM 清理临时文件
del generate-encrypted-jwt-keys.class

pause
