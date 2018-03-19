
public class bid {
	String bidderID;
	Integer bidPrice;
	
	bid(String bidderID, Integer bidPrice){
		this.bidderID = bidderID;
		this.bidPrice = bidPrice;
	}

	public String getBidderID() {
		return bidderID;
	}

	public void setBidderID(String bidderID) {
		this.bidderID = bidderID;
	}

	public Integer getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(Integer bidPrice) {
		this.bidPrice = bidPrice;
	}
	 
}
