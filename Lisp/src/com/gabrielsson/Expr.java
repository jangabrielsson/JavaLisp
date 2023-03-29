package com.gabrielsson;

public class Expr {

	public Expr eval(Env env) throws LispException {
		throw new LispException("Eval:" + this);
	}

	public Func funBinding(Env env) throws LispException {
		Expr expr = eval(env);
		try {
			return (Func) expr;
		} catch (Exception _e) {
			throw new LispException("No fun:" + this);
		}
	}

	public boolean isAtom() {
		return false;
	}

	public boolean isString() {
		return false;
	}

	public boolean isCons() {
		return false;
	}

	public boolean eq(Expr expr) {
		return expr == this;
	}

	public Object toJava() throws LispException {
		throw new LispException("Can't box:" + this);
	}

	public long longValue() throws LispException {
		throw new LispException("Not a number:" + this);
	}

	public double doubleValue() throws LispException {
		throw new LispException("Not a number:" + this);
	}

	public String stringValue() throws LispException {
		throw new LispException("No string value:" + this);
	}

	public Expr set(Env env, Expr e) throws LispException {
		throw new LispException("Trying to set:" + this);
	}

	public Expr funset(Env env, Expr e) throws LispException {
		throw new LispException("Trying to funset:" + this);
	}

	public boolean isTailRec(Expr curr) {
		return false;
	}

	public Expr first() throws LispException {
		throw new LispException("Not a cons");
	}

	public Expr rest() throws LispException {
		throw new LispException("Not a cons");
	}

	public Expr second() throws LispException {
		throw new LispException("Not a cons");
	}

	public Expr third() throws LispException {
		throw new LispException("Not a cons");
	}

	static String arrToList(Expr es[]) {
		return "(" + arrToListRest(es) + ")";
	}

	static String arrToListRest(Expr es[]) {
		if (es.length == 0)
			return "";
		String s = es[0].toString();
		for (int i = 1; i < es.length; i++)
			s = s + " " + es[i];
		return s;
	}

	public void print(boolean quote) {
		System.out.print(toString());
	}
	
	public String toString() {
		return "<Expr>";
	};
}
