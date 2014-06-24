package shopping.exceptions;

public class Defect extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public Defect(String message, Throwable cause) {
		super(message, cause);
	}

	public Defect(String message) {
		super(message);
	}
}
