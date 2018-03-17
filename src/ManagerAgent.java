import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class ManagerAgent
{
    private ManagerProtocol  protocol     = null ;
    private String           myname       = null ;
    private String           myURL        = null ;
    private String           hostname     = null ;
    private int              registryport = 0    ;
    private MailboxInterface mailbox      = null ;

    // load agent list
    private String[] agentNameList = Capability.getAgentNameList() ;
    
    
    // load capabilities of agent
    private Map<String,Capability> capabilityList = Capability.getCapabilityList(myname) ;

    /**
     * @param registryport 
     * @param hostname 
     * @param agentname 
     * @throws NotBoundException 
     * @throws IOException 
     * 
     */
    public ManagerAgent(String agentname, String hostname, int registryport) throws NotBoundException, IOException
    {
        this.myname       = agentname ;
        this.hostname     = hostname  ;
        this.registryport = registryport ;
        
        
        String regURL = "rmi://" + hostname + ":" + registryport + "/Mailbox";
        System.out.println( "Looking up " + regURL );        
        mailbox = (MailboxInterface) Naming.lookup( regURL );
        
        System.out.println( "Agent " + myname + " connected to mailbox." );
        
        
        // you have to press return to start the manager ...
        BufferedReader in = new BufferedReader( new InputStreamReader( System.in ));
        System.out.print( "Start manager, press return "); String command = in.readLine() ;

        
        protocol = new ManagerProtocol ( myname, mailbox ) ;

        protocol.conversation() ;
               
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
            
            new ManagerAgent ( agentname, hostname, registryport ) ;
            
        } 
        catch (NotBoundException | IOException e)
        {
            e.printStackTrace();
        }


    }

}
