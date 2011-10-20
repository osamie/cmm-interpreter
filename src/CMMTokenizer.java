import java.io.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.ListIterator;

public class CMMTokenizer implements CMMiTokenizer {

  public static final int ELSIF_TOKEN = 1; // elsif
  public static final int NE_TOKEN = 2; // != | <>
  public static final int PLUS_TOKEN = 3; // \+
  public static final int MOD_TOKEN = 4; // %
  public static final int MINUS_TOKEN = 5; // -
  public static final int GT_TOKEN = 6; // >
  public static final int LISTSEP_TOKEN = 7; // ,
  public static final int DO_TOKEN = 8; // do
  public static final int STRING_TOKEN = 9; // "([^"]|\\")*"
  public static final int NEGATE_L_TOKEN = 10; // !
  public static final int GE_TOKEN = 11; // >=
  public static final int ELSE_TOKEN = 12; // else
  public static final int STRING_T_TOKEN = 13; // string
  public static final int NUMBER_T_TOKEN = 14; // number
  public static final int MULTIPLY_TOKEN = 15; // \*
  public static final int DIVIDE_TOKEN = 16; // /
  public static final int GETS_TOKEN = 17; // =
  public static final int CONCAT_TOKEN = 18; // \.
  public static final int LT_TOKEN = 19; // <
  public static final int OR_TOKEN = 20; // \|
  public static final int LPAREN_TOKEN = 21; // \(
  public static final int BOOLEAN_T_TOKEN = 22; // boolean
  public static final int WHILE_TOKEN = 23; // while
  public static final int IF_TOKEN = 24; // if
  public static final int ID_TOKEN = 25; // :letter: (:digit:|:letter:)*
  public static final int RPAREN_TOKEN = 26; // \)
  public static final int LE_TOKEN = 27; // <=
  public static final int CINDEX_TOKEN = 28; // \]
  public static final int FOR_TOKEN = 29; // for
  public static final int TENARY_OP2_TOKEN = 30; // \:
  public static final int TENARY_OP1_TOKEN = 31; // \?
  public static final int AND_TOKEN = 32; // &
  public static final int EQ_TOKEN = 33; // ==
  public static final int OINDEX_TOKEN = 34; // \[
  public static final int RETURN_TOKEN = 35; // return
  public static final int PRINTS_TOKEN = 36; // [$]
  public static final int EOL_TOKEN = 37; // ;
  public static final int BE_TOKEN = 38; // }
  public static final int EXP_TOKEN = 39; // \^
  public static final int NUMBER_TOKEN = 40; // -? (:float: | :float: [eE] [-\+]? :integer:?) | :integer:+r(:letter:|:digit:)+
  public static final int BB_TOKEN = 41; // {
  public static final int BOOLEAN_TOKEN = 42; // true | false
  public static final int EOF_TOKEN = -1;

  private static final char wildcard = 3;

  private LineNumberReader input;

  private int currentColumn = 1;

  private Hashtable<Integer, Hashtable<Character, Integer>> DFA = new Hashtable<Integer, Hashtable<Character, Integer>>();

  private Hashtable<Integer, String> accepting = new Hashtable<Integer, String>();

  private ArrayList<CMMToken> tokenHistory = new ArrayList<CMMToken>();
  private ListIterator<CMMToken> tokenHistoryIT = tokenHistory.listIterator();
  private int tokenHistorySize = 20;

  private ArrayDeque<Integer> pushedChars = new ArrayDeque<Integer>();

  public CMMTokenizer (Reader reader) {
    input = new LineNumberReader(reader);
    input.setLineNumber(1); //start at one
    buildDFA();
  } // end constructor

  public void setTokenHistorySize(int size) { tokenHistorySize = size; }
  public int getTokenHistorySize() { return tokenHistorySize; }

  public int getLineNumber() { return input.getLineNumber(); }

  public CMMToken nextToken() throws CMMTokenizerException {
    if (tokenHistoryIT.hasNext()) {
      return tokenHistoryIT.next();
    } else {
      CMMToken token = _nextToken();
      tokenHistoryIT.add(token);
      if (tokenHistory.size() > tokenHistorySize) {
        tokenHistory.remove(0);
        tokenHistoryIT = tokenHistory.listIterator(tokenHistorySize);
      }
      return token;
    }
  } // end nextToken

