package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	public static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int MAX_PORT_ATTEMPTS = 10; // Number of attempts to find an available port
	private static final int PORT = 10000;
	private ServerSocket serverSocket = null;

	public NFServerSimple() throws IOException {
		int port = PORT;
		boolean portAssigned = false;

		for (int i = 0; i < MAX_PORT_ATTEMPTS; i++) {
			try {
				/* DONE: Crear una direción de socket a partir del puerto especificado */
				InetSocketAddress serverSocketAddress = new InetSocketAddress(port);
				/* DONE: Crear un socket servidor y ligarlo a la dirección de socket anterior */
				serverSocket = new ServerSocket();
				serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
				serverSocket.bind(serverSocketAddress);
				serverSocket.setReuseAddress(true);
				System.out.println("\nServer is listening on port " + port);
				portAssigned = true;
				break;
			} catch (IOException e) {
				// Puerto ocupado, prueba con el siguiente
				System.err.println("Port " + port + " is not available.");
				port++;
			}
		}

		if (!portAssigned) {
    	throw new IOException("Failed to assign a port for the server after " + MAX_PORT_ATTEMPTS + " attempts.");
		}
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional) 
	 * @throws IOException 
	 */
	public void run() throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		boolean shutDownServer = false;
		Socket socket = null;
		
		/* DONE: Comprobar que el socket servidor está creado y ligado */
		if (serverSocket != null && serverSocket.isBound()) {
			System.out.println("Server socket is running on " + serverSocket.getLocalSocketAddress());
			System.out.println("***Write the command " + STOP_SERVER_COMMAND + " to stop the server***");
		}else {
			System.err.println("Server socket is not properly running or binding");
			return;
		}
		
		/* DONE: Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros */
		while (!shutDownServer) {
			try {
				socket = serverSocket.accept();
				System.out.println("\nNew client connected: " +
						socket.getInetAddress().toString() + ":" + socket.getPort());
			} catch (SocketTimeoutException e) {
				if(input.ready() && input.readLine().equals(STOP_SERVER_COMMAND)) {
					shutDownServer = true;
					continue;
				}
			} catch (IOException e) {
				System.err.println("There was a problem");
				e.printStackTrace();
			} 
		/* DONE: Al establecerse la conexión con un peer, la comunicación con dicho
		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
		 * hay que pasarle el socket devuelto por accept */
			if (socket != null && !socket.isClosed()) {
				NFServerComm.serveFilesToClient(socket);
				System.out.println("Disconnected client " + socket.getInetAddress().toString() + ":" + socket.getPort());
			}
		}
		
//		if (socket != null) socket.close();
		serverSocket.close();
		System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");
	}
}
