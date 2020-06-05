FROM openjdk:8-jre-alpine
# copy WAR into image
COPY docker/sorting-project.war /app.war
# run application with this command line
CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=default", "/app.war"]