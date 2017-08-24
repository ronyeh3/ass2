package bgu.spl.app.messages;

import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.app.Receipt;
import bgu.spl.mics.*;

/**
 * Represents Purchase Order Request implements {@link Request}
 */
public class PurchaseOrderRequest implements Request<Receipt> { 
	private final String fShoeType;
	private AtomicInteger fOrderTick;
	private final boolean fIsInsistingDiscount; 
	private final String fClientName;

	/**
	 * Constructs a Purchase Order {@link Request}
	 * @param shoeType the type of shoe requested
	 * @param isInsistingDiscount true if the shoe is wanted only on discount
	 * @param orderTick the tick in which the order was sent
	 * @param clientName the customer (requester) name
	 */
	public PurchaseOrderRequest(String shoeType, boolean isInsistingDiscount, int orderTick, String clientName ) {
		fShoeType=shoeType;
		fOrderTick=new AtomicInteger(orderTick);
		fIsInsistingDiscount=isInsistingDiscount;
		fClientName=clientName;
	}

	/**
	 * @return the ordered shoe type
	 */
	public String getShoeType() {
		return fShoeType;
	}

	/**
	 * @return the tick in which the order was sent
	 */
	public AtomicInteger getPurchasedorderTick() {
		return fOrderTick;
	}

	/**
	 * @return true if the shoe is wanted only on discount, false otherwise
	 */
	public boolean isOnlyOnDiscount() {
		return fIsInsistingDiscount;
	}

	/**
	 * @return the client name
	 */
	public String GetCoustomrName (){
		return fClientName;
	}
	
	/**
	 * @return a String representation of the order (the shoe type)
	 */
	public String toString() {
		return "Purchase Order Request : "+fShoeType;
	}

}