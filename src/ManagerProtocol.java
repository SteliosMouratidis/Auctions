import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ManagerProtocol
{
    // load agent list
    private String[] agentNameList = Capability.getAgentNameList() ;
    private MailboxInterface mailbox      = null ;
    private String           myname       = null ;
    
    Message            message = null ;
    double             myprice     = 0 ;
    Map<String,Double> bidsOfAgents = new HashMap<String,Double>() ;

    /**
     * 
     */
    public ManagerProtocol( String agentname, MailboxInterface mailbox )
    {
        this.mailbox = mailbox ;
        this.myname  = agentname ;
    }

    
    private void printSubtasks ( String taskID )
    {
        List<String> subtaskList = Capability.getSubtaskList(taskID) ;
        System.out.println( "Subtasks : " + subtaskList );
        
        if (subtaskList.size() > 0)
        {
            
            for ( Iterator<String> iterator = subtaskList.iterator(); iterator.hasNext(); )
            {
                printSubtasks ( iterator.next() ) ;
            }
        }
    }
    
    public void conversation () throws IOException
    {
        List<String> subtaskList = Capability.getSubtaskList("1230") ;
        System.out.println( "length of 1230 = " + "1230".length() );
        System.out.println( "Subtask list: " + subtaskList );
        //printSubtasks ( "1000" ) ;
        
        //for ( String subtaskID : subtaskList )
        //{
            //callForProposals ( subtaskID) ;
        //}
            
        //receiveBids() ;
        
        //awardContract() ;


    }

    private void callForProposals ( String subtaskID) throws RemoteException
    {
        // broadcast CFP to all agents
        for ( String agent : agentNameList )
        {
            Message message = new Message (Message.CFP, myname, agent, subtaskID, 0 ) ;
            mailbox.send(message);
        }
        
    }
    
    private void receiveBids ( ) throws IOException
    {

        while ( (message = mailbox.receive(myname)) != null ) // get all bids
        {
            if ( message.getMessageType() == Message.TELL_BID )
            {
                System.out.println( "\n" + "   received from " + message.getSender() + ":");
                System.out.println("Task  = " + message.getTaskID() );
                System.out.println("Price = " + message.getPrice() );
                
                bidsOfAgents.put(message.getSender(), message.getPrice() ) ;
            }
            
        }
        
    }
    
    private void awardContract ( ) throws RemoteException
    {
        String [] bidders = bidsOfAgents.keySet().toArray(new String[bidsOfAgents.size()]) ;
        
        String bestBidder = getBestBid ( bidders) ;
        
        // if there is a best bid
        if ( bestBidder != null )
        {
            message = new Message ( Message.ACCEPT, myname, bestBidder, message.getTaskID(), message.getPrice()) ;
            mailbox.send(message);
            
            bidsOfAgents.remove(bestBidder) ;
            bidders = bidsOfAgents.keySet().toArray(new String[bidsOfAgents.size()]) ;
        }
        
        for ( String looser : bidders )
        {
            message = new Message ( Message.REJECT, myname, looser, "you are a looser") ;
            mailbox.send(message);
        }

    }

    
    private String getBestBid(String [] bidders)
    {
        // here, there should be some selection
        // we return simply on of the agent names
        
        if ( bidders.length > 0 ) return bidders[0] ;
        
        return null ;
    }



}
