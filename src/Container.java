import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Container
{   
    private String           myname       = null ;
    private String           hostname     = null ;
    private int              registryport = 0    ;
    private MailboxInterface mailbox      = null ;
    
    private List<Agent> agentList = new ArrayList<Agent>() ;


    /**
     * @throws NotBoundException 
     * @throws IOException 
     * 
     */
    public Container( String name, String hostname, int registryport ) throws NotBoundException, IOException
    {
        this.myname       = name ;
        this.hostname     = hostname  ;
        this.registryport = registryport ;
        
        String regURL = "rmi://" + hostname + ":" + registryport + "/Mailbox";
        System.out.println( "Container> Looking up " + regURL );
        // ===============================================
        // lookup the remote service, it will return a reference to the stub:
        mailbox = (MailboxInterface) Naming.lookup( regURL );
        
        System.out.println( "Container> Container " + myname + " connected to mailbox." );
        
        System.out.println( "Container> Starting agents: ");
        
        String[] agentNameList = Capability.getAgentNameList() ;
        
        for ( String agentName : agentNameList )
        {
            System.out.println( "Container> creating agent " + agentName ) ;
            Agent agent = new Agent ( agentName, hostname, registryport, this ) ;
            agentList.add( agent ) ;
            
            new Thread ( agent ).start();
        }
        
        consoleInput() ;
    }
    
    private void consoleInput () throws IOException, NotBoundException
    {
        // ===============================================
        // Console input:
        BufferedReader in = new BufferedReader( new InputStreamReader( System.in ));
        String input = null ;
        
        while (true)
        {
            System.out.print("Container> "); input = in.readLine() ;
            if ( input.equalsIgnoreCase("exit") ) break ;
            // add here more commands
        }
        
        shutdownAgents() ;
        System.exit(0);
    }
    
    private void shutdownAgents() throws RemoteException, MalformedURLException, NotBoundException
    {
        for ( Agent agent : agentList )
        {
            agent.shutdown() ;
        }
    }

    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // Specify the security policy and set the security manager.
        System.setProperty( "java.security.policy", "security.policy" ) ;
        System.setSecurityManager( new SecurityManager() ) ;

        // =====================================================================
        // this Java program requires two parameters
        String containername = args[0];
        int registryport = Integer.parseInt( args[1] ) ;
        
        try 
        {
            // =================================================================
            // get the hostname of the machine where this program is started
            String hostname = (InetAddress.getLocalHost()).getCanonicalHostName() ;
            
            new Container ( containername, hostname, registryport ) ;
            
        } 
        catch ( IOException | NotBoundException e )
        {
            e.printStackTrace();
        }


    }

}
