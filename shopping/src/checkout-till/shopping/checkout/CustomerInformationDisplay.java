package shopping.checkout;

import java.io.IOException;
import java.math.BigDecimal;

public class CustomerInformationDisplay {
	private final LEDDisplay display;
	private final Beeper beeper;
	
	public CustomerInformationDisplay(LEDDisplay display, Beeper beeper) {
		this.display = display;
		this.beeper = beeper;
	}
	
	public void displayRunningTotal(BigDecimal runningTotal) {
		display.displayText("Total = " + runningTotal);
	}
	
	public void reportError(IOException e) {
		display.displayText(e.getMessage());
		beeper.beep();
	}
}