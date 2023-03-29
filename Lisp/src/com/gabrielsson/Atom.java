package com.gabrielsson;

public class Atom extends Expr {
	String name;

	Expr value = null;
	
	Func fun = null;

	Atom(String name) {
		this.name = name.toUpperCase();
	}

	public Expr eval(Env env) throws LispException {
		if (value == null)
			throw new LispException.Unbound(this);
		return value;
	}

	public boolean isAtom() {
		return true;
	}
	
	public boolean isMacro() {
		return fun != null && fun.isMacro();
	}

	public String stringValue() throws LispException {
		return name;
	}

	public Object toJava() throws LispException {
		return name;
	}

	public Expr set(Env env, Expr expr) {
		return value = expr;
	}

	public Expr funset(Env env, Expr expr) {
		fun = (Func) expr;
		return expr;
	}

	public Func funBinding(Env env) throws LispException {
		if (fun != null)
			return fun;
		else
			throw new LispException("Undefined fun:" + this);
	}

	Atom intern() {
		Object o = JLisp.symbols.get(name);
		if (o != null)
			return (Atom) o;
		else {
			JLisp.symbols.put(name, this);
			return this;
		}
	}

	public String toString() {
		return name;
	}
}
