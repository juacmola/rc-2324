package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.Socket;

public class NFServerThread extends Thread {
	Socket socket = null;
	/*
	 * DONE: Esta clase modela los hilos que son creados desde NFServer y cada uno
	 * de los cuales simplemente se encarga de invocar a
	 * NFServerComm.serveFilesToClient con el socket retornado por el método accept
	 * (un socket distinto para "conversar" con un cliente)
	 */

	public NFServerThread(Socket s) {
		this.socket = s;
	}

	@Override
	public void run() {
			try {
				NFServerComm.serveFilesToClient(socket);
				System.out.println("Disconnected client " + socket.getInetAddress().toString() + ":" + socket.getPort());
			} catch (IOException e) {e.printStackTrace();}
	}

}
