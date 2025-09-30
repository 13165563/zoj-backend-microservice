# ZOJ 微服务部署指南

## 📋 **文件说明**

### **核心文件**
- `docker-compose.yml` - Docker编排文件，定义所有服务
- `Dockerfile.gateway` - API网关Docker镜像构建文件
- `Dockerfile.user-service` - 用户服务Docker镜像构建文件  
- `Dockerfile.question-service` - 题目服务Docker镜像构建文件
- `Dockerfile.judge-service` - 判题服务Docker镜像构建文件
- `env.example` - 环境变量配置示例

### **部署脚本**
- `deploy-to-vm.bat` - Windows自动部署脚本
- `deploy-to-vm.sh` - Linux/Mac自动部署脚本

### **JWT密钥生成**
- `generate-encrypted-jwt-keys.bat` - Windows JWT密钥生成脚本
- `generate-encrypted-jwt-keys.java` - JWT密钥生成Java程序

## 🚀 **快速部署**

### **方案一：自动部署（推荐）**

#### Windows用户：
```cmd
# 完整部署（一键完成）
deploy-to-vm.bat deploy

# 仅本地打包
deploy-to-vm.bat build

# 查看服务状态
deploy-to-vm.bat status
```

#### Linux/Mac用户：
```bash
# 给脚本添加执行权限
chmod +x deploy-to-vm.sh

# 完整部署
./deploy-to-vm.sh deploy

# 仅本地打包
./deploy-to-vm.sh build
```

### **方案二：手动部署**

#### 1. **Windows IDEA中打包**
```bash
# 在项目根目录执行
mvn clean package -DskipTests
```

#### 2. **传输到Linux虚拟机**
```bash
# 使用SCP传输
scp -r zoj-backend-microservice root@192.168.213.128:/opt/

# 或使用WinSCP等图形化工具
```

#### 3. **虚拟机中启动**
```bash
cd /opt/zoj-backend-microservice
docker-compose build
docker-compose up -d
```

## 📦 **打包说明**

### **Maven多模块项目**
- ✅ **整体打包** - `mvn clean package -DskipTests` 会打包所有4个服务模块
- ❌ **公共模块不单独打包** - `common`、`model`、`service-client` 会被其他模块自动包含

### **生成的jar文件**
- `zoj-backend-gateway/target/zoj-backend-gateway-0.0.1-SNAPSHOT.jar`
- `zoj-backend-user-service/target/zoj-backend-user-service-0.0.1-SNAPSHOT.jar`
- `zoj-backend-question-service/target/zoj-backend-question-service-0.0.1-SNAPSHOT.jar`
- `zoj-backend-judge-service/target/zoj-backend-judge-service-0.0.1-SNAPSHOT.jar`

## 🐳 **Docker说明**

### **Dockerfile策略**
- 使用预构建的jar文件（本地打包）
- 基于 `openjdk:17-jre-slim` 镜像
- 不包含Maven构建过程，构建速度更快

### **docker-compose.yml**
- 定义8个服务：MySQL、Redis、RabbitMQ、Nacos + 4个微服务
- 自动健康检查和依赖管理
- 网络隔离和数据持久化

## 🔧 **服务访问**

### **端口分配**
- **API Gateway**: 8100
- **User Service**: 8101  
- **Question Service**: 8102
- **Judge Service**: 8103
- **MySQL**: 3306
- **Redis**: 6379
- **RabbitMQ**: 5672
- **Nacos**: 8848

### **访问地址**
- **API Gateway**: http://192.168.213.128:8100
- **API 文档**: http://192.168.213.128:8100/doc.html
- **Nacos 控制台**: http://192.168.213.128:8848/nacos
- **RabbitMQ 管理**: http://192.168.213.128:15672

### **默认账号**
- **MySQL**: root/123
- **RabbitMQ**: itheima/123321
- **Nacos**: nacos/nacos

## 🛠️ **常用命令**

### **Docker命令**
```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f zoj-gateway

# 重启服务
docker-compose restart zoj-gateway

# 停止服务
docker-compose down

# 清理资源
docker-compose down -v
```

### **部署脚本命令**
```bash
# Windows
deploy-to-vm.bat {deploy|build|transfer|start|stop|restart|status|clean}

# Linux/Mac  
./deploy-to-vm.sh {deploy|build|transfer|start|stop|restart|status|clean}
```

## 🔑 **JWT密钥生成**

如果需要生成新的JWT密钥：

```cmd
# Windows
generate-encrypted-jwt-keys.bat
```

## 📝 **配置修改**

### **修改虚拟机IP**
编辑 `deploy-to-vm.bat` 或 `deploy-to-vm.sh`：
```bash
VM_IP="你的虚拟机IP"
```

### **修改环境变量**
复制 `env.example` 为 `.env` 并修改相应配置。

## 🎯 **总结**

**最简单的部署方式：**
1. 运行 `deploy-to-vm.bat deploy`（Windows）或 `./deploy-to-vm.sh deploy`（Linux/Mac）
2. 等待部署完成
3. 访问 http://虚拟机IP:8100 使用系统

**核心理解：**
- ✅ 整体Maven打包会生成4个服务jar文件
- ✅ 公共模块不需要单独打包
- ✅ Docker使用预构建jar文件，构建更快
- ✅ 一个docker-compose.yml管理所有服务
