package bgu.spl.app;

/**
 * Represents the storage information of a specific shoe type
 */
public class ShoeStorageInfo {
	
	private final String fShoeType;
	private int fAmountOnStorage;
	private int fDiscountedAmount=0;
	
	/**
	 * Constructs a ShoeStorageInfo instance
	 * @param shoeType
	 * @param amount
	 */
	public ShoeStorageInfo(String shoeType, int amount){
		fShoeType=shoeType;
		fAmountOnStorage=amount;
	}
	
	/**
	 * adds a given amount to the shoe storage
	 * @param byTheAmountOf the amount of shoes to be added
	 */
	public void increaseAmount(int byTheAmountOf) {
		fAmountOnStorage=fAmountOnStorage+byTheAmountOf;
	}
	
	/**
	 * decreases the amount in storage of the shoe by one
	 */
	public void decreaseAmount() {
		if (fAmountOnStorage>0) fAmountOnStorage--; // maybe with no condition
	}
	
	/**
	 * makes a given amount of this shoe type to be added to discounted shoes  
	 * @param byTheAmountOf amount of discounted shoes to be added
	 */
	public void increaseDiscountedAmount(int byTheAmountOf) {
		fDiscountedAmount=fDiscountedAmount+byTheAmountOf;
	}

	/**
	 * removes the amount of one shoe from the amount of discounted shoes of this type 
	 */
	public void decreaseDiscountedAmount() {
		fDiscountedAmount--;
	}
	
	/**
	 * @return the type name of the shoe
	 */
	public String getShoeType() {
		return fShoeType;
	}
	
	/**
	 * @return the current amount of this shoe on storage
	 */
	public int getCurrentAmount(){
		return fAmountOnStorage;
	}
	
	/**
	 * @return the discounted amount of this type shoes 
	 */
	public int getDiscountedAmount(){
		return fDiscountedAmount;
	}
	
	public void setAllDiscountedAmount(){
		fDiscountedAmount=fAmountOnStorage;
	}
	
	/**
	 * @return a String detailing this shoe storage information
	 */
	public String toString(){
		String s1;
		s1 = "Shoe type: "+fShoeType+", Amount: "+fAmountOnStorage+", Discounted amount: "+ fDiscountedAmount;
		return s1;
	}
}