package com.gabrielsson;

import java.util.*;

public class JLisp {

	static boolean verbose = true;

	static boolean uppercase = true;
	
	static Atom OPTIONAL, REST, KEY;

	static Atom NIL;

	static Atom T;

	static Atom QUOTE;

	static Atom LAMBDA, LAMBDA2;

	static Atom IF;

	static Atom LET;

	static Atom FLET;

	static Atom SETQ;

	static Atom WHILE;

	static Atom FUNSET;

	static Atom DEFUN;

	static Atom DEFMACRO;

	static Atom CATCH;

	static Atom THROW;

	static Atom PROGN;

	static Atom FUNCTION;

	static Atom BACK_QUOTE, BACK_COMMA, BACK_COMMA_DOT, BACK_COMMA_AT;

	static Atom TRACE_LEVEL, LOG_LEVEL;

	static Hashtable<String, Atom> symbols;

	JLisp() {
		symbols = new Hashtable<String, Atom>();
		OPTIONAL = define("&optional");
		REST = define("&rest");
		KEY = define("&key");
		NIL = define("nil");
		T = define("t");
		QUOTE = define("quote");
		LAMBDA2 = define("fn");
		LAMBDA = define("lambda");
		IF = define("if");
		LET = define("let");
		FLET = define("let*");
		SETQ = define("setq");
		WHILE = define("while");
		DEFUN = define("defun");
		DEFMACRO = define("defmacro");
		CATCH = define("catch");
		THROW = define("throw");
		PROGN = define("progn");
		BACK_QUOTE = define("*back-quote*");
		BACK_COMMA = define("*back-comma*");
		BACK_COMMA_DOT = define("*back-comma-dot*");
		BACK_COMMA_AT = define("*back-comma-at*");
		TRACE_LEVEL = define("*trace-level*", new Num.LNum(0));
		LOG_LEVEL = define("*log-level*", new Num.LNum(0));
		
		define("jlisp").value = new JObject(this);

		define("atom", Builtin.ATOM);
		define("numberp", Builtin.NUMBERP);
		define("consp", Builtin.CONSP);
		define("car", Builtin.CAR);
		define("cdr", Builtin.CDR);
		define("cons", Builtin.CONS);
		define("+", Builtin.ADD);
		define("-", Builtin.SUB);
		define("*", Builtin.MUL);
		define("/", Builtin.DIV);
		define("<", Builtin.LESSP);
		define("rplaca", Builtin.RPLACA);
		define("rplacd", Builtin.RPLACD);
		FUNCTION = define("function", Builtin.FUNCTION);
		define("funset", Builtin.FUNSET);
		define("apply", Builtin.APPLY);
		define("eval", Builtin.EVAL);
		define("eq", Builtin.EQ);
		define("print", Builtin.PRINT);
		define("flush", Builtin.FLUSH);
		define("throw", Builtin.THROW);
		define("*error*", Builtin.ERROR);
		define("strformat", Builtin.STRFORMAT);
		define("read", Builtin.READ);
		define("readfile", Builtin.READFILE);
		define("new", Builtin.NEW2);
		define("gensym", Builtin.GENSYM);
		define("%jmemory%", Builtin.JMEMORY);
		define("%jtime%", Builtin.JTIME);
		define("%funcall%", Builtin.FUNCALL);

		NIL.value = NIL;
		T.value = T;
	}

	Atom define(String name, int builtin) {
		Atom atom = define(name);
		atom.fun = new Builtin(builtin);
		return atom;
	}

	Atom define(String name, Expr expr) {
		Atom atom = define(name);
		atom.value = expr;
		return atom;
	}

	Atom define(String name) {
		return new Atom(name).intern();
	}

	public Hashtable<String, Atom> getSymbols() {
		return symbols;
	}
	
	public Expr call(String name) throws Exception {
		return new Atom(name).intern().funBinding(null).apply(new Env(0),null,new Expr[0]);
	}
	
	public static void trace(int level, String msg) {
		try {
			if (JLisp.TRACE_LEVEL.value.longValue() >= level)
				System.out.println("TRACE: "+msg);
		} catch (Exception _e) {
			JLisp.TRACE_LEVEL.value = new Num.LNum(0);
			trace(level,msg);
		}
	}

	public static void log(int level, String msg) {
		try {
			if (JLisp.LOG_LEVEL.value.longValue() >= level)
				System.out.println("LOG: "+msg);
		} catch (Exception _e) {
			JLisp.LOG_LEVEL.value = new Num.LNum(0);
			trace(level,msg);
		}
	}

}
