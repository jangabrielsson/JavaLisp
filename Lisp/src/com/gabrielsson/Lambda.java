package com.gabrielsson;

import java.util.Hashtable; 

public class Lambda extends Expr implements Func {
	
	static public class ParamExpr extends Expr {
		Atom param;
		Expr dflt;
		Atom inited;
		public ParamExpr(Atom param, Expr dflt, Atom inited) {
			this.param = param;
			this.dflt = dflt;
			this.inited = inited;
		}
		public String toString() {
			return "("+param+" "+dflt+((inited != null)? " "+inited : "")+")";
		}
	}
	
	static public class ParamBlock {
		Atom regular[];
		ParamExpr optional[];
		Atom restVar;
		Hashtable keys = null;
		int nLocals;
		public ParamBlock(Atom regular[], ParamExpr optional[], Atom restVar, Hashtable keys) {
			this.regular = regular;
			this.optional = optional;
			this.restVar = restVar;
			this.keys = keys;
			nLocals = regular.length+optional.length;
		}
		public boolean wrongArgs(int nargs) {
			return (nargs < regular.length) ||
				   (restVar == null && nargs > nLocals);
		}
		public String toString() {
				return "(" + Expr.arrToListRest(regular)+" "
				           + Expr.arrToListRest(optional)+" "
				           + ((restVar != null) ? "&rest "+restVar : "")
				           + ")";     
		}
	}
	
	ParamBlock pb;   // Parameters

	boolean rest;    // If we have an rest argument e.g. &rest x
	
	Expr body;       // Body expression

	int freeMap[];   // Ptrs into environment for free variables

	int nLocals;     // Number of locals (args + locals)

	boolean macro = false;;

	boolean tail;    // If we are tail recursive

	Lambda(ParamBlock pb, Expr body[], int nLocals, int freeMap[]) {
		this.pb = pb;
		this.body = Progn.mkBody(body);
		this.rest = pb.restVar != null;
		this.nLocals = nLocals;
		this.freeMap = freeMap;
	}

	public Func funBinding(Env env) throws LispException {
		return (Func) eval(env);
	}

	public boolean isMacro() {
		return macro;
	}
	
	public Expr eval(Env env) throws LispException {
		if (freeMap.length > 0) {  // If we have free variables - create a closure
			Binding free[] = new Binding[freeMap.length];
			for (int i = 0; i < freeMap.length; i++)
				free[i] = env.getBinding(freeMap[i]);   // Copy bindings
			return new Closure(this, free);
		} else return this;  // ...otherwise just return the lambda
	}

	Binding bindRest(Env env, Expr args[], boolean evl) throws LispException {
		if (args.length > pb.regular.length+pb.optional.length) {
			int i = pb.regular.length+pb.optional.length;
			Cons c = new Cons((evl) ? args[i++].eval(env) : args[i++]);
			Binding b = new Binding(c);
			while (i < args.length)
				c = (Cons) (c.cdr = new Cons((evl) ? args[i++].eval(env) : args[i++]));
			return b;
		} else
			return new Binding(JLisp.NIL);
	}

	public Expr apply(Env env, Binding free[], Expr args1[]) throws LispException {
		Expr args2[] = new Expr[args1.length];
		for(int i = 0; i < args1.length; i++)
			args2[i] = args1[i].eval(env);
		return apply2(env,free,args2);
	}

	public Expr apply2(Env env, Binding free[], Expr args[]) throws LispException {
		if (pb.wrongArgs(args.length))
			throw new LispException("Wrong number of arguments");
		Binding bindings[] = new Binding[nLocals];

		for (int i = 0; i < pb.regular.length; i++)       // Stuff regular args
			bindings[i] = new Binding(args[i]);

		int bo = args.length-pb.regular.length;           // Stuff optional args
		for (int i = 0; i < pb.optional.length; i++)
			bindings[pb.regular.length+i] = new Binding(i >= bo ? pb.optional[i].dflt.eval(env): args[pb.regular.length+i]);

		if (rest)                                         // Bind rest args
			bindings[pb.regular.length+pb.optional.length] = bindRest(env, args, false);

		Env nEnv = new Env(bindings, free);
		Expr temp[] = null;

		while (true) {
			Expr e = body.eval(nEnv);
			if (e instanceof Call) { // Tail recursive call
				Expr args2[] = ((Call) e).args;
				if (temp == null)
					temp = new Expr[args.length];
				
				for (int i = 0; i < args.length; i++)
					temp[i] = args2[i].eval(nEnv);
				
				for (int i = 0; i < pb.regular.length; i++)   // Stuff regular args
					bindings[i].val = temp[i];
				
				int boo = args.length-pb.regular.length;      // Stuff optional args
				for (int i = 0; i < pb.optional.length; i++)
					bindings[pb.regular.length+i].val = (i >= boo) ? pb.optional[i].dflt.eval(env) : temp[pb.regular.length+i];

				if (rest)
					bindings[pb.regular.length+pb.optional.length] = bindRest(env, temp, true);
			} else
				return e;
		}
	}

	public boolean isTailRec(Expr curr) {
		tail = body.isTailRec(curr);
		return tail;
	}

	public String toString() {
		if (JLisp.verbose)
			return "(LAMBDA " + bnds() + pb + " " + body
					+ ")";
		else
			return "(LAMBDA " + pb + " " + body + ")";
	}

	String bnds() {
		if (freeMap.length == 0)
			return "[]";
		String s = "[" + freeMap[0];
		for (int i = 1; i < freeMap.length; i++)
			s = s + "," + freeMap[i];
		return s + "]";
	}
}
