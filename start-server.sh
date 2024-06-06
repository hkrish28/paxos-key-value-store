#!/bin/sh

# Start the RMI registry in the background
rmiregistry -J-Djava.class.path=bin &

# Capture the PID of the rmiregistry process
RMIREGISTRY_PID=$!

sleep 1
echo "registry process started"

# Start the server application
java -Djava.security.policy=server.policy -cp bin server.ServerApp

# Wait for the server application to terminate
wait $!

# Stop the RMI registry when the server application terminates
kill $RMIREGISTRY_PID

echo "registry process killed"
