/**
 * A token returned by the tokenizer
 */
public class CMMToken {
  public int line = -1, column = -1, type = 0;
  public String name, value;
  public CMMToken (int t, String n, String v, int l, int c) { type=t; name=n; value=v; line=l; column=c; }
  public CMMToken (int t, String n, String v, int l) { type=t; name=n; value=v; line=l; }
  public CMMToken (int t, String n, String v) { type=t; name=n; value=v; }
  public CMMToken (String n, String v, int l, int c) { name=n; value=v; line=l; column=c; }
  public CMMToken (String n, String v, int l) { name=n; value=v; line=l; }
  public CMMToken (String n, String v) { name=n; value=v; }
  public CMMToken (String n) { name=n; }
  public String toString() { return (line != -1 ? "(" + line + ") " : "") + name + (value != null ? " => " + value : ""); }
  public boolean is (String str) { return str.equals(name); }
} // end CMMToken
