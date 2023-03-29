package com.gabrielsson;

public class While   extends Expr {
	Expr test;
	Expr body;

	public While(Expr test, Expr body) {
		this.test = test;
		this.body = body;
	}
	
	public Expr eval(Env env) throws LispException {
		while(test.eval(env) != JLisp.NIL)
			body.eval(env);
		return JLisp.NIL;
	}

	public String toString() {
		return "(WHILE "+test+" "+body+")";
	}
}