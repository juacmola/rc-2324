package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) throws IOException {
		/* DONE: Crear dis/dos a partir del socket */
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		PeerMessage readMessage;
		PeerMessage writeMessage;
		String path = "";
		String nombreArchivo = "peermsg.bin";
		String targethash = "";
		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));
		
		/* TODO: Mientras el cliente esté conectado, leer mensajes de socket,
		 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
		 * mensaje recibido, enviando los correspondientes mensajes de respuesta.*/
		while (socket.isConnected()) {
			readMessage = PeerMessage.readMessageFromInputStream(dis);
			
			switch(readMessage.getOpcode()) {
				case PeerMessageOps.OPCODE_DOWNLOAD_FROM: {
					/* TODO: Para servir un fichero, hay que localizarlo a partir de su hash (o
					 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
					 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
					 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
					 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
					 * devuelve la ruta al fichero a partir de su hash completo. */
					targethash = readMessage.getHash();
					
					FileInfo[] files = NanoFiles.db.getFiles();
					FileInfo[] filesMiniHash = FileInfo.lookupHashSubstring(files, targethash);
					
					int numFiles=0;
					for (FileInfo file : filesMiniHash) {
						path = NanoFiles.db.lookupFilePath(file.fileHash);
						numFiles++;
					}
					if (numFiles>1) {
						writeMessage = new PeerMessage(PeerMessageOps.OPCODE_AMBIGUOUS_HASH);
						System.err.println("Client sent ambiguous hash");
					}
					
					try {
						File f = new File(path);
						DataInputStream fis = new DataInputStream(new FileInputStream(f)); //TODO: Server could not find the file
						int fileLength = (int) f.length();
						byte data[] = new byte[(int) fileLength];
						fis.readFully(data);
						
						writeMessage = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_OK);
						writeMessage.setLength(fileLength);
						writeMessage.setHash(targethash);
						writeMessage.setFile(data);
						writeMessage.writeMessageToOutputStream(dos);
						
						fis.close();
						System.out.println("The download of the file was successful");
						
					}catch (FileNotFoundException e) {
						writeMessage = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
						writeMessage.setHash(targethash);
						writeMessage.writeMessageToOutputStream(dos);
						System.err.println("Server could not find the file");
			      e.printStackTrace();
					}
					catch (IOException e) {
			      e.printStackTrace();
					}
				}
				
			}
		}

	//	if(socket.isConnected()) {
	//	int dataFromClient = dis.readInt();				// Pruebas<
	//	dos.writeInt(dataFromClient);
	//}

	}




}
