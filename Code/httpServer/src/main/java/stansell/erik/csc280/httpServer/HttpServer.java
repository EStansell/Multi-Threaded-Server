package stansell.erik.csc280.httpServer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class HttpServer {

	public enum HttpMethod {
		Get, Post, Null
	}

	private ServerSocket servSock;
	private String virtualPath = "C:\\Users\\Wolftobe1\\Desktop\\Website";
	private SocketIOManager socketIO = new SocketIOManager();

	public HttpServer(int port) {
		try {
			servSock = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void run() throws IOException {
		Socket socket;

		while (true) {
			socket = servSock.accept();

			Map<String, String> headers = socketIO.readHttpRequestHeaders(socket.getInputStream());

			HttpMethod requestMethod = determineRequestType(headers);
			String CMD = headers.get("CMD");
			String[] split = CMD.split(" ");

			switch (requestMethod) {
			case Get:
				if (split[1].substring(0, 5).equals("/calc")) {
					double Answer = 0;
					
					try {
						String opperation = split[1].substring(6, split[1].indexOf("?")).toLowerCase();
						Calculations calc = new Calculations();
						String symbol = "";
						
						String sub = split[1].substring(split[1].indexOf('=')+1, split[1].indexOf('&'));
						double Opperand1 = Double.parseDouble(sub);
						sub = split[1].substring(split[1].lastIndexOf('=')+1);
						double Opperand2 = Double.parseDouble(sub);

						switch (opperation) {
						case "add":
							Answer = calc.Addition(Opperand1, Opperand2);
							symbol = "+";
							break;

						case "subtract":
							Answer = calc.Subtraction(Opperand1, Opperand2);
							symbol = "-";
							break;

						case "multiply":
							Answer = calc.Multiplication(Opperand1, Opperand2);
							symbol = "*";
							break;

						case "divide":
							Answer = calc.Division(Opperand1, Opperand2);
							symbol = "/";
							break;

						default:
							headers.put("Response", "HTTP/1.0 404 BadOperator");
							String error = "<!DOCTYPE html><html><body><h1>404 Operation Not Found</h1><p>The operation requested does not exist</p></body></html>";
							socketIO.writeHttpResponseHeaders(socket.getOutputStream(), headers);
							socketIO.writeHttpResponseBody(socket.getOutputStream(), error.getBytes());
							headers.remove("Response");
							break;
						}

						headers.put("Response", "HTTP/1.0 200 OK");
						String answer = "<!DOCTYPE html><html><body>" + "<h>" + Opperand1 + " " + symbol + " "
								+ Opperand2 + " = " + Answer + "</h1>" + "</body></html>";
						socketIO.writeHttpResponseHeaders(socket.getOutputStream(), headers);
						socketIO.writeHttpResponseBody(socket.getOutputStream(), answer.getBytes());
						headers.remove("Response");

					} catch (NumberFormatException e) {
						headers.put("Response", "HTTP/1.0 500 ServerError");
						String error = "<!DOCTYPE html><html><body><h1>500 Internal Server Error:</h1><p>While trying to read inputted data, we found that the format of the<br>data to be calculated was not correct.<br>Proper Format: (.../calc/add?operand1=2&operand2=3))</p></body></html>";
						socketIO.writeHttpResponseHeaders(socket.getOutputStream(), headers);
						socketIO.writeHttpResponseBody(socket.getOutputStream(), error.getBytes());
						headers.remove("Response");
					}
				} else {

					try (FileInputStream fis = new FileInputStream(virtualPath + "\\" + split[1].trim())) {
						byte[] content = new byte[fis.available()];
						fis.read(content);

						headers.put("Response", "HTTP/1.0 200 OK");

						socketIO.writeHttpResponseHeaders(socket.getOutputStream(), headers);
						socketIO.writeHttpResponseBody(socket.getOutputStream(), content);

						headers.remove("Response");
					} catch (FileNotFoundException e) {
						headers.put("Response", "HTTP/1.0 404");
						String notFound = "<!DOCTYPE html><html><body><h1>404 File Not Found</h1></body></html>";
						socketIO.writeHttpResponseHeaders(socket.getOutputStream(), headers);
						socketIO.writeHttpResponseBody(socket.getOutputStream(), notFound.getBytes());
						headers.remove("Response");
					}
				}

				break;
			case Post: // FILENAME
				byte[] requestBody = socketIO.readHttpRequestBody(socket.getInputStream());
				try {
					FileOutputStream fos = new FileOutputStream(virtualPath + "\\" + split[1].trim(), false);
					fos.write(requestBody);
					fos.close();
					headers.put("Response", "HTTP/1.0 200 OK");

					socketIO.writeHttpResponseHeaders(socket.getOutputStream(), headers);
				} catch (Exception e) {
					headers.put("Response", "HTTP/1.0 500 ServerError");
					socketIO.writeHttpResponseHeaders(socket.getOutputStream(), headers);
					headers.remove("Response");
				}
			case Null:
				headers.put("Response", "HTTP/1.0 500 ServerError");
				String notFound = "<!DOCTYPE html><html><body><h1>500 Internal Server Error</h1></body></html>";
				socketIO.writeHttpResponseHeaders(socket.getOutputStream(), headers);
				socketIO.writeHttpResponseBody(socket.getOutputStream(), notFound.getBytes());
				headers.remove("Response");
				break;
			}
			headers.clear();
			socket.close();
		}
	}

	private HttpMethod determineRequestType(Map<String, String> headers) {
		String CMD = headers.get("CMD");
		String[] split = CMD.split(" ");

		// expand as need be.
		if (split[0].equals(HttpMethod.Get.name().toUpperCase())) {
			return HttpMethod.Get;
		} else if (split[0].equals(HttpMethod.Post.name().toUpperCase())) {
			return HttpMethod.Post;
		}
		return HttpMethod.Null;
	}
}
