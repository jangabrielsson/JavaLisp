package com.gabrielsson;

public class Const extends Expr {
	Expr val;

	Const(Expr val) {
		this.val = val;
	}

	public Expr eval(Env env) throws LispException {
		return val;
	}

	public String toString() {
		return "'" + val;
	}
}
