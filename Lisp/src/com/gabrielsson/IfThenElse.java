package com.gabrielsson;

public class IfThenElse extends Expr { 
	Expr eTest;

	Expr eThen;

	Expr eElse;

	IfThenElse(Expr eTest, Expr eThen, Expr eElse) {
		this.eTest = eTest;
		this.eThen = eThen;
		this.eElse = eElse;
	}

	public Expr eval(Env env) throws LispException {
		if (eTest.eval(env) != JLisp.NIL)
			return eThen.eval(env);
		else
			return eElse.eval(env);
	}

	public boolean isTailRec(Expr curr) {
		boolean b1 = eThen.isTailRec(curr);
		boolean b2 = eElse.isTailRec(curr);
		return b1 || b2;
	}

	public String toString() {
		return "(IF " + eTest + " " + eThen + " " + eElse + ")";
	}
}
