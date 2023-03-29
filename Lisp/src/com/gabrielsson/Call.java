package com.gabrielsson;

public class Call extends Expr {
	Expr fun;

	Expr[] args;

	boolean tail = false;

	Call(Expr fun, Expr[] args) {
		this.fun = fun;
		this.args = args;
	}

	public Expr eval(Env env) throws LispException {
		JLisp.trace(4,""+this);
		return (tail) ? this : fun.funBinding(env).apply(env, null, args);
	}

	public boolean isTailRec(Expr curr) {
		if (fun instanceof FreeRef)
			tail = ((FreeRef) fun).atom == curr;
		else
			tail = fun == curr;
		return tail;
	}

	public void checkTailCall(String name) {
		if (fun instanceof FreeRef)
			tail = name == ((FreeRef) fun).atom.name;
		else if (fun instanceof LocalRef)
			tail = name == ((LocalRef) fun).atom.name;
		if (fun instanceof Atom)
			tail = name == ((Atom) fun).name;
	}

	public String toString() {
		return "(" + ((tail) ? "@" : "") + fun + " " + arrToListRest(args)+ ")";
	}
}
