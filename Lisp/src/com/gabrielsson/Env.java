package com.gabrielsson;

public class Env {
	Binding local[];

	Binding free[];

	Env(Binding local[], Binding free[]) {
		this.local = local;
		this.free = free;
	}

	Env(int n) {
		local = new Binding[n];
		for (int i = 0; i < n; i++)
			local[i] = new Binding(null);
		free = null;
	}

	Binding getBinding(int i) {
		return (i > 0) ? local[i - 1] : free[-i - 1];
	}
}
