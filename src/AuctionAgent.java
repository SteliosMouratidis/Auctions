import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
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
	public AuctionAgent(String agentname, String hostname, int registryport)
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

			ItemAuction[] items = new ItemAuction[4];
			items[0] = new ItemAuction("table", 150, 100, 5, 5);
			items[1] = new ItemAuction("chair", 150, 100, 5, 5);
			items[2] = new ItemAuction("bench", 150, 100, 5, 5);
			items[3] = new ItemAuction("tv", 150, 100, 5, 5);
			
			ArrayList<String> participants = new ArrayList<String>();
			participants.add("ag1");
			participants.add("ag2");
			participants.add("ag3");
			participants.add("ag4");

			dutchProtocol(myname, participants, items);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void dutchProtocol(String myID, ArrayList<String> participants, ItemAuction[] items) throws IOException, InterruptedException {
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
		String command = in.readLine();
		
		currentAskingPrice = currentItem.getStartingPrice();
		//tell the agents the auction is starting
		for (String participant : participants) {
			message = new Message(Message.INFORM_START_OF_AUCTION, myname, participant, "dutch");
			mailbox.send(message);
		}
		
		//wait for them to process the message
		TimeUnit.SECONDS.sleep(1);
		
		//tell them the starting price
		for (String participant : participants) {
			message = new Message(Message.INFORM_ASKING_PRICE, myname, participant, currentItem.getItemID(),
					currentItem.getStartingPrice());
			mailbox.send(message);
		}
		//wait for them to process the message and send something back
		TimeUnit.SECONDS.sleep(1);

		Boolean auction = true;
		while (auction) {
			ArrayList<Message> messages = new ArrayList<Message>();  //list of all messages
			//receive messages from all agents
			for (String participant : participants) {
				message = mailbox.receive(participant);
				if (message != null) {
					messages.add(message);
				}
			}
			if (messages.size() == 0) { // if there are no messages
				currentAskingPrice = currentAskingPrice - decrement;  //decrement price
				//if still above reserve, inform asking price
				if (currentAskingPrice >= currentItem.getReservePrice()) {
					for (String participant : participants) {
						message = new Message(Message.INFORM_ASKING_PRICE, myname, participant, currentItem.getItemID(),
								currentAskingPrice);
						mailbox.send(message);
						TimeUnit.SECONDS.sleep(1);
					}
				} 
				//else inform everyone they lose
				else {
					auction = false;
					for (String participant : participants) {
						message = new Message(Message.INFORM_LOSER, myname, participant, currentItem.getItemID());
						mailbox.send(message);
					}
					// end auction
				}
			} 
			//if there is at least 1 message
			else {
				ArrayList<String> accepting = new ArrayList<String>();  //list of ids of people accepting same bid
				//for each message, add people who accept this price to accepting list
				for (Message msg : messages) {
					if (msg.getMessageType() == 7) {
						accepting.add(msg.getSender());
					} 
					//else remove unnecessary messages
					else {
						messages.remove(msg);
					}
				}
				if (accepting.size() == 0) { // no real bids in messages
					currentAskingPrice = currentAskingPrice - decrement;
					if (currentAskingPrice >= currentItem.getReservePrice()) {
						for (String participant : participants) {
							message = new Message(Message.INFORM_ASKING_PRICE, myname, participant,
									currentItem.getItemID(), currentItem.getStartingPrice());
							mailbox.send(message);
							TimeUnit.SECONDS.sleep(1);
						}
					} else {
						auction = false;
						for (String participant : participants) {
							message = new Message(Message.INFORM_LOSER, myname, participant, currentItem.getItemID());
							mailbox.send(message);
						}
						// end auction
					}
				} 
				//if there's a real bid, and only one -> winner
				else if (accepting.size() == 1) {
					auction = false;
					message = new Message(Message.INFORM_WINNER, myname, messages.get(0).getSender(), currentItem.getItemID());
					mailbox.send(message);

					participants.remove(message.getReceiver());  //remove winner
					for (String participant : participants) {
						message = new Message(Message.INFORM_LOSER, myname, participant, currentItem.getItemID());
						mailbox.send(message);
					}
					// end auction
				} 
				//if more than one bid for accepting the price
				else if (accepting.size() > 1) {
					auction = false;  //end dutch auuction
					ArrayList<String> englishAuctionParticipants = new ArrayList<String>();

					for (String participant : accepting) {
						message = new Message(Message.INFORM_START_OF_AUCTION, myname, participant, "english");
						mailbox.send(message);
						englishAuctionParticipants.add(participant);
					}
					TimeUnit.SECONDS.sleep(1);
					// end auction, start english auction
					englishProtocol(myID, currentItem, englishAuctionParticipants, currentAskingPrice);
				}
			}
		}
	}

	private void englishProtocol(String auctionID, ItemAuction currentItem, ArrayList<String> participants, Integer askingPrice) throws IOException, InterruptedException
	{
	  int increment = 5;
	  Bid currentBid = new Bid(null, null);
	  int currentAskingPrice = askingPrice;
	  Message message = null ;
	  Boolean auction = true;
	  
	  //tell the agents still in the auction the asking price
	  for(String participant: participants) {
		  message = new Message (Message.INFORM_ASKING_PRICE, myname, participant, currentItem.getItemID(), askingPrice);
		  mailbox.send(message);
	  }
	  
	  TimeUnit.SECONDS.sleep(1);
	  
	  while(auction) {
		  ArrayList<Message> messages = new ArrayList<Message>();
		  for(String participant: participants) {
		      message = mailbox.receive(participant);
		      if(message != null) {
		      	  messages.add(message);
		      }
		  }
		  //if no one wants to bid, end the auction
		  if(messages.isEmpty()) {
			  auction = false; //end auction
			  if(currentBid.getBidderID() == null) {
				  //just choose the first message for the winner
				  message = new Message (Message.INFORM_WINNER, myname, messages.get(0).getSender(), currentItem.getItemID());
	    		  mailbox.send(message);
	    		  
	    		  //tell everyone else they lost
	    		  participants.remove(messages.get(0).getSender());  //remove guy who won
		  			for(String participant: participants) {
		    		    message = new Message (Message.INFORM_LOSER, myname, participant, currentItem.getItemID());
		    		    mailbox.send(message);
					}
			  }
			  //if no one wants to bid, end the auction, still have a current bid
			  else {
				  message = new Message (Message.INFORM_WINNER, myname, currentBid.getBidderID(), currentItem.getItemID());
	    		  mailbox.send(message);
		  			for(String participant: participants) {
		    		    message = new Message (Message.INFORM_LOSER, myname, participant, currentItem.getItemID());
		    		    mailbox.send(message);
					}
			  }
		  }
		  //else someone wants to bid
		  else {
			  //only accept 1 bid

			  for(Message msg: messages) {
				  //if message isnt a bid delete it
				  if(msg.getMessageType() != 8 || msg.getMessageType() != 9) { 
					  messages.remove(msg); 
				  }
			  }
			  
			  //copy participants
			  ArrayList<String> rejectRecipients = new ArrayList<String>(participants);
			  
			  Boolean higherBid = false;
			  //if there's a bidding price (new higher bid)
			  for(Message msg: messages) {
				  if(msg.getMessageType() == 8) { 
					  //if its higher than the current bid
					  if(msg.getBiddingPrice() > currentBid.getBidPrice()) {
						  currentBid.setBidderID(msg.getSender());
						  currentBid.setBidPrice(msg.getBiddingPrice());
						  higherBid = true;
					  }
				  }
			  }
			  //if theres a highest bid
			  if(higherBid) {
				  rejectRecipients.remove(currentBid.getBidderID());  //remove winner
				  for(String agent: rejectRecipients) {
		    		    message = new Message (Message.INFORM_REJECT, myname, agent, currentItem.getItemID());
		    		    mailbox.send(message);
				  }
				  currentBid.setBidPrice(currentBid.getBidPrice()+increment);
				  for(String agent: rejectRecipients) {
		    		    message = new Message (Message.INFORM_ASKING_PRICE, myname, agent, currentItem.getItemID(), currentAskingPrice);
		    		    mailbox.send(message);
				  }
				  TimeUnit.SECONDS.sleep(1);
			  }
			  //if there are only people bidding on current price
			  else {
				  //find the first message and give to him
				  String winner = "ag1";
				  for(Message msg: messages) {
					  if(msg.getMessageType() == 9) { 
						  winner = messages.get(0).getSender();
						  currentBid.setBidPrice((int)messages.get(0).getPrice());
						  currentBid.setBidderID(messages.get(0).getSender());
						  break;
					  }
				  }
				  rejectRecipients.remove(winner);
				  for(String agent: rejectRecipients) {
		    		    message = new Message (Message.INFORM_REJECT, myname, agent, currentItem.getItemID());
		    		    mailbox.send(message);
				  }
				  currentBid.setBidPrice(currentBid.getBidPrice()+increment);
				  for(String agent: rejectRecipients) {
		    		    message = new Message (Message.INFORM_ASKING_PRICE, myname, agent, currentItem.getItemID(), currentAskingPrice);
		    		    mailbox.send(message);
				  }
				  TimeUnit.SECONDS.sleep(1);			  
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

		try {
			// instantiate an agent from this class

			new AuctionAgent(agentname, hostname, registryport);

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

	}

}