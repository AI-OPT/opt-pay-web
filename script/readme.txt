1.编译打包
#一定要先clean
gradle clean
#然后再打包
gradle build -x test

2.生成镜像
#在本地生成带私服前缀的镜像  (每次打镜像前版本号要更新)
docker build -t 10.19.13.20:5000/opt-pay-web:v1.0 .
#将镜像推送到私服
docker push 10.19.13.20:5000/opt-pay-web:v1.0

3. 运行镜像
#--net=host  表示为主机(host)模式  去掉该配置，默认为桥接(bridge)模式
#-e 代表需要设置的环境变量
docker run -d --name opt-pay-web  -p 14110:8080  \
# 0:ipaas模式；1:sdk模式
-e "SDK_MODE=0" \
#ipaas地址
-e "PAAS_AUTH_URL=http://10.1.245.4:19811/service-portal-uac-web/service/auth" \
#ipaas用户pid
-e "PAAS_AUTH_PID=D14F7D708109471AB6F3084B2ABAE9A6" \
#ipaas配置中心id
-e "PAAS_CCS_ID=CCS004" \
#ipaas配置中心密码
-e "PAAS_CCS_PWD=123456" \
#服务注册中心zk地址
-e "REST_REGISTRY_ADDR=10.19.13.13:29181"  \
#验签证书目录
-e "VALIDATE_CERT_DIR=/assets/tst/" \
#敏感信息加密证书路径
-e "ENCRYPT_CERT_PATH=/certs/tst/acp_test_enc.cer" \
10.19.13.20:5000/opt-pay-web:v1.0_2

#查看镜像启动日志
docker logs -f opt-pay-web
#进入容器，查看镜像内部的情况
docker exec -it opt-pay-web /bin/bash
#删除运行的容器
docker rm -fv opt-pay-web

#=============更新日志========================#
*2016-09-23
1）初始打包