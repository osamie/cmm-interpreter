import java.util.ArrayList;

public class CMMASTNode {

  private CMMASTNode parent;

  private ArrayList<CMMASTNode> children = new ArrayList<CMMASTNode>();

  private String name, value;

  private boolean multi_child;

  public CMMASTNode (String n, String v, boolean m) { name=n; value=v; multi_child=m; }

  public boolean isMultiChild() { return multi_child; }

  public void addChild(CMMASTNode node) { if (node.parent != null) throw new RuntimeException("Node already has parent, can't add as child");  children.add(node); node.parent = this; }

  public void removeChild(CMMASTNode node) { children.remove(node); node.parent = null; }

  public ArrayList<CMMASTNode> getChildren() { return children; }

  public CMMASTNode getChild(int i) { return children.get(i); }

  public int numChildren() { return children.size(); }

  public String getName() { return name; }

  public String getValue() { return value; }

  public CMMASTNode getParent() { return parent; }

  public <X,Y> X accept(CMMVisitor<X,Y> visitor, Y data) { return visitor.visit(this, data); }

  public String toString() { if (value == null || value.isEmpty()) { return name; } else { return name + " => " + value; } }

} // end CMMASTNode
