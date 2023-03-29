package com.gabrielsson;

public class Catch extends Expr {
	Expr tag;

	Expr body;

	Catch(Expr tag, Expr body[]) {
		this.tag = tag;
		this.body = Progn.mkBody(body);
	}

	public Expr eval(Env env) throws LispException {
		Expr tag1 = tag.eval(env);
		try {
			return body.eval(env);
		} catch (LispException.UserThrow e) {
			if (e.tag.eq(tag1))
				return e.expr;
			else throw e;
		} catch (LispException e) {
			if (tag1 == JLisp.NIL)
				return new Str(e.toString());
			else
				throw e;
		}
	}

	public boolean isTailRec(Expr curr) {
		return body.isTailRec(curr);
	}

	public String toString() {
		return "(CATCH " + tag + " " + body + ")";
	}
}
