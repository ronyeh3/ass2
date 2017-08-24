package bgu.spl.app.messages;

import bgu.spl.mics.Broadcast;

/**
 * Represents a New Discount Broadcast message implements {@link Broadcast}
 */
public class NewDiscountBroadcast implements Broadcast {
	private final String fShoeType;
	
	/**
	 * Constructs a New Discount Broadcast message
	 * @param shoeType the discounted shoe type 
	 */
	public NewDiscountBroadcast(String shoeType) {
		fShoeType=shoeType;
	}
	
	/**
	 * @return the discounted shoe type
	 */
	public String getDiscountedShoeType() {
		return fShoeType;
	}
}