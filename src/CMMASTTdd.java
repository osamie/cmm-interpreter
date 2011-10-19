
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * 
 */
public class CMMASTTdd {

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    private static final String[] test01 = new String[]{
        "boolean main() { number x; x = 2r10010; print(x); true; }=>18.0=>=>5",
        "boolean main() { number x; x = 18; print(x); true; }=>18.0=>Decimal requires a base. =>3",
        "boolean main() { number x; x = 1r1111111; print(x); true; }=>7.0=>Base 1 doesn't work. =>2",
        "boolean main() { number x; x = 16r9FBF7; print(x); true; }=>654327.0=>Base 16 doesn't work. =>3",
        "boolean main() { print(3r120 + 4r120 + 5r120); true; }=>74.0=>Arithmatic with alternate base constants doesn't work. =>2"
    };
    private static final String[] test02 = new String[]{
        "boolean main() { number x; x = 0; do { print(x); x = x + 1; } while (x < 3); true; }=>0.01.02.0=>=>5",
        "boolean main() { do { print(1); } while (false); true; }=>1.0=>do ... while (false) doesn't work. =>2"
    };
    private static final String[] test03 = new String[]{
        "boolean main() { if (true) { print(1); } true; }=>1.0=>=>5",
        "boolean main() { if (false) { print(1); } true; }=>=>=>5",
        "boolean main() { if (true) { print(1); } else { print(2); } true; }=>1.0=>Else doesn't work. =>3",
        "boolean main() { if (false) { print(1); } else { print(2); } true; }=>2.0=>Else doesn't work. =>3",
        "boolean main() { if (true) { print(1); } elsif (false) { print(2); } true; }=>1.0=>Elseif doesn't work. =>2",
        "boolean main() { if (true) { print(1); } elsif (true) { print(2); } true; }=>1.0=>Elseif gets executed even if first condition is true. =>2",
        "boolean main() { if (false) { print(1); } elsif (false) { print(2); } true; }=>=>Elseif doesn't work. =>2",
        "boolean main() { if (false) { print(1); } elsif (true) { print(2); } true; }=>2.0=>Elseif doesn't work. =>2",
        "boolean main() { if (false) { print(1); } elsif (false) { print(2); } else { print(3); } true; }=>3.0=>Else doesn't work. =>2"
    };
    private static final String[] test04 = new String[]{
        "boolean main() { print(!true); print(!false); true; }=>falsetrue=>=>5",
        "boolean main() { print(!!true); true; }=>true=>Double negation doesn't work. =>2",
        "boolean main() { boolean x; x = true; print(!x); true; }=>false=>Negation of variables doesn't work. =>3",
        "boolean main() { boolean x; x = !((1 < 3) & (2 < 3)); print(x); true; }=>false=>Negation of expressions doesn't work. =>3"
    };
    private static final String[] test05 = new String[]{
        "boolean main() { print(true ? 1 : 2); print(false ? 1 : 2); true; }=>1.02.0=>=>5",
        "boolean main() { print(true ? \"a\" : \"b\"); print(false ? \"a\" : \"b\"); true; }=>ab=>=>5",
        "boolean main() { number x; x = (true ? 1 : 2); print(x); x = (false ? 1 : 2); print(x); true; }=>1.02.0=>=>5",
        "boolean main() { number x; x = 5; x = ((x > 1 ? false : true) ? 2 : 1); print(x < 2 ? \"a\" : \"b\"); true; }=>a=>Complex choice expressions don't work. =>3"
    };
    private static final String[] test06 = new String[]{
        "boolean main() { print(\"a\" . \"b\"); true; }=>ab=>=>5",
        "boolean main() { string x; x = \"a\"; x = x . \"b\"; print(x); true; }=>ab=>Concatenation doesn't accept variables. =>3",
        "boolean main() { print(\"a\" . \"b\" . \"c\". \"d\"); true; }=>abcd=>Concatenation of multiple strings doesn't work. =>3",
        "boolean main() { print(\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\" . \"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\"); true; }=>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb=>Concatenation of long strings doesn't work. =>3"
    };
    private static final String[] test07 = new String[]{
        "boolean main() { print(\"a\" \"b\"); true; }=>ab=>=>5",
        "boolean main() { print(\"a\" \"b\" \"c\" \"d\"); true; }=>abcd=>Concatenation of multiple strings doesn't work. =>3",
        "boolean main() { print(\"a\"     \"b\"	\"c\"); true; }=>abc=>Concatenation with other whitespace doesn't work. =>2",
        "boolean main() { print(\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\" \"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\"); true; }=>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb=>Concatenation of long strings doesn't work. =>3"
    };
    private static final String[] test08 = new String[]{
        "boolean main() { number x = 3; print(x); boolean b = true; print(b); string s = \"rty\"; print(s); true; }=>3.0truerty=>=>5",
        "boolean main() { number x = 3 + 2; print(x); true; }=>5.0=>Non-constant initializations don't work. =>2",
        "boolean main() { number x; x = 3; print(x); true; }=>3.0=>Declarations without initialization don't work. =>3",
        "boolean main() { number x = 7, y = 4; print(x + y); true; }=>11.0=>Multiple initializations don't work. =>3"
    };
    private static final String[] test09 = new String[]{
        "boolean main() { print(\"abc\"[0]); print(\"abc\"[1]); print(\"abc\"[2]); true; }=>97.098.099.0=>=>5",
        "boolean main() { string s; s = \"rtfh\"; print(s[2]); true; }=>102.0=>Indexing string variables doesn't work. =>4",
        "boolean main() { print(\"abc\"[0] + 5); true; }=>102.0=>ASCII values can't be used for arithmetic. =>2"
    };
    private static final String[] test10 = new String[]{
        "boolean main() { number i; for(i = 1; i < 5; i = i + 1) { print(i); } true; }=>1.02.03.04.0=>=>5",
        "boolean main() { number i; for(i = 10; i > 3; i = i - 3) { print(i); } true; }=>10.07.04.0=>=>5",
        "boolean main() { number i; boolean b; b = true; for(i = 3; b; i = 2) { print(i); b = (i > 2); } true; }=>3.02.0=>Boolean variables in condition don't work. =>2"
    };
    private static final String[] test11 = new String[]{
        "boolean main() { $3; $true; }=>3.0true=>=>5",
        "boolean main() { $\"ab\"; true; }=>ab=>=>5",
        "boolean main() { number x; x = 3; $x; true; }=>3.0=>=>3",
        "boolean main() { number x; x = $($5 + $10); true; }=>5.010.015.0=>=>3"
    };
    private static final String[] test12 = new String[]{
        "boolean main() { number f() { 3; } print(f()); true; }=>3.0=>=>5",
        "boolean main() { number f(number a, number b) { a + b; } print(f(1, 2)); true; }=>3.0=>=>5",
        "boolean main() { number f(number a, number b) { number g(number i) { i * i; } g(a) + g(b); } print(f(1, 2)); true; }=>5.0=>Nested functions with arguments don't work. =>4",
        "boolean main() { number f(number a, number b) { number g(number i) { b * i; } g(a) + g(b); } print(f(3, 2)); true; }=>10.0=>Nested functions that reference the parent function's arguments don't work. =>1",
        "boolean main() { number f1(number a1) { number f2(number a2) { number f3(number a3) { number f4(number a4) { number f5(number a5) { a5; } f5(a4 + 1); } f4(a3 + 1); } f3(a2 + 1); } f2(a1 + 1); } print(f1(123)); true; }=>127.0=>Lots of nested functions with arguments don't work. =>1"
    };
    private static final String[] test13 = new String[]{
        "boolean main() { true; }=>true=>Function return values aren't checked correctly. =>1",
        "boolean main() { 1; }=>false=>Function return values aren't checked correctly. =>1",
        "boolean main() { \"a\"; }=>false=>Function return values aren't checked correctly. =>1",
        "number main() { true; }=>false=>Function return values aren't checked correctly. =>1",
        "number main() { 1; }=>true=>Function return values aren't checked correctly. =>1",
        "number main() { \"a\"; }=>false=>Function return values aren't checked correctly. =>1",
        "string main() { true; }=>false=>Function return values aren't checked correctly. =>1",
        "string main() { 1; }=>false=>Function return values aren't checked correctly. =>1",
        "string main() { \"a\"; }=>true=>Function return values aren't checked correctly. =>1",
        "boolean main() { boolean x; x = true; true; }=>true=>Assignments aren't checked correctly. =>1",
        "boolean main() { boolean x; x = 1; true; }=>false=>Assignments aren't checked correctly. =>1",
        "boolean main() { boolean x; x = \"a\"; true; }=>false=>Assignments aren't checked correctly. =>1",
        "boolean main() { number x; x = true; true; }=>false=>Assignments aren't checked correctly. =>1",
        "boolean main() { number x; x = 1; true; }=>true=>Assignments aren't checked correctly. =>1",
        "boolean main() { number x; x = \"a\"; true; }=>false=>Assignments aren't checked correctly. =>1",
        "boolean main() { string x; x = true; true; }=>false=>Assignments aren't checked correctly. =>1",
        "boolean main() { string x; x = 1; true; }=>false=>Assignments aren't checked correctly. =>1",
        "boolean main() { string x; x = \"a\"; true; }=>true=>Assignments aren't checked correctly. =>1",
        "number main() { 1 + 2; }=>true=>Arguments to + aren't checked correctly. =>1",
        "number main() { true + 2; }=>false=>Arguments to + aren't checked correctly. =>1",
        "number main() { 1 + \"b\"; }=>false=>Arguments to + aren't checked correctly. =>1",
        "number main() { 1 - 2; }=>true=>Arguments to - aren't checked correctly. =>1",
        "number main() { true - 2; }=>false=>Arguments to - aren't checked correctly. =>1",
        "number main() { 1 - \"b\"; }=>false=>Arguments to - aren't checked correctly. =>1",
        "number main() { 1 * 2; }=>true=>Arguments to * aren't checked correctly. =>1",
        "number main() { true * 2; }=>false=>Arguments to * aren't checked correctly. =>1",
        "number main() { 1 * \"b\"; }=>false=>Arguments to * aren't checked correctly. =>1",
        "number main() { 1 / 2; }=>true=>Arguments to / aren't checked correctly. =>1",
        "number main() { true / 2; }=>false=>Arguments to / aren't checked correctly. =>1",
        "number main() { 1 / \"b\"; }=>false=>Arguments to / aren't checked correctly. =>1",
        "number main() { 1 % 2; }=>true=>Arguments to % aren't checked correctly. =>1",
        "number main() { true % 2; }=>false=>Arguments to % aren't checked correctly. =>1",
        "number main() { 1 % \"b\"; }=>false=>Arguments to % aren't checked correctly. =>1",
        "number main() { 1 ^ 2; }=>true=>Arguments to ^ aren't checked correctly. =>1",
        "number main() { true ^ 2; }=>false=>Arguments to ^ aren't checked correctly. =>1",
        "number main() { 1 ^ \"b\"; }=>false=>Arguments to ^ aren't checked correctly. =>1",
        "boolean main() { 1 > 2; }=>true=>Arguments to > aren't checked correctly. =>1",
        "boolean main() { true > 2; }=>false=>Arguments to > aren't checked correctly. =>1",
        "boolean main() { 1 > \"a\"; }=>false=>Arguments to > aren't checked correctly. =>1",
        "boolean main() { true & false; }=>true=>Arguments to & aren't checked correctly. =>1",
        "boolean main() { 1 & false; }=>false=>Arguments to & aren't checked correctly. =>1",
        "boolean main() { true & \"a\"; }=>false=>Arguments to & aren't checked correctly. =>1",
        "boolean main() { true | false; }=>true=>Arguments to | aren't checked correctly. =>1",
        "boolean main() { 1 | false; }=>false=>Arguments to | aren't checked correctly. =>1",
        "boolean main() { true | \"a\"; }=>false=>Arguments to | aren't checked correctly. =>1",
        "boolean main() { if (true) { true; } true; }=>true=>If conditions aren't checked correctly. =>1",
        "boolean main() { if (1) { true; } true; }=>false=>If conditions aren't checked correctly. =>1",
        "boolean main() { if (\"a\") { true; } true; }=>false=>If conditions aren't checked correctly. =>1",
        "boolean main() { while (false) { true; } true; }=>true=>While conditions aren't checked correctly. =>1",
        "boolean main() { while (1) { true; } true; }=>false=>While conditions aren't checked correctly. =>1",
        "boolean main() { while (\"a\") { true; } true; }=>false=>While conditions aren't checked correctly. =>1",
        "number f(number a) { a + 1; } number main() { f(1); }=>true=>Functions aren't checked correctly. =>3",
        "boolean f(number a) { a <= 1; } number main() { f(1); }=>false=>Functions aren't checked correctly. =>3",
        "number f(number a) { a + 1; } number main() { f(true); }=>false=>Functions aren't checked correctly. =>3",
        "number f(boolean a) { a + 1; } number main() { f(true); }=>false=>Functions aren't checked correctly. =>3",
        "string main() { \"a\" . \"b\"; }=>true=>Arguments to . aren't checked correctly. =>1",
        "string main() { true . \"b\"; }=>false=>Arguments to . aren't checked correctly. =>1",
        "string main() { \"a\" . 1; }=>false=>Arguments to . aren't checked correctly. =>1",
        "number main() { if (true) { 1; } else { \"a\"; } }=>false=>Both branches of if should yield a valid return value. =>3"
    };
    private static final String[][] mark5tests = new String[][]{test01, test02, test03, test04, test05, test06, test07, test08, test09, test10, test11, test12};
    private static final String[][] mark15tests = new String[][]{test13};
    private static final String[] dummytests = new String[]{
        "boolean main() { print(1); true; }=>1.0=>=>5",
        "boolean main() { print(\"ab\"); true; }=>ab=>=>5",
        "boolean main() { string s; s = \"ab\"; print(s); true; }=>ab=>=>5",
        "boolean main() { print(false); print(true); true; }=>falsetrue=>=>5"
    };
    private static String comments = "";

