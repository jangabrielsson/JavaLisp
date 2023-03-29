package com.gabrielsson;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

public class Reader {
	Tokenizer st;

	public static class LispReaderException extends IOException {
		/**
		 * 
		 */
		static final long serialVersionUID = 1L;

		int line;

		public LispReaderException(String s, int line) {
			super(s);
			this.line = line;
		}

		public int lineno() {
			return line;
		}
	}

	static public class  Tokenizer {
		static final int TT_EOF     = -1;
		static final int TT_LPAR    = -2;
		static final int TT_RPAR    = -3;
		static final int TT_ATOM    = -4;
		static final int TT_KEY     = -5;
		static final int TT_LONG    = -6;
		static final int TT_DOUBLE  = -7;
		static final int TT_STRING  = -8;
		static final int TT_UNKNOWN = -9;
		static final int TT_QUOTE   = -10;
		static final int TT_BACKQUOTE = -11;
		static final int TT_DOT     = -12;
		static final int TT_HASH    = -13;
		static final int TT_COMMA   = -14;
		static final int TT_AT      = -15;
		static final int TT_ERROR   = -16;

		StreamTokenizer st;

		Tokenizer(java.io.Reader r) {
			st = new StreamTokenizer(r);
			st.resetSyntax();
			st.wordChars('A', 'Z');
			st.wordChars('a', 'z');
			st.wordChars('-', '-');
			st.wordChars(':', ':');
//			st.wordChars('+', '+');
			st.wordChars('.', '.');
			st.whitespaceChars('\r', '\r');
			st.whitespaceChars('\n', '\n');
			st.whitespaceChars('\t', '\t');
			st.whitespaceChars(' ', ' ');
			st.wordChars('0','9');
			st.ordinaryChar(',');
			st.ordinaryChar('@');
			st.ordinaryChar('#');
			st.ordinaryChar('>');
			st.ordinaryChar('<');
			st.ordinaryChar('\'');
			st.ordinaryChar('/');
			st.wordChars('%', '%');
			st.wordChars(':', ':');
			st.wordChars('&', '&');
			st.wordChars('*', '*');
			st.quoteChar('\"');
			st.commentChar(';');
		}
		
		String sval;
		long lval;
		double dval;
		int lastToken;
		boolean pbFlag = false;
		
		int lineno() { return st.lineno(); }

		void pushBack() {
			pbFlag = true;
		}
		
		int nextToken() {
			if (pbFlag) {
				pbFlag = false;
				return lastToken;
			}
			try {
				int tk = st.nextToken();
				switch(tk) {
				case StreamTokenizer.TT_EOF: lastToken = TT_EOF; break;
				case '(':  sval = "(";  lastToken = TT_LPAR; break;
				case ')':  sval = ")";  lastToken = TT_RPAR; break;
				case '+':  sval = "+";  lastToken = TT_ATOM; break;
				case '/':  sval = "/";  lastToken = TT_ATOM; break;
				case '\'': sval = "\'"; lastToken = TT_QUOTE; break;
				case '`':  sval = "`";  lastToken = TT_BACKQUOTE; break;
				case '@':  sval = "@";  lastToken = TT_AT; break;
				case '#':  sval = "#";  lastToken = TT_HASH; break;
				case '-':  sval = "-";  lastToken = TT_ATOM; break;
				case '<':  sval = "<";  lastToken = TT_ATOM; break;
				case '>':  sval = ">";  lastToken = TT_ATOM; break;
				case ',':  sval = ",";  lastToken = TT_COMMA; break;
				case '\"': sval = st.sval; lastToken = TT_STRING; break;
				case StreamTokenizer.TT_WORD:
					if (st.sval.startsWith(":")) {
						sval = st.sval;
						lastToken = TT_KEY;
						break;
					} else if ((st.sval.startsWith("-") && st.sval.length() > 1 && Character.isDigit(st.sval.charAt(1))) || Character.isDigit(st.sval.charAt(0))) {
						if (st.sval.indexOf('.') >= 0) {
							try {
								dval = Double.parseDouble(st.sval);
								lastToken = TT_DOUBLE;
							} catch (Exception e) {
								sval = st.sval;
								lastToken = TT_ERROR;
							}
							break;
						} else {
							try {
								lval = Long.parseLong(st.sval);
								lastToken = TT_LONG; 
							} catch (Exception e) {
								sval = st.sval;
								lastToken = TT_ERROR; 
							}
							break;
						}
					} else {
						sval = st.sval;
						lastToken = (sval.equals(".")) ? TT_DOT : TT_ATOM;
						break;
					}
				default: sval = "C["+tk+"/"+(char)tk+"]"; lastToken = TT_UNKNOWN; break;
				}	
				return lastToken;
			} catch (Exception e) {
				lastToken = TT_EOF;
				return lastToken;
			}
		}
		
	}
	
