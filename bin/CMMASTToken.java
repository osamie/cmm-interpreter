public class CMMASTToken extends CMMASTNode {
  public CMMASTToken (String n, String v) { super(n,v,false); }
  public <X,Y> X accept(CMMVisitor<X,Y> visitor, Y data) { return visitor.visit(this, data); }

}
