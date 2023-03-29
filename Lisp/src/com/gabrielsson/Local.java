package com.gabrielsson;

public class Local extends Expr {
	Expr vars[];

	Expr exprs[];

	Expr body;

	int offset;

	boolean rec;

	Local(Expr vars[], Expr exprs[], Expr body[], int offset, boolean rec) {
		this.vars = vars;
		this.exprs = exprs;
		this.body = Progn.mkBody(body);
		this.offset = offset;
		this.rec = rec;
	}

	public Expr eval(Env env) throws LispException {
		Binding locals[] = env.local;
		for (int i = 0; i < vars.length; i++)
			locals[i + offset] = new Binding(null);
		for (int i = 0; i < vars.length; i++)
			locals[i + offset].val = exprs[i].eval(env);
		return body.eval(env);
	}

	public boolean isTailRec(Expr curr) {
		if (rec)
			for (int i = 0; i < vars.length; i++)
				exprs[i].isTailRec(vars[i]);
		return body.isTailRec(curr);
	}

	public String toString() {
		return "(" + ((rec) ? "FLET" : "LET") + va() + " " + body + ")";
	}

	String va() {
		if (vars.length == 0)
			return "()";
		String s = "((" + vars[0] + " " + exprs[0] + ")";
		for (int i = 1; i < vars.length; i++)
			s = s + "(" + vars[i] + " " + exprs[i] + ")";
		return s + ")";
	}
}
