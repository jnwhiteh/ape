package shopping.checkout;

import java.io.IOException;


/**
 * Information that the {@link Checkout} needs to know about the range 
 * of products on sale.
 * 
 * In a deployed system, the Checkout uses an implementation that
 * queries a remote web service backed by the company's stock database.
 * 
 * In the acceptance tests, the Checkout uses a {@link shopping.checkout.fakes.FakeProductRange}
 * that is primed with example data by the test.
 * 
 * @throws IOException the product information cannot be retrieved
 * @throws ProductNotFoundException the barcode does not correspond to a product in the stock database
 */
public interface ProductRange {
	/**
	 * Returns the product that has the given barcode.
	 * 
	 * @see Product#barcode()
	 */
	Product productWithBarcode(String barcode) throws IOException;
}
