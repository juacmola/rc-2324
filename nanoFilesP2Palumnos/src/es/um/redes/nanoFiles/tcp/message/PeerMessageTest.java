package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
		String nombreArchivo = "peermsg.bin";
		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));

		/*
		 * TODO: Probar a crear diferentes tipos de mensajes (con los opcodes válidos
		 * definidos en PeerMessageOps), estableciendo los atributos adecuados a cada
		 * tipo de mensaje. Luego, escribir el mensaje a un fichero con
		 * writeMessageToOutputStream para comprobar que readMessageFromInputStream
		 * construye un mensaje idéntico al original.
		 */
		PeerMessage msgOut = new PeerMessage();
		
		/*
		//InvalidOpCode
		msgOut.setOpcode(PeerMessageOps.OPCODE_INVALID_OPCODE);
		*/
		
		/*
		//DownloadFrom
		msgOut.setOpcode(PeerMessageOps.OPCODE_DOWNLOAD_FROM);
		msgOut.setHash("f93e3551b61f41fb7d6c410a47aba01df0cb9b56");
		*/
		
		File f = new File("nf-shared/prueba.txt");
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		long fLength = f.length();
		byte[] dataF = new byte[(int) fLength];
		dis.readFully(dataF);
		dis.close();
		
		/*
		//DownloadFromRespHS
		msgOut.setOpcode(PeerMessageOps.OPCODE_DOWNLOAD_FROM_RESP_HS);
		msgOut.setFileFragments(3);
		msgOut.setHash("f93e3551b61f41fb7d6c410a47aba01df0cb9b56");
		*/
		
		/*
		//DownloadFromResp
		msgOut.setOpcode(PeerMessageOps.OPCODE_DOWNLOAD_FROM_RESP);
		msgOut.setDataFile(dataF);
		*/
		
		/*
		//DownloadFromWhich
		msgOut.setOpcode(PeerMessageOps.OPCODE_DOWNLOAD_FROM_WHICH);
		ArrayList<String> hashes = new ArrayList<>();
		hashes.add("7d8c87e057be98f00f22e23b23fbf08999e4b02f");
		hashes.add("f93e3551b61f41fb7d6c410a47aba01df0cb9b56");
		msgOut.setHashes(hashes);
		ArrayList<String> fileNames = new ArrayList<>();
		fileNames.add("factura1.pdf");
		fileNames.add("factura2.pdf");
		msgOut.setFileNames(hashes);
		*/
		
		
		//DownloadFromFAIL
		msgOut.setOpcode(PeerMessageOps.OPCODE_DOWNLOAD_FROM_FAIL);
		
		
		msgOut.writeMessageToOutputStream(fos);

		DataInputStream fis = new DataInputStream(new FileInputStream(nombreArchivo));
		PeerMessage msgIn = PeerMessage.readMessageFromInputStream((DataInputStream) fis);
		/*
		 * TODO: Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 */
		if (msgOut.getOpcode() != msgIn.getOpcode()) {
			System.err.println("Opcode does not match!");
		}
		
		/*
		else if (!msgOut.getHash().equals(msgIn.getHash()))
			System.err.println("Hash does not match!");
		*/
		/*
		System.out.println(new String(msgOut.getDataFile(), "UTF-8"));
		System.out.println(new String(msgIn.getDataFile(), "UTF-8"));
		*/
		/*
		if (msgOut.getFileFragments() != msgIn.getFileFragments())
			System.err.println("File fragments does not match!");
		
		else if (!msgIn.getHashes().equals(msgOut.getHashes()))
			System.err.println("Hashes does not match!");
		
		else if (!msgIn.getFileNames().equals(msgOut.getFileNames()))
			System.err.println("File names does not match!");
		*/
	}

}

