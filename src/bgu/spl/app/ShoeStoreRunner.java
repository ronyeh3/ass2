package bgu.spl.app;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.*;

import bgu.spl.app.services.ManagementService;
import bgu.spl.app.services.SellingService;
import bgu.spl.app.services.ShoeFactoryService;
import bgu.spl.app.services.TimeService;
import bgu.spl.app.services.WebsiteClientService;
import bgu.spl.app.MyFormatter;

import java.util.logging.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class ShoeStoreRunner {
	
	private final static Logger logger=Logger.getGlobal();
	private final static ConsoleHandler consoleHandler=new ConsoleHandler();
	private final static MyFormatter  myFormatter=new MyFormatter();

	public static void main(String[] args) {
		LogManager.getLogManager().reset();
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.CONFIG);
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).addHandler(consoleHandler);
		consoleHandler.setFormatter(myFormatter); 
		
		List<Thread> runningThreads = new ArrayList<Thread>(); //list of all running threads
		FileReader fileReader;
		JsonObject jsonTree = null;
	try {
		fileReader = new FileReader (args[0]);
		JsonParser jsonParser = new JsonParser();
		 jsonTree =  (JsonObject)jsonParser.parse(fileReader);
		
	}
	 catch (FileNotFoundException | RuntimeException e) {
		 System.out.println("eror reading json");
		 e.printStackTrace();
	 }	  
		
	int numOfServices = 0;
		
	
		int timeSpeed;
		int timeDuration;
		ArrayList<DiscountSchedule>  discountSchedule = new ArrayList<DiscountSchedule> ();
		int factoryAmount = 0;
		int sellerAmount = 0;
		ArrayList<WebsiteClientService> allClients = new ArrayList<WebsiteClientService>();
		ArrayList<ShoeFactoryService> allFactories = new ArrayList<ShoeFactoryService>();
		ArrayList<SellingService> allSellers = new ArrayList<SellingService>();
		ManagementService manager;
		TimeService timeKeeper;
		
		
		storInitial(jsonTree);

		      
	//srevics	     
	JsonElement service = jsonTree.get("services");
		  
		  JsonObject  servicetype = service.getAsJsonObject();
		  
		  // Time
		     timeSpeed = servicetype.get("time").getAsJsonObject().get("speed").getAsInt();
		     timeDuration = servicetype.get("time").getAsJsonObject().get("duration").getAsInt(); 

	     /// Manager Schedule   
		     JsonObject jmanager =  servicetype.get("manager").getAsJsonObject(); 
			 numOfServices ++ ;
		     if ( jmanager.has("discountSchedule")){
		    	 JsonArray scjual = jmanager.get("discountSchedule").getAsJsonArray();
		     		
		    	 Iterator<JsonElement> e =  scjual.iterator();
		    
		    	 while(e.hasNext()){
		    		 JsonObject itemToDiscount =  e.next().getAsJsonObject();
		    		 String discShoeType = itemToDiscount.get("shoeType").getAsString();
		    		 int discTime = itemToDiscount.get("tick").getAsInt();
		    		 int discAmount = itemToDiscount.get("amount").getAsInt();
		    		 DiscountSchedule discount = new DiscountSchedule(discShoeType, discTime, discAmount);

		    			 discountSchedule.add(discount);
		    		 }
		    }
		    
		     
		     
		    factoryAmount =   servicetype.get("factories") .getAsInt() ;
		    numOfServices = numOfServices +  factoryAmount;
		    sellerAmount = servicetype.get("sellers") .getAsInt() ;
		    numOfServices = numOfServices +  sellerAmount;

		    
		    
		    //customers info arrey
		    JsonArray arr3 = servicetype.get("customers").getAsJsonArray();
		    
		    Iterator<JsonElement> byeriter =  arr3.iterator();
		    numOfServices  = numOfServices + arr3.size();
		    
		    
		    CountDownLatch latchObject = new CountDownLatch (numOfServices);		// Latch Object Initialzation
		    
		    
		    while(byeriter.hasNext()){ //for each customer
		    	JsonObject currentCustomer =  byeriter.next().getAsJsonObject();
		    	String customerName = currentCustomer.get("name").getAsString();

				List<PurchaseSchedule> purchaseSchedule = new ArrayList<PurchaseSchedule>();
				Set<String> wishList = new HashSet<String>();
		    	 
		    	JsonArray tempWishList =  currentCustomer.get("wishList").getAsJsonArray();
		    	for(int j = 0; j < tempWishList.size(); j++) {
		    		String wishListShoeType = tempWishList.get(j).getAsString();
		    		wishList.add(wishListShoeType);
		    	}
		    	 
		    	JsonArray tempPurchaseSchedule =  currentCustomer.get("purchaseSchedule").getAsJsonArray();
			    Iterator<JsonElement> ps =  tempPurchaseSchedule.iterator();
			    while(ps.hasNext()){
			    	JsonObject currentPS =  ps.next().getAsJsonObject();
			    	String regListShoeType =  currentPS.get("shoeType").getAsString();
			    	int regListTick = currentPS.get("tick").getAsInt();
			    	PurchaseSchedule regPurchase = new PurchaseSchedule(regListShoeType, regListTick);
			    	purchaseSchedule.add(regPurchase);
			    }

			    	WebsiteClientService webclient = new WebsiteClientService(customerName, purchaseSchedule, wishList, latchObject);  //ctor
			    	allClients.add(webclient);
			    	Thread t1 = new Thread(webclient);
			    	t1.setName("Client " + customerName);
			    	runningThreads.add(t1);
			
			    
		    }
		
		   
		    timeKeeper = new TimeService(timeSpeed, timeDuration, latchObject);   //ctor
		    
	
		    manager = new ManagementService(discountSchedule, latchObject);    //ctor
		    Thread t2 = new Thread(manager);
		    t2.setName("manager");
		    runningThreads.add(t2);
		    
		    for (int i1 = 1; i1 <= factoryAmount; i1++){
		    	ShoeFactoryService factoryNew = new ShoeFactoryService(latchObject);    //ctor
		    	allFactories.add(factoryNew);
		    	t2 = new Thread(factoryNew);
		    	t2.setName("factory " + i1);
		    	runningThreads.add(t2);
		    }
		    
		    for (int i1 = 1; i1 <= sellerAmount; i1++){
		    	SellingService sellerNew = new SellingService(latchObject);
		    	allSellers.add(sellerNew);
		    	t2 = new Thread(sellerNew);
		    	t2.setName("seller " + i1);
		    	runningThreads.add(t2);
		    }
	
		    for (Thread t : runningThreads)  
               t.start();
	
	    	try {
				latchObject.await();
			} catch (InterruptedException e1) {}
            //wait for all microservices to start before we launch time service
		    timeKeeper.run();   // start up		    
		    
		   for (Thread t : runningThreads) { //wait for all threads to terminate!! eyal need to fix the termination of everyting. maybe dont need counter of requst send in webclient , just termeniate
			try {																
				t.join();
			} catch (InterruptedException e) {}
		   }
			try {
				Thread.sleep(500); //for better print console
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    
			Store.getInstance().print();
		  
		    System.out.println("\nFinish!");
		    

		    
		}
 	
	public static void storInitial(JsonObject jsonTree){	// first storage initial - with no discount 
		JsonElement initialStorage = jsonTree.get("initialStorage");
		
		      JsonArray arr1 = initialStorage.getAsJsonArray();
		      ShoeStorageInfo[] storage = new ShoeStorageInfo[arr1.size()];  //arrry of shostogeinfi
		      int i=0;
		      Iterator<JsonElement> x =  arr1.iterator();
		      while(x.hasNext()){
		    	  JsonObject storageInfo =  x.next().getAsJsonObject();
		    	  String shoeType1 = storageInfo.get("shoeType").getAsString();
		    	  int amount1 = storageInfo.get("amount").getAsInt();
		    	  storage[i] = new ShoeStorageInfo(shoeType1, amount1);
		    	 i++;
		      }
		      
		     Store.getInstance().load(storage);
		}	
}

