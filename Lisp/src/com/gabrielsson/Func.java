package com.gabrielsson;

public interface Func {
	public Expr apply(Env env, Binding free[], Expr args[]) throws LispException;
	public Expr apply2(Env env, Binding free[], Expr args[]) throws LispException;
	public boolean isMacro();
}