  private CMMToken _nextToken() throws CMMTokenizerException {
    int c;
    String value;
    int curState;

  tokenLoop:
    while (true) {
      int lineNumber = getLineNumber();
      int column = currentColumn;
      curState = 0;
      value = "";

      while ( (c=getChar()) != -1 ) { // read in a character (-1 indicates EOF)
        if (transition(curState, (char)c) == -1) break;
        value += (char)c;
        curState = transition(curState, (char)c);
      }

      if (c == -1 && value.isEmpty()) {
        return createToken("eof", "", lineNumber, column);
      } else if (accepting.containsKey(curState)) {
        pushChar(c);
        if (accepting.get(curState).equals("skip")) continue tokenLoop;
        return createToken(accepting.get(curState), value, lineNumber, column);
      } else {
        value += (char)c;
        throw new CMMNoSuchTokenException(value, lineNumber, column);
      }
    }
  } // end _nextToken

  public void pushToken() throws CMMTokenizerException {
    if (tokenHistoryIT.hasPrevious()) {
      tokenHistoryIT.previous();
    } else {
      throw new CMMTokenizerException("Token push limit (" + tokenHistorySize + ") exceeded.");
    }
  } // end pushToken

  private int transition(Integer state, Character c) {
    Integer nextState = DFA.get(state).get(c);

    if (nextState == null) {
      nextState = DFA.get(state).get(wildcard);

      if (nextState == null) {
        nextState = -1;
      }

    }

    return nextState;
  } // end transition

  private void pushChar(Integer c) {
    pushedChars.push(c);
    if (c == 10 || (c == 13 && pushedChars.peek() != 10)) {
      input.setLineNumber(input.getLineNumber()-1);
      currentColumn = 999999999; // don't know the previous line's length, so set to a bajillion
    } else {
      currentColumn--;
    }
  }

  private int getChar() throws CMMTokenizerException {
    int c; int startLine = getLineNumber();
    if (!pushedChars.isEmpty()) {
      c = pushedChars.pop();
      if (c == 10 || (c == 13 && pushedChars.peek() != 10)) input.setLineNumber(input.getLineNumber()+1);
    } else {
      try {
        c = input.read();
      }
      catch (IOException ex) {
        throw new CMMTokenizerException(ex);
      }
    }
    if (getLineNumber() > startLine) { currentColumn = 1; } else { currentColumn++; }
    return c;
  } //end getChar

