import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public class Agent {
	private String myname = null;
	private String hostname = null;
	private int registryport = 0;
	private MailboxInterface mailbox = null;
	private int moneySaved;

	/**
	 * @throws NotBoundException
	 * @throws RemoteException
	 * @throws MalformedURLException
	 * @throws InterruptedException 
	 * 
	 */
	public Agent(String agentname, String hostname, int registryport, String filename)
			throws MalformedURLException, RemoteException, NotBoundException, InterruptedException {
		this.myname = agentname;
		this.hostname = hostname;
		this.registryport = registryport;
		this.moneySaved = 0;

		// Obtain the service reference from the RMI registry
		// listening at hostname:registryport.

		String regURL = "rmi://" + hostname + ":" + registryport + "/Mailbox";
		System.out.println("Looking up " + regURL);

		// ===============================================
		// lookup the remote service, it will return a reference to the stub:

		mailbox = (MailboxInterface) Naming.lookup(regURL);

		System.out.println("Agent " + myname + " connected to mailbox.");

		// new Thread ( receiver = new Receiver( myname, mailbox ) ).start(); ;

		try {
			String auctioneerID = "auctioneer";
			
			//ItemAgent[] items = new ItemAgent[4];
			//itemID, acceptPrice, maxPrice, increment
			
			ItemAgent[] items = getItems(myname, filename);
			
			dutchProtocol(auctioneerID, myname, items);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private ItemAgent[] getItems(String myname, String filename) throws IOException {
		File file_count = new File("./src/" + filename);
		FileReader fileReader_count = new FileReader(file_count);
		BufferedReader bufferedReader_count = new BufferedReader(fileReader_count);
		int i = 0;
		String line;
		while ((line = bufferedReader_count.readLine()) != null) {
			if(line.startsWith(myname+",")) {
				i+=1;
			}
		}
		System.out.println(i);
		ItemAgent[] itemArray = new ItemAgent[i];
		fileReader_count.close();
		
		File file = new File("./src/" + filename);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		i = 0;
		System.out.println("These are the goods I want to buy");
		while ((line = bufferedReader.readLine()) != null) {
			if(line.startsWith(myname+",")) {  //if line is agent item data
				//System.out.println(line);
				String[] attr = line.split(",");
				itemArray[i] = new ItemAgent(attr[1], Integer.parseInt(attr[2]), 
						Integer.parseInt(attr[3]), Integer.parseInt(attr[4]));
				System.out.println(attr[1] + " " + Integer.parseInt(attr[2]) + " " +
						Integer.parseInt(attr[3])+ " " + Integer.parseInt(attr[4]));
				i += 1;
			}
		}
		fileReader.close();
		return itemArray;
	}
	
	

	private void dutchProtocol(String auctionID, String myID, ItemAgent[] items) throws IOException, InterruptedException {
		// ===============================================
		// Console input:
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		//Message message = null;
		//String auctionType = null;
		//Integer currentAskingPrice = null;
		//String currentItem = null;

		while(true) {
			Message message = null;
			String auctionType = null;
			Integer currentAskingPrice = null;
			String currentItem = null;
			ItemAgent currentItemStats = null;
			Boolean auction = true;
		
			//wait for a start auction message
			while (true) {
				message = mailbox.receive(myname);
				if(message != null) {
					if (message.getMessageType() == 1) { // start auction
						auctionType = message.getAuctionType();
						System.out.println("Start " + auctionType + " auction");
						
						//wait for first asking price / item stats
						while (true) {
							message = mailbox.receive(myname);
							if(message != null) {
								if (message.getMessageType() == 2) {  //asking price
									currentAskingPrice = message.getAskingPrice();
									currentItem = message.getItemID();
									break;
								}
							}
						}
						
						System.out.println("Item: " + currentItem);
						
						for (ItemAgent item : items) {
							System.out.println(item.itemID);
							if (item.itemID.equals(currentItem)) {
								currentItemStats = item;
								break;
							}
						}
						//if i match item, break
						if(currentItemStats != null){
							System.out.println("I want to bid on this item");
							break;
						}
					}
				}
			}
			
			System.out.println("Current Asking Price: " + currentAskingPrice);
			
			while (auction) {
				//if asking price is below what i will bid on, then propose to accept
				if (currentAskingPrice <= currentItemStats.getAcceptBidPrice()) {
					message = new Message(Message.PROPOSE_ACCEPT_PRICE, myname, auctionID, currentItem);
					mailbox.send(message);
					System.out.println("At or below my price, send accept");
				} 
				//if above my price send no message
				else if (currentAskingPrice > currentItemStats.getAcceptBidPrice()) {
					message = new Message(Message.NO_MESSAGE, myname, auctionID, currentItem);
					mailbox.send(message);
					System.out.println("Above my price, send no message");
				}
				
				System.out.println("Going to wait for a message from the auctioneer");
				//wait for the next message
				//TimeUnit.SECONDS.sleep(2);
				while ((message = mailbox.receive(myname)) == null);
				System.out.println("Found Message " + message.getMessageType());
				
				if (message.getMessageType() == 6) { // inform loser
					auction = false;  //stop the auction
					System.out.println("I Lost");
				} else if (message.getMessageType() == 5) { // inform winner
					auction = false;  //stop the auction
					moneySaved = currentItemStats.getMaxPrice() - message.getSalePrice();
					System.out.println("I WON!!!!!!");
					System.out.println("I Saved $" + moneySaved);
				} else if (message.getMessageType() == 2) { // inform asking price
					//new asking price
					currentAskingPrice = message.getAskingPrice();
					currentItem = message.getItemID();
					System.out.println(currentAskingPrice);
				} else if (message.getMessageType() == 1) { // inform start of auction
					auction = false;  //stop this auction, time for english auction
					englishProtocol(auctionID, myID, currentAskingPrice, currentItemStats);
				}
				
			}
		}
	}

	private void englishProtocol(String auctionID, String myID, Integer askingPrice, ItemAgent currentItemStats)
			throws IOException {
		int currentAskingPrice = askingPrice;
		String currentItem = currentItemStats.getItemID();
		Message message = null;
		Boolean auction = true;
		
		//wait for first asking price
		while (true) {
			message = mailbox.receive(myname);
			if(message != null) {
				if (message.getMessageType() == 2) {  //asking price
					currentAskingPrice = message.getAskingPrice();
					currentItem = message.getItemID();
					break;
				}
			}
		}
		while (auction) {
			//if asking price is below my max, then bid
			System.out.println("currentAskingPrice: " + currentAskingPrice + "  MaxPrice: " + currentItemStats.getMaxPrice());
			if (currentAskingPrice <= currentItemStats.getMaxPrice()) {
				//if i can go above the bid, do it
				if (currentAskingPrice + currentItemStats.getIncrement() < currentItemStats.getMaxPrice()) {
					int nextBidPrice = currentAskingPrice + currentItemStats.getIncrement();
					message = new Message(Message.PROPOSE_BIDDING_PRICE, myname, auctionID,	currentItemStats.getItemID(), nextBidPrice);
					mailbox.send(message);
					System.out.println("Below my price, propose bid higher");
				} else {
					//or just bid on the current price
					message = new Message(Message.PROPOSE_BID_ON_PRICE, myname, auctionID, currentItemStats.getItemID());
					mailbox.send(message);
					System.out.println("At my price, propose bid on current price");
				}
			} 
			//else send nothing
			else if (currentAskingPrice > currentItemStats.getMaxPrice()) {
				message = new Message(Message.NO_MESSAGE, myname, auctionID, "");
				mailbox.send(message);
			}

			//wait for a response
			while ((message = mailbox.receive(myname)) == null);
			System.out.println("Found Message " + message.getMessageType());

			//if loser
			if (message.getMessageType() == 6) {
				auction = false;
				System.out.println("I Lost");
				// auction is over for this agent
			} 
			//if winner
			else if (message.getMessageType() == 5) {
				auction = false;
				moneySaved = currentItemStats.getMaxPrice() - message.getSalePrice();
				System.out.println("I WON!!!!!!");
				System.out.println("I Saved $" + moneySaved);
				// auction is over for this agent
			} 
			//if my proposal was rejected
			else if (message.getMessageType() == 4) {
				while ((message = mailbox.receive(myname)) == null);
				if(message.getAskingPrice() != null && message.getItemID() != null) {
					currentAskingPrice = message.getAskingPrice();
					currentItem = message.getItemID();
				}
				System.out.println("My proposal was rejected");
			} 
			//if my proposal was accepted
			else if (message.getMessageType() == 3) {
				int myBid = message.getOriginalBiddingPrice();
				int nextBid = message.getBiddingPrice();
				System.out.println("My proposal was accepted at " + myBid);
				System.out.println("Skipping next round, waiting for winner or asking price");
				while (true) {
					//wait for next message/skip a round
					while ((message = mailbox.receive(myname)) == null);
					//if winner
					if (message.getMessageType() == 5) {
						auction = false;
						System.out.println("I WON!!!!!!");
						moneySaved = currentItemStats.getMaxPrice() - myBid;
						System.out.println("I Saved $" + moneySaved);
						// auction is over for this agent
						break;
					} 
					//else if someone outbid me
					else if (message.getMessageType() == 2) {
						currentAskingPrice = message.getAskingPrice();
						currentItem = message.getItemID();
						//if new incremented bid is higher than my bid incremented
						if(nextBid < currentAskingPrice) {
							System.out.println("Someone outbid me");
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {

		// Specify the security policy and set the security manager.
		System.setProperty("java.security.policy", "security.policy");
		System.setSecurityManager(new SecurityManager());

		String agentname = args[0];
		String hostname = args[1];
		int registryport = Integer.parseInt(args[2]);
		String filename = args[3];

		try {
			// instantiate an agent from this class

			new Agent(agentname, hostname, registryport, filename);

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

	}

}