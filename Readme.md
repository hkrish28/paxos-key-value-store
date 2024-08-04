# Project 4


#### Running locally

* Compile the code using `javac -d bin src/client/*.java src/server/*.java src/shared/*.java`
* server usage should then be similar to `java -cp bin server.ServerApp`
* client usage should then be similar to `java -cp bin client.ClientApp`
* You can optionally pass a command line argument to connect to a particular server port or do it through
* command line after client starts up



#### When the client begins
* The client begins by running pre-population script which is present in the ClientInput.txt
* The client then prompts for user input of command  or `diconnect` to disconnect from the currently
  connected server. Client can then enter a different port to connect to another server 
* Commands are to be inputted as <b>[get | put | delete]</b>  <param_1> <i><param_2_for_put></i>
* Other supported commands are: disconnect and exit


### Graceful shutdown of server/client
* Input `exit` to shutdown the server/client resources