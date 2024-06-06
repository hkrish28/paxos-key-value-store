FROM bellsoft/liberica-openjdk-alpine-musl:11 AS client-build
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac -d bin src/shared/*.java src/client/*.java src/server/*.java


FROM bellsoft/liberica-openjdk-alpine-musl:11 AS server-build
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac -d bin src/server/*.java src/shared/*.java
RUN chmod +x *.sh
# cmd to run server locally - java server.ServerApp 1111 5555
#CMD ["java", "-cp", "bin", "server.ServerApp", "1111", "5555"]
#CMD ["sh", "-c", "rmiregistry -J-Djava.class.path=bin & java -cp bin server.ServerApp"]
CMD ["./start-server.sh"]