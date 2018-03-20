import java.io.Serializable;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	static public final int NO_MESSAGE = 0;
	static public final int INFORM_START_OF_AUCTION = 1;
	static public final int INFORM_ASKING_PRICE = 2;
	static public final int INFORM_ACCEPT = 3;
	static public final int INFORM_REJECT = 4;
	static public final int INFORM_WINNER = 5;
	static public final int INFORM_LOSER = 6;
	static public final int PROPOSE_ACCEPT_PRICE = 7;
	static public final int PROPOSE_BIDDING_PRICE = 8;
	static public final int PROPOSE_BID_ON_PRICE = 9;

	private int messageType = NO_MESSAGE;

	private String sender = null;
	private String receiver = null;

	// generic message content (String content can be anything)
	private String content = null;

	// protocol-specific message content:
	private String auctionType = null;
	private Integer askingPrice = null;
	private String itemID = null;
	private String taskID = null;
	private double price = 0;

	private Integer salePrice = null;

	//
	/**
	 * 
	 */
	public Message() {
	}

	// protocol-specific message constructor - NO_MESSAGE, INFORM_START_OF_AUCTION, INFORM_LOSER
	public Message(int messageType, String sender, String receiver, String message) {
		this.setMessageType(messageType);
		this.sender = sender;
		this.receiver = receiver;

		if (message.equals("dutch") || message.equals("english")) {
			this.auctionType = message;
		}
		
		
		
	}

	// protocol-specific message constructor - INFORM_ASKING_PRICE
	public Message(int messageType, String sender, String receiver, int askingPrice, String itemID) {
		this.setMessageType(messageType);
		this.sender = sender;
		this.receiver = receiver;
		this.askingPrice = askingPrice;
		this.itemID = itemID;
	}
	
	
	// protocol-specific message constructor - INFORM_ASKING_PRICE
	public Message(int messageType, String sender, String receiver, String itemID, int askingPrice) {
		this.setMessageType(messageType);
		this.sender = sender;
		this.receiver = receiver;
		this.askingPrice = askingPrice;
		this.itemID = itemID;
	}

	

	// protocol-specific message constructor - INFORM_ASKING_PRICE
	public Message(int messageType, String sender, String receiver, int askingPrice, int salePrice) {
		this.setMessageType(messageType);
		this.sender = sender;
		this.receiver = receiver;
		this.itemID = itemID;
		this.salePrice = salePrice;
	}

	/**
	 * @return the message
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param message
	 *            the message to set
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

	public String getAuctionType() {
		return auctionType;
	}

	public Integer getAskingPrice() {
		return askingPrice;
	}

	public String getItemID() {
		return itemID;
	}

}
