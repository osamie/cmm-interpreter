

public class CMMInterpreterVisitor implements
		CMMVisitor<CMMData, CMMEnvironment> {

	/**
	 * Environment keeps track of variable bindings
	 */
	protected CMMEnvironment env;
	
	public CMMInterpreterVisitor() {
		env = new CMMEnvironment();
	}

	public CMMData visit(CMMASTNode node, CMMEnvironment data) {
		return null;
	}
	
	public CMMData visit(CMMASTProgramNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	// FunctionDefinition -> Type id ParameterList Block
	public CMMData visit(CMMASTFunctionDefinitionNode node, CMMEnvironment data) {
		String id = node.getChild(1).getValue();
		env.bind(id, new CMMFunction(node));
		if (id.equals("main")) {
			return node.getChild(3).accept(this, data);
		}
		return null;
	}

	public CMMData visit(CMMASTParameterListNode node, CMMEnvironment data) {
		return null;
	}

	public CMMData visit(CMMASTParameterNode node, CMMEnvironment data) {
		return null;
	}

	public CMMData visit(CMMASTElementNode node, CMMEnvironment data) {
		if(node.getChild(0).getName().equals("negate_l") && node.getChild(1).getName().equals("Constant"))
			{
			//throw new RuntimeException ("cannot use the NOT operator on a non boolean value"+node.getChild(1).getName());
				
				CMMData cond= node.getChild(1).accept(this, data);
				if (!(cond instanceof CMMBoolean)) 
					return cond;//throw new RuntimeException ("cannot use the NOT operator on a non boolean value");
				CMMBoolean cd = (CMMBoolean) cond;
				return new CMMBoolean(!cd.value());
			}
			
		return visitChildren(node, data);
	}

	public CMMData visit(CMMASTExpressionListNode node, CMMEnvironment data) {
		
		return visitChildren(node, data);
	}
    
	//SimpleStatement -> negate_l* Assignment eol
	public CMMData visit(CMMASTSimpleStatementNode node, CMMEnvironment data) {
		
		if (node.getChild(0).getName().equals("negate_l")) throw new RuntimeException("found a negate assingment");
		return visitChildren(node, data);
	}
     
	//Constant -> negate_l?  
	public CMMData visit(CMMASTConstantNode node, CMMEnvironment data) {
		
		if (node.numChildren() == 1) return node.getChild(0).accept(this, data);
		
		CMMData cont = node.getChild(1).accept(this, data);
		
		if (!(cont instanceof CMMBoolean)) throw new RuntimeException("Expected a boolean");
		CMMBoolean cb = (CMMBoolean) cont;
		
		//return (new CMMBoolean(cb.value() ? false:true ));  
		
		return new CMMBoolean(!(cb.value()));
		//return visitChildren(node, data);
	}

	// Assignment -> Logical (gets Logical)?
	public CMMData visit(CMMASTAssignmentNode node, CMMEnvironment data) {
		
		//getChild(0).getName().equals("negate_l")
		
		//if (node.getChild(1).getName().equals("Negatedlogical")) throw new RuntimeException("found a bla");
		if (node.numChildren() > 1) {
			
			//if(node.getChild(0).getName().equals("negate_l")) throw new RuntimeException("found a negate! and a" + node.getChild(0).getName() );
			
			/**CMMASTNode n = node.getChild(0);  //Negatedlogical
			if (!n.getName().equals("Negatedlogical") || n.numChildren() != 1)
				throw new RuntimeException("Assigning to non-lvalue");
			 n = n.getChild(0);
			 **/
			CMMASTNode n = node.getChild(0); //Element
			if (!n.getName().equals("Element") || n.numChildren() != 1)
				throw new RuntimeException("Assigning to non-lvalue");
			n = n.getChild(0);   // ElementPlus
			if (!n.getName().equals("ElementPlus") || n.numChildren() != 1) 
				throw new RuntimeException("Assigning to non-lvalue");
			n = n.getChild(0);   // Token
			if (!n.getName().equals("id"))
				throw new RuntimeException("Assigning to non-lvalue");
			String id = n.getValue();
			if (env.lookup(id) == null)
				throw new RuntimeException("Assigning to undeclared variable " + id);
			CMMData res = node.getChild(2).accept(this, data);
			if (res.getClass() != env.lookup(id).getClass()) 
				throw new RuntimeException("Type mismatch on assignment " 
						+ res.getClass() + " vs. " + env.lookup(id).getClass());
			env.assign(id, res);
			return res; 
		} else {
			return visitChildren(node, data);
		}
	}

	// Logical -> Comparison ((and|or) Comparison)*  [>1]
	public CMMData visit(CMMASTLogicalNode node, CMMEnvironment data) {
		
		if(node.getChild(0).getName().equals("negate_l")) throw new RuntimeException("found a logical node");
		
		CMMData x = node.getChild(0).accept(this, data);
		if (!(x instanceof CMMBoolean)) {
			throw new RuntimeException("Invalid operand to logical operator");
		}
		CMMBoolean a = (CMMBoolean)x;
		for (int i = 1; i < node.numChildren(); i += 2) {
			CMMData y = node.getChild(i+1).accept(this, data);
			String op = node.getChild(i).getName();
			if (!(y instanceof CMMBoolean)) {
				throw new RuntimeException("Invalid operand to logical operator");
			}
			CMMBoolean b = (CMMBoolean)y;
			if (op.equals("and")) {
				a = new CMMBoolean(a.value() && b.value());
			} else if (op.equals("or")) {
				a = new CMMBoolean(a.value() || b.value());
			} else {
				throw new RuntimeException("Unknown operator:" + op);
			}
		}
		return a;
	}

	// Comparison -> Sum ((lt|gt|eq|le|ge|ne) Sum)?  [>1]
	public CMMData visit(CMMASTComparisonNode node, CMMEnvironment data) {
		CMMData x = node.getChild(0).accept(this, data);
		CMMData y = node.getChild(2).accept(this, data);
		if (!(x instanceof CMMNumber) || !(y instanceof CMMNumber)) {
			throw new RuntimeException("Invalid operand to comparison operator");
		}
		CMMNumber a = (CMMNumber)x;
		CMMNumber b = (CMMNumber)y;
		String op = node.getChild(1).getName();
		if (op.equals("lt")) {
			return new CMMBoolean(a.value < b.value);
		} else if (op.equals("gt")) {
			return new CMMBoolean(a.value > b.value);
		} else if (op.equals("le")) {
			return new CMMBoolean(a.value <= b.value);
		} else if (op.equals("ge")) {
			return new CMMBoolean(a.value >= b.value);
		} else if (op.equals("eq")) {
			return new CMMBoolean(a.value == b.value);
		} else if (op.equals("ne")) {
			return new CMMBoolean(a.value != b.value);
		} else {
			throw new RuntimeException("Unknown operator:" + op);
		}
	}

	// Sum -> Term ((plus|minus) Term)*  [>1]
	public CMMData visit(CMMASTSumNode node, CMMEnvironment data) {
		CMMData x = node.getChild(0).accept(this, data);
		if (!(x instanceof CMMNumber)) {
			throw new RuntimeException("Invalid operand to numerical operator");
		}
		CMMNumber a = (CMMNumber)x;
		for (int i = 1; i < node.numChildren(); i += 2) {
			CMMData y = node.getChild(i+1).accept(this, data);
			String op = node.getChild(i).getName();
			if (!(y instanceof CMMNumber)) {
				throw new RuntimeException("Invalid operand to numerical operator +/-");
			}
			CMMNumber b = (CMMNumber)y;
			if (op.equals("plus")) {
				a = new CMMNumber(a.value() + b.value());
			} else if (op.equals("minus")) {
				a = new CMMNumber(a.value() - b.value());
			} else {
				throw new RuntimeException("Unknown operator:" + op);
			}
		}
		return a;
	}

	// Term -> Exp ((multiply|divide|mod) Exp)* [>1]
	public CMMData visit(CMMASTTermNode node, CMMEnvironment data) {
		CMMData x = node.getChild(0).accept(this, data);
		if (!(x instanceof CMMNumber)) {
			throw new RuntimeException("Invalid operand to numerical operator +/-");
		}
		CMMNumber a = (CMMNumber)x;
		for (int i = 1; i < node.numChildren(); i += 2) {
			CMMData y = node.getChild(i+1).accept(this, data);
			String op = node.getChild(i).getName();
			if (!(y instanceof CMMNumber)) {
				throw new RuntimeException("Invalid operand to numerical operator +/-");
			}
			CMMNumber b = (CMMNumber)y;
			if (op.equals("multiply")) {
				a = new CMMNumber(a.value() * b.value());
			} else if (op.equals("divide")) {
				a = new CMMNumber(a.value() / b.value());
			} else if (op.equals("mod")) {
				a = new CMMNumber(a.value() % b.value());
			} else {
				throw new RuntimeException("Unknown operator:" + op);
			}
		}
		return a;
	}

	// Exp -> Element (exp Element)*  [>1] 
	public CMMData visit(CMMASTExpNode node, CMMEnvironment data) {
		CMMData x = node.getChild(0).accept(this, data);
		if (!(x instanceof CMMNumber)) {
			throw new RuntimeException("Invalid operand to numeric operator");
		}
		CMMNumber a = (CMMNumber)x;
		for (int i = 1; i < node.numChildren(); i += 2) {
			CMMData y = node.getChild(i+1).accept(this, data);
			String op = node.getChild(i).getName();
			if (!(y instanceof CMMNumber)) {
				throw new RuntimeException("Invalid operand to numeric operator");
			}
			CMMNumber b = (CMMNumber)y;
			if (op.equals("exp")) {
				a = new CMMNumber(Math.pow(a.value(), b.value()));
			} else {
				throw new RuntimeException("Unknown operator:" + op);
			}
		}
		return a;
	}

	// ElementPlus -> id ArgumentList?
	public CMMData visit(CMMASTElementPlusNode node, CMMEnvironment data) {
		if (node.numChildren() == 1) { // just an identifier
			return node.getChild(0).accept(this, data); 
		} else { // a function call
			String fname = node.getChild(0).getValue();
			if (fname.equals("print")) {
				CMMData res = visitChildren(node.getChild(1), data);
				System.out.print(res);
				return res;
			}
			if (fname.equals("println")) {
				CMMData res = visitChildren(node.getChild(1), data);
				System.out.println(res);
				return res;
			}
			CMMData f = env.lookup(fname);
			if (!(f instanceof CMMFunction)) {
				throw new RuntimeException("Attempt to call non-function "+ fname);
			}
			CMMFunction fn = (CMMFunction)f;
			env.pushFrame(); // add a frame for the parameters
			env.bind("11this", fn);
			node.getChild(1).accept(this, data);
			CMMData res = fn.value().getChild(3).accept(this, data);  // visit the block now
			if (res == null)
				throw new RuntimeException("Function not returning a value " + fname);
			// TODO: typecheck return value
			env.popFrame();
			return res;
		}
	}

	// ArgumentList -> lparen (Assignment (listsep Assignment)*)? rparen
	public CMMData visit(CMMASTArgumentListNode node, CMMEnvironment data) {
		CMMFunction fn = (CMMFunction)env.lookup("11this");
		CMMASTParameterListNode pl = (CMMASTParameterListNode)fn.value().getChild(2);
		if (pl.numChildren() != node.numChildren()) {
			throw new RuntimeException("Calling function with wrong number of arguments");
		}
		for (int i = 1; i < node.numChildren()-1; i += 2) {
			CMMData value = node.getChild(i).accept(this, data);
			String id = pl.getChild(i).getChild(1).getValue();
			env.bind(id, value);
		}
		return null;
	}

	// WhileLoop -> while Condition Block
	public CMMData visit(CMMASTWhileLoopNode node, CMMEnvironment data) {
		CMMData cont = node.getChild(1).accept(this, data);
		if (!(cont instanceof CMMBoolean)) {
			throw new RuntimeException("Invalid (non-boolean) condition in while loop");
		}
		CMMData res = null;
		CMMBoolean cb = (CMMBoolean)cont;
		while (cb.value()) {
			res = node.getChild(2).accept(this, data);
			cb = (CMMBoolean)node.getChild(1).accept(this, data);
		}
		return res;
	}
//conditon -> negate_l* lparen Assignment rparen
	public CMMData visit(CMMASTConditionNode node, CMMEnvironment data) {
		
		if (node.numChildren() == 3) return visitChildren(node,data);
		
		CMMData cond = node.getChild(2).accept(this, data);
		
		if (!(cond instanceof CMMBoolean)) {
			throw new RuntimeException("Invalid (non-boolean) condition in while loop");
		}
		CMMBoolean cd = (CMMBoolean) cond;
		
		return new CMMBoolean(!cd.value());
		//if(node.getChild(0).getName().equals("negate"))
		
		
		
		//if(node.getChild(0).getName().equals("negate_l")) throw new RuntimeException("found a negation sign");
		//return visitChildren(node, data);
	}

	// DoLoop -> do Block while Condition eol
	public CMMData visit(CMMASTDoLoopNode node, CMMEnvironment data) {
		//visit Block first
		CMMData res = node.getChild(1).accept(this, data);
		
		//check that child[2] is a while.....tokenization already handles that
		
		CMMData cont = node.getChild(3).accept(this, data);
		
		if (!(cont instanceof CMMBoolean)) {
			throw new RuntimeException("Invalid (non-boolean) condition in while loop");
		}
		
		CMMBoolean cb = (CMMBoolean)cont;
		
		while(cb.value()) //while condition is still true
		{
			res = node.getChild(1).accept(this, data);
			cb = (CMMBoolean)node.getChild(3).accept(this, data);
		}
		
		return res;
		
		//CMMData res = null;
		 
		//throw new UnsupportedOperationException();
	}


	//@Override
	public CMMData visit(CMMASTStatementNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	public CMMData visit(CMMASTTypeNode node, CMMEnvironment data) {
		return null;
	}
    //               0     1        2     3       4       5         3/6  4/7
	//IfStatement -> if Condition Block (elsif Condition Block)* (else Block)?
	public CMMData visit(CMMASTIfStatementNode node, CMMEnvironment data) {
		
		//check if child[1] is not CMMBoolean
		CMMData cont = node.getChild(1).accept(this, data);
		
		if (!(cont instanceof CMMBoolean)) {
			throw new RuntimeException("Invalid (non-boolean) condition in if statement");
		}
		
		
		//If child[1] is boolean true visit child[2],Block...return CMMData
		CMMBoolean cb = (CMMBoolean) cont;
		if (cb.value) return node.getChild(2).accept(this, data);
		
		
		
		if (node.numChildren()==3) return null; 
			//throw new RuntimeException("FOUND NULL");
		
		int i = 3;
		
		
		
		if (node.getChild(i).getName().equals("elsif")){
			//CMMASTNode efRes = node.getChild(i);
			
			while(node.getChild(i).getName().equals("elsif")){
				CMMData efcont = node.getChild(i+1).accept(this, data);
				if(!( efcont instanceof CMMBoolean)) 
					{
						throw new RuntimeException("Invalid or non-boolean condition in else if statement");
					}
				CMMBoolean cc = (CMMBoolean) efcont;
				
				if(cc.value) return node.getChild(i+2).accept(this, data);
				if(node.numChildren()==(i+3)) return null;
				i +=3;
			
			}
			
			
		}
		
		 
		return node.getChild(i+1).accept(this, data);
		
	}

	//@Override
	/*
	 * Declaration -> Type Identifier (listsep Identifier)* eol
	 */
	public CMMData visit(CMMASTDeclarationNode node, CMMEnvironment data) {
		CMMASTNode type = node.getChild(0);
		String stype = type.getChild(0).getName();
		if (stype.equals("number_t")) {
			for (int i = 1; i < node.numChildren(); i += 2)
				env.bind(node.getChild(i).getValue(), new CMMNumber(0));
		} else if (stype.equals("string_t")) {
			for (int i = 1; i < node.numChildren(); i += 2)
				env.bind(node.getChild(i).getValue(), new CMMString(""));			
		} else if (stype.equals("boolean_t")) {
			for (int i = 1; i < node.numChildren(); i += 2)
				env.bind(node.getChild(i).getValue(), new CMMBoolean(false));						
		}
		return null;
	}


	public CMMData visit(CMMASTBlockNode node, CMMEnvironment data) {
		env.pushFrame();
		CMMData res = visitChildren(node, data);
		env.popFrame();
		return res;
	}

	public CMMData visit(CMMASTToken node, CMMEnvironment data) {
		
		if (node.getName().equals("number")) {
			if (node.getValue().indexOf('r') > 0) //if this is an arbitrary base number
			{
				String str = node.getValue();
				//trim the string to avoid dealing with white spaces : str.trim() 
				
				String base = "";
				String number = "";
				int count = 0;
				for(int i = 0; i < str.length(); i++)
				{
					if (str.charAt(i) == 'r') {
						count = i + 1; // keeping track of index
						while(count < str.length()){
							number += str.charAt(count);
							count++;
							}
						break;
					}
					base += str.charAt(i);
					
				}
				
				
				
				int b = Integer.parseInt(base);
				
				if (b>29) throw new RuntimeException("cannot read numbers with bases greater than 29");
				
				
				return new CMMNumber(toDecimal(b,number).value);
				//OR I COULD HAVE SIMPLY DONE 
				//return new CMMNumber(Integer.parseInt(number,b));
			}
			
			return new CMMNumber(Double.parseDouble(node.getValue()));
		} else if (node.getName().equals("string")) {
			return new CMMString(node.getValue());
		} else if (node.getName().equals("boolean")) {
			return new CMMBoolean(Boolean.parseBoolean(node.getValue()));			
		} else if (node.getName().equals("id")) {
			String id = node.getValue();
			if (env.lookup(id) == null)
				throw new RuntimeException("Reference to undefined variable " + id);
			return env.lookup(id);
		}
		return null;
	}
	
	/**
	 * Helper function for converting from any base to decimal value
	 * @param base
	 * @param number
	 * @return CMMNumber
	 */
	private CMMNumber toDecimal(int base, String number)
	{
		//TODO: check for wrong base conversion e.g 2rB
		
		
		
		String baseChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String baseChars2 = "0123456789abcdefghijklmnopqrstuvwxyz";
		int iterator = number.length();
		int returnValue = 0; 
		int multiplier = 1;
		
		while( iterator > 0){
			
			String chr = number.substring(iterator-1, iterator);

			//int no_caps = baseChars2.indexOf(number.substring(iterator-1, iterator));
			
			int ch = baseChars.contains(chr) ? baseChars.indexOf(chr) : baseChars2.indexOf(chr); 
			returnValue = returnValue + (ch *multiplier);
			multiplier *= base;
			--iterator;
			
		}
		
		
		return new CMMNumber(returnValue);
	}
	
	// Cond_valueNode -> negate_l? Condition
	 
		/**if (node.numChildren() == 1) return node.getChild(0).accept(this, data);
		
		CMMData cont = node.getChild(1).accept(this, data);
		
		if (!(cont instanceof CMMBoolean)) throw new RuntimeException("Expected a boolean");
		
		CMMBoolean cb = (CMMBoolean) cont;
		
		//return (new CMMBoolean(cb.value() ? false:true ));  
		
		return new CMMBoolean(!(cb.value()));
		//throw new UnsupportedOperationException(); **/
	
	
	
	
	
	protected CMMData visitChildren(CMMASTNode node, CMMEnvironment data) {
		CMMData last = null;
		for (int i = 0; i < node.numChildren(); i++) {
			CMMData tmp = node.getChild(i).accept(this, data);
			if (tmp != null) last = tmp;
		}
		return last;	
	}

		
		public CMMData visit(CMMASTCondvalNode node, CMMEnvironment data) {
			if (node.numChildren() == 1) return node.getChild(0).accept(this, data);
			
			CMMData cont = node.getChild(1).accept(this, data);
			
			if (!(cont instanceof CMMBoolean)) throw new RuntimeException("Expected a boolean");
			
			CMMBoolean cb = (CMMBoolean) cont;
			
			//return (new CMMBoolean(cb.value() ? false:true ));  
			
			return new CMMBoolean(!(cb.value()));
			//throw new UnsupportedOperationException();
			//return null;
		}
		//NegatedlogicalNode -> negate_l* Logical
		public CMMData visit(CMMASTNegatedlogicalNode node, CMMEnvironment data) {
			if (node.numChildren() == 1) return node.getChild(0).accept(this, data);
			
			
			
			CMMData l = node.getChild(node.numChildren()-1).accept(this, data);
			
			//ASSUMING THAT LOGICAL WILL ALWAYS RETURN A BOOLEAN DATA
			
			
			Boolean ret = ((CMMBoolean) l).value();
			
			for (int i = 0; i < (node.numChildren()-1); i++)
			{
				ret = !ret;
			}
			
			return new CMMBoolean(ret);
			
			
			
			//throw new RuntimeException("found a negated logical node");
			//return null;
		}

	

	
}
