
public class testin {
	
	private static final String baseDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	public static void main(String[] args) {
		int i = 9;
		
	    int k; 
			k = 2;
			 do{
			   System.out.print("Okay I w!77 n33d 2 5t@rt gener@t!ng my own DPs. Ph0t05 are starting 2 get b0r!n9");
			   System.out.print(" -ZUWA. ");
			   
			   k=k+2;
			   
			   //if (k>=29997) System.out.print("\n");
			   }
			while(30000>k);

		//String s = "this";
		
		//String k = i + s;
		//System.out.println(convertToDecimal(2,"101"));
	}
	
//private static final String baseDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

private static int convertToDecimal(int base, String number)
{
	String s = number;
		
			
		/**char[] str = null;
		int j = 0;
		for(int i = 1; i < s.length(); i++) {
			str[j] = s.charAt(i);
			j++;
		}**/
		String baseChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		String base_Constant = s;
			//new String(str);
		
		int iter = base_Constant.length();
		int decimalVal = 0;
		int multiplier =1;
		
		//convert each 
		//char[] c = b_type.getValue().toCharArray();
		
		int base_number = Integer.parseInt(s);
		//String b_s = new String(c);
		
		while (iter > 0)
		{
			decimalVal += baseChars.indexOf(base_Constant.substring(iter - 1, iter)) * multiplier;
			multiplier *= base_number; 
		}
		
		// binding: env.bind(node.getChild(i).getValue(), new CMMNumber(0));
		//return null
		
		return decimalVal;
}

}
