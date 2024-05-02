package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.udp.server.NFDirectoryServer;
import es.um.redes.nanoFiles.util.FileDigest;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataOutputStream dos;
	private DataInputStream dis;

	public NFConnector(InetSocketAddress fserverAddr) throws IOException {
		serverAddr = fserverAddr;
		/* DONE: Se crea el socket a partir de la dirección del servidor (IP, puerto). La
		 * creación exitosa del socket significa que la conexión TCP ha sido
		 * establecida.*/
		this.socket = new Socket(fserverAddr.getAddress(), fserverAddr.getPort());
		
		/* DONE: Se crean los DataInputStream/DataOutputStream a partir de los streams de
		 * entrada/salida del socket creado. Se usarán para enviar (dos) y recibir (dis)
		 * datos del servidor.*/
		
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());

	}

	/**
	 * Método para descargar un fichero a través del socket mediante el que estamos
	 * conectados con un peer servidor.
	 * 
	 * @param targetFileHashSubstr Subcadena del hash del fichero a descargar
	 * @param file                 El objeto File que referencia el nuevo fichero
	 *                             creado en el cual se escribirán los datos
	 *                             descargados del servidor
	 * @return Verdadero si la descarga se completa con éxito, falso en caso
	 *         contrario.
	 * @throws IOException Si se produce algún error al leer/escribir del socket.
	 */
	public boolean downloadFile(String targetFileHashSubstr, File file) throws IOException {
		boolean downloaded = false;
		/* TODO: Construir objetos PeerMessage que modelen mensajes con los valores
		 * adecuados en sus campos (atributos), según el protocolo diseñado, y enviarlos
		 * al servidor a través del "dos" del socket mediante el método
		 * writeMessageToOutputStream.
		 */
		byte opcode = PeerMessageOps.operationToOpcode("DOWNLOAD_FROM");
		PeerMessage msgOut = new PeerMessage(opcode);
		msgOut.setHash(targetFileHashSubstr);
		msgOut.writeMessageToOutputStream(dos);
		
		
		/* TODO: Recibir mensajes del servidor a través del "dis" del socket usando
		 * PeerMessage.readMessageFromInputStream, y actuar en función del tipo de
		 * mensaje recibido, extrayendo los valores necesarios de los atributos del
		 * objeto (valores de los campos del mensaje).
		 */
		/* TODO: Para escribir datos de un fichero recibidos en un mensaje, se puede
		 * crear un FileOutputStream a partir del parámetro "file" para escribir cada
		 * fragmento recibido (array de bytes) en el fichero mediante el método "write".
		 * Cerrar el FileOutputStream una vez se han escrito todos los fragmentos.
		 */
		/* NOTA: Hay que tener en cuenta que puede que la subcadena del hash pasada como
		 * parámetro no identifique unívocamente ningún fichero disponible en el
		 * servidor (porque no concuerde o porque haya más de un fichero coincidente con
		 * dicha subcadena)
		 */
		PeerMessage msgIn = PeerMessage.readMessageFromInputStream(dis);
		switch (msgIn.getOpcode()) {
		case PeerMessageOps.OPCODE_DOWNLOAD_OK:
			byte[] data = msgIn.getFile();
			
			if (data != null) {
			try (FileOutputStream fos = new FileOutputStream(file)){
				fos.write(data);		//check how to write data from PeerMessage
				
				System.out.println("File successfully written: " + file.getAbsolutePath());
				fos.close();
			} catch (IOException e) {
        System.err.println("Error writing file: " + e.getMessage());
        e.printStackTrace();
      }
			}else System.err.println("File data is null.");
			
			break;
			
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			// File not found on the server
			System.err.println("Requested file not found on the server.");
			String hashError = msgIn.getHash();
			System.out.println(hashError);
			return downloaded;
		case PeerMessageOps.OPCODE_INVALID_CODE:
			System.out.println("Write a valid operation");
			return downloaded;
		default:
			// Unexpected response from the server
			System.err.println("Unexpected response from the server.");
			return downloaded;
		}

		
		/*
		 * TODO: Finalmente, comprobar la integridad del fichero creado para comprobar
		 * que es idéntico al original, calculando el hash a partir de su contenido con
		 * FileDigest.computeFileChecksumString y comparándolo con el hash completo del
		 * fichero solicitado. Para ello, es necesario obtener del servidor el hash
		 * completo del fichero descargado, ya que quizás únicamente obtuvimos una
		 * subcadena del mismo como parámetro.
		 */
		if (msgOut.getOpcode() != msgIn.getOpcode()) {
			System.err.println("Opcode does not match!");
		}
		if (msgOut.getLength() != msgIn.getLength()) {
			System.err.println("Length does not match!");
		}
//		FileDigest.computeFileChecksumString(file);
		if (!msgOut.getHash().equals(msgIn.getHash())) {
			System.err.println("Hash does not match!");
		}
		
//		dos.writeInt(5);						// PRUEBA
//		int dataFromClient = dis.readInt();
		
//		if (dataFromClient==5) System.out.println("******MATCH******");
//		else System.out.println("------NOT MATCH-------");

		
		downloaded = true;
		return downloaded;
	}





	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
