package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {
	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea



	private byte opcode;
	
	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */
	private int length;
	private byte[] value;



	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
		length = 0;
		value = new byte[0];
	}

	public PeerMessage(byte op, int len, byte[] val) {
		opcode = op;
		length = len;
		value = val;
	}
	
	/*
	 * TODO: Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */
	public byte getOpcode() {
		return opcode;
	}
	
	public int getLenght() {
		return length;
	}

	public byte[] getValue() {
		return value;
	}
	
	public void setOpcode(byte op) {
		opcode = op;
	}
	
	public void setLenght(int len) {
		length = len;
	}

	public void setValue(byte[] val) {
		value = val;
	}

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
		case PeerMessageOps.OPCODE_DOWNLOAD_FROM: {
			int longitudDatos = dis.readInt();
			byte[] datos = new byte[longitudDatos];
			dis.readFully(datos);
			message.setLenght(longitudDatos);
			message.setValue(datos);
			break;
		}

		case PeerMessageOps.OPCODE_DOWNLOAD_OK: {
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
		/*
		 * TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para escribir un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_DOWNLOAD_FROM: {
			dos.writeInt(length);
			dos.write(value);
			System.out.println(PeerMessageOps.opcodeToOperation(opcode) + DELIMITER + " longitud: " + length + " datos: " + value + END_LINE);
			break;
		}

		case PeerMessageOps.OPCODE_DOWNLOAD_OK: {
			break;
		}

		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
		case PeerMessageOps.OPCODE_INCORRECT_HASH: {
			break;
		}
		
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}





}
