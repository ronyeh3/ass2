package bgu.spl.app;

/**
 * Represents a purchase Receipt.
 * A purchase can be either a client purchase or a manager manufacturing order receipt.
 */
public class Receipt {
	
	private final String fSellerName; 
	private final String fCustomerName; 
	private final String fShoeType; 
	private final boolean fIsSoldOnDiscount; 
	private final int fIssuedTick; 
	private final int fRequestTick; 
	private final int fAmountSold; 
	
	/**
	 * Constructs a receipt.
	 * 
	 * @param seller The maker of the receipt. Can be a Seller (SellingService instance) or a Factory.  
	 * @param customer The buyer. Can be either a client or a manager.
	 * @param shoeType The shoe type ordered.
	 * @param isSoldOnDiscount Indicating if the shoe was sold at a discounted price.
	 * @param issuedTick The tick in which this receipt was issued.
	 * @param requestTick The tick in which the customer requested to buy the shoe.
	 * @param amountSold The amount of shoes sold.
	 */
	public Receipt(String seller, String customer , String shoeType, boolean isSoldOnDiscount, 
			int issuedTick, int requestTick, int amountSold ) {
		this.fSellerName=seller;
		this.fCustomerName = customer;
		this.fShoeType = shoeType;
		this.fIsSoldOnDiscount = isSoldOnDiscount;
		this.fIssuedTick = issuedTick;
		this.fRequestTick = requestTick;
		this.fAmountSold = amountSold;
	}

	/**
	 * @return The seller Name
	 */
	public String getSellerName() {
		return fSellerName;
	}

	/**
	 * @return the customer name.
	 */
	public String getCustomerName() {
		return fCustomerName;
	}

	/**
	 * @return the ordered shoe type
	 */
	public String getShoeType() {
		return fShoeType;
	}

	/**
	 * @return true if the shoe was sold at a discount price, false otherwise.
	 */
	public boolean isDiscount() {
		return fIsSoldOnDiscount;
	}

	/**
	 * @return the tick in which this receipt was issued.
	 */
	public int getIssuedTick() {
		return fIssuedTick;
	}

	/**
	 * @return The tick in which the customer requested to buy the shoe.
	 */
	public int getRequestTick() {
		return fRequestTick;
	}

	/**
	 * @return the amount of shoes that were sold.
	 */
	public int getAmountSold() {
		return fAmountSold;
	}
	
	/**
	 * prints the receipt details to the console
	 */
	public void print() {
		System.out.println("\t"+"Seller: "+this.fSellerName+"\n"+"\t"+"Customer: "+this.fCustomerName+"\n"+"\t"+"Shoe type: "+this.fShoeType);
		System.out.println("\t"+"Amount sold: "+this.fAmountSold+"\n"+"\t"+"Discount: "+this.fIsSoldOnDiscount);
		System.out.println("\t"+"Issued tick: "+this.fIssuedTick+"\n"+"\t"+"Request tick: "+this.fRequestTick+"\n");
	}
	
}