  private CMMToken createToken(String name, String value, int lineNumber, int column) {
    if ( name.equals("elsif") ) return new CMMToken(ELSIF_TOKEN, name, value, lineNumber, column);
    if ( name.equals("ne") ) return new CMMToken(NE_TOKEN, name, value, lineNumber, column);
    if ( name.equals("plus") ) return new CMMToken(PLUS_TOKEN, name, value, lineNumber, column);
    if ( name.equals("mod") ) return new CMMToken(MOD_TOKEN, name, value, lineNumber, column);
    if ( name.equals("minus") ) return new CMMToken(MINUS_TOKEN, name, value, lineNumber, column);
    if ( name.equals("gt") ) return new CMMToken(GT_TOKEN, name, value, lineNumber, column);
    if ( name.equals("listsep") ) return new CMMToken(LISTSEP_TOKEN, name, value, lineNumber, column);
    if ( name.equals("do") ) return new CMMToken(DO_TOKEN, name, value, lineNumber, column);
    if ( name.equals("string") ) return new CMMToken(STRING_TOKEN, name, value, lineNumber, column);
    if ( name.equals("negate_l") ) return new CMMToken(NEGATE_L_TOKEN, name, value, lineNumber, column);
    if ( name.equals("ge") ) return new CMMToken(GE_TOKEN, name, value, lineNumber, column);
    if ( name.equals("else") ) return new CMMToken(ELSE_TOKEN, name, value, lineNumber, column);
    if ( name.equals("string_t") ) return new CMMToken(STRING_T_TOKEN, name, value, lineNumber, column);
    if ( name.equals("number_t") ) return new CMMToken(NUMBER_T_TOKEN, name, value, lineNumber, column);
    if ( name.equals("multiply") ) return new CMMToken(MULTIPLY_TOKEN, name, value, lineNumber, column);
    if ( name.equals("divide") ) return new CMMToken(DIVIDE_TOKEN, name, value, lineNumber, column);
    if ( name.equals("gets") ) return new CMMToken(GETS_TOKEN, name, value, lineNumber, column);
    if ( name.equals("concat") ) return new CMMToken(CONCAT_TOKEN, name, value, lineNumber, column);
    if ( name.equals("lt") ) return new CMMToken(LT_TOKEN, name, value, lineNumber, column);
    if ( name.equals("or") ) return new CMMToken(OR_TOKEN, name, value, lineNumber, column);
    if ( name.equals("lparen") ) return new CMMToken(LPAREN_TOKEN, name, value, lineNumber, column);
    if ( name.equals("boolean_t") ) return new CMMToken(BOOLEAN_T_TOKEN, name, value, lineNumber, column);
    if ( name.equals("while") ) return new CMMToken(WHILE_TOKEN, name, value, lineNumber, column);
    if ( name.equals("if") ) return new CMMToken(IF_TOKEN, name, value, lineNumber, column);
    if ( name.equals("id") ) return new CMMToken(ID_TOKEN, name, value, lineNumber, column);
    if ( name.equals("rparen") ) return new CMMToken(RPAREN_TOKEN, name, value, lineNumber, column);
    if ( name.equals("le") ) return new CMMToken(LE_TOKEN, name, value, lineNumber, column);
    if ( name.equals("cindex") ) return new CMMToken(CINDEX_TOKEN, name, value, lineNumber, column);
    if ( name.equals("for") ) return new CMMToken(FOR_TOKEN, name, value, lineNumber, column);
    if ( name.equals("tenary_op2") ) return new CMMToken(TENARY_OP2_TOKEN, name, value, lineNumber, column);
    if ( name.equals("tenary_op1") ) return new CMMToken(TENARY_OP1_TOKEN, name, value, lineNumber, column);
    if ( name.equals("and") ) return new CMMToken(AND_TOKEN, name, value, lineNumber, column);
    if ( name.equals("eq") ) return new CMMToken(EQ_TOKEN, name, value, lineNumber, column);
    if ( name.equals("oindex") ) return new CMMToken(OINDEX_TOKEN, name, value, lineNumber, column);
    if ( name.equals("return") ) return new CMMToken(RETURN_TOKEN, name, value, lineNumber, column);
    if ( name.equals("prints") ) return new CMMToken(PRINTS_TOKEN, name, value, lineNumber, column);
    if ( name.equals("eol") ) return new CMMToken(EOL_TOKEN, name, value, lineNumber, column);
    if ( name.equals("be") ) return new CMMToken(BE_TOKEN, name, value, lineNumber, column);
    if ( name.equals("exp") ) return new CMMToken(EXP_TOKEN, name, value, lineNumber, column);
    if ( name.equals("number") ) return new CMMToken(NUMBER_TOKEN, name, value, lineNumber, column);
    if ( name.equals("bb") ) return new CMMToken(BB_TOKEN, name, value, lineNumber, column);
    if ( name.equals("boolean") ) return new CMMToken(BOOLEAN_TOKEN, name, value, lineNumber, column);
    if ( name.equals("eof") ) return new CMMToken(EOF_TOKEN, name, value, lineNumber, column);
    throw new RuntimeException("Cannot create token, unknown token name: " + name);
  }

