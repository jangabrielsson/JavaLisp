package com.gabrielsson;

public class LispException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Expr expr;

	public static class Unbound extends LispException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Unbound(Expr e) {
			super("Unbound var:" + e);
			expr = e;
		}
	}

	public static class UserThrow extends LispException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Expr tag;

		public UserThrow(Expr tag, Expr expr) {
			super("User throw:" + tag + " " + expr);
			this.tag = tag;
			this.expr = expr;
		}
	}

	public static class Error extends LispException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Error(String msg) {
			super(msg);
		}
	}

	public LispException(String msg) {
		super(msg);
	}
}
