import shopping.checkout.Product;
import fit.ColumnFixture;


public class TheCustomerPurchases extends ColumnFixture {
	public String Product;
	
	@Override
	public void execute() {
		Product product = SystemUnderTest.productRange.productNamed(Product);
		SystemUnderTest.till.barcodeScanned(product.barcode());
	}
	
	public String Displayed() {
		return SystemUnderTest.display.displayedText();
	}
}
