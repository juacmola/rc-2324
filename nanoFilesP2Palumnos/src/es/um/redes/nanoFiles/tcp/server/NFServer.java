package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Servidor que se ejecuta en un hilo propio. Creará objetos
 * {@link NFServerThread} cada vez que se conecte un cliente.
 */
public class NFServer implements Runnable {
	private int PORT = 5000;
	private ServerSocket serverSocket = null;
	private boolean stopServer = false;
	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private NFServerThread st = null;

	public NFServer() throws IOException {
		/* DONE: Crear un socket servidor y ligarlo a cualquier puerto disponible*/
		serverSocket = new ServerSocket();
//		serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);	No descomentarlo hasta probar que funciona
		InetSocketAddress serverSocketAddress = new InetSocketAddress(PORT);
		serverSocket.bind(serverSocketAddress);
	}

	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Socket socket = null;
		while (true) {
			/* DONE: Usar el socket servidor para esperar conexiones de otros peers que
			 * soliciten descargar ficheros*/
			try { socket = serverSocket.accept();
			System.out.println("\nNew client connected to NFServer: " +
					socket.getInetAddress().toString() + ":" + socket.getPort());
			} catch (IOException e) {
				System.err.println("There was a problem");
				e.printStackTrace();
			}
			
			/* DONE: Crear un hilo nuevo de la clase NFServerThread, que llevará
			 * a cabo la comunicación con el cliente que se acaba de conectar, mientras este
			 * hilo vuelve a quedar a la escucha de conexiones de nuevos clientes (para
			 * soportar múltiples clientes). Si este hilo es el que se encarga de atender al
			 * cliente conectado, no podremos tener más de un cliente conectado a este
			 * servidor.*/
			if (socket != null && socket.isConnected()) {
				st = new NFServerThread(socket);
				startThread();
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
	public void startThread() {
		this.st.start();
	}

	/** 2) Detener el servidor (stopserver)
	 * @throws IOException 
	 */
	public void stopserver() throws IOException {
		this.serverSocket.close();
	}

	/** 3) Obtener el puerto de escucha del servidor
	 * @return puerto del servidor
	 */
	public int getServerPort() {
		return PORT;
	}	
}
