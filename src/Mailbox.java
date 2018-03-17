import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;

public class Mailbox implements MailboxInterface
{
	private ArrayList<Message> messageList = new ArrayList<Message>() ;


	private String hostname     = null ;
	private int    registryport = 0 ;
	private String myURL        = null ;
	

	public Mailbox(String hostname, int registryport) throws IOException 
	{
		this.hostname     = hostname  ;
		this.registryport = registryport ;

		// =====================================================================
		// register myself with the rmiregistry
		myURL = "rmi://" + hostname + ":" + registryport + "/Mailbox" ;
		System.out.println( "Registering " + myURL );
		// we use 0 as the serverport, so that the Java VM is selecting a free port for us
		MailboxInterface mystub = (MailboxInterface) UnicastRemoteObject.exportObject( this, 0 ) ;
		Naming.rebind( myURL, mystub );
		        
		        
		        
		System.out.println( "Mailbox> started." );
		
		consoleInput() ;
	}


	@Override
	public synchronized void send(Message message) throws RemoteException
	{
		System.out.println ("Mailbox> in send: agent " + message.getSender() + " deposits message ]" ) ;
		messageList.add( message ) ;
	}

	@Override
	public synchronized Message receive(String agentname) throws RemoteException
	{
		Iterator<Message> it = messageList.iterator() ;
		
		while (it.hasNext() ) {
			Message m = it.next();
			if ( m.getReceiver().equals(agentname) )
			{
				System.out.println("Mailbox> in receive: message for agent " + agentname + " found" );
				it.remove();
				return m ;
				
			}
				
		}
				
		return null ;
	}
	
	private void consoleInput () throws IOException
	{
		// ===============================================
        // Console input:
		BufferedReader in = new BufferedReader( new InputStreamReader( System.in ));
		String input = null ;
		
		while (true)
		{
			System.out.print("Mailbox> "); input = in.readLine() ;

			if ( input.equalsIgnoreCase("exit") ) break ;

			// you can add your own commands here ...

		}
		
		System.exit(0);
	}

	

	/**
	 * @return the agentList
	 */
	public ArrayList<Message> getMessageList() throws RemoteException
	{
		return messageList ;
	}

	

	static public void main ( String args[] )
	{
        // Specify the security policy and set the security manager.
        System.setProperty( "java.security.policy", "security.policy" ) ;
        System.setSecurityManager( new SecurityManager() ) ;

        // =====================================================================
        // this Java program requires two parameters
        int registryport = Integer.parseInt( args[0] ) ;
        
        try 
        {
            // =================================================================
        	// get the hostname of the machine where this program is started
            String hostname = (InetAddress.getLocalHost()).getCanonicalHostName() ;
        	
			Mailbox mailbox = new Mailbox ( hostname, registryport ) ;
			
		} 
        catch ( IOException e )
        {
			e.printStackTrace();
		}

	}

}
