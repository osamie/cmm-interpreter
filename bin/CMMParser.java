import java.util.HashMap;
import java.util.Stack;
public class CMMParser {

  private static final String startRuleName = "Program";

  private CMMiTokenizer tokenizer;

  private HashMap<String, HashMap<String, GrammarRule>> table = new HashMap<String, HashMap<String, GrammarRule>>();

  public CMMParser (CMMiTokenizer tokenizer) { this.tokenizer = tokenizer; buildTable(); }

  public CMMASTProgramNode parse() throws CMMParserException, CMMTokenizerException {
    CMMToken curToken;

    GrammarState curState;

    Stack<GrammarState> stateStack = new Stack<GrammarState>();

    CMMASTProgramNode parseTree = null;
    CMMASTNode curNode = null;

    stateStack.push(new GrammarState(startRuleName, GrammarState.RULE));

    curToken = tokenizer.nextToken();

    while ( true ) {

      curState = stateStack.pop();

      if (curState == null) {

        CMMASTNode nextNode = curNode.getParent();

        if (curNode.isMultiChild() && curNode.numChildren() == 1) {
          CMMASTNode parentNode = curNode.getParent();
          parentNode.removeChild(curNode);

          CMMASTNode childNode = curNode.getChild(0);
          curNode.removeChild(childNode);
          parentNode.addChild(childNode);
        }
        else if (curNode.numChildren() == 0) {
          curNode.getParent().removeChild(curNode);
        }

        curNode = nextNode;

      }
      else if (curState.type == GrammarState.TOKEN) {

        if (!curState.name.equals(curToken.name)) {
          throw new CMMParserException("Invalid token \"" + curToken.value + "\" (" + curToken.name + "), expected token (" + curState.name + ")"	, curToken.line, curToken.column);
        }

        if (curToken.name.equals("eof")) break;

        curNode.addChild(new CMMASTToken(curToken.name, curToken.value));

        curToken = tokenizer.nextToken();

      }
      else if (curState.type == GrammarState.RULE) {

        GrammarRule newrule = table.get(curState.name).get(curToken.name);

        if (newrule == null) {
          String expected = "";
          for (String t : table.get(curState.name).keySet()) if (t != null) expected += t + ", ";
          throw new CMMParserException("Invalid token \"" + curToken.value + "\" (" + curToken.name + ") for rule \"" + curState.name.replaceAll("\\{.*", "") + "\", expected one of (" + expected.substring(0, expected.length()-2) + ")", curToken.line, curToken.column);
        }

        if (!newrule.subrule) {
          if (parseTree == null) {
            curNode = parseTree = new CMMASTProgramNode(newrule.name, null, newrule.multi_child);
          } else {
            CMMASTNode newnode = makenode(newrule.name, null, newrule.multi_child);
            curNode.addChild(newnode);
            curNode = newnode;
          }

          stateStack.push(null);
        }

        for (int i = newrule.graph.length-1; i >= 0; i--) {
          stateStack.push(newrule.graph[i]);
        }
      }
      else if (curState.type == GrammarState.EPSILON) {
        continue; //do nothing
      }

    }

    return parseTree;

  }

  private CMMASTNode makenode(String rulename, String value, boolean multi_child) {
    if (rulename.equals("Statement")) return new CMMASTStatementNode(rulename, value, multi_child);
    if (rulename.equals("Parameter")) return new CMMASTParameterNode(rulename, value, multi_child);
    if (rulename.equals("Subscript")) return new CMMASTSubscriptNode(rulename, value, multi_child);
    if (rulename.equals("FunctionDefinition")) return new CMMASTFunctionDefinitionNode(rulename, value, multi_child);
    if (rulename.equals("Negatedlogical")) return new CMMASTNegatedlogicalNode(rulename, value, multi_child);
    if (rulename.equals("ForLoop")) return new CMMASTForLoopNode(rulename, value, multi_child);
    if (rulename.equals("WhileLoop")) return new CMMASTWhileLoopNode(rulename, value, multi_child);
    if (rulename.equals("Block")) return new CMMASTBlockNode(rulename, value, multi_child);
    if (rulename.equals("String")) return new CMMASTStringNode(rulename, value, multi_child);
    if (rulename.equals("ConcatString")) return new CMMASTConcatStringNode(rulename, value, multi_child);
    if (rulename.equals("Program")) return new CMMASTProgramNode(rulename, value, multi_child);
    if (rulename.equals("ArgumentList")) return new CMMASTArgumentListNode(rulename, value, multi_child);
    if (rulename.equals("Declaration")) return new CMMASTDeclarationNode(rulename, value, multi_child);
    if (rulename.equals("Constant")) return new CMMASTConstantNode(rulename, value, multi_child);
    if (rulename.equals("ExpressionList")) return new CMMASTExpressionListNode(rulename, value, multi_child);
    if (rulename.equals("Condition")) return new CMMASTConditionNode(rulename, value, multi_child);
    if (rulename.equals("Comparison")) return new CMMASTComparisonNode(rulename, value, multi_child);
    if (rulename.equals("DoLoop")) return new CMMASTDoLoopNode(rulename, value, multi_child);
    if (rulename.equals("Element")) return new CMMASTElementNode(rulename, value, multi_child);
    if (rulename.equals("Type")) return new CMMASTTypeNode(rulename, value, multi_child);
    if (rulename.equals("Logical")) return new CMMASTLogicalNode(rulename, value, multi_child);
    if (rulename.equals("Tenary")) return new CMMASTTenaryNode(rulename, value, multi_child);
    if (rulename.equals("Assignment")) return new CMMASTAssignmentNode(rulename, value, multi_child);
    if (rulename.equals("SimpleStatement")) return new CMMASTSimpleStatementNode(rulename, value, multi_child);
    if (rulename.equals("Exp")) return new CMMASTExpNode(rulename, value, multi_child);
    if (rulename.equals("IfStatement")) return new CMMASTIfStatementNode(rulename, value, multi_child);
    if (rulename.equals("ParameterList")) return new CMMASTParameterListNode(rulename, value, multi_child);
    if (rulename.equals("Sum")) return new CMMASTSumNode(rulename, value, multi_child);
    if (rulename.equals("ElementPlus")) return new CMMASTElementPlusNode(rulename, value, multi_child);
    if (rulename.equals("Term")) return new CMMASTTermNode(rulename, value, multi_child);
    throw new RuntimeException("Unknown rule name, cannot make node \"" + rulename + "\"");
  }

