package com.gabrielsson;

public abstract class Num extends Expr {

	static public class LNum extends Num {
		long val;
		public LNum(long val) {
			this.val = val;
		}
		public String toString() {
			return Long.toString(val);
		}
		public Object toJava() throws LispException {
			return (Object)new Long(val);
		}
		public double doubleValue() {
			return val;
		}
		public long longValue() {
			return val;
		}
		public boolean eq(Expr expr) {
			try {
				if (expr instanceof Num)
					return val == expr.longValue();
				else
					return false;
			} catch (LispException le) {
				return false;
			}
		}

		public Num add(Num b) { return b.add(val); }
		public Num add(long b) { return new Num.LNum(val + b); }
		public Num add(double b) { return new Num.DNum(val + b); }
		public Num sub(Num b) { return b.sub(val); }
		public Num sub(long b) { return new Num.LNum(b - val); }
		public Num sub(double b) { return new Num.DNum(b - val); }
		public Num mul(Num b) { return b.mul(val); }
		public Num mul(long b) { return new Num.LNum(val * b); }
		public Num mul(double b) { return new Num.DNum(val * b); }
		public Num div(Num b) { return b.div(val); }
		public Num div(long b) { return new Num.DNum(b / val); }
		public Num div(double b) { return new Num.DNum(b / val); }
	}
	
	static public class DNum extends Num {
		double val;
		public DNum(double val) {
			this.val = val;
		}
		public String toString() {
			return Double.toString(val);
		}
		public Object toJava() throws LispException {
			return (Object)new Double(val);
		}
		public double doubleValue() {
			return val;
		}
		public long longValue() {
			return (long)val;
		}
		public boolean eq(Expr expr) {
			try {
				if (expr instanceof Num)
					return val == expr.doubleValue();
				else
					return false;
			} catch (LispException le) {
				return false;
			}
		}
		public Num add(Num b) { return b.add(val); }
		public Num add(long b) { return new Num.DNum(val + b); }
		public Num add(double b) { return new Num.DNum(val + b); }
		public Num sub(Num b) { return b.sub(val); }
		public Num sub(long b) { return new Num.DNum(b - val); }
		public Num sub(double b) { return new Num.DNum(b - val); }
		public Num mul(Num b) { return b.mul(val); }
		public Num mul(long b) { return new Num.DNum(val * b); }
		public Num mul(double b) { return new Num.DNum(val * b); }
		public Num div(Num b) { return b.div(val); }
		public Num div(long b) { return new Num.DNum(b / val); }
		public Num div(double b) { return new Num.DNum(b / val); }
	}

	public abstract Num add(Num b);
	public abstract Num add(long b);
	public abstract Num add(double b);
	public abstract Num sub(Num b);
	public abstract Num sub(long b);
	public abstract Num sub(double b);
	public abstract Num mul(Num b);
	public abstract Num mul(long b);
	public abstract Num mul(double b);
	public abstract Num div(Num b);
	public abstract Num div(long b);
	public abstract Num div(double b);
	
	public Expr eval(Env env) throws LispException {
		return this;
	}
	public boolean isAtom() {
		return true;
	}
}
