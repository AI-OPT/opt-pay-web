# Pull base image
FROM 10.19.13.36:5000/tomcat:8.x-GMT
MAINTAINER mayt<mayt@asiainfo.com>

# Install tomcat7
RUN rm -rf /opt/tomcat/webapps/* && mkdir /opt/tomcat/webapps/ROOT

# 如门户的为portal-web.war
COPY ./build/libs/opt-pay.war /opt/tomcat/webapps/opt-pay/opt-pay.war
RUN cd /opt/tomcat/webapps/opt-pay && jar -xf opt-pay.war && rm -rf /opt/tomcat/webapps/opt-pay/opt-pay.war

ADD ./script/start-web.sh /start-web.sh
RUN chmod 755 /*.sh

#拷贝证书
COPY ./assets /assets

# Define default command.
#CMD ["/start-web.sh"]