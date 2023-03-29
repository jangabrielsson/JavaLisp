package com.gabrielsson;

import java.lang.reflect.Method;

/**
 * @author jangabrielsson
 * 
 */
public class JObject extends Expr implements Func {
	Object object;

	Class _class;

	JObject(Object object) {
		this.object = object;
		_class = object.getClass();
	}

	JObject(Expr args[]) throws LispException {
		try {
			_class = Class.forName(args[0].stringValue());
			object = _class.newInstance();
		} catch (Exception e) {
			throw new LispException(e.toString());
		}
	}

	public Expr eval(Env env) throws LispException {
		return this;
	}

	public boolean isMacro() {
		return false;
	}

	/*
	 * (obj <string> arg0 ... arg1)
	 */
	public Expr apply(Env env, Binding free[], Expr params[])
			throws LispException {
		Expr args[] = new Expr[params.length];
		for (int i = 0; i < params.length; i++)
			args[i] = params[i].eval(env);
		return apply2(env, free, params);
	}

	public Expr apply2(Env env, Binding free[], Expr args[])
			throws LispException {
		String name = args[0].stringValue();
		Class mp[] = new Class[args.length - 1];
		Object pa[] = new Object[mp.length];
		try {
			for (int i = 1; i < args.length; i++) {
				Expr v = args[i];
				pa[i - 1] = v.toJava();
				mp[i - 1] = Class.forName("java.lang.Object"); // pa[i -
																// 1].getClass();
			}
			Method m;
			m = _class.getMethod(name, mp);
			return toLisp(m.invoke(object, pa));
		} catch (Exception e) {
			throw new LispException("" + e);
		}
	}

	public Object toJava() throws LispException {
		return object;
	}

	Expr toLisp(Object o) {
		if (o instanceof java.lang.String)
			return new Str((String) o);
		else if (o instanceof java.lang.Number)
			return new Num.DNum(((java.lang.Number) o).doubleValue());
		else if (o instanceof Expr)
			return (Expr) o;
		else
			return new JObject(o);
	}

	public String toString() {
		return "<" + _class.getCanonicalName() + ":" + object + ">";
	}
}
