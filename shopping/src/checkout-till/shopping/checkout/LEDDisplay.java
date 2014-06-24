package shopping.checkout;


/**
 * The interface used by the {@link Checkout} to display information
 * on the one-line LED screen.
 * 
 * In the deployed system, this is implemented by a driver that controls
 * a display device by USB.
 * 
 * In the acceptance tests, the Checkout is given a {@link shopping.checkout.fakes.FakeLEDDisplay}
 * that records what is displayed in memory so that it can be 
 * checked for correctness.
 */
public interface LEDDisplay {
	/**
	 * Replaces what is currently displayed with the given text.
	 * 
	 * @param text the text to be displayed.
	 */
	void displayText(String text);
}
