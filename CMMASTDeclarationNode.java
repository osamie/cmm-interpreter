public class CMMASTDeclarationNode extends CMMASTNode {
  public CMMASTDeclarationNode (String n, String v, boolean m) { super(n,v,m); }
  public <X,Y> X accept(CMMVisitor<X,Y> visitor, Y data) { return visitor.visit(this, data); }

} // end CMMASTDeclarationNode
