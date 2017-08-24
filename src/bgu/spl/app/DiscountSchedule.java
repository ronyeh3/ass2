package bgu.spl.app;

/**
 * Represents a single shoe type Discount Schedule
 */
public class DiscountSchedule {
	
	private String fShoeType;
	private int fTick; 
	private int fAmount;
	
	/**
	 * Constructs a DiscountSchedule
	 * @param shoeType discounted shoe type name
	 * @param tick tick in which the discount needs to be announced
	 * @param amount amount of shoes to be on discount
	 */
	public DiscountSchedule(String shoeType, int tick, int amount) {
		super();
		this.fShoeType = shoeType;
		this.fTick = tick;
		this.fAmount = amount;
	}

	/**
	 * @return the name of the discounted shoe this {@link DiscountSchedule} refers to
	 */
	public final String getShoeType() {
		return fShoeType;
	}
	
	/**
	 * @return the tick in which the discount needs to be announced
	 */
	public final int getTick() {
		return fTick;
	}
	
	/**
	 * @return the amount of shoes to be on discount
	 */
	public final int getAmount() {
		return fAmount;
	}
	
}
