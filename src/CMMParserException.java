public class CMMParserException extends Exception {
  private static final long serialVersionUID = 1L;
  private int lineNumber = -1, column = -1;
  public CMMParserException (String msg) { super(msg); }
  public CMMParserException (String msg, int lineNumber) { this(msg); this.lineNumber=lineNumber; }
  public CMMParserException (String msg, int lineNumber, int column) { this(msg, lineNumber); this.column=column; }
  public int getLineNumber() { return lineNumber; }
  public int getColumn() { return column; }
  public String toString() { return (lineNumber != -1 ? "[line " + lineNumber + (column != -1 ? ", col " + column : "") + "] " : "") + getMessage(); }
}
