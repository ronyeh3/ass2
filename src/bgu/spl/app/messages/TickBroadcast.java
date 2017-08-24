package bgu.spl.app.messages;
import bgu.spl.mics.Broadcast;

/**
 *Represents a Tick Broadcast message implementing {@link Broadcast}
 */
public class TickBroadcast implements Broadcast{
	private final int fTickNumber;

	/**
	 * Constructs a Tick Broadcast message
	 * @param currentTick the current tick
	 */
	public TickBroadcast(int currentTick){
		fTickNumber=currentTick;
	}
	
	/**
	 * @return the 
	 */
	public int getTickNumber(){
		return fTickNumber;
	}
	
}