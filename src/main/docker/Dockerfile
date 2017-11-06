FROM openjdk:8
VOLUME /tmp
ADD deploymentpipeline.jar app.jar
VOLUME /etc
ADD /etc/ /etc/
ADD startService.sh /startService.sh
RUN chmod 700 /startService.sh
ENTRYPOINT ./startService.sh