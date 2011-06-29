package shopping.checkout.fakes;

import java.util.HashMap;
import java.util.Map;

import shopping.checkout.Product;
import shopping.checkout.ProductRange;



public class FakeProductRange implements ProductRange {
	private final Map<String, Product> productsByBarcode = new HashMap<String, Product>();
	private final Map<String, Product> productsByName = new HashMap<String, Product>();

	public void deleteAll() {
		productsByBarcode.clear();
		productsByName.clear();
	}
	
	public void addProduct(Product product) {
		if (productsByBarcode.containsKey(product.barcode())) {
			throw new IllegalArgumentException("duplicate barcode: "
					+ product.barcode());
		}
		
		if (productsByName.containsKey(product.name())) {
			throw new IllegalArgumentException("duplicate product name: "
					+ product.name());
		}
		
		productsByBarcode.put(product.barcode(), product);
		productsByName.put(product.name(), product);
	}

	public Product productWithBarcode(String barcode) {
		return productsByBarcode.get(barcode);
	}

	public Product productNamed(String name) {
		return productsByName.get(name);
	}
}