    public static void main(String[] args) throws FileNotFoundException {
        setupSystem();

        /*for (String test : dummytests) {
            test(test);
        }*/

        //
        int mark = 0;
        List<Integer> wrong = new ArrayList<Integer>();

        for (int i = 0; i < mark5tests.length; i++) {
            System.err.println("Part " + (i + 1));
            int totalMarks = 5;
            int deduction = 0;
            int j = 0;

            while (deduction < totalMarks && j < mark5tests[i].length) {
                if (!test(mark5tests[i][j])) {
                    String[] parts = mark5tests[i][j].split("=>");
                    String comment = parts[2];
                    Integer value = Integer.parseInt(parts[3]);

                    deduction += value;

                    if (!comment.isEmpty() && value < totalMarks) {
                        if (deduction == value) {
                            comments += "Part " + (i + 1) + ": ";
                        }

                        comments += comment + "(-" + (deduction > totalMarks ? totalMarks + value - deduction : value) + ") ";
                    }
                }

                j++;
            }

            if (deduction < totalMarks) {
                mark += totalMarks - deduction;
            } else {
                wrong.add(i + 1);
            }
        }

        for (int i = 0; i < mark15tests.length; i++) {
            System.err.println("Part " + (i + mark5tests.length + 1));
            int totalMarks = 15;
            int deduction = 0;
            int j = 0;

            while (deduction < totalMarks && j < mark15tests[i].length) {
                if (!typecheck(mark15tests[i][j])) {
                    String[] parts = mark15tests[i][j].split("=>");
                    String comment = parts[2];
                    Integer value = Integer.parseInt(parts[3]);

                    deduction += value;

                    if (!comment.isEmpty() && value < totalMarks) {
                        if (deduction == value) {
                            comments += "Part " + (i + mark5tests.length + 1) + ": ";
                        }

                        comments += comment + "(-" + (deduction > totalMarks ? totalMarks + value - deduction : value) + ") ";
                    }
                }

                j++;
            }

            if (deduction < totalMarks) {
                mark += totalMarks - deduction;
            } else {
                wrong.add(i + 13);
            }
        }

        //System.err.println("Part 13 not completed.");
        //wrong.add(13);

        // Finalize the comments
        if (wrong.isEmpty() && comments.isEmpty()) {
            comments = "Correct.";
        } else {
            if (wrong.size() == 1) {
                comments = "Part " + wrong.get(0) + " is incorrect. " + comments;
            } else if (wrong.size() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("Parts ");

                int last = wrong.get(wrong.size() - 1);
                wrong.remove(wrong.size() - 1);

                boolean first = true;

                for (Integer i : wrong) {
                    if (first) {
                        sb.append(i);
                        first = false;
                    } else {
                        sb.append(", ");
                        sb.append(i);
                    }
                }

                sb.append(" and ");
                sb.append(last);
                sb.append(" are incorrect. ");
                sb.append(comments);

                comments = sb.toString();
            }
        }

        System.err.println("Mark: " + mark);
        System.err.println(comments);//*/

        restoreSystem();
    }
    private static PrintStream out = null;
    private static SecurityManager secure = null;
    private static final StringBuilder output = new StringBuilder();

