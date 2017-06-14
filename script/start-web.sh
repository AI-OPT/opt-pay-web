#!/bin/sh
#env
APP_NAME="opt-pay-web"

#set base home
RESOURCES_HOME=${CATALINA_HOME}/webapps/opt-pay/WEB-INF/classes

#change config
pushd ${RESOURCES_HOME}
sed -i "s%paas.auth.url=.*%paas.auth.url=${PAAS_AUTH_URL}%g" ./paas/paas-conf.properties
sed -i "s%paas.auth.pid=.*%paas.auth.pid=${PAAS_AUTH_PID}%g" ./paas/paas-conf.properties
sed -i "s%paas.ccs.serviceid=.*%paas.ccs.serviceid=${PAAS_CCS_ID}%g" ./paas/paas-conf.properties
sed -i "s%paas.ccs.servicepassword=.*%paas.ccs.servicepassword=${PAAS_CCS_PWD}%g" ./paas/paas-conf.properties
sed -i "s%paas.sdk.mode=.*%paas.sdk.mode=${SDK_MODE}%g" ./paas/paas-conf.properties
sed -i "s%ccs.appname=.*%ccs.appname=${CCS_NAME}%g" ./paas/paas-conf.properties
sed -i "s%ccs.zk_address=.*%ccs.zk_address=${ZK_ADDR}%g" ./paas/paas-conf.properties

# 各中心要根据情况自己修改成与dubbo.properties中对应的配置项
sed -i "s%dubbo.registry.address=.*%dubbo.registry.address=${REST_REGISTRY_ADDR}%g" ./dubbo/dubbo.properties

#替换证书目录
sed -i "s%acpsdk.validateCert.dir=.*%acpsdk.validateCert.dir=${VALIDATE_CERT_DIR}%g" ./acp_sdk.properties
sed -i "s%acpsdk.encryptCert.path=.*%acpsdk.encryptCert.path=${ENCRYPT_CERT_PATH}%g" ./acp_sdk.properties
popd


nohup ${CATALINA_HOME}/bin/catalina.sh run >> /${APP_NAME}.log 