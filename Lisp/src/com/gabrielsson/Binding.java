package com.gabrielsson;

public class Binding {
	Expr val = null;

	Binding(Expr val) {
		this.val = val;
	}

	public String toString() {
		return (val == null) ? "<unbound>" : val.toString();
	}
}
