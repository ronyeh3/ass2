package bgu.spl.mics.impl;

import bgu.spl.mics.*;
import java.util.logging.*;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * MessageBusImpl implements {@link MessageBus} implemented as singleton.
 */
public class MessageBusImpl implements MessageBus {
	
	private RequestsSubscribers fRequestsSubscribersInstance;  
	private BroadcastsSubscribers fBroadcastsSubscribersInstance;
	private ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>> fMicroServicesMessagesQueues;
	private ConcurrentHashMap<Request<?>,MicroService> fMicroServicesWaitingForResult;
	private final static Logger logger=Logger.getGlobal();

	
	private static class RequestsSubscribers {
		
		private  ConcurrentHashMap<Class<? extends Message>, CopyOnWriteArrayList<MicroService>> requestSubscribersQueues;		//key = name of message, value = queue of subscribers to the particular massage
		private  ConcurrentHashMap<Class<? extends Message>, ListIterator<MicroService>> requestSubscribersQueuesIterators; 	//key = name of message, value = iterator for a subscribers List of MassgeSubQlist 
		
		private RequestsSubscribers() {
			requestSubscribersQueues = new ConcurrentHashMap< Class<? extends Message> , CopyOnWriteArrayList<MicroService>>();
			requestSubscribersQueuesIterators = new ConcurrentHashMap< Class<? extends Message> , ListIterator<MicroService>>();
		}
		
	}	
	
	private static class BroadcastsSubscribers {
		
		private ConcurrentHashMap< Class<? extends Broadcast> , CopyOnWriteArrayList<MicroService>> broadcastsSubscribers;	//key = name of message, value = queue of subscribers to the particular massage
		
		private BroadcastsSubscribers(){
			broadcastsSubscribers = new ConcurrentHashMap< Class<? extends Broadcast> , CopyOnWriteArrayList<MicroService>>();
		}
		
	}
	
	private static class MessageBusHolder {
        private static MessageBusImpl messageBusInstance = new MessageBusImpl();  
        
    }
	
	private MessageBusImpl() {
		fRequestsSubscribersInstance=new RequestsSubscribers();
		fBroadcastsSubscribersInstance=new BroadcastsSubscribers();
		fMicroServicesMessagesQueues=new ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>>();
		fMicroServicesWaitingForResult = new ConcurrentHashMap<Request<?>,MicroService>();
	}
	
	/**
	 * @return the MessageBus instance (singleton)
	 */
	public static MessageBusImpl getInstance() {            //singleton
        return MessageBusHolder.messageBusInstance;
    }
	
	/**
	 * subscribes the {@link MicroService} m to a specified {@link Request} type messages
	 * @param type the specific {@link Request} type Class object to subscribe
	 * @param m the subscriber
	 */
	public  void subscribeRequest(Class<? extends Request> type, MicroService m) { //Subscribing to message types is done by using the required message class
		CopyOnWriteArrayList<MicroService> currentQueue =fRequestsSubscribersInstance.requestSubscribersQueues.get(type);
		if(currentQueue!=null){
			int cuurentiter = fRequestsSubscribersInstance.requestSubscribersQueuesIterators.get(type).nextIndex();
			currentQueue.add(m);
			logger.info(m.getName()+" has subscribed to "+type.getSimpleName()+"messages");
			ListIterator<MicroService> newiter = currentQueue.listIterator(cuurentiter);
			fRequestsSubscribersInstance.requestSubscribersQueuesIterators.put(type, newiter);
			return;
		} 
		else { // no microservice subscribed to this type of message yet
			currentQueue = new CopyOnWriteArrayList<MicroService>();	
			currentQueue.add(m);
			logger.info(m.getName()+" has subscribed to "+type.getSimpleName()+"messages");
			fRequestsSubscribersInstance.requestSubscribersQueues.put(type, currentQueue) ;
			ListIterator<MicroService> newIterator = currentQueue.listIterator();
			fRequestsSubscribersInstance.requestSubscribersQueuesIterators.put(type, newIterator);
		}
	}

	/**
	 * subscribes the {@link MicroService} m to a specified {@link Broadcast} type messages
	 * @param type the specific Broadcast type Class object to subscribe
	 * @param m the subscriber
	 */
	public  void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {	    	
		CopyOnWriteArrayList<MicroService> currentQueue =fBroadcastsSubscribersInstance.broadcastsSubscribers.get(type);
		if(currentQueue!=null) {
			currentQueue.add(m);
			logger.info(m.getName()+" has subscribed to "+type.getSimpleName()+"messages");
			return;
		}	
		else { // no microservice subscribed to this type of message yet
			currentQueue = new CopyOnWriteArrayList<MicroService>();	
			currentQueue.add(m);
			logger.info(m.getName()+" has subscribed to "+type.getSimpleName()+"messages");
			fBroadcastsSubscribersInstance.broadcastsSubscribers.put(type, currentQueue);
		}
	}

