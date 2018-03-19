
public class item {
	String itemID;
	Integer acceptBidPrice;
	Integer maxPrice;
	Integer increment;
	
	item(String itemID, Integer acceptBidPrice, Integer maxPrice, Integer increment){
		this.itemID = itemID;
		this.acceptBidPrice = acceptBidPrice;
		this.maxPrice = maxPrice;
		this.increment = increment;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public Integer getAcceptBidPrice() {
		return acceptBidPrice;
	}

	public void setAcceptBidPrice(Integer acceptBidPrice) {
		this.acceptBidPrice = acceptBidPrice;
	}

	public Integer getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(Integer maxPrice) {
		this.maxPrice = maxPrice;
	}

	public Integer getIncrement() {
		return increment;
	}

	public void setIncrement(Integer increment) {
		this.increment = increment;
	}
	

}
