package com.gabrielsson;

public class Str extends Expr {
	String str;

	Str(String str) {
		this.str = str;
	}

	public Expr eval(Env env) throws LispException {
		return this;
	}

	public String stringValue() throws LispException {
		return str;
	}

	public boolean isString() {
		return true;
	}
	
	public Object toJava() throws LispException {
		return str;
	}

	public void print(boolean quote) {
		System.out.print(str);
	}

	public String toString() {
		return "\"" + str + "\"";
	}
}
