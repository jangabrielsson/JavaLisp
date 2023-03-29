package com.gabrielsson;

public class Progn extends Expr {
	Expr exprs[];

	static Expr mkBody(Expr exprs[]) {
		if (exprs.length == 0)
			return JLisp.NIL;
		else if (exprs.length == 1)
			return exprs[0];
		else
			return new Progn(exprs);
	}

	Progn(Expr exprs[]) {
		this.exprs = exprs;
	}

	public Expr eval(Env env) throws LispException {
		Expr e = JLisp.NIL;
		for (int i = 0; i < exprs.length; i++)
			e = exprs[i].eval(env);
		return e;
	}

	public boolean isTailRec(Expr curr) {
		return (exprs.length > 0) ? exprs[exprs.length - 1].isTailRec(curr)
				: false;
	}

	public String toString() {
		return "(PROGN " + Expr.arrToListRest(exprs) + ")";
	}
}
