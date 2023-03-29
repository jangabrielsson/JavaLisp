package com.gabrielsson;

import java.util.Vector;
import java.util.Stack;

class Translate {

	static class TransException extends LispException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		TransException(String msg, Expr e) {
			super("Translate:" + msg + ":" + e);
		}
	}

	static class CEnv {
		Stack<Atom> locals = new Stack<Atom>();

		Vector<Atom> frees = new Vector<Atom>();

		CEnv outer;

		int nLocals = 0;

		int maxLocals = 0;

		CEnv(CEnv outer) {
			this.outer = outer;
		}

		void pushLocal(Atom var) {
			locals.push(var);
			nLocals++;
			maxLocals = Math.max(nLocals, maxLocals);
		}

		int pushLocals(Atom vars[]) {
			for (Atom a : vars)
				pushLocal(a);
			return vars.length;
		}

		void popLocals(int n) {
			locals.setSize(locals.size() - n);
			nLocals -= n;
		}

		Atom getVar(int i) {
			return (i > 0) ? (Atom) locals.elementAt(i - 1) : (Atom) frees
					.elementAt(-i - 1);
		}

		int[] freeMap() {
			int fm[] = new int[frees.size()];
			for (int i = 0; i < fm.length; i++)
				fm[i] = outer.bindingIndex1((Atom) frees.elementAt(i));
			return fm;
		}

		int bindingIndex1(Atom var) {
			int i = locals.search(var);
			if (i >= 0)
				return locals.size() - i + 1;
			i = frees.indexOf(var);
			return (i >= 0) ? -(i + 1) : 0;
		}

		int bindingIndex(Atom var) {
			int i = bindingIndex1(var);
			if (i != 0)
				return i;
			i = (outer == null) ? 0 : outer.bindingIndex(var);
			if (i == 0)
				return 0;
			frees.addElement(outer.getVar(i));
			return -frees.size();
		}
	}

	static class TransExpr {
		Expr expr;

		int nLocals;

		TransExpr(Expr expr, int nLocals) {
			this.expr = expr;
			this.nLocals = nLocals;
		}

		Expr run() throws LispException {
			return expr.eval(new Env(nLocals));
		}

		public String toString() {
			return "" + nLocals + ":" + expr;
		}
	}

	static TransExpr compile(Expr e) throws LispException {
		try {
			CEnv env = new CEnv(null);
			e = compile(e, env);
			e.isTailRec(null);
			return new TransExpr(e, env.maxLocals);
		} catch (TransException _e1) {
			throw _e1;
		} catch (Exception _e2) {
			if (JLisp.LOG_LEVEL.value.longValue() >= 1)
				_e2.printStackTrace();
			throw new TransException("Bad expr", e);
		}
	}

	static Expr compile(Expr e, CEnv env) throws Exception {
		// List
		if (e instanceof Cons) {
			Expr car = e.first();
			Expr cdr = e.rest();
			if (car instanceof Atom) {
				Atom a = (Atom) car;

				// Quote: i.e. '(1 2 3)
				if (a == JLisp.QUOTE)
					return new Const(cdr.first());

				// Lambda: (lambda vars . body)
				else if (a == JLisp.LAMBDA || a == JLisp.LAMBDA2)
					return compLambda(env, cdr.first(), listToArr(cdr.rest()));

				// Flet: (flet ((v1 e1) ...) . body)
				else if (a == JLisp.FLET)
					return compLocal(env, cdr.first(), listToArr(cdr.rest()),
							true);

				// Let: (let ((v1 e1) ...) . body)
				else if (a == JLisp.LET)
					return compLocal(env, cdr.first(), listToArr(cdr.rest()),
							false);

				// If: (if test then [else])
				else if (a == JLisp.IF)
					return compIfThenElse(env, cdr.first(), cdr.second(), cdr
							.rest().rest());

				// Catch: (catch tag . body)
				else if (a == JLisp.CATCH)
					return compCatch(env, cdr.first(), listToArr(cdr.rest()));

				// Progn: (progn . body)
				else if (a == JLisp.PROGN)
					return compProgn(env, listToArr(cdr));

				else if (a == JLisp.SETQ) {
					Expr r[] = listToArr(cdr);
					if (r.length % 2 != 0)
						throw new TransException(
								"Wrong number of args to SETQ:", e);
					for (int i = 0; i < r.length; i++)
						r[i] = compile(r[i], env);
					return new Setq(r);
				}

				else if (a == JLisp.WHILE) {
					Expr test = cdr.first();
					Expr r[] = listToArr(cdr.rest());
					return new While(compile(test, env), compProgn(env, r));
				}
				// Defun: (defun name vars . body)
				else if (a == JLisp.DEFUN || a == JLisp.DEFMACRO)
					return compDefun(env, (Atom) cdr.first(), cdr.second(),
							listToArr(cdr.rest().rest()), a == JLisp.DEFMACRO);
			}

			// Call: i.e. (fun . args)
			Expr r[] = listToArr(cdr);
			Expr f = compile(car, env);
			if (f.isAtom() && ((Atom) f).isMacro()) {
				for (int i = 0; i < r.length; i++)
					r[i] = new Const(r[i]);
				Expr exp1 = new Call(f, r);
				Expr exp2 = new TransExpr(exp1, 0).run();
				JLisp.trace(2,"MacroExpand "+exp1+" => "+exp2);
				return compile(exp2, env);
			} else {
				for (int i = 0; i < r.length; i++)
					r[i] = compile(r[i], env);
				return new Call(f, r);
			}
		}

		// Variable
		else if (e instanceof Atom) {
			int i = env.bindingIndex((Atom) e);
			if (i == 0)
				return e; // Global
			else if (i > 0)
				return new LocalRef((Atom) e, i - 1);// Local
			else
				return new FreeRef((Atom) e, -i - 1); // Free
		}

		// Number...
		else
			return e;
	}

	static Lambda.ParamExpr parseOpt(Expr e) throws LispException{
		Expr dflt = JLisp.NIL;
		Atom param = null;
		Atom inited = null;
		try {
			if (e.isCons()) {
				param = (Atom)e.first();
				Cons c = (Cons)e.rest();
				dflt = c.first();
				if (c.rest().isCons())
					inited = (Atom)c.second();
			} else param = (Atom)e;
			return new Lambda.ParamExpr(param,dflt,inited);
		} catch (Exception _e) {
			throw new LispException("Bad parameter:"+e);
		}
	}
	
	static Lambda compLambda(CEnv env, Expr prms, Expr body[]) throws Exception {
		int pState = 0;
		Atom restVar = null;
		Vector<Atom> pReg = new Vector<Atom>();
		Vector<Lambda.ParamExpr> pOpt = new Vector<Lambda.ParamExpr>();
//		Vector<Lambda.ParamExpr> pKey = new Vector<Lambda.ParamExpr>();

		for (; prms != JLisp.NIL; prms = prms.rest()) {
			if (!prms.isCons())
				break;
			Expr p = prms.first();

			if (p.eq(JLisp.OPTIONAL)) {
				if (pState >= 0x01)
					throw new LispException("Illegal &optional parameter");				
				pState += 1;
				continue;
			} else if (p.eq(JLisp.REST)) {
				if (pState >= 0x02)
					throw new LispException("Illegal &rest parameter");
				pState += 2;
				prms = prms.rest();
				restVar = (Atom) prms.first();
				continue;
			} else if (p.eq(JLisp.KEY)) {
				if (pState >= 0x04)
					throw new LispException("Illegal &key parameter");
				pState += 4;
				continue;
			}

			switch (pState) {
			case 0:                // Regular args
				if (p.isAtom())
					pReg.addElement((Atom) prms.first());
				break;
			case 1:                // Optional args
				pOpt.addElement(parseOpt(prms.first()));
				break;
			case 2:                // Rest args
				throw new LispException("Illegal params after &rest");
			case 4:                // Key args
				break;
			}

		}
		CEnv nEnv = new CEnv(env);

		Atom pRegs[] = new Atom[pReg.size()];
		pReg.copyInto(pRegs);
		nEnv.pushLocals(pRegs);
		
		Lambda.ParamExpr pOpts[] = new Lambda.ParamExpr[pOpt.size()];
		pOpt.copyInto(pOpts);
		for (Lambda.ParamExpr a : pOpts)
			nEnv.pushLocal(a.param);
		
		if (restVar != null)
			nEnv.pushLocal(restVar);
		
		// Compile optional params default values.
		for (Lambda.ParamExpr a : pOpts)
				a.dflt = compile(a.dflt,nEnv);
	
		// Get body, i.e vector of Exprs, while computing free vars.
		for (int i = 0; i < body.length; i++)
			body[i] = compile(body[i], nEnv);
		
		return new Lambda(new Lambda.ParamBlock(pRegs,pOpts,restVar,null), body, nEnv.maxLocals, nEnv.freeMap());
	}

	/**
	 * local: (let ((v1 e1) ...) . body) (flet ((v1 e1) ...) . body)
	 */
	static Local compLocal(CEnv env, Expr args, Expr body[], boolean rec)
			throws Exception {
		Atom vars[] = new Atom[length(args)];
		Expr exprs[] = new Expr[vars.length];
		int offset = env.locals.size();
		// Get vars and initial vals
		for (int i = 0; args != JLisp.NIL; args = args.rest(), i++) {
			Expr vl = args.first();
			vars[i] = (Atom) vl.first();
			exprs[i] = vl.second();
		}
		int l = 0;
		if (rec)
			l = env.pushLocals(vars);
		for (int i = 0; i < exprs.length; i++)
			exprs[i] = compile(exprs[i], env);
		if (!rec)
			l = env.pushLocals(vars);
		for (int i = 0; i < body.length; i++)
			body[i] = compile(body[i], env);
		env.popLocals(l);
		return new Local(vars, exprs, body, offset, rec);
	}

	/**
	 * if: (if test then [else])
	 */
	static IfThenElse compIfThenElse(CEnv env, Expr test, Expr eT, Expr eE)
			throws Exception {
		if (eE != JLisp.NIL)
			eE = eE.first();
		return new IfThenElse(compile(test, env), compile(eT, env), compile(eE,
				env));
	}

	/**
	 * Catch: (catch tag . body)
	 */
	static Catch compCatch(CEnv env, Expr tag, Expr body[]) throws Exception {
		for (int i = 0; i < body.length; i++)
			body[i] = compile(body[i], env);
		return new Catch(compile(tag, env), body);
	}

	/**
	 * Progn: (progn . body) =>
	 */
	static Expr compProgn(CEnv env, Expr body[]) throws Exception {
		for (int i = 0; i < body.length; i++)
			body[i] = compile(body[i], env);
		return Progn.mkBody(body);
	}

	/**
	 * Defun: (defun foo(x) . body) =>
	 */
	static Expr compDefun(CEnv env, Atom name, Expr params, Expr body[],
			boolean isMacro) throws Exception {
		Lambda lambda = compLambda(env, params, body);
		return new Defun(name, lambda, isMacro);
	}

	static Expr[] listToArr(Expr e) {
		Expr r[] = new Expr[length(e)];
		for (int i = 0; e != JLisp.NIL; e = ((Cons) e).cdr, i++)
			r[i] = ((Cons) e).car;
		return r;
	}

	static int length(Expr e) {
		int i = 0;
		for (; e != JLisp.NIL; e = ((Cons) e).cdr)
			i++;
		return i;
	}

}
