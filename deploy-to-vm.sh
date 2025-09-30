#!/bin/bash

# ZOJ 微服务系统 Windows → Linux 部署脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
VM_IP="192.168.213.128"  # 虚拟机IP地址
VM_USER="root"            # 虚拟机用户名
VM_PATH="/opt/zoj-backend-microservice"  # 虚拟机部署路径

# 打印带颜色的消息
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  ZOJ 微服务 Windows→Linux 部署${NC}"
    echo -e "${BLUE}================================${NC}"
}

# 检查环境
check_environment() {
    print_message "检查本地环境..."
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven 未安装，请先安装 Maven"
        exit 1
    fi
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        print_error "Java 未安装，请先安装 Java 17+"
        exit 1
    fi
    
    print_message "本地环境检查通过"
}

# 本地打包
build_locally() {
    print_message "开始本地打包..."
    
    # 清理并编译
    print_message "清理并编译项目..."
    mvn clean compile
    
    # 打包所有模块
    print_message "打包所有模块..."
    mvn clean package -DskipTests
    
    # 检查生成的jar文件
    print_message "检查生成的jar文件..."
    for module in zoj-backend-gateway zoj-backend-user-service zoj-backend-question-service zoj-backend-judge-service; do
        jar_file="$module/target/$module-0.0.1-SNAPSHOT.jar"
        if [ -f "$jar_file" ]; then
            print_message "✓ $jar_file 生成成功"
        else
            print_error "✗ $jar_file 生成失败"
            exit 1
        fi
    done
    
    print_message "本地打包完成"
}

# 传输文件到虚拟机
transfer_to_vm() {
    print_message "传输文件到虚拟机 $VM_IP..."
    
    # 检查SSH连接
    if ! ssh -o ConnectTimeout=5 $VM_USER@$VM_IP "echo 'SSH连接测试成功'" 2>/dev/null; then
        print_error "无法连接到虚拟机 $VM_IP，请检查："
        print_error "1. 虚拟机是否启动"
        print_error "2. SSH服务是否运行"
        print_error "3. IP地址是否正确"
        print_error "4. 是否已配置SSH密钥认证"
        exit 1
    fi
    
    # 创建目标目录
    ssh $VM_USER@$VM_IP "mkdir -p $VM_PATH"
    
    # 传输项目文件（排除target目录）
    print_message "传输项目源码..."
    rsync -av --exclude='target/' --exclude='.git/' --exclude='*.iml' ./ $VM_USER@$VM_IP:$VM_PATH/
    
    # 传输target目录
    print_message "传输编译后的jar文件..."
    rsync -av */target/ $VM_USER@$VM_IP:$VM_PATH/
    
    print_message "文件传输完成"
}

# 在虚拟机中构建Docker镜像
build_docker_images() {
    print_message "在虚拟机中构建Docker镜像..."
    
    ssh $VM_USER@$VM_IP << EOF
        cd $VM_PATH
        
        # 检查Docker是否安装
        if ! command -v docker &> /dev/null; then
            echo "安装Docker..."
            yum install -y yum-utils
            yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
            yum install -y docker-ce docker-ce-cli containerd.io
            systemctl start docker
            systemctl enable docker
        fi
        
        # 检查Docker Compose是否安装
        if ! command -v docker-compose &> /dev/null; then
            echo "安装Docker Compose..."
            curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-\$(uname -s)-\$(uname -m)" -o /usr/local/bin/docker-compose
            chmod +x /usr/local/bin/docker-compose
        fi
        
        # 构建镜像
        echo "构建Docker镜像..."
        docker-compose build
        
        echo "Docker镜像构建完成"
EOF
    
    print_message "Docker镜像构建完成"
}

# 启动服务
start_services() {
    print_message "启动服务..."
    
    ssh $VM_USER@$VM_IP << EOF
        cd $VM_PATH
        
        # 启动服务
        docker-compose up -d
        
        # 等待服务启动
        echo "等待服务启动..."
        sleep 30
        
        # 检查服务状态
        echo "检查服务状态..."
        docker-compose ps
EOF
    
    print_message "服务启动完成"
}

# 显示访问信息
show_access_info() {
    print_message "服务访问信息："
    echo -e "${BLUE}API Gateway:${NC} http://$VM_IP:8100"
    echo -e "${BLUE}API 文档:${NC} http://$VM_IP:8100/doc.html"
    echo -e "${BLUE}Nacos 控制台:${NC} http://$VM_IP:8848/nacos"
    echo -e "${BLUE}RabbitMQ 管理界面:${NC} http://$VM_IP:15672"
    echo ""
    print_message "默认账号信息："
    echo -e "${BLUE}Nacos:${NC} nacos/nacos"
    echo -e "${BLUE}RabbitMQ:${NC} itheima/123321"
    echo -e "${BLUE}MySQL:${NC} root/123"
}

# 检查服务状态
check_services() {
    print_message "检查服务状态..."
    
    ssh $VM_USER@$VM_IP << EOF
        cd $VM_PATH
        docker-compose ps
EOF
}

# 查看日志
view_logs() {
    local service=${1:-""}
    
    if [ -z "$service" ]; then
        print_message "查看所有服务日志..."
        ssh $VM_USER@$VM_IP "cd $VM_PATH && docker-compose logs -f"
    else
        print_message "查看 $service 服务日志..."
        ssh $VM_USER@$VM_IP "cd $VM_PATH && docker-compose logs -f $service"
    fi
}

# 停止服务
stop_services() {
    print_message "停止服务..."
    
    ssh $VM_USER@$VM_IP << EOF
        cd $VM_PATH
        docker-compose down
EOF
    
    print_message "服务已停止"
}

# 清理资源
cleanup() {
    print_message "清理Docker资源..."
    
    ssh $VM_USER@$VM_IP << EOF
        cd $VM_PATH
        docker-compose down -v
        docker system prune -f
EOF
    
    print_message "资源清理完成"
}

# 主函数
main() {
    print_header
    
    case "${1:-deploy}" in
        "deploy")
            check_environment
            build_locally
            transfer_to_vm
            build_docker_images
            start_services
            show_access_info
            ;;
        "build")
            check_environment
            build_locally
            ;;
        "transfer")
            transfer_to_vm
            ;;
        "start")
            start_services
            show_access_info
            ;;
        "stop")
            stop_services
            ;;
        "restart")
            stop_services
            sleep 5
            start_services
            show_access_info
            ;;
        "status")
            check_services
            ;;
        "logs")
            view_logs "$2"
            ;;
        "clean")
            cleanup
            ;;
        *)
            echo "用法: $0 {deploy|build|transfer|start|stop|restart|status|logs|clean}"
            echo ""
            echo "命令说明："
            echo "  deploy     - 完整部署流程（默认）"
            echo "  build      - 仅本地打包"
            echo "  transfer   - 仅传输文件到虚拟机"
            echo "  start      - 启动服务"
            echo "  stop       - 停止服务"
            echo "  restart    - 重启服务"
            echo "  status     - 检查服务状态"
            echo "  logs [service] - 查看日志"
            echo "  clean      - 清理Docker资源"
            echo ""
            echo "配置说明："
            echo "  虚拟机IP: $VM_IP"
            echo "  虚拟机用户: $VM_USER"
            echo "  部署路径: $VM_PATH"
            echo ""
            echo "使用前请确保："
            echo "1. 已配置SSH密钥认证"
            echo "2. 虚拟机已安装Docker和Docker Compose"
            echo "3. 虚拟机网络可访问"
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
