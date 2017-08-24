package bgu.spl.app.services;

import bgu.spl.app.Receipt;
import bgu.spl.app.messages.ManufacturingOrderRequest;
import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Representing a Shoe Factory, extending {@link MicroService}
 */
public class ShoeFactoryService extends MicroService{

	private class Order { //representing an received order for shoes

		private String shoeType;
		private int amountLeftToMake;
		private int orderedAmount; 
		private ManufacturingOrderRequest request;

		private Order(String type, int amount, ManufacturingOrderRequest request) {
			this.shoeType=type;
			this.orderedAmount=amount;
			this.amountLeftToMake=orderedAmount;
			this.request=request;
		}

		public String getShoeType() {
			return shoeType;
		}

		public int getOrderedAmount() {
			return orderedAmount;
		}

		public ManufacturingOrderRequest getRequest() {
			return request;
		}

		public void makeShoe() {
			this.amountLeftToMake--;
		}

		public boolean doneWithThisOrder() {
			return amountLeftToMake==0;
		}
		public String toString() {
			return (orderedAmount+" "+shoeType);
		}

	}

	private AtomicInteger fCurrentTick=new AtomicInteger(0);
	private ConcurrentLinkedQueue<Order> fOrdersQueue;
	private Order fCurrentOrder=null;
	private static int factoryIndx=1;
	private CountDownLatch m_latchObject;
	private boolean fShouldTerminate;
	private final static Logger logger=Logger.getGlobal();

	/**
	 * Constructs a Shoe Factory
	 * @param duration the number of ticks of the simulation
	 * @param latchObject
	 */
	public ShoeFactoryService(CountDownLatch latchObject) {
		super("factory "+factoryIndx);
		factoryIndx++;
		m_latchObject = latchObject;
		fOrdersQueue=new ConcurrentLinkedQueue<Order>();
		
	}

	private int getCurrTick() {
		return fCurrentTick.get();
	}

	/**
	 * Initializes the factory, subscribing to {@link TickBroadcast} and {@link ManufacturingOrderRequest} messages.
	 */
	@Override
	protected void initialize() {
		subscribeToTickBroadcast();
		subscribeToManufacturingOrderRequest();
		subscribeBroadcast(TerminateBroadcast.class, terminateMessage -> {
		fShouldTerminate = false;
		});
		subscribeBroadcast(TerminateBroadcast.class, terminateMessage -> {
			terminate();
		});
		m_latchObject.countDown();
	}
	
	private void subscribeToTickBroadcast() {
		subscribeBroadcast(TickBroadcast.class, tick -> {
			fCurrentTick.getAndIncrement();
                                				//with everyday passed i have less days to make shoes
			
			if (fCurrentOrder==null) return; 		//I have'nt got my first order yet
			else
				fCurrentOrder.makeShoe();   //decrement
				
			if (fCurrentOrder.doneWithThisOrder()) {
				logger.info("tick "+getCurrTick()+", "+this.getName()+" : finished manufacturing the order of: "+fCurrentOrder.toString());
				Receipt receipt=new Receipt(this.getName(), "store", fCurrentOrder.getShoeType(), false, fCurrentTick.get(), fCurrentOrder.getRequest().getPurchasedorderTick(), fCurrentOrder.getOrderedAmount());
				complete(fCurrentOrder.getRequest(), receipt);
				fCurrentOrder=fOrdersQueue.poll(); // null if empthy
			}
		});
	}
	
	private void subscribeToManufacturingOrderRequest() {
		subscribeRequest(ManufacturingOrderRequest.class, orderRequest -> {	
			int amountOrdered=orderRequest.getAmaount();
				logger.info("tick "+getCurrTick()+", "+this.getName()+" : received a ManufacturingOrderRequest for "+orderRequest.getAmaount()+" "+orderRequest.getShoeType());
				String shoeType=orderRequest.getShoeType();
				Order newOrder=new Order(shoeType,amountOrdered, orderRequest);
				if (fCurrentOrder==null) fCurrentOrder=newOrder;
				else {
					fOrdersQueue.add(newOrder);
				}
			
		});
	}
	
}