package stansell.erik.csc280.httpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SocketIOManager {
	public Map<String, String> readHttpRequestHeaders(InputStream in) {
		String rawHeaders = readToTerminalSymbol(in, "\r\n\r\n");
		Map<String, String> headers = new HashMap<>();
		String[] headersArray = rawHeaders.split("\r\n");
		for (String h : headersArray) {
			h = h.trim();
			String[] kvArr = h.split(": ");
			if (kvArr.length == 2) {
				headers.put(kvArr[0].trim(), kvArr[1].trim());
			} else {
				headers.put("CMD", h);
			}
		}

		return headers;
	}

	public String readToTerminalSymbol(InputStream in, String ts) {
		int character;
		String input = "";
		boolean stop = true;

		try {
				do {
					input += (char) in.read();
					if(input.length() > ts.length()) {
						if(input.substring(input.length() - ts.length()).equals(ts)) {
							stop = false;
						}
					}
				} while (stop);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}

	public String readKnownLength(InputStream in, int size) {
		String input = "";

		for (int i = 0; i > size; i++) {
			try {
				input += (char) in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return input;
	}

	public byte[] readHttpRequestBody(InputStream in) throws IOException {
		byte[] b = new byte[in.available()];
		in.read(b);

		return b;
	}

	public void writeHttpResponseHeaders(OutputStream out, Map<String, String> headers) {
		String response = headers.get("Response").toString() + "\r\n";
		headers.remove("CMD");

		for (String key : headers.keySet()) {
			response += key.toString() + ": " + headers.get(key).toString() + "\r\n";
		}
		response += "\r\n";

		try {
			out.write(response.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
}

	public void writeHttpResponseBody(OutputStream out, byte[] bodyData) {
		try {
			out.write(bodyData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
