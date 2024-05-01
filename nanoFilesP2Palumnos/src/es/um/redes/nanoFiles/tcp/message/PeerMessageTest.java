package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
//		String nombreArchivo = "./nf-shared/peermsg.bin";
//		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));
		try {
			File f = new File("./nf-shared/prueba.txt");
			DataInputStream dis = new DataInputStream(new FileInputStream(f));
			long filelength = f.length();
			byte data[] = new byte[(int) filelength];
			dis.readFully(data);
			dis.close();
		
			FileInfo arrayfileinfo[] = NanoFiles.db.getFiles();
			
		// Create a DataOutputStream to write to a file
      String outputFile = "./nf-shared/output.txt";
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile));
      dos.write(data); // Write the file content to the output file
      dos.close();

			
			System.out.println("File content:");
			System.out.println(new String(data)); // Print the file content
		}catch (FileNotFoundException e) {
			System.err.println("File not found: prueba.txt");
      e.printStackTrace();
		}
		catch (IOException e) {
      e.printStackTrace();
		}
		
		
	
		/*
		 * TODO: Probar a crear diferentes tipos de mensajes (con los opcodes válidos
		 * definidos en PeerMessageOps), estableciendo los atributos adecuados a cada
		 * tipo de mensaje. Luego, escribir el mensaje a un fichero con
		 * writeMessageToOutputStream para comprobar que readMessageFromInputStream
		 * construye un mensaje idéntico al original.
		 */
		/*
		byte opcode = PeerMessageOps.operationToOpcode("DOWNLOAD_FROM");
		int length = 2;
		byte[] value = {9, 8};
		PeerMessage msgOut = new PeerMessage(opcode);
		msgOut.setHash("123456");
		msgOut.writeMessageToOutputStream(fos);

		DataInputStream fis = new DataInputStream(new FileInputStream(nombreArchivo));
		PeerMessage msgIn = PeerMessage.readMessageFromInputStream((DataInputStream) fis);
		
		/*
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/* TODO: Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 */
		/*
		if (msgOut.getOpcode() != msgIn.getOpcode()) {
			System.err.println("Opcode does not match!");
		}
		if (msgOut.getLength() != msgIn.getLength()) {
			System.err.println("Length does not match!");
		}
		if (!msgOut.getHash().equals(msgIn.getHash())) {
			System.err.println("Hash does not match!");
		}*/
	}

}

