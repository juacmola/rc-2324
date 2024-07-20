package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

/**
 * Servidor que se ejecuta en un hilo propio. Creará objetos
 * {@link NFServerThread} cada vez que se conecte un cliente.
 */
public class NFServer implements Runnable {
	private int PORT = 0;
	private ServerSocket serverSocket = null;
	private boolean stopServer = false;
//	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private static final int MAX_PORT_ATTEMPTS = 5; // Number of attempts to find an available port
	private NFServerThread st = null;

	public NFServer() throws IOException {
		Random random = new Random();
		int port = random.nextInt(10000) + 1;
		boolean portAssigned = false;
		
		/* DONE: Crear un socket servidor y ligarlo a cualquier puerto disponible*/
		for (int i = 0; i < MAX_PORT_ATTEMPTS; i++) {
			try{
				InetSocketAddress serverSocketAddress = new InetSocketAddress(port);
				serverSocket = new ServerSocket();
//				serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);	//No descomentarlo hasta probar que funciona
				serverSocket.bind(serverSocketAddress);
				portAssigned = true;
				PORT = port;
				break;
			}catch (BindException e) {
			// Puerto ocupado, prueba con el siguiente
				System.err.println("Port " + port + " is not available.");
				port = random.nextInt(10000) + 1;
			}
		}
		
		if (!portAssigned) {
    	throw new IOException("Failed to assign a port for the server after " + MAX_PORT_ATTEMPTS + " attempts.");
		}
	}

	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Socket socket = null;
		while (!stopServer) {
			/* DONE: Usar el socket servidor para esperar conexiones de otros peers que
			 * soliciten descargar ficheros*/
			try { 
				socket = serverSocket.accept();
				System.out.println("\nNew client connected to NFServer: " +
							socket.getInetAddress().toString() + ":" + socket.getPort());
			}catch (SocketTimeoutException e) {
				System.err.println("Timeout ocurrence");
			}catch (IOException e) {
				if (stopServer) System.out.println("Server stopped.");	// El servidor fue detenido intencionalmente
				else System.err.println("There was a problem: " + e.getMessage()); // Ocurrió un error inesperado
      
      break;
			}
			
			/* DONE: Crear un hilo nuevo de la clase NFServerThread, que llevará
			 * a cabo la comunicación con el cliente que se acaba de conectar, mientras este
			 * hilo vuelve a quedar a la escucha de conexiones de nuevos clientes (para
			 * soportar múltiples clientes). Si este hilo es el que se encarga de atender al
			 * cliente conectado, no podremos tener más de un cliente conectado a este
			 * servidor.*/
			if (socket != null && socket.isConnected()) {
				st = new NFServerThread(socket);
				st.start();
			}
		}

	}
	/**
	 * TODO: Añadir métodos a esta clase para: 1) Arrancar el servidor en un hilo
	 * nuevo que se ejecutará en segundo plano 2) Detener el servidor (stopserver)
	 * 3) Obtener el puerto de escucha del servidor etc.
	 */

	/** 1) Arrancar el servidor en un hilo nuevo que se ejecutará en segundo plano
	 */
	public void startBG() {
		new Thread(this).start();
	}

	/** 2) Detener el servidor (stopserver)
	 * @throws IOException 
	 */
	public void stopServer() throws IOException {
		stopServer = true;
		if (serverSocket != null && !serverSocket.isClosed()) {
      serverSocket.close();
		}
	}

	/** 3) Obtener el puerto de escucha del servidor
	 * @return puerto del servidor
	 */
	public int getServerPort() {
		return PORT;
	}	
}
