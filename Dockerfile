FROM maven:3.8.7-openjdk-18
EXPOSE 8080

RUN mkdir -p /authlete/app

ADD . /authlete/app

WORKDIR /authlete/app

RUN mvn -s /usr/share/maven/ref/settings-docker.xml clean install

CMD ["mvn", "-s", "/usr/share/maven/ref/settings-docker.xml", "clean", "spring-boot:run"]
