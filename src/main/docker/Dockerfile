FROM openjdk:8

ADD herman*.jar /herman.jar
#ADD log4j-1.2.17.jar /usr/local/openjdk-8/jre/lib/ext/log4j.jar

ADD *.sh /usr/bin/
RUN chmod -R 777 /usr/bin/*.sh

CMD ["deploy.sh"]