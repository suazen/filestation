# From java image, version : 8
FROM java:8

# 挂载app目录
VOLUME /app

# COPY or ADD to image
ADD target/filestation.jar app.jar

RUN bash -c "touch /app.jar"
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]