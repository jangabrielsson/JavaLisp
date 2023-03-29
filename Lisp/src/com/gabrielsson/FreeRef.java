package com.gabrielsson;

public class FreeRef extends Expr {
	Atom atom;

	int index;

	FreeRef(Atom atom, int index) {
		this.atom = atom;
		this.index = index;
	}

	public Expr eval(Env env) throws LispException {
		Expr res = env.free[index].val;
		if (res != null)
			return res;
		else
			throw new LispException.Unbound(this);
	}

	public Expr set(Env env, Expr expr) {
		return env.free[index].val = expr;
	}

	public String toString() {
		if (JLisp.verbose)
			return "<" + atom + ":F" + index + ">";
		else
			return atom.toString();
	}
}
