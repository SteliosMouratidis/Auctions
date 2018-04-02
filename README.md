# Multi Agent Auction Simulation
### Setup Instructions:
1. Install java JDK
2. Unzip the files to a local directory

#### Ubuntu Linux

3. Navigate to the root folder of the project in a terminal window

4. Run the following command, [number of agents] is an integer value
```
./testRun.sh [number of agents] [Agent data file name (AgentData.txt)] [Auctioneer data file name (AuctioneerData.txt)]
```
  
  
5. When all windows have opened, press Enter to start the auction


Note:
The terminals can close upon completion of their processes e.g. when an agent loses or wins an auction.
The following code in the testRun.sh file opens new terminal windows and pauses them on completion of the command to run the java file.
```
bash -c \" ... exec bash\"
```

This line of code keeps the windows open
```
gnome-terminal -e "bash -c \"java -classpath ./src Agent ag$i localhost 50010 $2; exec bash\""
```

While this line closes them on completion
```
gnome-terminal -e "java -classpath ./src Agent ag$i localhost 50010 $2"
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
Java -classpath ./src Agent [agentName (agX, X = integer)] localhost 50010 [data file name (AgentData.txt)]
```


7. Run the Auctioneer:
```
Java -classpath ./src AuctionAgent auctioneer  localhost 50010 [Number of agents (integer)] [data file name (AuctioneerData.txt)]
```
