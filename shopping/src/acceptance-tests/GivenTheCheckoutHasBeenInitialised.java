import fit.Fixture;
import fit.Parse;


public class GivenTheCheckoutHasBeenInitialised extends Fixture {
	@Override
	public void doTable(Parse p) {
		SystemUnderTest.resetTill();
	}
}