    private static void setupSystem() {
        // Change the System.out to write to my stringbuilder
        System.setOut(new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                output.append((char) b);
            }
        }));

        // Don't allow calls to System.exit();
        secure = System.getSecurityManager();

        System.setSecurityManager(new SecurityManager() {

            @Override
            public void checkExit(int code) {
                throw new ExitTrappedException();
            }

            @Override
            public void checkPermission(Permission perm) {
                if ("exitVM".equals(perm.getName())) {
                    throw new ExitTrappedException();
                } else if (secure != null) {
                    secure.checkPermission(perm);
                }
            }

            @Override
            public void checkPermission(Permission perm, Object context) {
                if ("exitVM".equals(perm.getName())) {
                    throw new ExitTrappedException();
                } else if (secure != null) {
                    secure.checkPermission(perm);
                }
            }
        });
    }

    private static void restoreSystem() {
        System.setOut(out);
        System.setSecurityManager(secure);
    }

    private static boolean test(String test) {
        String[] parts = test.split("=>");
        String code = parts[0];
        String expectedResult = parts[1];

        Reader r = new StringReader(code);

        try {
            CMMTokenizer t = new CMMTokenizer(r);
            CMMParser p = new CMMParser(t);
            CMMASTNode n = p.parse();
            n.accept(new CMMInterpreterVisitor(), new CMMEnvironment());

            // Retrieve the ouput
            String progOutput = output.toString().trim().replaceAll("\n", "").replaceAll("\"", "");
            output.delete(0, output.length());

            if (expectedResult.equals(progOutput)) {
                return true;
            } else {
                if (expectedResult.contains("+-")) {
                    String[] parts2 = expectedResult.split("\\+-");
                    double expected = java.lang.Double.parseDouble(parts2[0]);
                    double deviation = java.lang.Double.parseDouble(parts2[1]);

                    double actualResult = java.lang.Double.parseDouble(progOutput);

                    if (Math.abs(expected - actualResult) < deviation) {
                        return true;
                    }
                }

                System.err.println("For test (" + test + "), program output (" + progOutput + ") did not match expected output (" + expectedResult + ")");
                System.err.println();
                return false;
            }
        } catch (ExitTrappedException e) {
            System.err.println("ExitTrappedException for test: " + test);
            System.err.println();
            return false;
        } catch (Exception e) {
            System.err.println("Exception for test: " + test);
            System.err.println(e.getClass() + " - " + e.getMessage());
            System.err.println();
            return false;
        }
    }

    private static boolean typecheck(String test) {
        String[] parts = test.split("=>");
        String code = parts[0];
        boolean expectSuccess = Boolean.parseBoolean(parts[1]);

        Reader r = new StringReader(code);

        try {
            CMMTokenizer t = new CMMTokenizer(r);
            CMMParser p = new CMMParser(t);
            CMMASTNode n = p.parse();
            //n.accept(, null);

            //// TODO:
            if (false) {
                throw new RuntimeException("Type checking failed.");
            }

            // Retrieve the ouput ?
            String progOutput = output.toString().trim();//.replaceAll("\n", "");
            output.delete(0, output.length());

            if (expectSuccess) {
                return true;
            } else {
                System.err.println("For test (" + test + "), typechecking was succesful, while typing is incorrect.");
                System.err.println("Program output: " + progOutput);
                System.err.println();
                return false;
            }
        } catch (ExitTrappedException e) {
            System.err.println("Exception for test: " + test);
            System.err.println(e.getClass() + " - " + e.getMessage());

            if (expectSuccess) {
                System.err.println("TEST FAILED!");
            } else {
                System.err.println("Test succesful.");
            }

            System.err.println();
            return !expectSuccess;
        } catch (Exception e) {
            System.err.println("Exception for test: " + test);
            System.err.println(e.getClass() + " - " + e.getMessage());

            if (expectSuccess) {
                System.err.println("TEST FAILED!");
            } else {
                System.err.println("Test succesful.");
            }

            System.err.println();
            return !expectSuccess;
        }
    }

    private static class ExitTrappedException extends SecurityException {
    }
}
