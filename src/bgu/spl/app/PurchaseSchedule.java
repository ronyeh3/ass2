package bgu.spl.app;

/**
 * Represents a single shoe Purchase Schedule
 */
public class PurchaseSchedule { 
	
	private final String fShoeType;
	private final int fTick; //the tick number to send the PurchaseOrderRequest at
	
	/**
	 * constructs a Purchase Schedule
	 * @param shoeType	name of the shoe to buy
	 * @param tickToBuy	tick to buy the shoe
	 */
	public PurchaseSchedule(String shoeType, int tickToBuy) {
		fShoeType=shoeType;
		fTick=tickToBuy;
	}
	
	/**
	 * @return the name of the shoe this {@link PurchaseSchedule} refers to
	 */
	public String getShoeType() {
		return fShoeType;
	}
	
	/**
	 * @return the tick to buy the shoe this {@link PurchaseSchedule} refers to
	 */
	public int getTick() {
		return fTick;
	}
	
}