	public Reader(String s) {
		this(new StringReader(s));
	}

	public Reader(java.io.Reader r) {
		st = new Tokenizer(r);
	}

	public Expr parse() throws LispReaderException {
		try {
			if (st.nextToken() == Tokenizer.TT_EOF)
				return JLisp.NIL;
			else st.pushBack();			
			return parse(st);
		} catch (IOException _e2) {
			throw new LispReaderException(""+_e2, st.lineno());
		} catch (LispException _e2) {
			throw new LispReaderException(""+_e2, st.lineno());
		}
	}

	Expr parse(Tokenizer st) throws LispReaderException, LispException,
			IOException {
		int tk = st.nextToken();
		switch (tk) {
		case Tokenizer.TT_EOF:
			throw new LispReaderException("Read beyond EOF", st.lineno());
		case Tokenizer.TT_LONG:
			return new Num.LNum(st.lval);
		case Tokenizer.TT_DOUBLE:
			return new Num.DNum(st.dval);
		case Tokenizer.TT_STRING:
			return new Str(st.sval);
		case Tokenizer.TT_ATOM:
			return new Atom(st.sval).intern();
		case Tokenizer.TT_QUOTE:
			return new Cons(JLisp.QUOTE, new Cons(parse(st), JLisp.NIL));
		case Tokenizer.TT_HASH:
			// Hack
			Expr e = parse(st).second();
			if (e.isAtom())
				e = new Cons(JLisp.QUOTE, e, JLisp.NIL);
			return new Cons(JLisp.FUNCTION, e, JLisp.NIL);
		case Tokenizer.TT_BACKQUOTE:
			return new Cons(new Atom("BACKQUOTE").intern(),parse(st),JLisp.NIL);
		case Tokenizer.TT_COMMA:
			int n = st.nextToken();
			if (n == Tokenizer.TT_DOT)
				return new Cons(JLisp.BACK_COMMA_DOT,parse(st),JLisp.NIL);
			else if (n == Tokenizer.TT_AT)
				return new Cons(JLisp.BACK_COMMA_AT,parse(st),JLisp.NIL);
			else {
				st.pushBack();
				return new Cons(JLisp.BACK_COMMA,parse(st),JLisp.NIL);
			}
		case Tokenizer.TT_LPAR:
			if (st.nextToken() == Tokenizer.TT_RPAR)
				return JLisp.NIL;
			else {
				st.pushBack();
				Cons l = new Cons(parse(st), JLisp.NIL);
				Cons t = l;
				while (true) {
					switch (st.nextToken()) {
					case Tokenizer.TT_RPAR:
						return l;
					case Tokenizer.TT_DOT:
						t.cdr = parse(st);
						if (st.nextToken() != Tokenizer.TT_RPAR)
							throw new LispReaderException("Missing ')'", st
									.lineno());
						return l;
					case Tokenizer.TT_EOF:
						throw new LispReaderException("Malformed list!", st
								.lineno());
					default:
						st.pushBack();
						t.cdr = new Cons(parse(st), JLisp.NIL);
						t = (Cons) t.cdr;
						break;
					}
				}
			}
		case Tokenizer.TT_UNKNOWN:
			JLisp.log(0,"Reader TT_UNKNOWN:"+tk+" "+st.sval);
			return new Atom(st.sval).intern();
		case Tokenizer.TT_ERROR:
			JLisp.log(0,"Reader TT_ERROR:"+tk+" "+st.sval);
			throw new LispReaderException("Missing ')'", st
					.lineno());
		default:
			//JLisp.trace(0,"SPEC:"+(char)tk+".");
			return new Atom(String.valueOf((char) tk)).intern();
		}
	}
}
