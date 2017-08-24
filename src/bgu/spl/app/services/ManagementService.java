package bgu.spl.app.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import bgu.spl.app.DiscountSchedule;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.app.Store;
import bgu.spl.app.messages.ManufacturingOrderRequest;
import bgu.spl.app.messages.NewDiscountBroadcast;

/**
 * Represents a Management Service extends {@link MicroService}
 */
public class ManagementService extends MicroService{ 

	Object lock = new Object();
	private CountDownLatch m_latchObject;
	private AtomicInteger fCurrentTick= new AtomicInteger(0);
	private ConcurrentHashMap<Integer,ConcurrentLinkedQueue<DiscountSchedule>> fDiscountSchedule ;
	private ConcurrentHashMap<String, ArrayList<ManufacturingOrderRequest>> orderedShoes;
	private final ConcurrentHashMap<ManufacturingOrderRequest,LinkedBlockingQueue<RestockRequest>> manufacturyToRestock; //stores the restock requested for each RestockRequest order made
	private final static Logger logger=Logger.getGlobal();
	private AtomicInteger fRequestsCounter=new AtomicInteger(0);
	private boolean fShouldTerminate;

	/**
	 * constructs a Management Service
	 * @param discountSchedule
	 * @param latchObject the CountDown Latch
	 */
	public ManagementService(List<DiscountSchedule> discountSchedule, CountDownLatch latchObject){
		super("manager");
		m_latchObject = latchObject;
		manufacturyToRestock = new ConcurrentHashMap<ManufacturingOrderRequest,LinkedBlockingQueue<RestockRequest>>();
		fDiscountSchedule  = new ConcurrentHashMap<Integer,ConcurrentLinkedQueue<DiscountSchedule>>();
		orderedShoes = new ConcurrentHashMap<String, ArrayList<ManufacturingOrderRequest>>();
		Iterator<DiscountSchedule> i = discountSchedule.iterator();
		while(i.hasNext()){
			DiscountSchedule discountScedule = i.next();
			if(! fDiscountSchedule.containsKey(discountScedule.getTick())) { //no discount was scheduled at that tick yet
				ConcurrentLinkedQueue<DiscountSchedule> newDiscountsQueue = new ConcurrentLinkedQueue<DiscountSchedule>();
				newDiscountsQueue.add(discountScedule);
				fDiscountSchedule.put(discountScedule.getTick(), newDiscountsQueue);
			}
			else fDiscountSchedule.get(discountScedule.getTick()).add(discountScedule);
		}
	}
	
	private int getCurrentTick() {
		return fCurrentTick.get();	
	}
	
	private void subscribeToTickBroadcast() {
		subscribeBroadcast(TickBroadcast.class,new Callback<TickBroadcast>(){
			@Override
			public void call(TickBroadcast t) {
				fCurrentTick.incrementAndGet();
				if(fDiscountSchedule.containsKey(fCurrentTick.get() ) ) {
					while (fDiscountSchedule.get(fCurrentTick.get()).size()>0) {
						DiscountSchedule currentDiscount = fDiscountSchedule.get(fCurrentTick.get()).poll();
						Store.getInstance().addDiscount(currentDiscount.getShoeType(), currentDiscount.getAmount());
						sendBroadcast(new NewDiscountBroadcast(currentDiscount.getShoeType()));
						logger.info("tick "+getCurrentTick()+", the manager has announced: new discount on the "+currentDiscount.getShoeType()+" !");
					}
				}
			}
		}); 
	}
	
	private void subscribeToRestockRequest() {
		subscribeRequest(RestockRequest.class,  restockRequest ->{ 
			String shoeType = restockRequest.getShoeType();
			if (orderedShoes.containsKey(shoeType)) {
				ManufacturingOrderRequest order = orderedShoes.get(shoeType).get(0);
				int qunatity = order.getAmaount();
				int waiting = manufacturyToRestock.get(order).size();
				if (qunatity > waiting){
					manufacturyToRestock.get(order).add(restockRequest);
					logger.info("tick "+getCurrentTick()+", the manager has a upcoming factory Request for " +restockRequest.getShoeType()+" and try to save one ");
				}
				else makeOrder(shoeType,restockRequest);
			}
			else makeOrder(shoeType,restockRequest);
		});
	}
	
	/**
	 * initializes the Management Service, subscribing it to {@link TickBroadcast} and {@link RestockRequest} messages
	 */
	@Override
	protected void initialize() {
		subscribeToTickBroadcast();
		subscribeToRestockRequest();
		subscribeBroadcast(TerminateBroadcast.class, terminateMessage -> {
			terminate();
		});
		m_latchObject.countDown();
	}

	private void makeOrder(String shoeType,RestockRequest restockRequest){ //making restock request if necessary
		ManufacturingOrderRequest newOrder = new ManufacturingOrderRequest(shoeType,fCurrentTick.get(),
				(fCurrentTick.get() % 5) + 1);
		orderedShoes.putIfAbsent(shoeType, new ArrayList<ManufacturingOrderRequest>());
		orderedShoes.get(shoeType).add(0, newOrder);
	//	logger.info("tick "+getCurrentTick()+", the manager has sent a Manufacturing Order Request for "+((fCurrentTick.get()%5)+1)+" "+restockRequest.getShoeType());
		manufacturyToRestock.put(newOrder, new LinkedBlockingQueue<RestockRequest>());
		manufacturyToRestock.get(newOrder).add(restockRequest);
		fRequestsCounter.incrementAndGet();
		sendRequest(newOrder, receipt -> {
			fRequestsCounter.decrementAndGet();
			if (fShouldTerminate && 0==fRequestsCounter.get()) terminate();
			LinkedBlockingQueue<RestockRequest> queueOfRestockRequest = manufacturyToRestock.get(newOrder);
			if (receipt == null) {
				while (!queueOfRestockRequest.isEmpty())
					complete(queueOfRestockRequest.poll(), false);
			} 
			else {
				int waitingRestocks =queueOfRestockRequest.size();
				int quantityManufactured=newOrder.getAmaount();
				Store.getInstance().file(receipt);
				Store.getInstance().add(shoeType, quantityManufactured-waitingRestocks);
				logger.info("tick "+getCurrentTick()+", the manager successfully got the order of "+receipt.getAmountSold()+" "+restockRequest.getShoeType());
				while (!queueOfRestockRequest.isEmpty())
					complete(queueOfRestockRequest.poll(), true);
			}
			orderedShoes.get(shoeType).remove(newOrder);
			manufacturyToRestock.remove(newOrder);
			if(orderedShoes.get(shoeType).isEmpty()) orderedShoes.remove(shoeType);
		});	
	}

}