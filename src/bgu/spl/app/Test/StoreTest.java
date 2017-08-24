package bgu.spl.app.Test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import 	bgu.spl.app.BuyResult;
import bgu.spl.app.ShoeStorageInfo;
import bgu.spl.app.Store;

public class StoreTest {
	@Before
	public void setUp() throws Exception {
		ShoeStorageInfo shoe1 = new ShoeStorageInfo("pink-allstar", 1);
		ShoeStorageInfo shoe2 = new ShoeStorageInfo("red-allstar", 10);
		ShoeStorageInfo shoe3 = new ShoeStorageInfo("ugg", 3);
		ShoeStorageInfo shoe4 = new ShoeStorageInfo("crocks", 0);
		ShoeStorageInfo shoe5 = new ShoeStorageInfo("teva-naot", 9);
		ShoeStorageInfo[] stock = {shoe1, shoe2, shoe3, shoe4, shoe5};
		Store.getInstance().load(stock);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testTake(){
		assertEquals(Store.getInstance().take("no-such-shoe", false), BuyResult.NOT_IN_STOCK); //no such shoe
		assertEquals(Store.getInstance().take("no-such-shoe", true), BuyResult.NOT_IN_STOCK);
		assertEquals(Store.getInstance().take("crocks", true), BuyResult.NOT_IN_STOCK); //not in stock
		assertEquals(Store.getInstance().take("crocks", false), BuyResult.NOT_IN_STOCK);
		assertEquals(Store.getInstance().take("pink-allstar", true), BuyResult.NOT_ON_DISCOUNT);
		assertEquals(Store.getInstance().take("pink-allstar", false), BuyResult.REGULAR_PRICE); //only 1 shoe in stock
		assertEquals(Store.getInstance().take("pink-allstar", false), BuyResult.NOT_IN_STOCK); //not in stock
		assertEquals(Store.getInstance().take("teva-naot", true), BuyResult.DISCOUNTED_PRICE);
		assertEquals(Store.getInstance().take("teva-naot", true), BuyResult.DISCOUNTED_PRICE);
		assertEquals(Store.getInstance().take("teva-naot", false), BuyResult.REGULAR_PRICE);
	}
	
	@Test
	public void testAdd(){
		assertEquals(Store.getInstance().take("no-such-shoe", false), BuyResult.NOT_IN_STOCK);
		Store.getInstance().add("no-such-shoe", 1);
		assertEquals(Store.getInstance().take("no-such-shoe", false), BuyResult.REGULAR_PRICE);
		Store.getInstance().add("crocks", 1);
		assertEquals(Store.getInstance().take("crocks", false), BuyResult.REGULAR_PRICE);
	}
	
	@Test
	public void testAddDiscount(){
		assertEquals(Store.getInstance().take("pink-allstar", true), BuyResult.NOT_ON_DISCOUNT);
		Store.getInstance().addDiscount("pink-allstar", 1);
		assertEquals(Store.getInstance().take("pink-allstar", true), BuyResult.DISCOUNTED_PRICE);
		Store.getInstance().addDiscount("random-name", 1);
		assertEquals(Store.getInstance().take("random-name", true), BuyResult.NOT_IN_STOCK);
	}
}
