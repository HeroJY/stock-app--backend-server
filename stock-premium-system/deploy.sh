#!/bin/bash

# 股票溢价率监控系统部署脚本
# 使用方法: ./deploy.sh [环境] [操作]
# 环境: dev|test|prod
# 操作: build|start|stop|restart|logs

set -e

# 默认参数
ENVIRONMENT=${1:-prod}
ACTION=${2:-restart}
PROJECT_NAME="stock-premium-system"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker和Docker Compose
check_requirements() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
}

# 构建应用
build_app() {
    log_info "开始构建应用..."
    
    # Maven打包
    log_info "Maven打包中..."
    mvn clean package -DskipTests -Pprod
    
    if [ $? -ne 0 ]; then
        log_error "Maven打包失败"
        exit 1
    fi
    
    # Docker构建
    log_info "Docker镜像构建中..."
    docker-compose build
    
    if [ $? -ne 0 ]; then
        log_error "Docker镜像构建失败"
        exit 1
    fi
    
    log_info "应用构建完成"
}

# 启动服务
start_services() {
    log_info "启动服务..."
    docker-compose up -d
    
    if [ $? -eq 0 ]; then
        log_info "服务启动成功"
        log_info "应用地址: http://localhost:8080/api"
        log_info "API文档: http://localhost:8080/api/swagger-ui/"
        log_info "健康检查: http://localhost/health"
    else
        log_error "服务启动失败"
        exit 1
    fi
}

# 停止服务
stop_services() {
    log_info "停止服务..."
    docker-compose down
    log_info "服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启服务..."
    stop_services
    start_services
}

# 查看日志
show_logs() {
    log_info "查看应用日志..."
    docker-compose logs -f app
}

# 检查服务状态
check_status() {
    log_info "检查服务状态..."
    docker-compose ps
    
    # 健康检查
    log_info "执行健康检查..."
    sleep 10
    
    if curl -f http://localhost/health > /dev/null 2>&1; then
        log_info "健康检查通过 ✓"
    else
        log_warn "健康检查失败，请检查服务状态"
    fi
}

# 清理资源
cleanup() {
    log_info "清理Docker资源..."
    docker-compose down -v --remove-orphans
    docker system prune -f
    log_info "清理完成"
}

# 主函数
main() {
    log_info "股票溢价率监控系统部署脚本"
    log_info "环境: $ENVIRONMENT"
    log_info "操作: $ACTION"
    
    check_requirements
    
    case $ACTION in
        build)
            build_app
            ;;
        start)
            start_services
            check_status
            ;;
        stop)
            stop_services
            ;;
        restart)
            build_app
            restart_services
            check_status
            ;;
        logs)
            show_logs
            ;;
        status)
            check_status
            ;;
        cleanup)
            cleanup
            ;;
        *)
            log_error "未知操作: $ACTION"
            echo "支持的操作: build|start|stop|restart|logs|status|cleanup"
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"