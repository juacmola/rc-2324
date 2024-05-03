package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {
	

	private byte opcode;
	
	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */
	private String hash;
	private int length;
	private FileInfo fileInfo;
	private byte[] file;


	public PeerMessage() {}

	public PeerMessage(byte op) {
		opcode = op;
	}
	
	/*
	 * TODO: Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */
	public byte getOpcode() { return opcode;	}
	public String getHash() { return hash;	}
	public int getLength() { return length; }
	public byte[] getFile() { return file; }
	public void setOpcode(byte op) { this.opcode = op;	}
	public void setHash(String val) { this.hash = val; }
	public void setLength(int len) { this.length = len;	}
	public void setFile(byte[] f) { this.file = f; }

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
			case PeerMessageOps.OPCODE_AMBIGUOUS_HASH:
			case PeerMessageOps.OPCODE_INVALID_CODE: 
			case PeerMessageOps.OPCODE_END_OF_FILE: {
				
				break;
			}
		
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM: { //Nos quedamos con la cadena hash
				int len=dis.readInt();
				byte[] data=new byte[len];
				dis.readFully(data);
				String str=new String(data,"UTF-8");
				message.setHash(str);
				break;
			}

			case PeerMessageOps.OPCODE_DOWNLOAD_OK: {
				int lenFile=dis.readInt();
				int lenHash=dis.readInt();
				byte[] dataHash=new byte[lenHash];
				dis.readFully(dataHash);
				String strHash=new String(dataHash,"UTF-8");
				byte[] dataFile=new byte[lenFile];
				dis.readFully(dataFile);
				message.setLength(lenFile);
				message.setHash(strHash);
				message.setFile(dataFile);
        break;
			}
		
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND: { //Nos quedamos con la cadena hash
				int len=dis.readInt();
				byte[] data=new byte[len];
				dis.readFully(data);
				String str=new String(data,"UTF-8");
				message.setHash(str);
				break;
			}
		
			case PeerMessageOps.OPCODE_TEST: {
				System.out.println("The reading works");
				break;
			}
		
			default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para escribir un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_DOWNLOAD_FROM: {
			byte[] data=hash.getBytes("UTF-8");
			dos.writeInt(data.length);
			dos.write(data);
			break;
		}

		case PeerMessageOps.OPCODE_AMBIGUOUS_HASH: 
		case PeerMessageOps.OPCODE_INVALID_CODE: 
		case PeerMessageOps.OPCODE_END_OF_FILE: {
		 
			break;
		}
		
		case PeerMessageOps.OPCODE_DOWNLOAD_OK: {
			dos.writeInt(length);				//La longitud del fichero
			byte[] data=hash.getBytes("UTF-8");
			dos.writeInt(data.length);
			dos.write(data);
			dos.write(file);
			break;
		}
		
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND: {
			byte[] data=hash.getBytes("UTF-8");
			dos.writeInt(data.length);
			dos.write(data);
			break;
		}
		
		case PeerMessageOps.OPCODE_TEST: {
			System.out.println("The writing works");
			break;
		}
		
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}





}
