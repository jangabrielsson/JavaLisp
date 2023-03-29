package com.gabrielsson;

public class Setq  extends Expr {
	Expr[] exprs;

	public Setq(Expr[] exprs) {
		this.exprs = exprs;
	}
	
	public Expr eval(Env env) throws LispException {
		Expr e = JLisp.NIL;
		for(int i = 0; i < exprs.length; i += 2)
			e = exprs[i].set(env,exprs[i+1].eval(env));
		return e;
	}

	public String toString() {
		return "(SETQ " + arrToListRest(exprs);
	}
}
