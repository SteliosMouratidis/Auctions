#!/bin/bash
# A sample Bash script

#echo Hello World!
path=$(pwd)

path="$path/src/"
#echo $path

path1=$path
path1+='*.java'

#echo $path1
javac $path1
echo "Done Compiling"

echo "Starting rmiregistry 50010"
x-terminal-emulator -e "bash -c \"cd src; pwd; rmiregistry 50010\""

sleep .5
echo "Starting Mailbox"
x-terminal-emulator -e "java -classpath ./src Mailbox 50010 50011"

sleep .5
echo "Starting Auctioneer"
gnome-terminal -e "bash -c \"java -classpath ./src AuctionAgent auctioneer dom-VirtualBox 50010; exec bash\""

echo "Starting ag1"
gnome-terminal -e "bash -c \"java -classpath ./src Agent ag1 dom-VirtualBox 50010; exec bash\""

echo "Starting ag2"
gnome-terminal -e "bash -c \"java -classpath ./src Agent ag2 dom-VirtualBox 50010; exec bash\""

#echo "Starting ag2"
#gnome-terminal -e "bash -c \"java -classpath ./src Agent ag3 dom-VirtualBox 50010; exec bash\""
