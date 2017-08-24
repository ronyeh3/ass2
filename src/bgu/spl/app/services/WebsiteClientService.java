package bgu.spl.app.services;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl.app.PurchaseSchedule;
import bgu.spl.app.messages.*;
import bgu.spl.mics.MicroService;
import java.util.logging.*;

/**
 * Represents a website client, which is a store customer, entering the store. <p>
 * extending  {@link MicroService} 
 */
public class WebsiteClientService extends MicroService {
	private Set<String> fMyWishList;
	private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PurchaseSchedule>> fMyPurchaseSchedule;
	private AtomicInteger fCurrentTick= new AtomicInteger(0);
	private CountDownLatch m_latchObject;	
	private final static Logger logger=Logger.getGlobal();

	/**
	 * Constructs a website client instance.
	 * @param clientName the new customer name.
	 * @param purchaseSchedule the new customer purchase schedule.
	 * @param wishList the new customer wishlist.
	 * @param latchObject 
	 */
	public WebsiteClientService(String clientName, List<PurchaseSchedule> purchaseSchedule, Set<String> wishList, CountDownLatch latchObject){ //Constructor
		super(clientName);
		m_latchObject = latchObject;
		fMyWishList=wishList;
		fMyPurchaseSchedule=new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PurchaseSchedule>>();
		ListIterator<PurchaseSchedule> i=purchaseSchedule.listIterator();	//coping the purchaseSchedule to a map
		while (i.hasNext()) {
			PurchaseSchedule current = i.next();
			int tick=current.getTick();
			if (fMyPurchaseSchedule.containsKey(tick)) fMyPurchaseSchedule.get(tick).add(current); //multiple shoes to buy at the same tick.
			else { 			//this is the first shoe we need to buy at that tick
				ConcurrentLinkedQueue<PurchaseSchedule> newTickSchedule=new ConcurrentLinkedQueue<PurchaseSchedule>();
				newTickSchedule.add(current);
				fMyPurchaseSchedule.put(tick, newTickSchedule);
			}
		}
	}

	private int getCurrentTick() {
		return fCurrentTick.get();
	}

	private void subscribeToTickBroadcastMessages() {
		subscribeBroadcast(TickBroadcast.class, tickMessage -> {	
			fCurrentTick.incrementAndGet(); 
			int tack=fCurrentTick.get();
			if (fMyPurchaseSchedule.containsKey(fCurrentTick.get())) {		//buy the shoes in this list
				ConcurrentLinkedQueue<PurchaseSchedule> purchaseList = fMyPurchaseSchedule.get(fCurrentTick.get());
				int amountOfShoesToBuy=purchaseList.size();
				while (!purchaseList.isEmpty()  ) {           //try purchase and removes the shoe from this tick list					
					String shoesType=purchaseList.poll().getShoeType();
					PurchaseOrderRequest purchaseOrder = new PurchaseOrderRequest(shoesType, false, fCurrentTick.get(), this.getName() );
					sendRequest(purchaseOrder, receipt -> {
						int amountOfShoesWithoutResult=fMyPurchaseSchedule.get(tack).poll().getTick();	//poll removes the
						fMyPurchaseSchedule.get(receipt.getRequestTick()).add(new PurchaseSchedule("", amountOfShoesWithoutResult+1)); // i have less results to wait for
						if (receipt!=null) {
							String shoeType=receipt.getShoeType();
							String dis="luckily";
							if(receipt.isDiscount()==false) dis="no";
							logger.info("tick "+getCurrentTick()+", "+getName()+" has successfully bought "+shoeType+ " with "+dis+" discount ");
							if (fMyWishList.contains(shoeType)) fMyWishList.remove(shoeType);
							if (amountOfShoesWithoutResult==0) fMyPurchaseSchedule.remove(receipt.getRequestTick()); //if that was last shoe to buy at that tick- delete the entry in the map
							if (fMyPurchaseSchedule.isEmpty() && fMyWishList.isEmpty()) { 	//if that was the last shoe in my map and my wishlist
								logger.info("tick "+getCurrentTick()+", "+getName()+" has finished the shoping and now terminating");
								terminate(); 	
							}
						}
					});
				}
				purchaseList.add(new PurchaseSchedule("", -amountOfShoesToBuy)); // keep track of waiting for results- as long as negetive- i'm still waiting for results 
			}
		});
	}

	private void subscribeToDiscountBroadcastMessages() {		
		subscribeBroadcast(NewDiscountBroadcast.class, newDiscountMessage -> {	
			String discountedShoe=newDiscountMessage.getDiscountedShoeType();
			if (fMyWishList.contains(discountedShoe)){ // buy the shoes if on the WishList
				PurchaseOrderRequest purchaseOrder = new PurchaseOrderRequest(discountedShoe, true, getCurrentTick(), this.getName()); 
				sendRequest(purchaseOrder , receipt -> {
					if (receipt!=null) {
						logger.info("tick "+getCurrentTick()+", "+getName()+" has successfully bought "+discountedShoe+" with an end-of-season discounted price");
						fMyWishList.remove(discountedShoe);
						if (fMyPurchaseSchedule.isEmpty() && fMyWishList.isEmpty()) {
							logger.info("tick "+getCurrentTick()+", "+getName()+" has finished the shoping and now terminating");
							terminate();
						}
					}
				});
			}
		});
	}

	/**
	 * Initialises the customer, subscribing to {@link TickBroadcast} and {@link NewDiscountBroadcast} messages.
	 */
	@Override
	protected void initialize() {
		subscribeToTickBroadcastMessages();
		subscribeToDiscountBroadcastMessages();
		subscribeBroadcast(TerminateBroadcast.class, terminateMessage -> {
			terminate();
		});
		m_latchObject.countDown();
	}



}