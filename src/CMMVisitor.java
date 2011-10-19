/**
 * An interface to implement in order to traverse the resulting parse tree
 */
public interface CMMVisitor<X,Y> {
  public X visit(CMMASTNode node, Y data);
  public X visit(CMMASTParameterNode node, Y data);
  public X visit(CMMASTSumNode node, Y data);
  public X visit(CMMASTSimpleStatementNode node, Y data);
  public X visit(CMMASTConstantNode node, Y data);
  public X visit(CMMASTLogicalNode node, Y data);
  public X visit(CMMASTParameterListNode node, Y data);
  public X visit(CMMASTArgumentListNode node, Y data);
  public X visit(CMMASTElementNode node, Y data);
  public X visit(CMMASTExpressionListNode node, Y data);
  public X visit(CMMASTComparisonNode node, Y data);
  public X visit(CMMASTElementPlusNode node, Y data);
  public X visit(CMMASTTermNode node, Y data);
  public X visit(CMMASTConditionNode node, Y data);
  public X visit(CMMASTWhileLoopNode node, Y data);
  public X visit(CMMASTDoLoopNode node, Y data);
  public X visit(CMMASTExpNode node, Y data);
  public X visit(CMMASTNegatedlogicalNode node, Y data);
  public X visit(CMMASTFunctionDefinitionNode node, Y data);
  public X visit(CMMASTAssignmentNode node, Y data);
  public X visit(CMMASTStatementNode node, Y data);
  public X visit(CMMASTTypeNode node, Y data);
  public X visit(CMMASTIfStatementNode node, Y data);
  public X visit(CMMASTDeclarationNode node, Y data);
  public X visit(CMMASTProgramNode node, Y data);
  public X visit(CMMASTBlockNode node, Y data);
  public X visit(CMMASTToken node, Y data);
} // end CMMVisitor<X,Y>
