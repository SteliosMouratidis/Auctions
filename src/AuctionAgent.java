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
		for (String participant : participants) {
			message = new Message(Message.INFORM_START_OF_AUCTION, myname, participant, "dutch");
			mailbox.send(message);
		}
		TimeUnit.SECONDS.sleep(1);
		for (String participant : participants) {
			message = new Message(Message.INFORM_ASKING_PRICE, myname, participant, items[0].getItemID(),
					items[0].getStartingPrice());
			mailbox.send(message);
		}
		TimeUnit.SECONDS.sleep(1);

		Boolean auction = true;
		while (auction) {
			ArrayList<Message> messages = new ArrayList<Message>();
			for (String participant : participants) {
				message = mailbox.receive(participant);
				if (message != null) {
					messages.add(message);
				}
			}
			if (messages.size() == 0) { // if there are no messages
				currentAskingPrice = currentAskingPrice - decrement;
				if (currentAskingPrice >= currentItem.getReservePrice()) {
					for (String participant : participants) {
						message = new Message(Message.INFORM_ASKING_PRICE, myname, participant, items[0].getItemID(),
								items[0].getStartingPrice());
						mailbox.send(message);
					}
				} else {
					auction = false;
					for (String participant : participants) {
						message = new Message(Message.INFORM_LOSER, myname, participant, items[0].getItemID());
						mailbox.send(message);
					}
					// end auction
				}
			} else {
				ArrayList<String> accepting = new ArrayList<String>();  //list of ids of people accepting same bid
				for (Message msg : messages) {
					if (msg.getMessageType() == 7) {
						accepting.add(msg.getSender());
					} else {
						messages.remove(msg);
					}
				}
				if (accepting.size() == 0) { // no real bids in messages
					currentAskingPrice = currentAskingPrice - decrement;
					if (currentAskingPrice >= currentItem.getReservePrice()) {
						for (String participant : participants) {
							message = new Message(Message.INFORM_ASKING_PRICE, myname, participant,
									items[0].getItemID(), items[0].getStartingPrice());
							mailbox.send(message);
							TimeUnit.SECONDS.sleep(1);
						}
					} else {
						auction = false;
						for (String participant : participants) {
							message = new Message(Message.INFORM_LOSER, myname, participant, items[0].getItemID());
							mailbox.send(message);
						}
						// end auction
					}
				} else if (accepting.size() == 1) {
					auction = false;
					message = new Message(Message.INFORM_WINNER, myname, messages.get(0).getSender(), items[0].getItemID());
					mailbox.send(message);

					participants.remove(message.getReceiver());  //remove winner
					for (String participant : participants) {
						message = new Message(Message.INFORM_LOSER, myname, participant, items[0].getItemID());
						mailbox.send(message);
					}
					// end auction
				} else if (accepting.size() > 1) {
					auction = false;
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
		  if(messages.isEmpty()) {
			  auction = false; //end auction
			  if(currentBid.getBidderID() == null) {
				  message = new Message (Message.INFORM_WINNER, myname, messages.get(0).getSender(), currentItem.getItemID());
	    		  mailbox.send(message);
	    		  participants.remove(message.getReceiver());
		  			for(String participant: participants) {
		    		    message = new Message (Message.INFORM_LOSER, myname, participant, "english", askingPrice);
		    		    mailbox.send(message);
					}
			  }
			  else {
				  message = new Message (Message.INFORM_WINNER, myname, messages.get(0).getSender(), currentItem.getItemID());
	    		  mailbox.send(message);
		  			for(String participant: participants) {
		    		    message = new Message (Message.INFORM_LOSER, myname, participant, "english", askingPrice);
		    		    mailbox.send(message);
					}
			  }
		  }
		  else {
			  ArrayList<Message> rejectRecipients = new ArrayList<Message>();
			  for(Message msg: messages) {
				  rejectRecipients.add(msg);
				  if(msg.getMessageType() != 8 || msg.getMessageType() != 9) {  //if message isnt a bid delete it
					  messages.remove(msg);
				  }
				  else {
					  if(msg.getAskingPrice() < currentAskingPrice) {  //if below asking price
						  messages.remove(msg);
					  }
				  }
				  //message.remove(all messages not at asking price);
			  }
			  
			  currentBid.setBidPrice((int)messages.get(0).getPrice());
			  currentBid.setBidderID(messages.get(0).getSender());
			  
			  for(Message msg: rejectRecipients) {
				  rejectRecipients.remove(messages.get(0));  //remove winner
			  }
			  for(Message msg: messages) {
	    		    message = new Message (Message.INFORM_REJECT, myname, msg.getSender(), currentItem.getItemID());
	    		    mailbox.send(message);
			  }
			  currentBid.setBidPrice(currentBid.getBidPrice()+increment);
			  for(Message msg: messages) {
	    		    message = new Message (Message.INFORM_ASKING_PRICE, myname, msg.getSender(), currentItem.getItemID() ,currentAskingPrice);
	    		    mailbox.send(message);
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

		try {
			// instantiate an agent from this class

			new AuctionAgent(agentname, hostname, registryport);

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

	}

}