  private void buildTable() {
    GrammarState[] graph;
    table.put("Logical{20}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Logical{21}", 2);
      graph[1] = new GrammarState("Tenary", 2);
      table.get("Logical{20}").put("or", new GrammarRule("Logical{20}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Logical{21}", 2);
      graph[1] = new GrammarState("Tenary", 2);
      table.get("Logical{20}").put("and", new GrammarRule("Logical{20}", false, true, graph));

    table.put("Condition", new HashMap<String, GrammarRule>());
      graph = new GrammarState[3];
      graph[0] = new GrammarState("lparen", 1);
      graph[1] = new GrammarState("Assignment", 2);
      graph[2] = new GrammarState("rparen", 1);
      table.get("Condition").put("lparen", new GrammarRule("Condition", false, false, graph));

    table.put("ArgumentList{46}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("ArgumentList{43}", 2);
      table.get("ArgumentList{46}").put("id", new GrammarRule("ArgumentList{46}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ArgumentList{46}").put("rparen", new GrammarRule("ArgumentList{46}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ArgumentList{43}", 2);
      table.get("ArgumentList{46}").put("lparen", new GrammarRule("ArgumentList{46}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ArgumentList{43}", 2);
      table.get("ArgumentList{46}").put("negate_l", new GrammarRule("ArgumentList{46}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ArgumentList{43}", 2);
      table.get("ArgumentList{46}").put("string", new GrammarRule("ArgumentList{46}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ArgumentList{43}", 2);
      table.get("ArgumentList{46}").put("number", new GrammarRule("ArgumentList{46}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ArgumentList{43}", 2);
      table.get("ArgumentList{46}").put("boolean", new GrammarRule("ArgumentList{46}", false, true, graph));

    table.put("ElementPlus", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("id", 1);
      graph[1] = new GrammarState("ElementPlus{34}", 2);
      table.get("ElementPlus").put("id", new GrammarRule("ElementPlus", false, false, graph));

    table.put("IfStatement{16}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("else", 1);
      graph[1] = new GrammarState("Block", 2);
      table.get("IfStatement{16}").put("else", new GrammarRule("IfStatement{16}", false, true, graph));

    table.put("String{40}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("null", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("oindex", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("ge", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("lt", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{37}", 2);
      graph[1] = new GrammarState("String{40}", 2);
      table.get("String{40}").put("id", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("rparen", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("tenary_op1", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("tenary_op2", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("eol", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{37}", 2);
      graph[1] = new GrammarState("String{40}", 2);
      table.get("String{40}").put("boolean", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("plus", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("minus", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("or", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("listsep", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("gets", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("mod", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{37}", 2);
      graph[1] = new GrammarState("String{40}", 2);
      table.get("String{40}").put("number", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("divide", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("multiply", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{37}", 2);
      graph[1] = new GrammarState("String{40}", 2);
      table.get("String{40}").put("concat", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("and", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("exp", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("le", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{37}", 2);
      graph[1] = new GrammarState("String{40}", 2);
      table.get("String{40}").put("string", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("ne", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("gt", new GrammarRule("String{40}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{40}").put("eq", new GrammarRule("String{40}", false, true, graph));

    table.put("SimpleStatement", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("eol", 1);
      table.get("SimpleStatement").put("id", new GrammarRule("SimpleStatement", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("eol", 1);
      table.get("SimpleStatement").put("lparen", new GrammarRule("SimpleStatement", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("eol", 1);
      table.get("SimpleStatement").put("negate_l", new GrammarRule("SimpleStatement", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("eol", 1);
      table.get("SimpleStatement").put("string", new GrammarRule("SimpleStatement", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("eol", 1);
      table.get("SimpleStatement").put("number", new GrammarRule("SimpleStatement", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("eol", 1);
      table.get("SimpleStatement").put("boolean", new GrammarRule("SimpleStatement", false, false, graph));

    table.put("Assignment{18}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("gets", 1);
      graph[1] = new GrammarState("Negatedlogical", 2);
      table.get("Assignment{18}").put("gets", new GrammarRule("Assignment{18}", false, true, graph));

    table.put("Term{31}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("null", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("plus", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("minus", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("listsep", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("or", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term{29}", 2);
      graph[1] = new GrammarState("Term{31}", 2);
      table.get("Term{31}").put("mod", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("gets", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term{29}", 2);
      graph[1] = new GrammarState("Term{31}", 2);
      table.get("Term{31}").put("divide", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term{29}", 2);
      graph[1] = new GrammarState("Term{31}", 2);
      table.get("Term{31}").put("multiply", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("ge", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("lt", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("and", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("rparen", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("le", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("tenary_op1", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("ne", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("tenary_op2", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("eol", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("gt", new GrammarRule("Term{31}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Term{31}").put("eq", new GrammarRule("Term{31}", false, true, graph));

    table.put("ParameterList{2}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Parameter", 2);
      graph[1] = new GrammarState("ParameterList{4}", 2);
      table.get("ParameterList{2}").put("boolean_t", new GrammarRule("ParameterList{2}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Parameter", 2);
      graph[1] = new GrammarState("ParameterList{4}", 2);
      table.get("ParameterList{2}").put("number_t", new GrammarRule("ParameterList{2}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Parameter", 2);
      graph[1] = new GrammarState("ParameterList{4}", 2);
      table.get("ParameterList{2}").put("string_t", new GrammarRule("ParameterList{2}", false, true, graph));

    table.put("FunctionDefinition", new HashMap<String, GrammarRule>());
      graph = new GrammarState[4];
      graph[0] = new GrammarState("Type", 2);
      graph[1] = new GrammarState("id", 1);
      graph[2] = new GrammarState("ParameterList", 2);
      graph[3] = new GrammarState("Block", 2);
      table.get("FunctionDefinition").put("boolean_t", new GrammarRule("FunctionDefinition", false, false, graph));

      graph = new GrammarState[4];
      graph[0] = new GrammarState("Type", 2);
      graph[1] = new GrammarState("id", 1);
      graph[2] = new GrammarState("ParameterList", 2);
      graph[3] = new GrammarState("Block", 2);
      table.get("FunctionDefinition").put("number_t", new GrammarRule("FunctionDefinition", false, false, graph));

      graph = new GrammarState[4];
      graph[0] = new GrammarState("Type", 2);
      graph[1] = new GrammarState("id", 1);
      graph[2] = new GrammarState("ParameterList", 2);
      graph[3] = new GrammarState("Block", 2);
      table.get("FunctionDefinition").put("string_t", new GrammarRule("FunctionDefinition", false, false, graph));

    table.put("Constant", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("string", 1);
      table.get("Constant").put("string", new GrammarRule("Constant", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("number", 1);
      table.get("Constant").put("number", new GrammarRule("Constant", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("boolean", 1);
      table.get("Constant").put("boolean", new GrammarRule("Constant", false, false, graph));

    table.put("Comparison", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Sum", 2);
      graph[1] = new GrammarState("Comparison{25}", 2);
      table.get("Comparison").put("id", new GrammarRule("Comparison", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Sum", 2);
      graph[1] = new GrammarState("Comparison{25}", 2);
      table.get("Comparison").put("lparen", new GrammarRule("Comparison", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Sum", 2);
      graph[1] = new GrammarState("Comparison{25}", 2);
      table.get("Comparison").put("string", new GrammarRule("Comparison", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Sum", 2);
      graph[1] = new GrammarState("Comparison{25}", 2);
      table.get("Comparison").put("number", new GrammarRule("Comparison", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Sum", 2);
      graph[1] = new GrammarState("Comparison{25}", 2);
      table.get("Comparison").put("boolean", new GrammarRule("Comparison", true, false, graph));

    table.put("Declaration{12}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Declaration{9}", 2);
      graph[1] = new GrammarState("Declaration{12}", 2);
      table.get("Declaration{12}").put("listsep", new GrammarRule("Declaration{12}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Declaration{12}").put("eol", new GrammarRule("Declaration{12}", false, true, graph));

    table.put("DoLoop", new HashMap<String, GrammarRule>());
      graph = new GrammarState[5];
      graph[0] = new GrammarState("do", 1);
      graph[1] = new GrammarState("Block", 2);
      graph[2] = new GrammarState("while", 1);
      graph[3] = new GrammarState("Condition", 2);
      graph[4] = new GrammarState("eol", 1);
      table.get("DoLoop").put("do", new GrammarRule("DoLoop", false, false, graph));

    table.put("Negatedlogical{47}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Negatedlogical{47}").put("id", new GrammarRule("Negatedlogical{47}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Negatedlogical{47}").put("lparen", new GrammarRule("Negatedlogical{47}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("negate_l", 1);
      graph[1] = new GrammarState("Negatedlogical{47}", 2);
      table.get("Negatedlogical{47}").put("negate_l", new GrammarRule("Negatedlogical{47}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Negatedlogical{47}").put("string", new GrammarRule("Negatedlogical{47}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Negatedlogical{47}").put("boolean", new GrammarRule("Negatedlogical{47}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Negatedlogical{47}").put("number", new GrammarRule("Negatedlogical{47}", false, true, graph));

    table.put("Negatedlogical", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical{47}", 2);
      graph[1] = new GrammarState("Logical", 2);
      table.get("Negatedlogical").put("id", new GrammarRule("Negatedlogical", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical{47}", 2);
      graph[1] = new GrammarState("Logical", 2);
      table.get("Negatedlogical").put("lparen", new GrammarRule("Negatedlogical", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical{47}", 2);
      graph[1] = new GrammarState("Logical", 2);
      table.get("Negatedlogical").put("negate_l", new GrammarRule("Negatedlogical", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical{47}", 2);
      graph[1] = new GrammarState("Logical", 2);
      table.get("Negatedlogical").put("string", new GrammarRule("Negatedlogical", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical{47}", 2);
      graph[1] = new GrammarState("Logical", 2);
      table.get("Negatedlogical").put("number", new GrammarRule("Negatedlogical", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical{47}", 2);
      graph[1] = new GrammarState("Logical", 2);
      table.get("Negatedlogical").put("boolean", new GrammarRule("Negatedlogical", true, false, graph));

    table.put("Program", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Program{1}", 2);
      graph[1] = new GrammarState("eof", 1);
      table.get("Program").put("boolean_t", new GrammarRule("Program", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Program{1}", 2);
      graph[1] = new GrammarState("eof", 1);
      table.get("Program").put("eof", new GrammarRule("Program", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Program{1}", 2);
      graph[1] = new GrammarState("eof", 1);
      table.get("Program").put("number_t", new GrammarRule("Program", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Program{1}", 2);
      graph[1] = new GrammarState("eof", 1);
      table.get("Program").put("string_t", new GrammarRule("Program", false, false, graph));

    table.put("Sum{28}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("null", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Sum{26}", 2);
      graph[1] = new GrammarState("Sum{28}", 2);
      table.get("Sum{28}").put("plus", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Sum{26}", 2);
      graph[1] = new GrammarState("Sum{28}", 2);
      table.get("Sum{28}").put("minus", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("listsep", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("or", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("gets", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("ge", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("lt", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("and", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("rparen", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("le", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("tenary_op1", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("tenary_op2", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("ne", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("eol", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("gt", new GrammarRule("Sum{28}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Sum{28}").put("eq", new GrammarRule("Sum{28}", false, true, graph));

    table.put("ConcatString", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("string", 1);
      table.get("ConcatString").put("string", new GrammarRule("ConcatString", false, false, graph));

    table.put("Logical{21}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("or", 1);
      table.get("Logical{21}").put("or", new GrammarRule("Logical{21}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("and", 1);
      table.get("Logical{21}").put("and", new GrammarRule("Logical{21}", false, true, graph));

    table.put("ArgumentList{45}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ArgumentList{45}").put("rparen", new GrammarRule("ArgumentList{45}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("ArgumentList{44}", 2);
      graph[1] = new GrammarState("ArgumentList{45}", 2);
      table.get("ArgumentList{45}").put("listsep", new GrammarRule("ArgumentList{45}", false, true, graph));

    table.put("IfStatement{17}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("IfStatement{16}", 2);
      table.get("IfStatement{17}").put("else", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("lparen", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("boolean_t", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("number", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("number_t", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("while", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("id", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("if", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("negate_l", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("string", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("do", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("boolean", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("string_t", new GrammarRule("IfStatement{17}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{17}").put("be", new GrammarRule("IfStatement{17}", false, true, graph));

    table.put("Assignment{19}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Assignment{19}").put("rparen", new GrammarRule("Assignment{19}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Assignment{19}").put("listsep", new GrammarRule("Assignment{19}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Assignment{18}", 2);
      table.get("Assignment{19}").put("gets", new GrammarRule("Assignment{19}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Assignment{19}").put("eol", new GrammarRule("Assignment{19}", false, true, graph));

    table.put("Declaration", new HashMap<String, GrammarRule>());
      graph = new GrammarState[5];
      graph[0] = new GrammarState("Type", 2);
      graph[1] = new GrammarState("id", 1);
      graph[2] = new GrammarState("Declaration{8}", 2);
      graph[3] = new GrammarState("Declaration{12}", 2);
      graph[4] = new GrammarState("eol", 1);
      table.get("Declaration").put("boolean_t", new GrammarRule("Declaration", false, false, graph));

      graph = new GrammarState[5];
      graph[0] = new GrammarState("Type", 2);
      graph[1] = new GrammarState("id", 1);
      graph[2] = new GrammarState("Declaration{8}", 2);
      graph[3] = new GrammarState("Declaration{12}", 2);
      graph[4] = new GrammarState("eol", 1);
      table.get("Declaration").put("number_t", new GrammarRule("Declaration", false, false, graph));

      graph = new GrammarState[5];
      graph[0] = new GrammarState("Type", 2);
      graph[1] = new GrammarState("id", 1);
      graph[2] = new GrammarState("Declaration{8}", 2);
      graph[3] = new GrammarState("Declaration{12}", 2);
      graph[4] = new GrammarState("eol", 1);
      table.get("Declaration").put("string_t", new GrammarRule("Declaration", false, false, graph));

    table.put("Tenary{48}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[4];
      graph[0] = new GrammarState("tenary_op1", 1);
      graph[1] = new GrammarState("Comparison", 2);
      graph[2] = new GrammarState("tenary_op2", 1);
      graph[3] = new GrammarState("Comparison", 2);
      table.get("Tenary{48}").put("tenary_op1", new GrammarRule("Tenary{48}", false, true, graph));

    table.put("Declaration{9}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[3];
      graph[0] = new GrammarState("listsep", 1);
      graph[1] = new GrammarState("id", 1);
      graph[2] = new GrammarState("Declaration{11}", 2);
      table.get("Declaration{9}").put("listsep", new GrammarRule("Declaration{9}", false, true, graph));

    table.put("ParameterList{3}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("listsep", 1);
      graph[1] = new GrammarState("Parameter", 2);
      table.get("ParameterList{3}").put("listsep", new GrammarRule("ParameterList{3}", false, true, graph));

    table.put("Term", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Exp", 2);
      graph[1] = new GrammarState("Term{31}", 2);
      table.get("Term").put("id", new GrammarRule("Term", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Exp", 2);
      graph[1] = new GrammarState("Term{31}", 2);
      table.get("Term").put("lparen", new GrammarRule("Term", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Exp", 2);
      graph[1] = new GrammarState("Term{31}", 2);
      table.get("Term").put("string", new GrammarRule("Term", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Exp", 2);
      graph[1] = new GrammarState("Term{31}", 2);
      table.get("Term").put("number", new GrammarRule("Term", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Exp", 2);
      graph[1] = new GrammarState("Term{31}", 2);
      table.get("Term").put("boolean", new GrammarRule("Term", true, false, graph));

    table.put("Declaration{11}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Declaration{11}").put("listsep", new GrammarRule("Declaration{11}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Declaration{10}", 2);
      table.get("Declaration{11}").put("gets", new GrammarRule("Declaration{11}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Declaration{11}").put("eol", new GrammarRule("Declaration{11}", false, true, graph));

    table.put("Subscript{42}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("null", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Subscript{41}", 2);
      table.get("Subscript{42}").put("oindex", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("plus", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("minus", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("listsep", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("or", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("mod", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("gets", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("divide", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("multiply", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("ge", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("lt", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("and", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("exp", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("rparen", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("le", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("tenary_op1", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("ne", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("tenary_op2", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("eol", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("gt", new GrammarRule("Subscript{42}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Subscript{42}").put("eq", new GrammarRule("Subscript{42}", false, true, graph));

    table.put("String", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{35}", 2);
      graph[1] = new GrammarState("String{36}", 2);
      table.get("String").put("id", new GrammarRule("String", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{35}", 2);
      graph[1] = new GrammarState("String{36}", 2);
      table.get("String").put("string", new GrammarRule("String", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{35}", 2);
      graph[1] = new GrammarState("String{36}", 2);
      table.get("String").put("number", new GrammarRule("String", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{35}", 2);
      graph[1] = new GrammarState("String{36}", 2);
      table.get("String").put("boolean", new GrammarRule("String", true, false, graph));

    table.put("Tenary{49}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Tenary{49}").put("rparen", new GrammarRule("Tenary{49}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Tenary{49}").put("or", new GrammarRule("Tenary{49}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Tenary{49}").put("listsep", new GrammarRule("Tenary{49}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Tenary{48}", 2);
      table.get("Tenary{49}").put("tenary_op1", new GrammarRule("Tenary{49}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Tenary{49}").put("gets", new GrammarRule("Tenary{49}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Tenary{49}").put("eol", new GrammarRule("Tenary{49}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Tenary{49}").put("and", new GrammarRule("Tenary{49}", false, true, graph));

    table.put("String{35}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("ElementPlus", 2);
      table.get("String{35}").put("id", new GrammarRule("String{35}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Constant", 2);
      table.get("String{35}").put("string", new GrammarRule("String{35}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Constant", 2);
      table.get("String{35}").put("number", new GrammarRule("String{35}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Constant", 2);
      table.get("String{35}").put("boolean", new GrammarRule("String{35}", false, true, graph));

    table.put("ElementPlus{34}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("null", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("oindex", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("ge", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("lt", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("id", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("rparen", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("tenary_op1", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("tenary_op2", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("eol", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("boolean", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("plus", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("minus", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ArgumentList", 2);
      table.get("ElementPlus{34}").put("lparen", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("or", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("listsep", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("gets", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("mod", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("number", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("divide", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("multiply", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("concat", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("and", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("exp", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("le", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("ne", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("string", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("gt", new GrammarRule("ElementPlus{34}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ElementPlus{34}").put("eq", new GrammarRule("ElementPlus{34}", false, true, graph));

    table.put("Comparison{23}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison{24}", 2);
      graph[1] = new GrammarState("Sum", 2);
      table.get("Comparison{23}").put("le", new GrammarRule("Comparison{23}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison{24}", 2);
      graph[1] = new GrammarState("Sum", 2);
      table.get("Comparison{23}").put("ne", new GrammarRule("Comparison{23}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison{24}", 2);
      graph[1] = new GrammarState("Sum", 2);
      table.get("Comparison{23}").put("ge", new GrammarRule("Comparison{23}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison{24}", 2);
      graph[1] = new GrammarState("Sum", 2);
      table.get("Comparison{23}").put("gt", new GrammarRule("Comparison{23}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison{24}", 2);
      graph[1] = new GrammarState("Sum", 2);
      table.get("Comparison{23}").put("lt", new GrammarRule("Comparison{23}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison{24}", 2);
      graph[1] = new GrammarState("Sum", 2);
      table.get("Comparison{23}").put("eq", new GrammarRule("Comparison{23}", false, true, graph));

    table.put("Declaration{8}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Declaration{8}").put("listsep", new GrammarRule("Declaration{8}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Declaration{7}", 2);
      table.get("Declaration{8}").put("gets", new GrammarRule("Declaration{8}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Declaration{8}").put("eol", new GrammarRule("Declaration{8}", false, true, graph));

    table.put("Logical{22}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Logical{22}").put("rparen", new GrammarRule("Logical{22}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Logical{22}").put("listsep", new GrammarRule("Logical{22}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Logical{20}", 2);
      graph[1] = new GrammarState("Logical{22}", 2);
      table.get("Logical{22}").put("or", new GrammarRule("Logical{22}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Logical{22}").put("gets", new GrammarRule("Logical{22}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Logical{22}").put("eol", new GrammarRule("Logical{22}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Logical{20}", 2);
      graph[1] = new GrammarState("Logical{22}", 2);
      table.get("Logical{22}").put("and", new GrammarRule("Logical{22}", false, true, graph));

    table.put("ArgumentList{44}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("listsep", 1);
      graph[1] = new GrammarState("Assignment", 2);
      table.get("ArgumentList{44}").put("listsep", new GrammarRule("ArgumentList{44}", false, true, graph));

    table.put("Tenary", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison", 2);
      graph[1] = new GrammarState("Tenary{49}", 2);
      table.get("Tenary").put("id", new GrammarRule("Tenary", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison", 2);
      graph[1] = new GrammarState("Tenary{49}", 2);
      table.get("Tenary").put("lparen", new GrammarRule("Tenary", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison", 2);
      graph[1] = new GrammarState("Tenary{49}", 2);
      table.get("Tenary").put("string", new GrammarRule("Tenary", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison", 2);
      graph[1] = new GrammarState("Tenary{49}", 2);
      table.get("Tenary").put("number", new GrammarRule("Tenary", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Comparison", 2);
      graph[1] = new GrammarState("Tenary{49}", 2);
      table.get("Tenary").put("boolean", new GrammarRule("Tenary", true, false, graph));

    table.put("Type", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("boolean_t", 1);
      table.get("Type").put("boolean_t", new GrammarRule("Type", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("number_t", 1);
      table.get("Type").put("number_t", new GrammarRule("Type", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("string_t", 1);
      table.get("Type").put("string_t", new GrammarRule("Type", false, false, graph));

    table.put("Statement", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("SimpleStatement", 2);
      table.get("Statement").put("id", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("SimpleStatement", 2);
      table.get("Statement").put("lparen", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("SimpleStatement", 2);
      table.get("Statement").put("negate_l", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("IfStatement", 2);
      table.get("Statement").put("if", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("SimpleStatement", 2);
      table.get("Statement").put("string", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Declaration", 2);
      table.get("Statement").put("boolean_t", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("SimpleStatement", 2);
      table.get("Statement").put("number", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("SimpleStatement", 2);
      table.get("Statement").put("boolean", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("DoLoop", 2);
      table.get("Statement").put("do", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Declaration", 2);
      table.get("Statement").put("number_t", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Declaration", 2);
      table.get("Statement").put("string_t", new GrammarRule("Statement", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("WhileLoop", 2);
      table.get("Statement").put("while", new GrammarRule("Statement", false, false, graph));

    table.put("Assignment", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical", 2);
      graph[1] = new GrammarState("Assignment{19}", 2);
      table.get("Assignment").put("id", new GrammarRule("Assignment", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical", 2);
      graph[1] = new GrammarState("Assignment{19}", 2);
      table.get("Assignment").put("lparen", new GrammarRule("Assignment", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical", 2);
      graph[1] = new GrammarState("Assignment{19}", 2);
      table.get("Assignment").put("negate_l", new GrammarRule("Assignment", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical", 2);
      graph[1] = new GrammarState("Assignment{19}", 2);
      table.get("Assignment").put("string", new GrammarRule("Assignment", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical", 2);
      graph[1] = new GrammarState("Assignment{19}", 2);
      table.get("Assignment").put("number", new GrammarRule("Assignment", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Negatedlogical", 2);
      graph[1] = new GrammarState("Assignment{19}", 2);
      table.get("Assignment").put("boolean", new GrammarRule("Assignment", false, false, graph));

    table.put("ParameterList{4}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ParameterList{4}").put("rparen", new GrammarRule("ParameterList{4}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("ParameterList{3}", 2);
      graph[1] = new GrammarState("ParameterList{4}", 2);
      table.get("ParameterList{4}").put("listsep", new GrammarRule("ParameterList{4}", false, true, graph));

    table.put("Sum{27}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("plus", 1);
      table.get("Sum{27}").put("plus", new GrammarRule("Sum{27}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("minus", 1);
      table.get("Sum{27}").put("minus", new GrammarRule("Sum{27}", false, true, graph));

    table.put("Exp{33}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("null", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("plus", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("minus", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("listsep", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("or", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("mod", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("gets", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("divide", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("multiply", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("ge", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("lt", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("and", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("rparen", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Exp{32}", 2);
      graph[1] = new GrammarState("Exp{33}", 2);
      table.get("Exp{33}").put("exp", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("le", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("tenary_op1", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("tenary_op2", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("ne", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("eol", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("gt", new GrammarRule("Exp{33}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Exp{33}").put("eq", new GrammarRule("Exp{33}", false, true, graph));

    table.put("Program{1}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Program{1}").put("null", new GrammarRule("Program{1}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("FunctionDefinition", 2);
      graph[1] = new GrammarState("Program{1}", 2);
      table.get("Program{1}").put("boolean_t", new GrammarRule("Program{1}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Program{1}").put("eof", new GrammarRule("Program{1}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("FunctionDefinition", 2);
      graph[1] = new GrammarState("Program{1}", 2);
      table.get("Program{1}").put("number_t", new GrammarRule("Program{1}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("FunctionDefinition", 2);
      graph[1] = new GrammarState("Program{1}", 2);
      table.get("Program{1}").put("string_t", new GrammarRule("Program{1}", false, true, graph));

    table.put("Subscript{41}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[3];
      graph[0] = new GrammarState("oindex", 1);
      graph[1] = new GrammarState("number", 1);
      graph[2] = new GrammarState("cindex", 1);
      table.get("Subscript{41}").put("oindex", new GrammarRule("Subscript{41}", false, true, graph));

    table.put("Sum", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term", 2);
      graph[1] = new GrammarState("Sum{28}", 2);
      table.get("Sum").put("id", new GrammarRule("Sum", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term", 2);
      graph[1] = new GrammarState("Sum{28}", 2);
      table.get("Sum").put("lparen", new GrammarRule("Sum", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term", 2);
      graph[1] = new GrammarState("Sum{28}", 2);
      table.get("Sum").put("string", new GrammarRule("Sum", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term", 2);
      graph[1] = new GrammarState("Sum{28}", 2);
      table.get("Sum").put("number", new GrammarRule("Sum", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term", 2);
      graph[1] = new GrammarState("Sum{28}", 2);
      table.get("Sum").put("boolean", new GrammarRule("Sum", true, false, graph));

    table.put("Exp", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Element", 2);
      graph[1] = new GrammarState("Exp{33}", 2);
      table.get("Exp").put("id", new GrammarRule("Exp", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Element", 2);
      graph[1] = new GrammarState("Exp{33}", 2);
      table.get("Exp").put("lparen", new GrammarRule("Exp", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Element", 2);
      graph[1] = new GrammarState("Exp{33}", 2);
      table.get("Exp").put("string", new GrammarRule("Exp", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Element", 2);
      graph[1] = new GrammarState("Exp{33}", 2);
      table.get("Exp").put("number", new GrammarRule("Exp", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Element", 2);
      graph[1] = new GrammarState("Exp{33}", 2);
      table.get("Exp").put("boolean", new GrammarRule("Exp", true, false, graph));

    table.put("Declaration{10}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("gets", 1);
      graph[1] = new GrammarState("Negatedlogical", 2);
      table.get("Declaration{10}").put("gets", new GrammarRule("Declaration{10}", false, true, graph));

    table.put("Term{30}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("mod", 1);
      table.get("Term{30}").put("mod", new GrammarRule("Term{30}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("multiply", 1);
      table.get("Term{30}").put("multiply", new GrammarRule("Term{30}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("divide", 1);
      table.get("Term{30}").put("divide", new GrammarRule("Term{30}", false, true, graph));

    table.put("Exp{32}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("exp", 1);
      graph[1] = new GrammarState("Element", 2);
      table.get("Exp{32}").put("exp", new GrammarRule("Exp{32}", false, true, graph));

    table.put("String{39}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("ElementPlus", 2);
      table.get("String{39}").put("id", new GrammarRule("String{39}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Constant", 2);
      table.get("String{39}").put("string", new GrammarRule("String{39}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Constant", 2);
      table.get("String{39}").put("number", new GrammarRule("String{39}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Constant", 2);
      table.get("String{39}").put("boolean", new GrammarRule("String{39}", false, true, graph));

    table.put("ExpressionList{6}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("lparen", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("boolean_t", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("number", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("number_t", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("while", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("id", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("if", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("negate_l", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("string", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("do", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("boolean", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Statement", 2);
      graph[1] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList{6}").put("string_t", new GrammarRule("ExpressionList{6}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ExpressionList{6}").put("be", new GrammarRule("ExpressionList{6}", false, true, graph));

    table.put("String{36}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("null", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("oindex", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("ge", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("lt", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("id", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("rparen", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("tenary_op1", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("tenary_op2", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("eol", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("boolean", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("plus", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("minus", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("or", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("listsep", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("gets", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("mod", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("number", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("divide", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("multiply", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("concat", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("and", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("exp", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("le", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("string", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("ne", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("gt", new GrammarRule("String{36}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("String{40}", 2);
      table.get("String{36}").put("eq", new GrammarRule("String{36}", false, true, graph));

    table.put("Comparison{24}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("le", 1);
      table.get("Comparison{24}").put("le", new GrammarRule("Comparison{24}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ne", 1);
      table.get("Comparison{24}").put("ne", new GrammarRule("Comparison{24}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ge", 1);
      table.get("Comparison{24}").put("ge", new GrammarRule("Comparison{24}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("gt", 1);
      table.get("Comparison{24}").put("gt", new GrammarRule("Comparison{24}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("lt", 1);
      table.get("Comparison{24}").put("lt", new GrammarRule("Comparison{24}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("eq", 1);
      table.get("Comparison{24}").put("eq", new GrammarRule("Comparison{24}", false, true, graph));

    table.put("IfStatement{14}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[3];
      graph[0] = new GrammarState("elsif", 1);
      graph[1] = new GrammarState("Condition", 2);
      graph[2] = new GrammarState("Block", 2);
      table.get("IfStatement{14}").put("elsif", new GrammarRule("IfStatement{14}", false, true, graph));

    table.put("Parameter", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Type", 2);
      graph[1] = new GrammarState("id", 1);
      table.get("Parameter").put("boolean_t", new GrammarRule("Parameter", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Type", 2);
      graph[1] = new GrammarState("id", 1);
      table.get("Parameter").put("number_t", new GrammarRule("Parameter", false, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Type", 2);
      graph[1] = new GrammarState("id", 1);
      table.get("Parameter").put("string_t", new GrammarRule("Parameter", false, false, graph));

    table.put("Block", new HashMap<String, GrammarRule>());
      graph = new GrammarState[3];
      graph[0] = new GrammarState("bb", 1);
      graph[1] = new GrammarState("ExpressionList", 2);
      graph[2] = new GrammarState("be", 1);
      table.get("Block").put("bb", new GrammarRule("Block", false, false, graph));

    table.put("WhileLoop", new HashMap<String, GrammarRule>());
      graph = new GrammarState[3];
      graph[0] = new GrammarState("while", 1);
      graph[1] = new GrammarState("Condition", 2);
      graph[2] = new GrammarState("Block", 2);
      table.get("WhileLoop").put("while", new GrammarRule("WhileLoop", false, false, graph));

    table.put("Declaration{7}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("gets", 1);
      graph[1] = new GrammarState("Negatedlogical", 2);
      table.get("Declaration{7}").put("gets", new GrammarRule("Declaration{7}", false, true, graph));

    table.put("ArgumentList{43}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("ArgumentList{45}", 2);
      table.get("ArgumentList{43}").put("id", new GrammarRule("ArgumentList{43}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("ArgumentList{45}", 2);
      table.get("ArgumentList{43}").put("lparen", new GrammarRule("ArgumentList{43}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("ArgumentList{45}", 2);
      table.get("ArgumentList{43}").put("negate_l", new GrammarRule("ArgumentList{43}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("ArgumentList{45}", 2);
      table.get("ArgumentList{43}").put("string", new GrammarRule("ArgumentList{43}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("ArgumentList{45}", 2);
      table.get("ArgumentList{43}").put("number", new GrammarRule("ArgumentList{43}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Assignment", 2);
      graph[1] = new GrammarState("ArgumentList{45}", 2);
      table.get("ArgumentList{43}").put("boolean", new GrammarRule("ArgumentList{43}", false, true, graph));

    table.put("ForLoop{13}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[5];
      graph[0] = new GrammarState("SimpleStament", 1);
      graph[1] = new GrammarState("eol", 1);
      graph[2] = new GrammarState("SimpleStament", 1);
      graph[3] = new GrammarState("eol", 1);
      graph[4] = new GrammarState("SimpleStament", 1);
      table.get("ForLoop{13}").put("SimpleStament", new GrammarRule("ForLoop{13}", false, true, graph));

    table.put("Logical", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Tenary", 2);
      graph[1] = new GrammarState("Logical{22}", 2);
      table.get("Logical").put("id", new GrammarRule("Logical", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Tenary", 2);
      graph[1] = new GrammarState("Logical{22}", 2);
      table.get("Logical").put("lparen", new GrammarRule("Logical", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Tenary", 2);
      graph[1] = new GrammarState("Logical{22}", 2);
      table.get("Logical").put("string", new GrammarRule("Logical", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Tenary", 2);
      graph[1] = new GrammarState("Logical{22}", 2);
      table.get("Logical").put("number", new GrammarRule("Logical", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Tenary", 2);
      graph[1] = new GrammarState("Logical{22}", 2);
      table.get("Logical").put("boolean", new GrammarRule("Logical", true, false, graph));

    table.put("Subscript", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("String", 2);
      graph[1] = new GrammarState("Subscript{42}", 2);
      table.get("Subscript").put("id", new GrammarRule("Subscript", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String", 2);
      graph[1] = new GrammarState("Subscript{42}", 2);
      table.get("Subscript").put("string", new GrammarRule("Subscript", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String", 2);
      graph[1] = new GrammarState("Subscript{42}", 2);
      table.get("Subscript").put("number", new GrammarRule("Subscript", true, false, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String", 2);
      graph[1] = new GrammarState("Subscript{42}", 2);
      table.get("Subscript").put("boolean", new GrammarRule("Subscript", true, false, graph));

    table.put("String{37}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{38}", 2);
      graph[1] = new GrammarState("String{39}", 2);
      table.get("String{37}").put("id", new GrammarRule("String{37}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{38}", 2);
      graph[1] = new GrammarState("String{39}", 2);
      table.get("String{37}").put("string", new GrammarRule("String{37}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{38}", 2);
      graph[1] = new GrammarState("String{39}", 2);
      table.get("String{37}").put("number", new GrammarRule("String{37}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{38}", 2);
      graph[1] = new GrammarState("String{39}", 2);
      table.get("String{37}").put("boolean", new GrammarRule("String{37}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("String{38}", 2);
      graph[1] = new GrammarState("String{39}", 2);
      table.get("String{37}").put("concat", new GrammarRule("String{37}", false, true, graph));

    table.put("ExpressionList", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("lparen", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("boolean_t", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("number", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("number_t", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("while", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("id", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("if", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("negate_l", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("string", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("do", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("boolean", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("string_t", new GrammarRule("ExpressionList", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ExpressionList{6}", 2);
      table.get("ExpressionList").put("be", new GrammarRule("ExpressionList", false, false, graph));

    table.put("ForLoop", new HashMap<String, GrammarRule>());
      graph = new GrammarState[4];
      graph[0] = new GrammarState("for", 1);
      graph[1] = new GrammarState("lparen", 1);
      graph[2] = new GrammarState("ForLoop{13}", 2);
      graph[3] = new GrammarState("rparen", 1);
      table.get("ForLoop").put("for", new GrammarRule("ForLoop", false, false, graph));

    table.put("Sum{26}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Sum{27}", 2);
      graph[1] = new GrammarState("Term", 2);
      table.get("Sum{26}").put("plus", new GrammarRule("Sum{26}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Sum{27}", 2);
      graph[1] = new GrammarState("Term", 2);
      table.get("Sum{26}").put("minus", new GrammarRule("Sum{26}", false, true, graph));

    table.put("ParameterList{5}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("ParameterList{5}").put("rparen", new GrammarRule("ParameterList{5}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ParameterList{2}", 2);
      table.get("ParameterList{5}").put("boolean_t", new GrammarRule("ParameterList{5}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ParameterList{2}", 2);
      table.get("ParameterList{5}").put("number_t", new GrammarRule("ParameterList{5}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("ParameterList{2}", 2);
      table.get("ParameterList{5}").put("string_t", new GrammarRule("ParameterList{5}", false, true, graph));

    table.put("IfStatement", new HashMap<String, GrammarRule>());
      graph = new GrammarState[5];
      graph[0] = new GrammarState("if", 1);
      graph[1] = new GrammarState("Condition", 2);
      graph[2] = new GrammarState("Block", 2);
      graph[3] = new GrammarState("IfStatement{15}", 2);
      graph[4] = new GrammarState("IfStatement{17}", 2);
      table.get("IfStatement").put("if", new GrammarRule("IfStatement", false, false, graph));

    table.put("Term{29}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term{30}", 2);
      graph[1] = new GrammarState("Exp", 2);
      table.get("Term{29}").put("mod", new GrammarRule("Term{29}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term{30}", 2);
      graph[1] = new GrammarState("Exp", 2);
      table.get("Term{29}").put("multiply", new GrammarRule("Term{29}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("Term{30}", 2);
      graph[1] = new GrammarState("Exp", 2);
      table.get("Term{29}").put("divide", new GrammarRule("Term{29}", false, true, graph));

    table.put("String{38}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{38}").put("id", new GrammarRule("String{38}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{38}").put("string", new GrammarRule("String{38}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{38}").put("boolean", new GrammarRule("String{38}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("String{38}").put("number", new GrammarRule("String{38}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("concat", 1);
      table.get("String{38}").put("concat", new GrammarRule("String{38}", false, true, graph));

    table.put("Comparison{25}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Comparison{25}").put("listsep", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Comparison{25}").put("or", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Comparison{25}").put("gets", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Comparison{23}", 2);
      table.get("Comparison{25}").put("ge", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Comparison{23}", 2);
      table.get("Comparison{25}").put("lt", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Comparison{25}").put("and", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Comparison{25}").put("rparen", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Comparison{23}", 2);
      table.get("Comparison{25}").put("le", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Comparison{25}").put("tenary_op1", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Comparison{23}", 2);
      table.get("Comparison{25}").put("ne", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Comparison{25}").put("tenary_op2", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("Comparison{25}").put("eol", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Comparison{23}", 2);
      table.get("Comparison{25}").put("gt", new GrammarRule("Comparison{25}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Comparison{23}", 2);
      table.get("Comparison{25}").put("eq", new GrammarRule("Comparison{25}", false, true, graph));

    table.put("ParameterList", new HashMap<String, GrammarRule>());
      graph = new GrammarState[3];
      graph[0] = new GrammarState("lparen", 1);
      graph[1] = new GrammarState("ParameterList{5}", 2);
      graph[2] = new GrammarState("rparen", 1);
      table.get("ParameterList").put("lparen", new GrammarRule("ParameterList", false, false, graph));

    table.put("Element", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("Subscript", 2);
      table.get("Element").put("id", new GrammarRule("Element", false, false, graph));

      graph = new GrammarState[3];
      graph[0] = new GrammarState("lparen", 1);
      graph[1] = new GrammarState("Logical", 2);
      graph[2] = new GrammarState("rparen", 1);
      table.get("Element").put("lparen", new GrammarRule("Element", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Subscript", 2);
      table.get("Element").put("string", new GrammarRule("Element", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Subscript", 2);
      table.get("Element").put("number", new GrammarRule("Element", false, false, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("Subscript", 2);
      table.get("Element").put("boolean", new GrammarRule("Element", false, false, graph));

    table.put("ArgumentList", new HashMap<String, GrammarRule>());
      graph = new GrammarState[3];
      graph[0] = new GrammarState("lparen", 1);
      graph[1] = new GrammarState("ArgumentList{46}", 2);
      graph[2] = new GrammarState("rparen", 1);
      table.get("ArgumentList").put("lparen", new GrammarRule("ArgumentList", false, false, graph));

    table.put("IfStatement{15}", new HashMap<String, GrammarRule>());
      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("else", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("lparen", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("boolean_t", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("number", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("number_t", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("while", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("id", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[2];
      graph[0] = new GrammarState("IfStatement{14}", 2);
      graph[1] = new GrammarState("IfStatement{15}", 2);
      table.get("IfStatement{15}").put("elsif", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("if", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("negate_l", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("string", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("do", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("boolean", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("string_t", new GrammarRule("IfStatement{15}", false, true, graph));

      graph = new GrammarState[1];
      graph[0] = new GrammarState("\0", 3);
      table.get("IfStatement{15}").put("be", new GrammarRule("IfStatement{15}", false, true, graph));

  }

  private class GrammarRule {
    String name;
    boolean multi_child, subrule;
    GrammarState[] graph;

    public GrammarRule(String n, boolean m, boolean s, GrammarState[] g) {
      name = n; multi_child = m; subrule = s; graph = g;
    }
  } // end GrammarRule

  private class GrammarState {
    public static final int TOKEN = 1, RULE = 2, EPSILON = 3;
    String name;
    int type;
    public GrammarState(String n, int t) { name = n; type = t; }
  } // end GrammarState

} // end CMMParser
