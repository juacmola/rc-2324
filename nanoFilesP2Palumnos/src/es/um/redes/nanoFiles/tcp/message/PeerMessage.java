package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {
	

	private byte opcode;
	
	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */
	private String hash;
	//private int length;
	//private FileInfo fileInfo;
	private byte[] dataFile;
	private int fileFragments;
	private ArrayList<String> hashes = new ArrayList<>();
	private ArrayList<String> fileNames = new ArrayList<>();


	public PeerMessage() {}

	public PeerMessage(byte op) {
		this.opcode = op;
	}
	
	/*
	 * TODO: Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */
	public byte getOpcode() { return opcode;	}
	public String getHash() { return hash;	}
	//public int getLength() { return length; }
	public byte[] getDataFile() { return dataFile; }
	public int getFileFragments() { return fileFragments;	}
	public ArrayList<String> getHashes() { return hashes;	}
	public ArrayList<String> getFileNames() { return fileNames;	}
	
	public void setOpcode(byte op) { this.opcode = op;	}
	public void setHash(String val) { this.hash = val;	}
	//public void setLength(int len) { this.length = len;	}
	public void setDataFile(byte[] dataFile) { this.dataFile = dataFile;	}
	public void setFileFragments(int fileFragments) { this.fileFragments = fileFragments;	}
	public void setHashes(ArrayList<String> hashes) { this.hashes = hashes;	}
	public void setFileNames(ArrayList<String> fileNames) { this.fileNames = fileNames;	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: En función del tipo de mensaje, leer del socket a través del "dis" el
		 * resto de campos para ir extrayendo con los valores y establecer los atributos
		 * del un objeto DirMessage que contendrá toda la información del mensaje, y que
		 * será devuelto como resultado. NOTA: Usar dis.readFully para leer un array de
		 * bytes, dis.readInt para leer un entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		message.setOpcode(opcode);
		
		switch (opcode) {
			//case PeerMessageOps.OPCODE_AMBIGUOUS_HASH:
			case PeerMessageOps.OPCODE_INVALID_OPCODE:
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM_FAIL: {
				
				break;
			}
		
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM: { //Nos quedamos con la cadena hash
				int hashLength = dis.readInt();
				byte[] dataHash = new byte[hashLength];
				dis.readFully(dataHash);
				String strHash = new String(dataHash,"UTF-8");
				message.setHash(strHash);
				break;
			}
			
			//case PeerMessageOps.OPCODE_END_OF_FILE:
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM_RESP: {
				//int lenFile=dis.readInt();
				int hashLength = dis.readInt();
				byte[] dataHash = new byte[hashLength];
				dis.readFully(dataHash);
				String strHash = new String(dataHash,"UTF-8");
				int dataFileLength = dis.readInt();
				byte[] dataFile = new byte[dataFileLength];
				dis.readFully(dataFile);
				//message.setLength(lenFile);
				message.setHash(strHash);
				message.setDataFile(dataFile);
				break;
			}
			
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM_RESP_HS: {
				int fileFragments = dis.readInt();
				int hashLength = dis.readInt();
				byte[] dataHash = new byte[hashLength];
				dis.readFully(dataHash);
				String strHash = new String(dataHash,"UTF-8");
				int dataFileLength = dis.readInt();
				byte[] dataFile = new byte[dataFileLength];
				dis.readFully(dataFile);
				message.setFileFragments(fileFragments);
				message.setHash(strHash);
				message.setDataFile(dataFile);
				break;
			}
			
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM_WHICH: {
				int hashesLength = dis.readInt();
				byte[] dataHashes = new byte[hashesLength];
				dis.readFully(dataHashes);
				String strConcatenatedHashes = new String(dataHashes,"UTF-8");
				String[] strArrHashes = strConcatenatedHashes.split("/");
				int fileNamesLength = dis.readInt();
				byte[] dataFileNames = new byte[fileNamesLength];
				dis.readFully(dataFileNames);
				String strConcatenatedFileNames = new String(dataFileNames,"UTF-8");
				String[] strArrFileNames = strConcatenatedFileNames.split("/");
				message.setHashes(new ArrayList<>(Arrays.asList(strArrHashes)));
				message.setFileNames(new ArrayList<>(Arrays.asList(strArrFileNames)));
				break;
			}
				
				
			/*
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND: { //Nos quedamos con la cadena hash
				int len=dis.readInt();
				byte[] data=new byte[len];
				dis.readFully(data);
				String str=new String(data,"UTF-8");
				message.setHash(str);
				break;
			}
			*/
			
			/*
			case PeerMessageOps.OPCODE_TEST: {
				System.out.println("The reading works");
				break;
			}
			*/
			
			default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}
	
	public static byte[] ListToArrayByte(List<String> list) throws IOException {
		//Concatenamos todos los elementos con "/" entre ellos.
		StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append("/"); // Agregamos "/" solo entre elementos.
            }
        }

        // Convertimos el resultado en un array de bytes
        return sb.toString().getBytes("UTF-8");
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para escribir un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
			//case PeerMessageOps.OPCODE_AMBIGUOUS_HASH: 
			case PeerMessageOps.OPCODE_INVALID_OPCODE:
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM_FAIL: {
		 
				break;
			}
			
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM: {
				byte[] dataHash = hash.getBytes("UTF-8");
				dos.writeInt(dataHash.length);
				dos.write(dataHash);
				break;
			}

			//case PeerMessageOps.OPCODE_END_OF_FILE:
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM_RESP: {
				byte[] dataHash = hash.getBytes("UTF-8");
				dos.writeInt(dataHash.length);
				dos.write(dataHash);
				dos.writeInt(dataFile.length);
				dos.write(dataFile);
				break;
			}
			
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM_RESP_HS: {
				byte[] dataHash = hash.getBytes("UTF-8");
				dos.writeInt(fileFragments);
				dos.writeInt(dataHash.length);
				dos.write(dataHash);
				dos.writeInt(dataFile.length);
				dos.write(dataFile);
				break;
			}
			
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM_WHICH: {
				byte[] dataHashes = ListToArrayByte(hashes);
				byte[] dataFileNames = ListToArrayByte(fileNames);
				dos.writeInt(dataHashes.length);
				dos.write(dataHashes);
				dos.writeInt(dataFileNames.length);
				dos.write(dataFileNames);
				break;
			}
			
			
			/*
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND: {
				byte[] data=hash.getBytes("UTF-8");
				dos.writeInt(data.length);
				dos.write(data);
				break;
			}
			*/
			
			/*
			case PeerMessageOps.OPCODE_TEST: {
				System.out.println("The writing works");
				break;
			}
			*/
			
			default:
				System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}





}
