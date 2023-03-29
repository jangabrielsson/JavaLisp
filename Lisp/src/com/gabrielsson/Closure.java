package com.gabrielsson;

public class Closure extends Expr implements Func {
	Binding free[];

	Lambda lambda;

	Closure(Lambda lambda, Binding free[]) {
		this.free = free;
		this.lambda = lambda;
	}

	public Expr eval(Env env) throws LispException {
		return this;
	}

	public Expr apply(Env env, Binding free[], Expr args[]) throws LispException {
		return lambda.apply(env, this.free, args);
	}

	public Expr apply2(Env env, Binding free[], Expr args[]) throws LispException {
		return lambda.apply2(env, this.free, args);
	}

	public boolean isMacro() {
		return lambda.isMacro();
	}
	
	public String toString() {
		if (JLisp.verbose)
			return "#'(LAMBDA " + bnds() + lambda.pb + " "
					+ lambda.body + ")";
		else
			return "#'" + lambda;
	}

	String bnds() {
		if (free.length == 0)
			return "[]";
		String s = "[" + cfs(free[0]);
		for (int i = 1; i < free.length; i++)
			s = s + "," + cfs(free[i]);
		return s + "]";
	}

	String cfs(Binding b) {
		return (b.val == this) ? "<$>" : b.toString();
	}
}
