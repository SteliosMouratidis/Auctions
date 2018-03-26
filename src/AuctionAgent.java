import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class AuctionAgent {
	private String myname = null;
	private String hostname = null;
	private int registryport = 0;
	private MailboxInterface mailbox = null;

	/**
	 * @throws NotBoundException
	 * @throws RemoteException
	 * @throws MalformedURLException
	 * @throws InterruptedException
	 * 
	 */
	public AuctionAgent(String agentname, String hostname, int registryport, int numAgents, String filename)
			throws MalformedURLException, RemoteException, NotBoundException, InterruptedException {
		this.myname = agentname;
		this.hostname = hostname;
		this.registryport = registryport;

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
			//itemID, startingPrice, reservePrice, decrement, increment
			//ItemAuction[] items = new ItemAuction[4];
			
			ItemAuction[] items = getItems(filename);
			ArrayList<String> participants = new ArrayList<String>();
			for(int i = 1; i <= numAgents; i++) {
				participants.add("ag"+i);
			}

			dutchProtocol(myname, participants, items);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private ItemAuction[] getItems(String filename) throws IOException {
		File file_count = new File("./src/" + filename);
		FileReader fileReader_count = new FileReader(file_count);
		BufferedReader bufferedReader_count = new BufferedReader(fileReader_count);
		int i = 0;
		String line;
		while ((line = bufferedReader_count.readLine()) != null) {
			if(line.startsWith(myname)) {
				i+=1;
			}
		}
		ItemAuction[] itemArray = new ItemAuction[i];
		fileReader_count.close();
		
		File file = new File("./src/AuctioneerData.txt");
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		i = 0;
		while ((line = bufferedReader.readLine()) != null) {
			if(line.startsWith("auctioneer")) {  //if line is auctioneer item data
				String[] attr = line.split(",");
				itemArray[i] = new ItemAuction(attr[1], Integer.parseInt(attr[2]), 
						Integer.parseInt(attr[3]), Integer.parseInt(attr[4]), Integer.parseInt(attr[5]));
				System.out.println(attr[1] + Integer.parseInt(attr[2]) +
						Integer.parseInt(attr[3])+ Integer.parseInt(attr[4])+ Integer.parseInt(attr[5]));
				i += 1;
			}
		}
		fileReader.close();
		return itemArray;
	}
	

	private void dutchProtocol(String myID, ArrayList<String> participants, ItemAuction[] items)
			throws IOException, InterruptedException {
		// ===============================================
		// Console input:
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		Message message = null;
		String auctionType = null;
		Integer currentAskingPrice = null;
		ItemAuction currentItem = items[0];
		ArrayList<Bid> currentBids = new ArrayList<Bid>();
		int decrement = 5;

		System.out.print("Start Auction, press return ");
		//String command = in.readLine();

		currentAskingPrice = currentItem.getStartingPrice();
		// tell the agents the auction is starting
		for (String participant : participants) {
			message = new Message(Message.INFORM_START_OF_AUCTION, myID, participant, "dutch");
			mailbox.send(message);
			System.out.println("Inform Start of Dutch Auction");
		}

		// command = in.readLine(); //debug wait

		// tell them the starting price
		for (String participant : participants) {
			message = new Message(Message.INFORM_ASKING_PRICE, myID, participant, currentItem.getItemID(),
					currentItem.getStartingPrice());
			mailbox.send(message);
			System.out.println("Inform First Asking Price " + Integer.toString(currentItem.getStartingPrice()));
		}
		// wait for them to process the message and send something back
		TimeUnit.SECONDS.sleep(2);

		Boolean auction = true;
		while (auction) {
			ArrayList<Message> messages = new ArrayList<Message>(); // list of all messages

			// receive messages from all agents
			for (String participant : participants) {
				message = mailbox.receive(myID);
				if (message != null) {
					messages.add(message);
				}
			}

			Boolean onlyNoMessages = true;
			for (Message msg : messages) {
				if (msg.getMessageType() != 0) {
					onlyNoMessages = false;
				}
			}

			System.out.println("Size of messages " + messages.size());
			// for (Message msg : messages) {
			// System.out.println("mt: " + msg.getMessageType());
			// }

			if (onlyNoMessages) { // if there only no message messages
				currentAskingPrice = currentAskingPrice - decrement; // decrement price
				// if still above reserve, inform asking price
				if (currentAskingPrice >= currentItem.getReservePrice()) {
					for (String participant : participants) {
						message = new Message(Message.INFORM_ASKING_PRICE, myID, participant, currentItem.getItemID(),
								currentAskingPrice);
						mailbox.send(message);
						System.out.println("Inform Asking Price " + Integer.toString(currentAskingPrice));
					}
				}
				// else inform everyone they lose
				else {
					auction = false;
					for (String participant : participants) {
						message = new Message(Message.INFORM_LOSER, myname, participant, currentItem.getItemID());
						mailbox.send(message);
						System.out.println("Inform Loser " + participant);
					}
					// end auction
				}
				TimeUnit.SECONDS.sleep(2);
			}
			// if there is at least 1 message accepting
			else {
				ArrayList<String> accepting = new ArrayList<String>(); // list of ids of people accepting same bid
				// for each message, add people who accept this price to accepting list
				for (Message msg : messages) {
					// System.out.println("in loop type: " + msg.getMessageType());
					if (msg.getMessageType() == 7) {
						accepting.add(msg.getSender());
					}
					// else remove unnecessary messages
					// else {
					// messages.remove(msg);
					// }
				}
				// System.out.println(accepting.size());
				// System.out.println(accepting.get(0));

				if (accepting.size() == 0) { // no real bids in messages
					currentAskingPrice = currentAskingPrice - decrement;
					if (currentAskingPrice >= currentItem.getReservePrice()) {
						for (String participant : participants) {
							message = new Message(Message.INFORM_ASKING_PRICE, myID, participant,
									currentItem.getItemID(), currentAskingPrice);
							mailbox.send(message);
							System.out.println("Inform Asking Price " + Integer.toString(currentAskingPrice));
							TimeUnit.SECONDS.sleep(2);
						}
					} else {
						auction = false;
						for (String participant : participants) {
							message = new Message(Message.INFORM_LOSER, myID, participant, currentItem.getItemID());
							mailbox.send(message);
							System.out.println("Inform Loser " + participant);
						}
						// end auction
					}
				}
				// if there's a real bid, and only one -> winner
				else if (accepting.size() == 1) {
					auction = false;
					message = new Message(Message.INFORM_WINNER, myname, messages.get(0).getSender(),
							currentItem.getItemID());
					mailbox.send(message);
					System.out.println("Inform Winner " + messages.get(0).getSender());
					System.out.println("I made $" + (currentAskingPrice - currentItem.getReservePrice()) + " in profit");

					participants.remove(message.getReceiver()); // remove winner
					for (String participant : participants) {
						message = new Message(Message.INFORM_LOSER, myID, participant, currentItem.getItemID());
						mailbox.send(message);
						System.out.println("Inform Loser " + participant);
					}
					// end auction
				}
				// if more than one bid for accepting the price
				else if (accepting.size() > 1) {
					auction = false; // end dutch auuction
					ArrayList<String> englishAuctionParticipants = new ArrayList<String>();

					for (String participant : accepting) {
						message = new Message(Message.INFORM_START_OF_AUCTION, myID, participant, "english");
						mailbox.send(message);
						System.out.println("Inform Start of English Auction " + participant);
						englishAuctionParticipants.add(participant);
					}
					
					for (String participant : participants) {
						if(!(accepting.contains(participant))){  //if not accepting, inform loser
							message = new Message(Message.INFORM_LOSER, myID, participant, currentItem.getItemID());
							mailbox.send(message);
							System.out.println("Inform Loser " + participant);
						}
					}
					TimeUnit.SECONDS.sleep(2);
					// end auction, start english auction
					englishProtocol(myID, currentItem, englishAuctionParticipants, currentAskingPrice);
				}
			}
		}
		System.out.println(Integer.toString(mailbox.getNumberOfMessages()) + " Mesages were exchanged");
	}

	private void englishProtocol(String myID, ItemAuction currentItem, ArrayList<String> participants,
		Integer askingPrice) throws IOException, InterruptedException {
		int increment = 5;
		int currentAskingPrice = askingPrice + currentItem.increment;  //initial increment
		Bid currentBid = new Bid(null, currentAskingPrice);
		Message message = null;
		Boolean auction = true;

		// tell the agents still in the auction the asking price
		for (String participant : participants) {
			message = new Message(Message.INFORM_ASKING_PRICE, myID, participant, currentItem.getItemID(), currentBid.getBidPrice());
			mailbox.send(message);
			System.out.println("Inform First Asking Price " + Integer.toString(currentBid.getBidPrice()));
		}

		TimeUnit.SECONDS.sleep(2);

		while (auction) {
			ArrayList<Message> messages = new ArrayList<Message>();
			// receive messages from all agents
			for (String participant : participants) {
				message = mailbox.receive(myID);
				if (message != null) {
					messages.add(message);
				}
			}
			
			System.out.println("Size of messages " + messages.size());
			// if no one wants to bid, end the auction
			if (messages.isEmpty()) {
				auction = false; // end auction
				if (currentBid.getBidderID() == null) {
					// just choose the first message for the winner
					message = new Message(Message.INFORM_WINNER, myID, messages.get(0).getSender(),
							currentItem.getItemID());
					mailbox.send(message);
					System.out.println("Inform Winner " + messages.get(0).getSender());
					System.out.println("I made $" + (currentBid.getBidPrice() - currentItem.getReservePrice()) + " in profit");
					// tell everyone else they lost
					participants.remove(messages.get(0).getSender()); // remove guy who won
					for (String participant : participants) {
						message = new Message(Message.INFORM_LOSER, myID, participant, currentItem.getItemID());
						mailbox.send(message);
						System.out.println("Inform Loser " + participant);
					}
				}
				// if no one wants to bid, end the auction, still have a current bid
				else {
					message = new Message(Message.INFORM_WINNER, myID, currentBid.getBidderID(),
							currentItem.getItemID());
					mailbox.send(message);
					System.out.println("Inform Winner " + messages.get(0).getSender());
					System.out.println("I made $" + (currentBid.getBidPrice() - currentItem.getReservePrice()) + " in profit");
					
					for (String participant : participants) {
						message = new Message(Message.INFORM_LOSER, myID, participant, currentItem.getItemID());
						mailbox.send(message);
						System.out.println("Inform Loser " + participant);
					}
				}
			}
			// else someone wants to bid
			else {
				// only accept 1 bid
				
				// if message isnt a bid delete it
				Iterator<Message> mIT = messages.iterator();
				while(mIT.hasNext()) {
					Message m = mIT.next();
					if (m.getMessageType() != 8 && m.getMessageType() != 9) {
						mIT.remove();
					}
				}
				
				//else no bids, auction over
				if(messages.isEmpty()) {
					auction = false;
					message = new Message(Message.INFORM_WINNER, myID, currentBid.getBidderID(),
							currentItem.getItemID());
					mailbox.send(message);
					System.out.println("Inform Winner " + currentBid.getBidderID());
					System.out.println("I made $" + (currentBid.getBidPrice() - currentItem.getReservePrice()) + " in profit");
					
					for (String participant : participants) {
						if(!(participant.equals(currentBid.getBidderID()))) {
							message = new Message(Message.INFORM_LOSER, myID, participant, currentItem.getItemID());
							mailbox.send(message);
							System.out.println("Inform Loser " + participant);
						}
					}
					break;
				}
				
				
				// copy participants
				ArrayList<String> rejectRecipients = new ArrayList<String>(participants);

				Boolean higherBid = false;
				// if there's a bidding price (new higher bid)
				for (Message msg : messages) {
					if (msg.getMessageType() == 8) {
						// if its higher than the current bid
						if (msg.getBiddingPrice() > currentBid.getBidPrice()) {
							currentBid.setBidderID(msg.getSender());
							currentBid.setBidPrice(msg.getBiddingPrice());
							higherBid = true;
						}
					}
				}
				
				// if theres a highest bid
				if (higherBid) {
					System.out.println("New Bid is " + currentBid.getBidPrice());
					
					rejectRecipients.remove(currentBid.getBidderID()); // remove winner
					for (String agent : rejectRecipients) {
						message = new Message(Message.INFORM_REJECT, myID, agent, currentItem.getItemID());
						mailbox.send(message);
						System.out.println("Inform Reject " + agent);
					}
					
					//send accept message to winner of current bid
					message = new Message(Message.INFORM_ACCEPT, myID, currentBid.getBidderID(), currentItem.getItemID(), 
							currentBid.getBidPrice(), currentBid.getBidPrice() + increment);
					mailbox.send(message);
					System.out.println("Inform Accept " + currentBid.getBidderID() + " at " + currentBid.getBidPrice());
					
					//increment the bid
					currentBid.setBidPrice(currentBid.getBidPrice() + increment);
					System.out.println("Current Bid is " + currentBid.getBidPrice());
					
					for (String agent : rejectRecipients) {
						message = new Message(Message.INFORM_ASKING_PRICE, myID, agent, currentItem.getItemID(),
								currentBid.getBidPrice());
						mailbox.send(message);
						System.out.println("Inform Asking Price " + Integer.toString(currentBid.getBidPrice()) +
								" to " + agent);
					}
					TimeUnit.SECONDS.sleep(2);
				}
				
				// if there are only people bidding on current price
				else {
					// find the first message and give to him
					String winner = null;
					for (Message msg : messages) {
						if (msg.getMessageType() == 9) {
							winner = messages.get(0).getSender();
							currentBid.setBidPrice(currentBid.getBidPrice());
							currentBid.setBidderID(messages.get(0).getSender());
							break;
						}
					}
					rejectRecipients.remove(winner);
					for (String agent : rejectRecipients) {
						message = new Message(Message.INFORM_REJECT, myID, agent, currentItem.getItemID());
						mailbox.send(message);
						System.out.println("Inform Reject " + agent);
					}

					//send accept message to winner of current bid
					message = new Message(Message.INFORM_ACCEPT, myID, currentBid.getBidderID(), currentItem.getItemID(), 
							currentBid.getBidPrice(), currentBid.getBidPrice() + increment);
					mailbox.send(message);
					System.out.println("Inform Accept " + currentBid.getBidderID() + " at " + currentBid.getBidPrice());
					
					currentBid.setBidPrice(currentBid.getBidPrice() + increment);
					
					for (String agent : rejectRecipients) {
						message = new Message(Message.INFORM_ASKING_PRICE, myID, agent, currentItem.getItemID(),
								currentBid.getBidPrice());
						mailbox.send(message);
						System.out.println("Inform Asking Price " + Integer.toString(currentBid.getBidPrice()) + 
								" to " + agent);
					}
					TimeUnit.SECONDS.sleep(2);
				}
				TimeUnit.SECONDS.sleep(1);
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
		int numAgents = Integer.parseInt(args[3]);
		String filename = args[4];
		try {
			// instantiate an agent from this class

			new AuctionAgent(agentname, hostname, registryport, numAgents, filename);

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

	}

}