	/**
	 * sends a {@link Broadcast} message to all its subscribers
	 * @param b the message to a send
	 */
	public  void sendBroadcast(Broadcast b) {
		CopyOnWriteArrayList<MicroService> subscribersQueue =fBroadcastsSubscribersInstance.broadcastsSubscribers.get(b.getClass()); 
		if(subscribersQueue!=null) {                                      
			Iterator<MicroService> i=subscribersQueue.iterator();
			while (i.hasNext()) {
				MicroService currentService= i.next();
				LinkedBlockingQueue<Message> microServiceQueue =fMicroServicesMessagesQueues.getOrDefault(currentService, null);
				if(microServiceQueue!=null)
					try {
						microServiceQueue.put(b);
					} catch (InterruptedException exception) {
						exception.printStackTrace();
					}
			}	
		}
	}

	private   MicroService nextInRoundRubin(Request<?> r) {										
		synchronized (RequestsSubscribers.class) {	
		ListIterator<MicroService> subscribersQueueIterator =fRequestsSubscribersInstance.requestSubscribersQueuesIterators.get(r.getClass());    // EMPHSISE ITS GETTING THE RELEVANT Q BY THE TYPE OF MESEGE CLASS AND NOT REFERANCE OF A OBJECT
		if(subscribersQueueIterator==null) return null;
		else if(subscribersQueueIterator.hasNext()) return subscribersQueueIterator.next();
		while(subscribersQueueIterator.hasPrevious()) subscribersQueueIterator.previous();
		return subscribersQueueIterator.next();
	}}

	/**
	 * sends a {@link Request} message to a subscriber of the message type, in a RoundRubin manner
	 * @param r the message to a send
	 * @param requester the sender
	 * @return true if a {@link MicroService} received the message, false otherwise
	 */
	public  boolean sendRequest(Request<?> r, MicroService requester) { 
		MicroService subscriber = nextInRoundRubin(r);
		if(subscriber==null) return false;
		LinkedBlockingQueue<Message> subscriberMessagesQueue =fMicroServicesMessagesQueues.getOrDefault(subscriber, null);
		if(subscriberMessagesQueue!=null ){
			fMicroServicesWaitingForResult.put(r, requester);
			try {
				subscriberMessagesQueue.put(r);	//the sending of the request
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}      
			logger.info(requester.getName()+" has sent a "+r.toString());
			return true;
		}
		logger.info(requester.getName()+" has sent a "+r.toString()+", but no service has recived it");
		return false;
	}       

	/**
	 * registers a {@link MicroService} to the MessageBus, providing it with a personal Message Queue
	 * @param m the registering MicroService
	 */
	public void register(MicroService m) {
		if (! fMicroServicesMessagesQueues.contains(m.getClass()))
			fMicroServicesMessagesQueues.put(m,new LinkedBlockingQueue<Message>());
		logger.info(m.getName()+" has registered to the MessageBus, and now has a messages queue");
	}

	/**
	 * unregisters a {@link MicroService} from the MessageBus
	 * @param m the unregistering MicroService
	 */
	public synchronized void unregister(MicroService m) {
		Iterator<Entry<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>>> i = fBroadcastsSubscribersInstance.broadcastsSubscribers.entrySet().iterator();
		while (i.hasNext()) {	//unregister from broadcasts
			Map.Entry<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> currentMessage = i.next();
			if(currentMessage.getValue().contains(m)) currentMessage.getValue().remove(m);
		}	   
		for (Entry<Class<? extends Message>, CopyOnWriteArrayList<MicroService>> currentMessage : fRequestsSubscribersInstance.requestSubscribersQueues.entrySet()){  //unregister from requests
			if(currentMessage.getValue().contains(m)) {
				ListIterator<MicroService> j = fRequestsSubscribersInstance.requestSubscribersQueuesIterators.get(currentMessage.getKey());
				int index = j.nextIndex();  // saving the iterator index for the new iterator
				while (j.hasPrevious ()) {
					if (j.previous().equals(m) ){
						index--;
						break;
					}	   
				}
				currentMessage.getValue().remove(m);  
				this.fRequestsSubscribersInstance.requestSubscribersQueuesIterators.replace(currentMessage.getKey(), currentMessage.getValue().listIterator(index));  // make a new iterator and put him at the last iterator index   
			}
		}
		this.fMicroServicesMessagesQueues.remove(m);
		
		this.fMicroServicesWaitingForResult.remove(m);
		logger.info(m.getName()+" has unregistered from the MessageBus");
	}

	/**
	 * creates and sends a {@link RequestCompleted} message to the requester {@link MicroService} with the request result
	 * @param r the completed {@link Request}
	 * @param result the Request result 
	 */
	public synchronized  <T> void complete(Request<T> r, T result){
		if(fMicroServicesWaitingForResult.containsKey(r)){
			RequestCompleted<T> requestCompletedMsg = new RequestCompleted(r, result);
			MicroService requster = this.fMicroServicesWaitingForResult.get(r);
			if(requster==null) return;
			LinkedBlockingQueue<Message> requesterQueue = this.fMicroServicesMessagesQueues.get(requster);  
			this.fMicroServicesWaitingForResult.remove(r);
			try {
				requesterQueue.put(requestCompletedMsg);
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
		} 
	}

	/**
	 * @param m the {@link MicroService} taking its messages 
	 * @return the oldest message in the messages queue of the MicroService
	 */
	public Message awaitMessage(MicroService m) throws InterruptedException {
		LinkedBlockingQueue<Message> messagesQueue = fMicroServicesMessagesQueues.get(m); 
		if (messagesQueue==null) throw new  IllegalStateException();
		Message msg =  messagesQueue.take();
		return msg;
	}

}