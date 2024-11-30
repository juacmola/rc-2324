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
import java.util.ArrayList;
import java.util.Arrays;

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
		PeerMessage writeMessage = new PeerMessage();
		String path = "";
		String targethash = "";
		long fileFragments;
		int fragmentSize = 8192;
		
		/* TODO: Mientras el cliente esté conectado, leer mensajes de socket,
		 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
		 * mensaje recibido, enviando los correspondientes mensajes de respuesta.*/
		//quizas sea un while
		if (socket.isConnected()) {
				readMessage = PeerMessage.readMessageFromInputStream(dis);

				switch(readMessage.getOpcode()) {
					case PeerMessageOps.OPCODE_DOWNLOAD_FROM:
						/* TODO: Para servir un fichero, hay que localizarlo a partir de su hash (o
						 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
						 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
						 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
						 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
						 * devuelve la ruta al fichero a partir de su hash completo. */
						targethash = readMessage.getHash();
						String hashToClient = "";
						FileInfo[] files = NanoFiles.db.getFiles();
						FileInfo[] filesMiniHash = FileInfo.lookupHashSubstring(files, targethash);
						
						switch(filesMiniHash.length) {
							case 0:
								writeMessage.setOpcode(PeerMessageOps.OPCODE_DOWNLOAD_FROM_FAIL);
								break;
							
							case 1: 
								writeMessage.setOpcode(PeerMessageOps.OPCODE_DOWNLOAD_FROM_RESP_HS);
								fileFragments = (filesMiniHash[0].fileSize + fragmentSize - 1) / fragmentSize;
								hashToClient = filesMiniHash[0].fileHash;
								writeMessage.setFileFragments(fileFragments);
								writeMessage.setHash(hashToClient);
								writeMessage.writeMessageToOutputStream(dos);
								
								path = NanoFiles.db.lookupFilePath(hashToClient);
								try (DataInputStream fis = new DataInputStream(new FileInputStream(path))){
									byte[] data = new byte[fragmentSize];
									int nBytes;
									
									while((nBytes = fis.read(data)) != -1) {
										byte[] actualData = Arrays.copyOf(data, nBytes);
										writeMessage.setOpcode(PeerMessageOps.OPCODE_DOWNLOAD_FROM_RESP);
										writeMessage.setDataFile(actualData);
										writeMessage.writeMessageToOutputStream(dos);
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
								break;
							
							default: 
								ArrayList<String> fileNames = new ArrayList<>();
								ArrayList<String> hashes = new ArrayList<>();
								
								for (FileInfo file : filesMiniHash) {
									hashes.add(file.getHash());
									fileNames.add(file.fileName);
								}
								writeMessage.setOpcode(PeerMessageOps.OPCODE_DOWNLOAD_FROM_WHICH);
								writeMessage.setHashes(hashes);
								writeMessage.setFileNames(fileNames);
								writeMessage.writeMessageToOutputStream(dos);
				
						}
						break;
						
					case PeerMessageOps.OPCODE_INVALID_OPCODE:
						System.err.println("Invalid operation code.");
						break;
						
				}
	}
	
	socket.close();		
	
	}

}
