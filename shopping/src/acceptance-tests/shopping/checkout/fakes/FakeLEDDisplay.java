package shopping.checkout.fakes;

import shopping.checkout.LEDDisplay;

public class FakeLEDDisplay implements LEDDisplay {
	private final Object lock = new Object();
	private String displayedText = "";

	public void displayText(String text) {
		synchronized (lock) {
			displayedText = text;
		}
	}

	public String displayedText() {
		synchronized (lock) {
			return displayedText;
		}
	}

	public void clearDisplay() {
		synchronized (lock) {
			displayedText = "";
		}
	}
}
