package http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

public class Servidor {

	static Executor poolHilos;

	public static void main(String[] args) throws IOException {

		ServerSocket server = new ServerSocket(45000);

		System.out.println("Conectandose...");
		// esperando una conexion...
		while (true) {
			Socket connection = server.accept();
			RequestThread hilo = new RequestThread(connection);
			new Thread(hilo).start();

		}
	}
}
