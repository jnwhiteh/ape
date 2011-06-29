package shopping.checkout.fakes;

import shopping.checkout.Printer;

/**
 * A Printer that prints characters to a string in memory, not 
 * to a receipt printer attached by USB.
 */
public class FakePrinter implements Printer {
	private final Object lock = new Object();
	private StringBuilder output = new StringBuilder();
	private boolean receiptEnded = false;
	
	public void print(String characters) {
		synchronized (lock) {
			output.append(characters);
		}
	}
	
	public void feed() {
		synchronized (lock) {
			receiptEnded = true;
		}
	}
	
	public String output() {
		synchronized (lock) {
			if (!receiptEnded) {
				throw new IllegalStateException("the receipt has not been ended");
			}
			return output.toString();
		}
	}
	
	public void clearOutput() {
		synchronized (lock) {
			output = new StringBuilder();
			receiptEnded = false;
		}
	}
}
