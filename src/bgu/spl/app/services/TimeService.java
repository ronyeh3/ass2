package bgu.spl.app.services;

import bgu.spl.app.Store;
import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;

/**
 * Represents a Time Service, extending {@link MicroService}
 */
public class TimeService extends MicroService { //singleton??-NO
	private Timer fClock;
	private AtomicInteger fLifeTime; 	//duration of the simulation
	private int fSpeed;
	private TickBroadcast fTick;
	private AtomicInteger fCurrentTick;
	private CountDownLatch m_latchObject;
	private final static Logger logger=Logger.getGlobal();
	
	/**
	 * Constructs a Time Service
	 * @param speed speed of a single tick
	 * @param duration amount of ticks for the simulation to run
	 * @param latchObject
	 */
	public TimeService(int speed, int duration, CountDownLatch latchObject) { 
		super("timer");
		m_latchObject = latchObject;
		fLifeTime=new AtomicInteger(duration);
		fCurrentTick=new AtomicInteger(0);
		fClock=new Timer();
		this.fSpeed=speed;
		
	}
	
	/**
	 * initializes the Time Service
	 */
	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminateMessage -> {
			terminate();
		});
		
		fClock.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					m_latchObject.await();
				} catch (InterruptedException exception) {
					exception.printStackTrace();
				}
				fTick=new TickBroadcast(fCurrentTick.getAndIncrement());
				sendBroadcast(fTick);
				logger.info("tick "+fCurrentTick+ " has been broadcats");
				fLifeTime.set(fLifeTime.decrementAndGet());
				if (fLifeTime.get()==0) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					
						e.printStackTrace();
					}
					fClock.cancel();	
					terminate();
					sendBroadcast(new TerminateBroadcast());
					logger.info("Time Service is Terminating");
				}	
			}
		}, 0, fSpeed);
		
	}
}