package bgu.spl.app.messages;

import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.app.services.SellingService;
import bgu.spl.mics.Request;

/**
 * Represents Restock Request message implements {@link Request}
 */
public class RestockRequest implements Request<Boolean>  {            
	
	private final String fShoeType;
	private final AtomicInteger fRequestTick; 
	private final String fSellerName;
	
	/**
	 * Constructs a Restock Request message
	 * @param shoeType the shoe type Requested
	 * @param requestTick the tick in which the request was issued
	 * @param sellerName the name of the {@link SellingService} which sent the request
	 */
	public RestockRequest(String shoeType, AtomicInteger requestTick, String sellerName ) {
		fShoeType=shoeType;
		fRequestTick=requestTick;
		this.fSellerName=sellerName;
	}

	/**
	 * @return the Requested shoe type 
	 */
	public String getShoeType() {
		return fShoeType;
	}

	/**
	 * @return the tick in which the request was issued
	 */
	public AtomicInteger getRequestTick() {
		return fRequestTick;
	}

	/**
	 * @return the name of the {@link SellingService} which sent the request 
	 */
	public String getSellerName (){
		return fSellerName;
	}
	
	/**
	 * @return a String representation of the request (the shoe type)
	 */
	public String toString() {
		return "Restock Request : "+fShoeType;
	}
}
