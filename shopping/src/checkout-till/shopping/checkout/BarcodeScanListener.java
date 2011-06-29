package shopping.checkout;

import java.util.EventListener;

/**
 * This interface through which the driver for the barcode scanner 
 * reports scanned barcodes.
 * 
 * In acceptance tests, the driver is not activated and the tests
 * call the barcodeScanned method of the {@link Checkout}.
 */
public interface BarcodeScanListener extends EventListener {
	/**
	 * A barcode has been scanned by the operator.
	 * 
	 * @param barcode the barcode that has been scanned.
	 */
	void barcodeScanned(String barcode);
}
