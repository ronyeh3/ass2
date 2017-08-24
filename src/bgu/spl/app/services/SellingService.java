package bgu.spl.app.services;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import bgu.spl.app.BuyResult;
import bgu.spl.app.Receipt;
import bgu.spl.app.Store;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;

/**
 * Represents a store Seller (a cashier), extending {@link MicroService} 
 */
public class SellingService extends MicroService {
	
	private static int sellerIndexNumber=1;
	private AtomicInteger fCurrentTick= new AtomicInteger(0);
	private CountDownLatch m_latchObject;
	private AtomicInteger fRequestsCounter=new AtomicInteger(0);
	private boolean fShouldTerminate;
	private final static Logger logger=Logger.getGlobal();
	
	/**
	 * Constructs a Seller instance.
	 * @param latchObject the CountDownLatch 
	 */
	public SellingService(CountDownLatch latchObject) {
		super("seller "+sellerIndexNumber);
		sellerIndexNumber++;
		m_latchObject = latchObject;
	}
	
	private int getCurrentTick() {
		return fCurrentTick.get();
	}

	/**
	 * Initializes the seller, subscribing to {@link TickBroadcast}, {@link TerminateBroadcast} and {@link PurchaseOrderRequest} messages.
	 */
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class,new Callback<TickBroadcast>(){
			@Override
			public void call(TickBroadcast t) {
				fCurrentTick.getAndIncrement(); 
			}	
		});
		subscribeToPurchaseOrderRequests();
		subscribeBroadcast(TerminateBroadcast.class, terminateMessage -> {
			terminate();
		});
		m_latchObject.countDown();
	}

	private void subscribeToPurchaseOrderRequests(){
		subscribeRequest(PurchaseOrderRequest.class, purchaseRequest -> { 
			BuyResult buyAnswer =  Store.getInstance().take(purchaseRequest.getShoeType(), purchaseRequest.isOnlyOnDiscount());
			switch(buyAnswer){
			case NOT_IN_STOCK:
				logger.info("tick "+getCurrentTick()+", "+this.getName()+" : received "+purchaseRequest.GetCoustomrName()+"'s order of "+purchaseRequest.getShoeType()+", but there were none in stock. waiting for restock answer");
				RestockRequest newRestockRequest=new RestockRequest(purchaseRequest.getShoeType(),  this.fCurrentTick, this.getName());
				fRequestsCounter.incrementAndGet();
				sendRequest(newRestockRequest, isNowOnStock -> {
					fRequestsCounter.decrementAndGet();
					if (fShouldTerminate && 0==fRequestsCounter.get()) terminate();
					if(isNowOnStock){  		
						Receipt receipt = new Receipt(getName(), purchaseRequest.GetCoustomrName(), purchaseRequest.getShoeType(), false, fCurrentTick.get(), purchaseRequest.getPurchasedorderTick().get(), 1);
						Store.getInstance().file(receipt);
						logger.info("tick "+getCurrentTick()+", "+this.getName()+" : the "+purchaseRequest.getShoeType()+"for "+purchaseRequest.GetCoustomrName()+" has arrived! completing the order");
						complete(purchaseRequest, receipt);	
					}
					else   {    // the shoes Couldn't be made
						logger.info("tick "+getCurrentTick()+", "+this.getName()+" : the result of the restock request for "+purchaseRequest.getShoeType()+" for the client "+purchaseRequest.GetCoustomrName()+" is FALSE");
						complete(purchaseRequest, null);
					}
				});									
			break;
			case DISCOUNTED_PRICE:   //which means that was successfully taken in a discounted price (th          
				Receipt receipt = new Receipt(getName(), purchaseRequest.GetCoustomrName(), purchaseRequest.getShoeType(), true, this.fCurrentTick.get(), purchaseRequest.getPurchasedorderTick().get(), 1);
				Store.getInstance().file(receipt);
				logger.info("tick "+getCurrentTick()+", "+this.getName()+" : received "+purchaseRequest.GetCoustomrName()+"'s order of "+purchaseRequest.getShoeType()+", completing with a discounted price!");
				complete(purchaseRequest, receipt);
			break;
			case NOT_ON_DISCOUNT:        // indicates that the client insisted on buying with discount but there are none on discount
				logger.info("tick "+getCurrentTick()+", "+this.getName()+" : received "+purchaseRequest.GetCoustomrName()+"'s order of "+purchaseRequest.getShoeType()+", but there were none on discount");
				complete(purchaseRequest, null);       
			break;
			case REGULAR_PRICE:	 // means that the item was successfully taken with regular price
				Receipt receipt1 = new Receipt(getName(), purchaseRequest.GetCoustomrName(), purchaseRequest.getShoeType(), false, this.fCurrentTick.get(), purchaseRequest.getPurchasedorderTick().get(), 1);
				Store.getInstance().file(receipt1);
				logger.info("tick "+getCurrentTick()+", "+this.getName()+" : received "+purchaseRequest.GetCoustomrName()+"'s order of "+purchaseRequest.getShoeType()+", completing with regular price");
				complete(purchaseRequest, receipt1);  		                                                                                   //of this type was reduced by one)
			break;
			}});  
	}
	
}	



