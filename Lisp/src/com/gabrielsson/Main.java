package com.gabrielsson;

import java.io.*;

//import com.gabrielsson.Reader.LispReaderException;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		run();
	}

	public static void tokentest() {
		try {
			FileInputStream fis = new FileInputStream("init.lsp");
			InputStreamReader isr = new InputStreamReader(fis);
			Reader.Tokenizer st = new Reader.Tokenizer(isr);

			
			if (st.nextToken() == Reader.Tokenizer.TT_EOF)
				return;
			else st.pushBack();	
			
			while(true) {
				switch (st.nextToken()) {
				case Reader.Tokenizer.TT_EOF:
					System.out.println("TT_EOF");
					return;
				case Reader.Tokenizer.TT_LONG:
					System.out.println("TT_LONG:"+st.lval);
					break;
				case Reader.Tokenizer.TT_DOUBLE:
					System.out.println("TT_DOUBLE:"+st.dval);
					break;
				case Reader.Tokenizer.TT_ATOM:
					System.out.println("TT_ATOM:"+st.sval);
					break;
				case Reader.Tokenizer.TT_STRING:
					System.out.println("TT_STRING:"+st.sval);
					break;
				case Reader.Tokenizer.TT_KEY:
					System.out.println("TT_KEY:"+st.sval);
					break;
				case Reader.Tokenizer.TT_LPAR:
					System.out.println("TT_LPAR:"+st.sval);
					break;
				case Reader.Tokenizer.TT_RPAR:
					System.out.println("TT_RPAR:"+st.sval);
					break;
				case Reader.Tokenizer.TT_UNKNOWN:
					System.out.println("TT_UNKNOWN:"+st.sval);
					break;
				case Reader.Tokenizer.TT_ERROR:
					System.out.println("TT_ERROR:"+st.sval);
					break;
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

 	public static void run() {
		try {
			//tokentest();
			JLisp jl = new JLisp();
			readFile("init.lsp");
			jl.call("toploop");
		} catch (Exception _e) {
			System.out.println(_e);
		}
 	}

	static void readFile(String name) {
		try {
			FileInputStream fis = new FileInputStream(name);
			InputStreamReader isr = new InputStreamReader(fis);
			Reader r = new Reader(isr);
			while (true) {
				Expr expr = r.parse();
				JLisp.trace(2,"Read "+expr);
				if (expr == JLisp.NIL)
					break;
				Translate.TransExpr tt = Translate.compile(expr);
				expr = tt.run();
				JLisp.trace(1,""+expr);
			}
		} catch (Exception _e) {
			System.out.println(_e);
		}
	}

}
