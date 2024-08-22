FROM openjdk:21-jdk-buster

WORKDIR /usr/lib/playlists

EXPOSE 8080 8888

# Provide correct values through the -e command line option with docker container run
ENV PL_DB_HOST="mariadb" \
    PL_USER_SECRET="pl_user" \
    PL_LIQUIBASE_SECRET="liquibase"

COPY playlist-server/build/libs/playlists.jar .

## Add healthcheck?

## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait
RUN chmod +x /wait

CMD /wait && java -jar playlists.jar