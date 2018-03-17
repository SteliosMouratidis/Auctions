import java.io.Serializable;

public class Message implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1;
    
    static public final int NO_MESSAGE = 0 ;
    static public final int CFP        = 1 ;
    static public final int TELL_BID   = 2 ;
    static public final int ACCEPT     = 3 ;
    static public final int REJECT     = 4 ;
    
    private int    messageType = NO_MESSAGE ;
    
    private String sender   = null ;
    private String receiver = null ;

    // generic message content (String content can be anything)
    private String content  = null ;
    
    // protocol-specific message content:
    private String taskID   = null ;
    private double price    = 0 ;
    //
    /**
     * 
     */
    public Message() {}

    // generic message constructor
    public Message(int messageType, String sender, String receiver, String message )
    {
        this.setMessageType(messageType) ;
        this.sender      = sender ;
        this.receiver    = receiver ;
        this.content     = message ;
    }
    
    // protocol-specific message constructor
    public Message(int messageType, String sender, String receiver, String taskID, double price)
    {
        this.setMessageType(messageType) ;
        this.sender      = sender ;
        this.receiver    = receiver ;
        this.taskID      = taskID ;
        this.price       = price ;
        
    }
    /**
     * @return the message
     */
    public String getContent() {
        return content;
    }
    /**
     * @param message the message to set
     */
    public void setContent(String content) {
        this.content = content;
    }
    public String getReceiver() {
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    public int getMessageType() {
        return messageType;
    }
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getTaskID() {
        return taskID;
    }
    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

}
