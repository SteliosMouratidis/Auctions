import java.rmi.RemoteException;
import java.util.Map;

public class AgentProtocol
{
    private MailboxInterface mailbox      = null ;
    private String           myname       = null ;

    // load capabilities of agent
    private Map<String,Capability> capabilityList = null ;
    
    Message        message = null ;
    double         myprice     = 0 ;



    /**
     * @param mailbox2 
     * @param myname 
     * 
     */
    public AgentProtocol(String myname, MailboxInterface mailbox)
    {
        this.mailbox   = mailbox ;
        capabilityList = Capability.getCapabilityList(myname) ;
    }
       



    public void conversation()
    {

        while (true)
        {
            try
            {
                if ((message = mailbox.receive(myname)) != null) {
                    System.out.println( "\n" + myname + " received from " + message.getSender() + ":");

                    if ( message.getMessageType() == Message.ACCEPT) {  
                        System.out.println( "Manager accepts at price " + message.getPrice() );
                        break ;
                    }
                    
                    if ( (myprice = cando(message.getTaskID())) >= 0)
                    {
                        message = new Message ( Message.TELL_BID, myname, message.getSender(), message.getTaskID(), myprice) ;
                        mailbox.send(message);
                    }
                    else
                    {
                        message = new Message ( Message.REJECT, myname, message.getSender(), message.getTaskID() + " is not for me") ;
                        mailbox.send(message);
                    }
                }

            }
            catch (Exception e)
            {
                
            }
        }
    }
    
    private void receiveCallForProposal () throws RemoteException
    {
        while ((message = mailbox.receive(myname)) != null)
        {
            if ( message.getMessageType() == Message.CFP )
            {
                if ( (myprice = cando(message.getTaskID())) >= 0)
                {
                    message = new Message ( Message.TELL_BID, myname, message.getSender(), message.getTaskID(), myprice) ;
                    mailbox.send(message);
                }
                else
                {
                    message = new Message ( Message.REJECT, myname, message.getSender(), message.getTaskID() + " is not for me") ;
                    mailbox.send(message);
                }

            }
            else if ( message.getMessageType() == Message.ACCEPT )
            {
                System.out.println( "Manager accepts at price " + message.getPrice() );
                break ;
            }
            else if ( message.getMessageType() == Message.REJECT )
            {
                
            }
        }
    }

    private double cando ( String taskID )
    {
        Capability cap = capabilityList.get(taskID) ;
        if (cap != null) return cap.getPrice() ;
        
        return -1 ;
    }

    

}









// class AgentProtocol
/* Possible conversation:
    private void conversation () throws RemoteException
    {
        Message        message = null ;

        while (true)
        {
            // receive messages from the Mailbox
            //      message = mailbox.receive(myname))
            // do something with it
            // send messages to the Mailbox
            //      message = new Message ( Message.XXXX, myname, receiver, ..., ...) ;
            //      mailbox.send(message);
        }

    }
    


    private void conversation () throws RemoteException
    {
        Message        message = null ;
        double         myprice     = 0 ;

        while (true)
        {
            if ((message = mailbox.receive(myname)) != null) {
                System.out.println( "\n" + myname + " received from " + message.getSender() + ":");

                if ( message.getMessageType() == Message.ACCEPT) {  
                    System.out.println( "Manager accepts at price " + message.getPrice() );
                    break ;
                }
                
                if ( (myprice = cando(message.getTaskID())) >= 0)
                {
                    message = new Message ( Message.TELL_BID, myname, message.getSender(), message.getTaskID(), myprice) ;
                    mailbox.send(message);
                }
                else
                {
                    message = new Message ( Message.REJECT, myname, message.getSender(), message.getTaskID() + " is not for me") ;
                    mailbox.send(message);
                }
            }
        }
    }

    private double cando ( String taskID )
    {
        Capability cap = capabilityList.get(taskID) ;
        if (cap != null) return cap.getPrice() ;
        
        return -1 ;
    }
*/
