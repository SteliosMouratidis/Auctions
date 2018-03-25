import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MailboxInterface extends Remote
{
	public Message receive (String agentname ) throws RemoteException ;
	public void send(Message message) throws RemoteException;
	public int getNumberOfMessages() throws RemoteException;

}
