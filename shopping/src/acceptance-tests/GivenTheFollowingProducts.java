import java.math.BigDecimal;

import shopping.checkout.Product;
import fit.ColumnFixture;
import fit.Parse;


public class GivenTheFollowingProducts extends ColumnFixture {
	public String Name;
	public String Barcode;
	public BigDecimal UnitPrice;
	
	@Override
	public void doRows(Parse rows) {
		SystemUnderTest.productRange.deleteAll();
		super.doRows(rows);
	}
	
	@Override
	public void reset() throws Exception {
		Name = null;
		Barcode = null;
		UnitPrice = null;
	}

	@Override
	public void execute() throws Exception {
		SystemUnderTest.productRange.addProduct(new Product(Name, Barcode, UnitPrice));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Object parse(String s, Class type) throws Exception {
		if (type == BigDecimal.class) {
			return new BigDecimal(s);
		}
		else {
			return super.parse(s, type);
		}
	}
}
