# Multi Agent Auction Simulation
### Setup Instructions:
1. Install java JDK
2. Unzip the files to a local directory

#### Ubuntu Linux

3. Navigate to the root folder of the project in a terminal window

4. Run the following command, [number of agents] is an integer value
```
./testRun.sh [number of agents]
```
  
  
5. When all windows have opened, press Enter to start the auction


Note:
The following code in the testRun.sh file opens new terminal windows and pauses them on completion of the command to run the java file.
This can be taken out to make terminals close on completion.
```
bash -c \" ... exec bash\"
```


#### Windows

3. Navigate to the root folder in a cmd window

4. Run the RMI registry: 
```
rmiregistry 50010
```


5. Run the Mailbox: 
```
Java -classpath ./src Mailbox 50010 50011
```


6. Run the Agents: 
```
Java -classpath ./src Agent [agentName (agX, X = integer)] localhost 50010
```


7. Run the Auctioneer:
```
Java -classpath ./src AuctionAgent auctioneer  localhost 50010 [Number of agents (integer)]
```
