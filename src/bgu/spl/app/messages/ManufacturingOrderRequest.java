package bgu.spl.app.messages;

import bgu.spl.app.Receipt;
import bgu.spl.mics.Request;

/**
 * ManufacturingOrderRequset implements {@link Request}.
 * Represents a manufacturing order request.
 */
public class ManufacturingOrderRequest implements Request<Receipt>  { 

	private final String fShoeType;
	private final int fOrderRequestTick;
	private final int fAmountOrdered;

	/**
	 * Constructs a Manufacturing Order Request
	 * @param fShoeType	the shoe type to order
	 * @param fAmountOrdered the amount of shoes to order
	 * @param fTick	the tick in which the order was sent from the manager
	 */
	public ManufacturingOrderRequest(String shoeType, int orderRequestTick, int amaount) {
		this.fShoeType = shoeType;
		this.fOrderRequestTick = orderRequestTick;
		this.fAmountOrdered = amaount;
	}

	/**
	 * @return the ordered shoe type
	 */
	public final String getShoeType() {
		return fShoeType;
	}

	/**
	 * @return the tick in which the order was sent from the manager
	 */
	public final int getPurchasedorderTick() {
		return fOrderRequestTick;
	}

	/**
	 * 
	 * @return the amount of shoes ordered 
	 */
	public final int getAmaount() {
		return fAmountOrdered;
	}
	
	/**
	 * @return a String representing the order
	 */
	public String toString() {
		return "Manufacturing Order Request : shoe-"+fShoeType+", amount- "+fAmountOrdered;
	}
	
}