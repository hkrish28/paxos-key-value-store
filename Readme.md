# Project 2

#### Running the Server and Client (Docker)
* Execute `deploy.sh` to run the server application as a docker container and set up a docker network
* Execute `run_client.sh` to run a client that connects to the server. Examples to run the code is as below:
  * ./run_client.sh client-name (Runs the client interactively)
  * ./run_client.sh client-name-2 (Runs the second client interactively)


#### Scripting

* The script `deploy.sh` should help you build and deploy server images along with a custom network

* The script `run_client.sh` script should help you start a client container on the same network

Note: Do not forget to change the permission of sh files to executable `chmod +x *.sh`

#### Running locally

* rmiregistry -J-Djava.class.path=bin &
* Compile the code using `javac -d bin src/client/*.java src/server/*.java src/shared/*.java`
* server usage should then be similar to `java -cp bin server.ServerApp`
* client usage should then be similar to `java -cp bin client.ClientApp`



#### When the client begins
* The client begins by running pre-population script which is present in the ClientInput.txt
* The client then prompts for user input of command
* User can exit the client application by inputting `exit`
* Commands are to be inputted as <b>[get | put | delete]</b>  <param_1> <i><param_2_for_put></i>

#### Troubleshooting
* If there is an issue with starting the server due to the rmiregistry instantiation:
  * Run `lsof -i :1099` to see if there is already a process listening to the port that rmiregistry uses, i.e, 1099
  * Use `kill <pid>` to kill the process obtained above and re-run the application 