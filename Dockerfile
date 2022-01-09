FROM openjdk:8

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ADD wolkezeug*.jar /wolkezeug.jar

ENTRYPOINT ["/entrypoint.sh"]