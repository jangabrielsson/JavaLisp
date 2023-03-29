package com.gabrielsson;

public class Cons extends Expr {
	public Expr car, cdr;

	public Cons(Expr car, Expr cdr) {
		this.car = car;
		this.cdr = cdr;
	}

	public boolean isCons() {
		return true;
	}

	public Expr first() {
		return car;
	}

	public Expr rest() {
		return cdr;
	}

	public Expr second() {
		return ((Cons) cdr).car;
	}

	public Expr third() {
		return ((Cons) ((Cons) cdr).cdr).car;
	}

	public Cons(Expr e) {
		this(e, JLisp.NIL);
	}

	public Cons(Expr a, Expr b, Expr c) {
		this(a, new Cons(b, c));
	}

	public Cons(Expr a, Expr b, Expr c, Expr d) {
		this(a, new Cons(b, new Cons(c, d)));
	}

	String stringRest(String pad) {
		return pad
				+ car
				+ ((cdr instanceof Cons) ? ((Cons) cdr).stringRest(" ")
						: ((cdr == JLisp.NIL) ? "" : " . " + cdr));
	}

	public String toString() {
		return "(" + stringRest("") + ")";
	}
}
