
public class ItemAuction {
	String itemID;
	Integer startingPrice;
	Integer reservePrice;
	Integer decrement;
	Integer increment;
	
	ItemAuction(String itemID, Integer startingPrice, Integer reservePrice, Integer decrement, Integer increment){
		this.itemID = itemID;
		this.startingPrice = startingPrice;
		this.reservePrice = reservePrice;
		this.decrement = decrement;
		this.increment = increment;
	}

	public Integer getStartingPrice() {
		return startingPrice;
	}

	public void setStartingPrice(Integer startingPrice) {
		this.startingPrice = startingPrice;
	}

	public Integer getReservePrice() {
		return reservePrice;
	}

	public void setReservePrice(Integer reservePrice) {
		this.reservePrice = reservePrice;
	}

	public Integer getDecrement() {
		return decrement;
	}

	public void setDecrement(Integer decrement) {
		this.decrement = decrement;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public Integer getIncrement() {
		return increment;
	}

	public void setIncrement(Integer increment) {
		this.increment = increment;
	}
	

}
