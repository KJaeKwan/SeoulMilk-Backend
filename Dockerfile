FROM openjdk:17
WORKDIR /app

# 환경 변수 설정 (Docker Build 시점에 전달 가능)
ARG DB_USERNAME
ARG DB_PASSWORD
ARG DB_URL

# 환경 변수 값을 Docker 컨테이너 실행 시에도 사용할 수 있도록 설정
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV DB_URL=${DB_URL}

COPY build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
