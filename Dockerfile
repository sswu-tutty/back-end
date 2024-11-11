# JDK17 기반 이미지 사용
FROM openjdk:17

# JAR 파일 경로 인자 설정
ARG JAR_FILE=build/libs/*.jar

# JAR 파일을 이미지 내 app.jar로 복사
COPY ${JAR_FILE} app.jar

# 실행 명령어 설정
ENTRYPOINT ["java", "-jar", "app.jar"]
