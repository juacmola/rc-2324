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
		FileOutputStream fos = new FileOutputStream(file);

		while (msgIn.getLength()>=0){
			switch (msgIn.getOpcode()) {
			case PeerMessageOps.OPCODE_DOWNLOAD_OK:
      case PeerMessageOps.OPCODE_END_OF_FILE:
          byte[] data = msgIn.getFile();
          if (data != null) fos.write(data); // write data to file
          else System.err.println("File data is null.");
          
          System.out.println("El opcode que recibe el cliente es: " + msgIn.getOpcode());
          
          if (msgIn.getOpcode() == PeerMessageOps.OPCODE_END_OF_FILE) {
              System.out.println("File successfully written: " + file.getAbsolutePath());
              fos.close();
              if (msgIn.getHash().equals(FileDigest.computeFileChecksumString(file.getName()))) {
                  System.out.println("Same hash!");
              } else {
                  System.err.println("Hash does not match!");
              }
              socket.close();
              return downloaded = true;
          }
          msgIn = PeerMessage.readMessageFromInputStream(dis);
          break;

			case PeerMessageOps.OPCODE_AMBIGUOUS_HASH:
				System.err.println("Your hash is ambiguous.");
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
				System.err.println("Requested file not found on the server.");
				String hashError = msgIn.getHash();
				System.out.println(hashError);
				return downloaded = false;
			case PeerMessageOps.OPCODE_INVALID_CODE:
				System.out.println("Write a valid operation");
				return downloaded = false;
			default:
				System.err.println("Unexpected response from the server.");
				return downloaded = false;
			}
//			msgIn = PeerMessage.readMessageFromInputStream(dis);
		}
//		if (msgIn.getLength()<8192) {
//			byte[] data = msgIn.getFile();
//
//			if (data != null) fos.write(data);		//check how to write data from PeerMessage
//			System.out.println("File successfully written: " + file.getAbsolutePath());
//			fos.close();
//			if (msgIn.getHash().equals(FileDigest.computeFileChecksumString(file.getName()))) {
//				System.out.println("Same hash!");
//			} else System.err.println("Hash does not match!");
//
//		}

//		socket.close();
		fos.close();
		downloaded = true;
		return downloaded;
	}



	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
