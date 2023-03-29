package com.gabrielsson;

public class LocalRef extends Expr {
	Atom atom;

	int index;

	LocalRef(Atom atom, int index) {
		this.atom = atom;
		this.index = index;
	}

	public Expr eval(Env env) throws LispException {
		Expr res = env.local[index].val;
		if (res != null)
			return res;
		else
			throw new LispException.Unbound(this);
	}

	public Expr set(Env env, Expr expr) {
		return env.local[index].val = expr;
	}

	public String toString() {
		if (JLisp.verbose)
			return "<" + atom + ":L" + index + ">";
		else
			return atom.toString();
	}
}
