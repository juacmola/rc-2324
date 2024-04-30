package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
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
		
		/* TODO: Mientras el cliente esté conectado, leer mensajes de socket,
		 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
		 * mensaje recibido, enviando los correspondientes mensajes de respuesta.*/
//		if(socket.isConnected()) {
//			int dataFromClient = dis.readInt();				// Pruebas<
//		dos.writeInt(dataFromClient);
//		}
		
		while (socket.isConnected()) {
			readMessage = PeerMessage.readMessageFromInputStream(dis);
			
			switch(readMessage.getOpcode()) {
				case PeerMessageOps.OPCODE_DOWNLOAD_FROM: {
					FileInfo[] files = NanoFiles.db.getFiles();
					for (FileInfo file : files) {
						if (file.fileHash.equals(readMessage.getHash())) path = file.filePath;		//Si coinciden -> Nos quedamos su ruta 
					}
//					File f = new File("file.tgz");
//					if (!f.exists()) {
//						f.createNewFile();
//						FileOutputStream fos = new FileOutputStream(f);
//						fos.write(fichero);
//						fos.close();
//					}
				}
				
			}
		}
		/* TODO: Para servir un fichero, hay que localizarlo a partir de su hash (o
		 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
		 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
		 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
		 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
		 * devuelve la ruta al fichero a partir de su hash completo. */



	}




}
