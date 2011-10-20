public class CMMASTTenaryNode extends CMMASTNode {
  public CMMASTTenaryNode (String n, String v, boolean m) { super(n,v,m); }
  public <X,Y> X accept(CMMVisitor<X,Y> visitor, Y data) { return visitor.visit(this, data); }

} // end CMMASTTenaryNode
