import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Agent
{
  private String           myname       = null ;
  private String           hostname     = null ;
  private int              registryport = 0    ;
  private MailboxInterface mailbox      = null ;

  
  
  /**
   * @throws NotBoundException 
   * @throws RemoteException 
   * @throws MalformedURLException 
   * 
   */
  public Agent(String agentname, String hostname, int registryport) throws MalformedURLException, RemoteException, NotBoundException
  {
    this.myname       = agentname ;
    this.hostname     = hostname  ;
    this.registryport = registryport ;
    
        // Obtain the service reference from the RMI registry
        // listening at hostname:registryport.
        
        String regURL = "rmi://" + hostname + ":" + registryport + "/Mailbox";
        System.out.println( "Looking up " + regURL );

        // ===============================================
        // lookup the remote service, it will return a reference to the stub:
        
        mailbox = (MailboxInterface) Naming.lookup( regURL );
        
        System.out.println( "Agent " + myname + " connected to mailbox." );
        
        //new Thread ( receiver = new Receiver( myname, mailbox ) ).start(); ;
        
        try
        {
          
        	item[] items = new item[4];
        	items[0] = null;
        	dutchProtocol(auctioneerID, myname, items) ;
    } 
        catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  

  
  private void dutchProtocol(String auctionID, String myID, item[] items) throws IOException
  {
    // ===============================================
    // Console input:
    BufferedReader in = new BufferedReader( new InputStreamReader( System.in ));
    Message        message = null ;
    String		   auctionType = null;
    Integer		   currentAskingPrice = null;
    String		   currentItem = null;

    System.out.print( "Start buyer, press return "); String command = in.readLine() ;

    while((message = mailbox.receive(myname)) == null) {
    	if(message.getMessageType() == 1) {  //start auction
    		auctionType = message.getAuctionType();
    		break;
    	}
    }
    
    Boolean auction = true;
	if(auctionType == "dutch") {
	    while((message = mailbox.receive(myname)) == null) {
	    	if(message.getMessageType() == 2) {  //inform asking price
	    		currentAskingPrice = message.getAskingPrice();
	    		currentItem = message.getItem();
	    		break;
	    	}
	    }
	    item currentItemStats = null;
	    for(item item: items) {
	    	if(item.itemID.equals(currentItem)) {
	    		currentItemStats = item;
	    		if(currentAskingPrice <= item.acceptBidPrice) {
	    		    message = new Message (Message.PROPOSE_ACCEPT_PRICE, myname, auctionID, currentItem);
	    		    mailbox.send(message);
	    		}
	    		else if(currentAskingPrice > item.acceptBidPrice) {
	    		    message = new Message (Message.NO_MESSAGE, myname, auctionID, currentItem);
	    		    mailbox.send(message);
	    		}
	    	}
	    }
	    while((message = mailbox.receive(myname)) == null);
	    if(message.getMessageType() == 6) {  //inform loser
	    	auction = false;
	    }
	    else if(message.getMessageType() == 5) {  //inform winner
	    	auction = false;
	    }
	    else if(message.getMessageType() == 2) {  //inform asking price
	    	currentAskingPrice = message.getAskingPrice();
	    	currentItem = message.getItem();
	    }
	    else if(message.getMessageType() == 1) {  //inform start of auction
	    	auction = false;
	    	englishProtocol(auctionID, myID, currentAskingPrice, currentItemStats);
	    }	
    }
  }
  
  
  
  private void englishProtocol(String auctionID, String myID, Integer askingPrice, item currentItemStats) throws IOException
  {
	  int currentAskingPrice = askingPrice;
	  String currentItem =currentItemStats.getItemID();
	  Message message = null ;
	  Boolean auction = true;
	  while(auction) {
		  if(askingPrice <= currentItemStats.getMaxPrice()) {
			  if(askingPrice + currentItemStats.getIncrement() < currentItemStats.getMaxPrice()) {
				  int nextBidPrice = askingPrice + currentItemStats.getIncrement();
	    		  message = new Message (Message.PROPOSE_BIDDING_PRICE, myname, auctionID, currentItemStats.getItemID(), nextBidPrice);
	    		  mailbox.send(message);
			  }
			  else {
	    		  message = new Message (Message.PROPOSE_BID_ON_PRICE, myname, auctionID, item);
	    		  mailbox.send(message);
			  }
		  }
		  else if(askingPrice > currentItemStats.getMaxPrice()) {
    		  message = new Message (Message.NO_MESSAGE, myname, auctionID, currentItemStats.getItemID(), nextBidPrice);
    		  mailbox.send(message);
		  }
		  
		  while((message = mailbox.receive(myname)) == null);
		  
		  if(message.getMessageType() == 6) {
			  auction = false;
			  //auction is over for this agent
		  }
		  else if(message.getMessageType() == 5) {
			  auction = false;
			  //auction is over for this agent
		  }
		  else if(message.getMessageType() == 4) {
			  while((message = mailbox.receive(myname)) == null);
			  currentAskingPrice = message.getAskingPrice();
			  currentItem = message.getItem();
		  }
		  else if(message.getMessageType() == 3) {
			  while(true) {
				  while((message = mailbox.receive(myname)) == null);
				  if(message.getMessageType() == 5) {
					  auction = false;
					  //auction is over for this agent
					  break;
				  }
				  else if(message.getMessageType() == 2) {
					  currentAskingPrice = message.getAskingPrice();
					  currentItem = message.getItem();
					  break;
				  }	
			  }
		  }

	  }
			 
	  
	  
	  
	  
  }
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {

        // Specify the security policy and set the security manager.
        System.setProperty( "java.security.policy", "security.policy" ) ;
        System.setSecurityManager( new SecurityManager() ) ;

        String agentname = args[0];
        String hostname  = args[1];
        int registryport = Integer.parseInt( args[2] );

        try 
        {
          // instantiate an agent from this class
          
      new Agent ( agentname, hostname, registryport ) ;
      
    } 
        catch (MalformedURLException | RemoteException | NotBoundException e)
        {
      e.printStackTrace();
    }


  }

}