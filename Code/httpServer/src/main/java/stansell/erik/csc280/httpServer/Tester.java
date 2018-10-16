package stansell.erik.csc280.httpServer;

import java.io.IOException;

public class Tester {
	public static void main(String args[]) throws IOException {	
		
		HttpServer hs = new HttpServer(3005);
		hs.run();
	}
}