  private void buildDFA() {
    buildState0();
    buildState1();
    buildState2();
    buildState3();
    buildState4();
    buildState5();
    buildState6();
    buildState7();
    buildState8();
    buildState9();
    buildState10();
    buildState11();
    buildState12();
    buildState13();
    buildState14();
    buildState15();
    buildState16();
    buildState17();
    buildState18();
    buildState19();
    buildState20();
    buildState21();
    buildState22();
    buildState23();
    buildState24();
    buildState25();
    buildState26();
    buildState27();
    buildState28();
    buildState29();
    buildState30();
    buildState31();
    buildState32();
    buildState33();
    buildState34();
    buildState35();
    buildState36();
    buildState37();
    buildState38();
    buildState39();
    buildState40();
    buildState41();
    buildState42();
    buildState43();
    buildState44();
    buildState45();
    buildState46();
    buildState47();
    buildState48();
    buildState49();
    buildState50();
    buildState51();
    buildState52();
    buildState53();
    buildState54();
    buildState55();
    buildState56();
    buildState57();
    buildState58();
    buildState59();
    buildState60();
    buildState61();
    buildState62();
    buildState63();
    buildState64();
    buildState65();
    buildState66();
    buildState67();
    buildState68();
    buildState69();
    buildState70();
    buildState71();
    buildState72();
    buildState73();
    buildState74();
    buildState75();
    buildState76();
    buildState77();
    buildState78();
    buildState79();
    buildState80();
    buildState81();
    buildState82();
    buildState83();
    buildState84();
    buildState85();
    buildState86();
    buildState87();
    buildState88();
    buildState89();
    buildState90();
    buildState91();
    buildState92();
    buildState93();
    buildState94();
    buildState95();
    buildState96();
    buildState97();
    buildState98();
    buildState99();
    buildState100();
    buildState101();
    buildState102();
    buildState103();
    buildState104();
    buildState105();
    buildState106();
    buildState107();
    buildState108();

    accepting.put(108, "elsif");
    accepting.put(107, "id");
    accepting.put(106, "else");
    accepting.put(105, "id");
    accepting.put(104, "id");
    accepting.put(103, "number");
    accepting.put(102, "number");
    accepting.put(100, "ge");
    accepting.put(99, "do");
    accepting.put(98, "string");
    accepting.put(97, "string");
    accepting.put(94, "ne");
    accepting.put(93, "string_t");
    accepting.put(92, "id");
    accepting.put(91, "id");
    accepting.put(90, "id");
    accepting.put(89, "id");
    accepting.put(88, "number_t");
    accepting.put(87, "id");
    accepting.put(86, "id");
    accepting.put(85, "id");
    accepting.put(84, "id");
    accepting.put(83, "skip");
    accepting.put(82, "skip");
    accepting.put(81, "eq");
    accepting.put(80, "number");
    accepting.put(79, "number");
    accepting.put(78, "le");
    accepting.put(77, "ne");
    accepting.put(76, "boolean_t");
    accepting.put(75, "id");
    accepting.put(74, "id");
    accepting.put(73, "id");
    accepting.put(72, "id");
    accepting.put(71, "id");
    accepting.put(70, "while");
    accepting.put(69, "id");
    accepting.put(68, "id");
    accepting.put(67, "id");
    accepting.put(66, "if");
    accepting.put(65, "boolean");
    accepting.put(64, "id");
    accepting.put(63, "id");
    accepting.put(62, "for");
    accepting.put(61, "id");
    accepting.put(60, "id");
    accepting.put(59, "boolean");
    accepting.put(58, "id");
    accepting.put(57, "id");
    accepting.put(56, "return");
    accepting.put(55, "id");
    accepting.put(54, "id");
    accepting.put(53, "id");
    accepting.put(52, "id");
    accepting.put(51, "id");
    accepting.put(50, "id");
    accepting.put(48, "number");
    accepting.put(47, "number");
    accepting.put(46, "number");
    accepting.put(45, "number");
    accepting.put(44, "number");
    accepting.put(43, "number");
    accepting.put(41, "number");
    accepting.put(39, "number");
    accepting.put(38, "skip");
    accepting.put(37, "number");
    accepting.put(36, "bb");
    accepting.put(35, "exp");
    accepting.put(34, "be");
    accepting.put(33, "eol");
    accepting.put(32, "prints");
    accepting.put(31, "oindex");
    accepting.put(30, "and");
    accepting.put(29, "tenary_op1");
    accepting.put(28, "tenary_op2");
    accepting.put(27, "cindex");
    accepting.put(26, "rparen");
    accepting.put(25, "id");
    accepting.put(24, "id");
    accepting.put(23, "id");
    accepting.put(22, "id");
    accepting.put(21, "id");
    accepting.put(20, "id");
    accepting.put(19, "id");
    accepting.put(18, "lparen");
    accepting.put(17, "or");
    accepting.put(16, "lt");
    accepting.put(15, "concat");
    accepting.put(14, "gets");
    accepting.put(13, "divide");
    accepting.put(12, "multiply");
    accepting.put(11, "id");
    accepting.put(10, "id");
    accepting.put(9, "negate_l");
    accepting.put(7, "id");
    accepting.put(6, "listsep");
    accepting.put(5, "gt");
    accepting.put(4, "minus");
    accepting.put(3, "mod");
    accepting.put(2, "plus");
    accepting.put(1, "id");
  } // end buildDFA

