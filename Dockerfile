# Pull base image
FROM 10.19.13.18:5000/tomcat:7-GMT
MAINTAINER mayt<mayt@asiainfo.com>

# Install tomcat7
RUN rm -rf /opt/apache-tomcat-7.0.72/webapps/* && mkdir /opt/apache-tomcat-7.0.72/webapps/ROOT

# 如门户的为portal-web.war
COPY ./build/libs/opt-pay.war /opt/apache-tomcat-7.0.72/webapps/ROOT/ROOT.war
RUN cd /opt/apache-tomcat-7.0.72/webapps/ROOT && jar -xf ROOT.war && rm -rf /opt/apache-tomcat-7.0.72/webapps/ROOT.war

ADD ./script/start-web.sh /start-web.sh
RUN chmod 755 /*.sh

#拷贝证书
COPY ./assets /assets

# Define default command.
CMD ["/start-web.sh"]