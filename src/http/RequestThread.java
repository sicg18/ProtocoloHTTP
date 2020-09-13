package http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RequestThread implements Runnable {

	private Socket connection;
	private boolean existe;
	byte[] bloque = null;
	int BLOCK_SIZE = 10;
	int bytesRead = 0;

	public RequestThread(Socket connection) {
		this.connection = connection;
	}

	public Path obtenerRuta(String path) {
		if ("/".equals(path)) {
			path = "\\index.html";
		}
		return Paths.get("..\\Protocolo\\recursos", path);
	}

	public String leerContenido(Path filePath) throws IOException {
		return Files.probeContentType(filePath);
	}

	@Override
	public void run() {
		try {
			PrintStream salida = new PrintStream(connection.getOutputStream());
			BufferedReader entrada = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			FileInputStream content = null;
			String comando = entrada.readLine().toLowerCase();
			String[] campos = comando.split(" ");
			Path rutaArchivo = obtenerRuta(campos[1]);
			if (Files.exists(rutaArchivo)) {
				String contentType = leerContenido(rutaArchivo);
				bloque = new byte[BLOCK_SIZE];
				existe = true;
				content = new FileInputStream(rutaArchivo.toString());
				enviarRespuesta(salida, connection, "200 OK", contentType, Files.readAllBytes(rutaArchivo), true,
						bloque, content);
			} else {
				byte[] contenido = "<h1>No se encuentra :(</h1>".getBytes();
				existe = false;
				bloque = new byte[BLOCK_SIZE];
				enviarRespuesta(salida, connection, "404 Not Found", "text/html", contenido, false, bloque, content);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void enviarRespuesta(PrintStream clientOutput, Socket cliente, String estado, String contentType,
			byte[] contenido, boolean existencia, byte[] bloque, FileInputStream content) throws IOException {
		clientOutput.write(("HTTP/1.1 \r\n" + estado).getBytes());
		System.out.println("HTTP/1.1 " + estado);
		clientOutput.write(("Content-Length: " + contenido.length + "\r\n").getBytes());
		System.out.println("Content-Length: " + contenido.length);
		clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
		System.out.println("ContentType: " + contentType);
		clientOutput.write("\r\n".getBytes());
		System.out.println("");
		if (existe) {
			while ((bytesRead = content.read(bloque)) == BLOCK_SIZE) {
				clientOutput.write(bloque, 0, bytesRead);
				System.out.println(new String(bloque));
			}
		} else {
			clientOutput.write(bloque);
		}

		clientOutput.write("\r\n\r\n".getBytes());
		System.out.println("\r\n\r\n");

		clientOutput.flush();
		cliente.close();
	}
}
