package bgu.spl.app.Test;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.Receipt;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.impl.MessageBusImpl;

public class MessageBusImplTest {
	private MicroService service1;
	private MicroService service2;
	
	@Before
	public void setUp() throws Exception {
		service1 = new MicroServiceTest("service1");
		service2 = new MicroServiceTest("service2");
		MessageBusImpl.getInstance().register(service1);
		MessageBusImpl.getInstance().register(service2);
	}
	
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGetInstance() {
		assertTrue(MessageBusImpl.getInstance() != null);
	}
	
	@Test
	public void testSendRequest() {
		assertTrue(MessageBusImpl.getInstance().sendRequest(new PurchaseOrderRequest("white-boots", true, 2, "Luke Skywalker"), service1) == false);
	}
	
	@Test
	public void testSendBroadcast() throws InterruptedException { //will check subscribe broadcast and await message
		MessageBusImpl.getInstance().subscribeBroadcast(TickBroadcast.class, service1);
		MessageBusImpl.getInstance().subscribeBroadcast(TickBroadcast.class, service2);
		MessageBusImpl.getInstance().sendBroadcast(new TickBroadcast(777));
		assertTrue(MessageBusImpl.getInstance().awaitMessage(service1).getClass() == TickBroadcast.class);
		assertTrue(MessageBusImpl.getInstance().awaitMessage(service2).getClass() == TickBroadcast.class);
	}
	
	@Test
	public void testComplete() throws InterruptedException { //will check subscribe request and await message
		RestockRequest uselessRequest = new RestockRequest("Crocks", new AtomicInteger(2), "Ron");
		MessageBusImpl.getInstance().subscribeRequest(RestockRequest.class, service2);														
		MessageBusImpl.getInstance().sendRequest(uselessRequest, service1);
		MessageBusImpl.getInstance().complete(uselessRequest, true);
		assertTrue(MessageBusImpl.getInstance().awaitMessage(service1).getClass() == RequestCompleted.class);
	}
}
