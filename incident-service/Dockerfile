FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=default
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
