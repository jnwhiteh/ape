package shopping.checkout.fakes;

import shopping.checkout.Beeper;

public class FakeBeeper implements Beeper {
	private final Object lock = new Object();
	private int beepCount = 0;
	
	@Override
	public void beep() {
		synchronized(lock) {
			beepCount++;
		}
	}
	
	public int beepCount() {
		synchronized(lock) {
			return beepCount;
		}
	}
	
	public boolean hasBeeped() {
		return beepCount() > 0;
	}

	public void clearBeepCount() {
		synchronized (lock) {
			beepCount = 0;
		}
	}
}
