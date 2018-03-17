import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;


public class Agent implements AgentInterface, Runnable
{
    private AgentProtocol    protocol     = null ;
    private String           myname       = null ;
    private String           myURL        = null ;
    private String           hostname     = null ;
    private int              registryport = 0    ;
    private MailboxInterface mailbox      = null ;
    private Container        myContainer  = null ;
    
    
    
    /**
     * @throws NotBoundException 
     * @throws RemoteException 
     * @throws MalformedURLException 
     * 
     */
    
    public Agent(String agentname, String hostname, int registryport, Container container ) throws MalformedURLException, RemoteException, NotBoundException
    {
        this.myname       = agentname ;
        this.hostname     = hostname  ;
        this.registryport = registryport ;
        this.myContainer  = container ;
        
        // register myself with the rmiregistry
        myURL = "rmi://" + hostname + ":" + registryport + "/" + myname;
        System.out.println( myname + "> Registering " + myURL );
        
        AgentInterface mystub = (AgentInterface) UnicastRemoteObject.exportObject( this, 0 ) ;
        Naming.rebind( myURL, mystub );


        // Obtain the service reference from the RMI registry
        // listening at hostname:registryport.
        
        String regURL = "rmi://" + hostname + ":" + registryport + "/Mailbox";
        System.out.println( myname + "> Looking up " + regURL );

        // ===============================================
        // lookup the remote service, it will return a reference to the stub:
        
        mailbox = (MailboxInterface) Naming.lookup( regURL );
        
        System.out.println( myname + "> Agent " + myname + " connected to mailbox." );
        
        //new Thread ( new AgentProtocol ( myname, mailbox ) ).start() ;
    }
    

    public void shutdown() throws RemoteException, MalformedURLException, NotBoundException
    {
        Naming.unbind(myURL);
        
    }


    @Override
    public void run()
    {
        System.out.println( myname + "> Thread started." );
        protocol = new AgentProtocol ( myname, mailbox ) ;
    }
}
