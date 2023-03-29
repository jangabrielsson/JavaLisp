package com.gabrielsson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Builtin extends Expr implements Func {

	public static final int CAR = 0;

	public static final int CDR = CAR+1;

	public static final int CONS = CDR+1;

	public static final int ADD = CONS+1;

	public static final int SUB = ADD+1;

	public static final int MUL = SUB+1;

	public static final int DIV = MUL+1;

	public static final int LESSP = DIV+1;

	public static final int RPLACA = LESSP+1;

	public static final int RPLACD = RPLACA+1;

	public static final int FUNCTION = RPLACD+1;

	public static final int FUNSET = FUNCTION+1;

	public static final int APPLY = FUNSET+1;

	public static final int FUNCALL = APPLY+1;

	public static final int EVAL = FUNCALL+1;

	public static final int EQ = EVAL+1;

	public static final int ATOM = EQ+1;
	
	public static final int NUMBERP = ATOM+1;
	
	public static final int CONSP = NUMBERP+1;

	public static final int PRINT = CONSP+1;

	public static final int FLUSH = PRINT+1;

	public static final int THROW = FLUSH+1;

	public static final int ERROR = THROW+1;

	public static final int STRFORMAT = ERROR+1;

	public static final int READ = STRFORMAT+1;

	public static final int READFILE = READ+1;

	public static final int NEW2 = READFILE+1;

	public static final int GENSYM = NEW2+1;

	public static final int JMEMORY = GENSYM+1;
	
	public static final int JTIME = JMEMORY+1;

	int type;
	static int gensymCount = 0;
	
	static String name[] = new String[] { "car", "cdr", "cons", "add", "sub",
			"mul", "div", "lessp", "rplaca", "rplacd", "function", "funset", "apply", "%funcall%", "eval", "eq", "atom", "numberp", "consp", "print",
			"flush", "throw", "error", "strformat", "read", "readfile", "new", "gensym", "%jmemory%", "%jtime%" };

	public Builtin(int type) {
		this.type = type;
	}

	public Expr eval(Env env) throws LispException {
		return this;
	}

	public boolean isMacro() {
		return false;
	}
	
	public Expr apply(Env env, Binding free[], Expr params[]) throws LispException {
		Expr args[] = new Expr[params.length];
		for (int i = 0; i < params.length; i++)
			args[i] = params[i].eval(env);
		return apply2(env,free,args);
	}
	public Expr apply2(Env env, Binding free[], Expr args[]) throws LispException {
		int n = args.length;
		switch (type) {
		case CAR:
			if (n != 1)
				err();
			return ((Cons) args[0]).car;
		case CDR:
			if (n != 1)
				err();
			return ((Cons) args[0]).cdr;
		case CONS:
			if (n != 2)
				err();
			return new Cons(args[0], args[1]);
		case ADD:
			if (n != 2)
				err();
			return ((Num)args[0]).add((Num)args[1]);
		case SUB:
			if (n != 2)
				err();
			return ((Num)args[0]).sub((Num)args[1]);
		case MUL:
			if (n != 2)
				err();
			return ((Num)args[0]).mul((Num)args[1]);
		case DIV:
			if (n != 2)
				err();
			return ((Num)args[0]).div((Num)args[1]);
		case LESSP:
			if (n != 2)
				err();
			return args[0].doubleValue() < args[1].doubleValue() ?
					JLisp.T : JLisp.NIL;
		case FUNCTION:
			Expr e;
			if (n != 1)
				err();
			e = (Expr) args[0].funBinding(env);
			return e;
		case FUNSET:
			if (n != 2)
				err();
			args[0].funset(env,args[1]);
			return (Expr) args[0].funBinding(env);
		case RPLACA:
			if (n != 2)
				err();
			e = args[0];
		    ((Cons)e).car = args[1];
			return e;
		case RPLACD:
			if (n != 2)
				err();
			e = args[0];
		    ((Cons)e).cdr = args[1];
			return e;
		case APPLY:
			if (n != 2)
				err();
			e = (Expr) args[0].funBinding(env); // Macro expands?
			args = Translate.listToArr(args[1]);
			return ((Func)e).apply2(env,null,args);
		case FUNCALL:
			if (n < 1)
				err();
			e = (Expr) args[0].funBinding(env); // Macro expands?
			Expr args3[] = new Expr[args.length-1];
			for(int i = 0; i < args3.length; i++)
				args3[i] = args[i+1];
			return ((Func)e).apply2(env,null,args3);
		case EVAL:
			if (n != 1)
				err();
			Translate.TransExpr tt = Translate.compile(args[0]);
			return tt.run();
		case EQ:
			if (n != 2)
				err();
			return args[0].eq(args[1]) ? JLisp.T
					: JLisp.NIL;
		case ATOM:
			if (n != 1)
				err();
			return args[0].isAtom() || args[0].isString() ? JLisp.T
					: JLisp.NIL;
		case NUMBERP:
			if (n != 1)
				err();
			return args[0] instanceof Num ? JLisp.T
					: JLisp.NIL;
		case CONSP:
			if (n != 1)
				err();
			return args[0] instanceof Cons ? JLisp.T
					: JLisp.NIL;
		case PRINT:
			if (n != 1)
				err();
			e = args[0];
			e.print(false);
			return e;
		case FLUSH:
			System.out.flush();
			return JLisp.NIL;
		case THROW:
			break;
		case ERROR:
			if (n != 1)
				err();
			throw new LispException.Error(args[0].stringValue());
		case STRFORMAT:
			if (n < 1)
				err();
			String format = args[0].stringValue();
			Expr args2[] = new Expr[args.length-1];
			for(int i = 1; i < args.length; i++)
				args2[i-1] = args[i];
			return new Str(String.format(format, (Object[])args2));			
		case READ:
			if (n != 0)
				err();
			else {
				try {
					InputStreamReader isr = new InputStreamReader(System.in);
					BufferedReader in = new BufferedReader(isr);
					return (new Reader(in.readLine())).parse();
				} catch (IOException e4) {
					throw new LispException("Bad expression");
				}
			}
		case READFILE:
			if (n != 1)
				err();
			Main.readFile(args[0].stringValue());
			return JLisp.NIL;
		case NEW2:
			if (n != 1)
				err();
			return new JObject(args);
		case GENSYM:
			if (n != 0)
				err();
			return new Atom("<G:"+gensymCount++ + ">");
		case JMEMORY:
			if (n != 1)
				err();
			long m = 0;
			switch((int)args[0].longValue()) {
			case 0: m = Runtime.getRuntime().freeMemory(); break;
			case 1: m = Runtime.getRuntime().maxMemory(); break;
			case 2: m = Runtime.getRuntime().totalMemory(); break;
			}
			return new Num.LNum(m);
		case JTIME:
			if (n != 1)
				err();
			m = 0;
			switch((int)args[0].longValue()) {
			case 0: m = System.currentTimeMillis(); break;
			case 1: m = System.nanoTime(); break;
			}
			return new Num.LNum(m);

		}
	
		throw new LispException("Unknown builtin");
	}

	void err() throws LispException {
		throw new LispException("Wrong number of args to:" + this);
	}

	public String toString() {
		return "<" + name[type] + ">";
	}
}