  private void buildState0() {
    char[] tc = {125,124,123,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,94,93,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,38,37,36,34,33,32,13,10,9,};
    int[]  st = {34,17,36,22,22,22,20,22,22,24,10,25,22,22,22,11,22,22,22,22,21,22,22,23,1,7,22,19,22,22,35,27,31,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,22,29,5,14,16,33,28,37,37,37,37,37,37,37,37,37,37,13,15,4,6,2,12,26,18,30,3,32,8,9,38,38,38,38,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(0, trans);
  } // end buildState0

  private void buildState1() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,104,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(1, trans);
  } // end buildState1

  private void buildState2() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(2, trans);
  } // end buildState2

  private void buildState3() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(3, trans);
  } // end buildState3

  private void buildState4() {
    char[] tc = {57,56,55,54,53,52,51,50,49,48,46,};
    int[]  st = {102,102,102,102,102,102,102,102,102,102,101,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(4, trans);
  } // end buildState4

  private void buildState5() {
    char[] tc = {61,};
    int[]  st = {100,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(5, trans);
  } // end buildState5

  private void buildState6() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(6, trans);
  } // end buildState6

  private void buildState7() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,99,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(7, trans);
  } // end buildState7

  private void buildState8() {
    char[] tc = {255,254,253,252,251,250,249,248,247,246,245,244,243,242,241,240,239,238,237,236,235,234,233,232,231,230,229,228,227,226,225,224,223,222,221,220,219,218,217,216,215,214,213,212,211,210,209,208,207,206,205,204,203,202,201,200,199,198,197,196,195,194,193,192,191,190,189,188,187,186,185,184,183,182,181,180,179,178,177,176,175,174,173,172,171,170,169,168,167,166,165,164,163,162,161,160,159,158,157,156,155,154,153,152,151,150,149,148,147,146,145,144,143,142,141,140,139,138,137,136,135,134,133,132,131,130,129,128,127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,};
    int[]  st = {95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,96,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,97,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(8, trans);
  } // end buildState8

  private void buildState9() {
    char[] tc = {61,};
    int[]  st = {94,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(9, trans);
  } // end buildState9

  private void buildState10() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,89,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(10, trans);
  } // end buildState10

  private void buildState11() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,84,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(11, trans);
  } // end buildState11

  private void buildState12() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(12, trans);
  } // end buildState12

  private void buildState13() {
    char[] tc = {47,};
    int[]  st = {82,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(13, trans);
  } // end buildState13

  private void buildState14() {
    char[] tc = {61,};
    int[]  st = {81,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(14, trans);
  } // end buildState14

  private void buildState15() {
    char[] tc = {57,56,55,54,53,52,51,50,49,48,};
    int[]  st = {79,79,79,79,79,79,79,79,79,79,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(15, trans);
  } // end buildState15

  private void buildState16() {
    char[] tc = {62,61,};
    int[]  st = {77,78,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(16, trans);
  } // end buildState16

  private void buildState17() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(17, trans);
  } // end buildState17

  private void buildState18() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(18, trans);
  } // end buildState18

  private void buildState19() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,71,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(19, trans);
  } // end buildState19

  private void buildState20() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,67,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(20, trans);
  } // end buildState20

  private void buildState21() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,66,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(21, trans);
  } // end buildState21

  private void buildState22() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(22, trans);
  } // end buildState22

  private void buildState23() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,61,52,52,52,52,52,52,52,52,52,52,52,52,52,60,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(23, trans);
  } // end buildState23

  private void buildState24() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,57,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(24, trans);
  } // end buildState24

  private void buildState25() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,50,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(25, trans);
  } // end buildState25

  private void buildState26() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(26, trans);
  } // end buildState26

  private void buildState27() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(27, trans);
  } // end buildState27

  private void buildState28() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(28, trans);
  } // end buildState28

  private void buildState29() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(29, trans);
  } // end buildState29

  private void buildState30() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(30, trans);
  } // end buildState30

  private void buildState31() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(31, trans);
  } // end buildState31

  private void buildState32() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(32, trans);
  } // end buildState32

  private void buildState33() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(33, trans);
  } // end buildState33

  private void buildState34() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(34, trans);
  } // end buildState34

  private void buildState35() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(35, trans);
  } // end buildState35

  private void buildState36() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(36, trans);
  } // end buildState36

  private void buildState37() {
    char[] tc = {114,57,56,101,55,54,53,52,51,50,49,48,69,46,};
    int[]  st = {42,39,39,41,39,39,39,39,39,39,39,39,41,40,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(37, trans);
  } // end buildState37

  private void buildState38() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(38, trans);
  } // end buildState38

  private void buildState39() {
    char[] tc = {114,57,56,101,55,54,53,52,51,50,49,48,69,46,};
    int[]  st = {42,37,37,41,37,37,37,37,37,37,37,37,41,49,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(39, trans);
  } // end buildState39

  private void buildState40() {
    char[] tc = {57,56,55,54,53,52,51,50,49,48,};
    int[]  st = {47,47,47,47,47,47,47,47,47,47,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(40, trans);
  } // end buildState40

  private void buildState41() {
    char[] tc = {45,43,57,56,55,54,53,52,51,50,49,48,};
    int[]  st = {45,45,46,46,46,46,46,46,46,46,46,46,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(41, trans);
  } // end buildState41

  private void buildState42() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,44,44,44,44,44,44,44,44,44,44,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(42, trans);
  } // end buildState42

  private void buildState43() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,44,44,44,44,44,44,44,44,44,44,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(43, trans);
  } // end buildState43

  private void buildState44() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,44,44,44,44,44,44,44,44,44,44,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,43,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(44, trans);
  } // end buildState44

  private void buildState45() {
    char[] tc = {57,56,55,54,53,52,51,50,49,48,};
    int[]  st = {46,46,46,46,46,46,46,46,46,46,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(45, trans);
  } // end buildState45

  private void buildState46() {
    char[] tc = {57,56,55,54,53,52,51,50,49,48,};
    int[]  st = {46,46,46,46,46,46,46,46,46,46,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(46, trans);
  } // end buildState46

  private void buildState47() {
    char[] tc = {57,56,101,55,54,53,52,51,50,49,48,69,};
    int[]  st = {48,48,41,48,48,48,48,48,48,48,48,41,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(47, trans);
  } // end buildState47

  private void buildState48() {
    char[] tc = {57,56,101,55,54,53,52,51,50,49,48,69,};
    int[]  st = {47,47,41,47,47,47,47,47,47,47,47,41,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(48, trans);
  } // end buildState48

  private void buildState49() {
    char[] tc = {57,56,55,54,53,52,51,50,49,48,};
    int[]  st = {48,48,48,48,48,48,48,48,48,48,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(49, trans);
  } // end buildState49

  private void buildState50() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,53,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(50, trans);
  } // end buildState50

  private void buildState51() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(51, trans);
  } // end buildState51

  private void buildState52() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(52, trans);
  } // end buildState52

  private void buildState53() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,54,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(53, trans);
  } // end buildState53

  private void buildState54() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,55,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(54, trans);
  } // end buildState54

  private void buildState55() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,56,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(55, trans);
  } // end buildState55

  private void buildState56() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(56, trans);
  } // end buildState56

  private void buildState57() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,58,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(57, trans);
  } // end buildState57

  private void buildState58() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,59,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(58, trans);
  } // end buildState58

  private void buildState59() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(59, trans);
  } // end buildState59

  private void buildState60() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,63,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(60, trans);
  } // end buildState60

  private void buildState61() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,62,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(61, trans);
  } // end buildState61

  private void buildState62() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(62, trans);
  } // end buildState62

  private void buildState63() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,64,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(63, trans);
  } // end buildState63

  private void buildState64() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,65,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(64, trans);
  } // end buildState64

  private void buildState65() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(65, trans);
  } // end buildState65

  private void buildState66() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(66, trans);
  } // end buildState66

  private void buildState67() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,68,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(67, trans);
  } // end buildState67

  private void buildState68() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,69,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(68, trans);
  } // end buildState68

  private void buildState69() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,70,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(69, trans);
  } // end buildState69

  private void buildState70() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(70, trans);
  } // end buildState70

  private void buildState71() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,72,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(71, trans);
  } // end buildState71

  private void buildState72() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,73,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(72, trans);
  } // end buildState72

  private void buildState73() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,74,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(73, trans);
  } // end buildState73

  private void buildState74() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,75,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(74, trans);
  } // end buildState74

  private void buildState75() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,76,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(75, trans);
  } // end buildState75

  private void buildState76() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(76, trans);
  } // end buildState76

  private void buildState77() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(77, trans);
  } // end buildState77

  private void buildState78() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(78, trans);
  } // end buildState78

  private void buildState79() {
    char[] tc = {57,56,101,55,54,53,52,51,50,49,48,69,};
    int[]  st = {80,80,41,80,80,80,80,80,80,80,80,41,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(79, trans);
  } // end buildState79

  private void buildState80() {
    char[] tc = {57,56,101,55,54,53,52,51,50,49,48,69,};
    int[]  st = {79,79,41,79,79,79,79,79,79,79,79,41,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(80, trans);
  } // end buildState80

  private void buildState81() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(81, trans);
  } // end buildState81

  private void buildState82() {
    char[] tc = {255,254,253,252,251,250,249,248,247,246,245,244,243,242,241,240,239,238,237,236,235,234,233,232,231,230,229,228,227,226,225,224,223,222,221,220,219,218,217,216,215,214,213,212,211,210,209,208,207,206,205,204,203,202,201,200,199,198,197,196,195,194,193,192,191,190,189,188,187,186,185,184,183,182,181,180,179,178,177,176,175,174,173,172,171,170,169,168,167,166,165,164,163,162,161,160,159,158,157,156,155,154,153,152,151,150,149,148,147,146,145,144,143,142,141,140,139,138,137,136,135,134,133,132,131,130,129,128,127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,12,11,9,};
    int[]  st = {83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(82, trans);
  } // end buildState82

  private void buildState83() {
    char[] tc = {255,254,253,252,251,250,249,248,247,246,245,244,243,242,241,240,239,238,237,236,235,234,233,232,231,230,229,228,227,226,225,224,223,222,221,220,219,218,217,216,215,214,213,212,211,210,209,208,207,206,205,204,203,202,201,200,199,198,197,196,195,194,193,192,191,190,189,188,187,186,185,184,183,182,181,180,179,178,177,176,175,174,173,172,171,170,169,168,167,166,165,164,163,162,161,160,159,158,157,156,155,154,153,152,151,150,149,148,147,146,145,144,143,142,141,140,139,138,137,136,135,134,133,132,131,130,129,128,127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,12,11,9,};
    int[]  st = {83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,83,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(83, trans);
  } // end buildState83

  private void buildState84() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,85,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(84, trans);
  } // end buildState84

  private void buildState85() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,86,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(85, trans);
  } // end buildState85

  private void buildState86() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,87,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(86, trans);
  } // end buildState86

  private void buildState87() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,88,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(87, trans);
  } // end buildState87

  private void buildState88() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(88, trans);
  } // end buildState88

  private void buildState89() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,90,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(89, trans);
  } // end buildState89

  private void buildState90() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,91,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(90, trans);
  } // end buildState90

  private void buildState91() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,92,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(91, trans);
  } // end buildState91

  private void buildState92() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,93,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(92, trans);
  } // end buildState92

  private void buildState93() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(93, trans);
  } // end buildState93

  private void buildState94() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(94, trans);
  } // end buildState94

  private void buildState95() {
    char[] tc = {255,254,253,252,251,250,249,248,247,246,245,244,243,242,241,240,239,238,237,236,235,234,233,232,231,230,229,228,227,226,225,224,223,222,221,220,219,218,217,216,215,214,213,212,211,210,209,208,207,206,205,204,203,202,201,200,199,198,197,196,195,194,193,192,191,190,189,188,187,186,185,184,183,182,181,180,179,178,177,176,175,174,173,172,171,170,169,168,167,166,165,164,163,162,161,160,159,158,157,156,155,154,153,152,151,150,149,148,147,146,145,144,143,142,141,140,139,138,137,136,135,134,133,132,131,130,129,128,127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,};
    int[]  st = {95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,96,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,97,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(95, trans);
  } // end buildState95

  private void buildState96() {
    char[] tc = {255,254,253,252,251,250,249,248,247,246,245,244,243,242,241,240,239,238,237,236,235,234,233,232,231,230,229,228,227,226,225,224,223,222,221,220,219,218,217,216,215,214,213,212,211,210,209,208,207,206,205,204,203,202,201,200,199,198,197,196,195,194,193,192,191,190,189,188,187,186,185,184,183,182,181,180,179,178,177,176,175,174,173,172,171,170,169,168,167,166,165,164,163,162,161,160,159,158,157,156,155,154,153,152,151,150,149,148,147,146,145,144,143,142,141,140,139,138,137,136,135,134,133,132,131,130,129,128,127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,};
    int[]  st = {95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,96,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,98,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(96, trans);
  } // end buildState96

  private void buildState97() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(97, trans);
  } // end buildState97

  private void buildState98() {
    char[] tc = {255,254,253,252,251,250,249,248,247,246,245,244,243,242,241,240,239,238,237,236,235,234,233,232,231,230,229,228,227,226,225,224,223,222,221,220,219,218,217,216,215,214,213,212,211,210,209,208,207,206,205,204,203,202,201,200,199,198,197,196,195,194,193,192,191,190,189,188,187,186,185,184,183,182,181,180,179,178,177,176,175,174,173,172,171,170,169,168,167,166,165,164,163,162,161,160,159,158,157,156,155,154,153,152,151,150,149,148,147,146,145,144,143,142,141,140,139,138,137,136,135,134,133,132,131,130,129,128,127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,};
    int[]  st = {95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,96,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,97,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,95,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(98, trans);
  } // end buildState98

  private void buildState99() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(99, trans);
  } // end buildState99

  private void buildState100() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(100, trans);
  } // end buildState100

  private void buildState101() {
    char[] tc = {57,56,55,54,53,52,51,50,49,48,};
    int[]  st = {79,79,79,79,79,79,79,79,79,79,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(101, trans);
  } // end buildState101

  private void buildState102() {
    char[] tc = {57,56,101,55,54,53,52,51,50,49,48,69,46,};
    int[]  st = {103,103,41,103,103,103,103,103,103,103,103,41,40,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(102, trans);
  } // end buildState102

  private void buildState103() {
    char[] tc = {57,56,101,55,54,53,52,51,50,49,48,69,46,};
    int[]  st = {102,102,41,102,102,102,102,102,102,102,102,41,49,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(103, trans);
  } // end buildState103

  private void buildState104() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,105,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(104, trans);
  } // end buildState104

  private void buildState105() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,107,52,52,52,106,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(105, trans);
  } // end buildState105

  private void buildState106() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(106, trans);
  } // end buildState106

  private void buildState107() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,108,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(107, trans);
  } // end buildState107

  private void buildState108() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,51,51,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(108, trans);
  } // end buildState108

} // end CMMTokenizer
