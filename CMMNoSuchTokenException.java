public class CMMNoSuchTokenException extends CMMTokenizerException {
  private static final long serialVersionUID = 1L;
  private String value;
  public CMMNoSuchTokenException (String value, int lineNumber, int column) { super("No such token: " + value, lineNumber, column); this.value = value; }
  public String getValue() { return value; }
} // end CMMNoSuchTokenException
