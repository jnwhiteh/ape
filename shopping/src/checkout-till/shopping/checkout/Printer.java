package shopping.checkout;


/**
 * The interface used by the {@link Checkout} to print receipts.
 * 
 * In the deployed system, this is implemented by a driver that controls
 * a printer by USB.
 * 
 * In the acceptance tests, the Checkout is given a {@link shopping.checkout.fakes.FakePrinter}
 * that records what is printed in memory so that the output can be 
 * checked for correctness.
 */
public interface Printer {
	/**
	 * Prints characters to the printer.  Print "\n" to start a new line.
	 * @param characters
	 */
	void print(String characters);
	
	/**
	 * Feeds paper from the printer so that all printed output can be
	 * torn off and handed to the customer.
	 */
	void feed();
}
