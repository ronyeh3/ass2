package bgu.spl.app;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.*;

/**
 * Represents a Store singleton
 */
public class Store { 

	private ConcurrentHashMap<String,ShoeStorageInfo> fStoredShoes;
	private ArrayList<Receipt> fReceiptsToStore;  				
	private ArrayList<Receipt> fReceiptsByStore; 
	private final static Logger logger=Logger.getGlobal();

	private static class StoreHolder {
		private static Store StoreInstance = new Store();  
	}

	private Store(){
		fReceiptsToStore = new ArrayList<Receipt>();
		fReceiptsByStore = new ArrayList<Receipt>();
		fStoredShoes = new ConcurrentHashMap<String,ShoeStorageInfo>() ;
	}

	/**
	 * @return the Store Instance
	 */
	public static Store getInstance() {            //singleton
		return StoreHolder.StoreInstance;
	}

	/**
	 * loads a given storage information of shoes
	 * @param storage this Store initial shoes ShoeStorageInfo's objects
	 */
	public void load(ShoeStorageInfo[] storage) {
		for (int i = 0; i< storage.length; i++){
			this.fStoredShoes.put(storage[i].getShoeType(), storage[i]);
		}
	}				             		

	/**
	 * attempts to buy a shoe
	 * @param shoeType the requested shoe type
	 * @param onlyDiscount boolean is true if the client insists on buying only on discount
	 * @return an enum BuyResult of the purchase
	 */
	public synchronized BuyResult take (String shoeType , boolean onlyDiscount ){
		BuyResult result = BuyResult.NOT_IN_STOCK;
		ShoeStorageInfo shoeinfo = fStoredShoes.get(shoeType);
		if(shoeinfo == null || shoeinfo.getCurrentAmount()==0)
			if(!onlyDiscount) return result;
			else return BuyResult.NOT_ON_DISCOUNT;
		if (onlyDiscount==true){
			if (shoeinfo.getDiscountedAmount() > 0){
				result = BuyResult.DISCOUNTED_PRICE;
				shoeinfo.decreaseAmount();
				shoeinfo.decreaseDiscountedAmount();
			}
			else result = BuyResult.NOT_ON_DISCOUNT;
		}
		else {
			if (shoeinfo.getDiscountedAmount() > 0){
				result = BuyResult.DISCOUNTED_PRICE;
				shoeinfo.decreaseAmount();
				shoeinfo.decreaseDiscountedAmount();
			}
			else if (shoeinfo.getCurrentAmount() > 0){
				result = BuyResult.REGULAR_PRICE;
				shoeinfo.decreaseAmount();
			}
			else result = BuyResult.NOT_IN_STOCK;
		}
		return result;
	}

	/**
	 * adds a given amount of shoes to a shoe storage
	 * @param shoeType the shoe type to be added
	 * @param amount the amount of shoes to add
	 */
	public void add(String shoeType ,int amount){ //This method adds the given amount to the ShoeStorageInfo of the given shoeType.
		if(fStoredShoes.containsKey(shoeType))
			this.fStoredShoes.get(shoeType).increaseAmount(amount);
		else{
			ShoeStorageInfo newSI = new ShoeStorageInfo(shoeType, amount);
			fStoredShoes.put(shoeType, newSI);
		}
		logger.info(amount+" "+shoeType+" were added to the store stock");
	}

	/**
	 * increases the amount of discounted shoes of a specific shoe type 
	 * @param shoeType the discounted shoe type
	 * @param amount the amount of shoes to add to the discounted amount 
	 */
	public void addDiscount(String shoeType ,int amount) { //Adds the given amount to the corresponding ShoeStorageInfo�s discountedAmount field.
		if(fStoredShoes.containsKey(shoeType) && this.fStoredShoes.get(shoeType).getCurrentAmount()>amount){
			this.fStoredShoes.get(shoeType).increaseDiscountedAmount(amount);
			logger.info(shoeType+" are now on discount");
		}
		else if (fStoredShoes.containsKey(shoeType) && this.fStoredShoes.get(shoeType).getCurrentAmount()<amount){  //store does'nt have those shoes, they will be on discount when add to store
			fStoredShoes.get(shoeType).setAllDiscountedAmount();
	        logger.info(shoeType+" are now on discount with a clearance sale ");
		}
		else
			logger.info(shoeType+" will be on discount when they will be orderd from factory ");
		
	}

	/**
	 * files a given receipt in the Store Receipts archive 
	 * @param receipt the receipt to be filed
	 */
	public synchronized void file(Receipt receipt) { 
		if (receipt.getCustomerName()=="store") fReceiptsToStore.add(receipt);
		else fReceiptsByStore.add(receipt);
	}
 
	/**
	 * prints to the console the Store information: <br> 
	 * For each item on stock - its name, amount and discountedAmount  <br> 
	 * For each receipt filed in the store - all its fields
	 */
	public synchronized void print(){
		if (this.fStoredShoes!=null && (!this.fStoredShoes.isEmpty())){
			System.out.println("\nStorage:");
			for(String i : this.fStoredShoes.keySet()){
				System.out.println(this.fStoredShoes.get(i).toString());
			}
		}

		if (this.fReceiptsByStore!=null && (!this.fReceiptsByStore.isEmpty())){
			System.out.println();
			System.out.println("Receipts sent by the store:\n");
			for (ListIterator<Receipt> iter = this.fReceiptsByStore.listIterator(); iter.hasNext(); ) {
				Receipt r = iter.next();
				r.print();
			}

		}
		if (this.fReceiptsToStore!=null && (!this.fReceiptsToStore.isEmpty())){
			System.out.println();
			System.out.println("Receipts sent to the store:\n");
			for (ListIterator<Receipt> iter = this.fReceiptsToStore.listIterator(); iter.hasNext(); ) {
				Receipt r = iter.next();
				r.print();
			}
			
		}
		System.out.println(fReceiptsToStore.size()+fReceiptsByStore.size()+" receipts overall");
	}

}