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
	 * 
	 */
	public AuctionAgent(String agentname, String hostname, int registryport)
			throws MalformedURLException, RemoteException, NotBoundException {
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

			item[] items = new item[4];
			items[0] = null;
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

	private void dutchProtocol(String myID, ArrayList<String> participants, item[] items) throws IOException {
		// ===============================================
		// Console input:
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Message message = null;
		String auctionType = null;
		Integer currentAskingPrice = null;
		String currentItem = null;
		ArrayList<bid> currentBids = new ArrayList<bid>();
		int decrement = 5;

		System.out.print("Start Auction, press return ");
		String command = in.readLine();
		int askingPrice = items[0].getStartingPrice();
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
				askingPrice = askingPrice - decrement;
				if (askingPrice >= reservePrice) {
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
				accepting = null;
				for (Message msg : messages) {
					if (msg.getMessageType() == 7) {
						accepting.add(msg.getSender(), msg.getItem(), askingPrice);
					} else {
						messages.delete(msg);
					}
				}
				if (accepting.size() == 0) { // no real bids in messages
					askingPrice = askingPrice - decrement;
					if (askingPrice >= reservePrice) {
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
					message = new Message(Message.INFORM_WINNER, myname, messages.get(0).getSender(),
							items[0].getItemID());
					mailbox.send(message);

					participants = participants - winner;

					for (String participant : participants) {
						message = new Message(Message.INFORM_LOSER, myname, participant, items[0].getItemID());
						mailbox.send(message);
					}
					// end auction
				} else if (accepting.size() > 1) {
					auction = false;
					englishAuctionParticipants = null;

					for (String participant : accepting) {
						message = new Message(Message.INFORM_START_OF_AUCTION, myname, participant, "english",
								askingPrice);
						mailbox.send(message);
						englishAuctionParticipants.add(participant);
					}
					TimeUnit.SECONDS.sleep(1);
					// end auction, start english auction
					englishProtocol();
				}
			}
		}
	}

	private void englishProtocol(String auctionID, String itemID, ArrayList<String> participants, Integer askingPrice) throws IOException
	{
	  int increment = 5;
	  bid currentBid = new bid(null, null);
	  int currentAskingPrice = askingPrice;
	  Message message = null ;
	  Boolean auction = true;
	  for(String participant: participants) {
		  message = new Message (Message.INFORM_ASKING_PRICE, myname, participant, items[0].getItemID(), items[0].getStartingPrice());
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
				  message = new Message (Message.INFORM_WINNER, myname, messages.get(0).getSender(), items[0].getItemID());
	    		  mailbox.send(message);
		  			for(String participant: participant-winner) {
		    		    message = new Message (Message.INFORM_LOSER, myname, participant, "english", askingPrice);
		    		    mailbox.send(message);
					}
			  }
			  else {
				  message = new Message (Message.INFORM_WINNER, myname, messages.get(0).getSender(), items[0].getItemID());
	    		  mailbox.send(message);
		  			for(String participant: participant-winner) {
		    		    message = new Message (Message.INFORM_LOSER, myname, participant, "english", askingPrice);
		    		    mailbox.send(message);
					}
			  }
		  }
		  else {
			  for(Message msg: messages) {
				  message.remove(all messages not at asking price);
			  }
			  currentBid.setBidPrice((int)msg.getPrice());
			  currentBid.setBidderID(msg.getSender());
			  for(Message msg: messages) {
				  message.remove(high bid message);
			  }
			  for(Message msg: messages) {
	    		    message = new Message (Message.INFORM_REJECT, myname, participant, "english", askingPrice);
	    		    mailbox.send(message);
			  }
			  currentBid.setBidPrice(currentBid.getBidPrice()+increment);
			  for(Message msg: messages) {
	    		    message = new Message (Message.INFORM_ASKING_PRICE, myname, participant, "english", askingPrice);
	    		    mailbox.send(message);
			  }
				TimeUnit.SECONDS.sleep(1);
		  }
	  }
  }

	/**
	 * @param args
	 */
	public static void main(String[] args) {

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