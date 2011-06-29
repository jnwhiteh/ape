import java.util.ArrayList;
import java.util.List;

import fit.RowFixture;


public class TheReceiptShows extends RowFixture {
	public static class Row {
		public int Line;
		public String Text;
		
		public Row(int line, String text) {
			Line = line;
			Text = text;
		}
	}
	
	@Override
	public Class<?> getTargetClass() {
		return Row.class;
	}
	
	@Override
	public Object[] query() throws Exception {
		SystemUnderTest.till.paymentAccepted();
		
		List<Row> rows = new ArrayList<Row>();
		for (String line : SystemUnderTest.printer.output().split("\n")) {
			rows.add(new Row(rows.size() + 1, line));
		}
		return rows.toArray();
	}
}
