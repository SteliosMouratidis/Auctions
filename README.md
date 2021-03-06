# Multi Agent Auction Simulation

### Description
Java implementation of a multi-agent auction simulation. Using the Java RMI interface, the agents representing the auctioneer and the bidders communicate through messages sent to and from a simulated mailbox using RMI. Each item the auctioneer wants to sell triggers a dutch auction, where the asking price is decreased until a bidder wishes to buy the item at the specified price. If more than one bidder bids at a specific price, this triggers an english auction where the bids increase until there are no new bids.

The entire system is run using a bash script, specified for running in Ubuntu Linux. This script is very simple, as it opens new terminal windows for each component and agent necessary to run the auction. It starts the RMI registry, mailbox, buyer agents, and auctioneer agent in that order, each with separate processes that can be left open to view output or closed on completion of their processes. When everything is ready, the user must press enter on the auctioneer’s terminal window to start the auction. Each agent prints out statements that explain what it is doing during the whole auction process.

### Youtube Video Demo
[![Auction Demo](https://img.youtube.com/vi/4X99rUrgx5Y/maxresdefault.jpg)](https://youtu.be/4X99rUrgx5Y "Multi-Agent Auction")

### Setup Instructions:
1. Install java JDK
2. Download the repo and unzip the files to a local directory

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
