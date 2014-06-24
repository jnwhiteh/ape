import fit.ColumnFixture;
import fit.Parse;
import shopping.checkout.Product;

import java.math.BigDecimal;


public class GivenTheFollowingProducts extends ColumnFixture {
	public String Name;
	public String Barcode;
	public BigDecimal UnitPrice;
    public boolean IsAvailableIn3For2Discount;

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
		SystemUnderTest.productRange.addProduct(new Product(Name, Barcode, UnitPrice, IsAvailableIn3For2Discount));
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
