package com.gabrielsson;

public class Defun extends Expr {
	Atom name;

	boolean isMacro;

	Lambda lambda;

	Defun(Atom name, Lambda lambda, boolean isMacro) {
		this.name = name;
		this.lambda = lambda;
		this.isMacro = isMacro;
	}

	public Expr eval(Env env) throws LispException {
		name.funset(env, lambda.eval(env));
		lambda.macro = isMacro;
		return name;
	}

	public boolean isTailRec(Expr curr) {
		return lambda.isTailRec(name);
	}

	public String toString() {
		return "(DEFUN " + name + " " + lambda;
	}
}
