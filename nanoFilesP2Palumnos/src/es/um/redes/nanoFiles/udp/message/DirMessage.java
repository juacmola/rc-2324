package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import es.um.redes.nanoFiles.util.FileInfo;


/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_SESSIONKEY = "sessionKey";
	private static final String FIELDNAME_USER = "user";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_IP = "ip";
	private static final String FIELDNAME_PEER = "isPeer";
	private static final String FIELDNAME_PUBLISH = "publish";
	private static final String FIELDNAME_NUM_FILES = "numFiles";
	private static final String FIELDNAME_PUBLISHED = "published";
	private static final String FIELDNAME_FILE_HASH = "fileHash";
	private static final String FIELDNAME_SERVER = "server";

	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String nickname = DirMessageOps.NICKNAME_INVALID;
	private int sessionKey = DirMessageOps.SESSIONKEY_INVALID;
	private int port = DirMessageOps.PORT_INVALID;
	private String ip = DirMessageOps.IP_INVALID;
	private ArrayList<String> usersList = new ArrayList<>();
	private ArrayList<String> isPeerList = new ArrayList<>();
	private FileInfo[] files = null;
	private String filesInString = DirMessageOps.FILES_IN_STRING_INVALID;
	private int numFiles = 0;
	private String fileHash = DirMessageOps.HASH_INVALID;
	private ArrayList<String> fileServers = new ArrayList<>();

	public DirMessage(String op) {
		this.operation = op;
	}
	
	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */
	
	//Getters
	public String getOperation() { return operation; }
	public String getNickname() {	return nickname;	}
	public int getSessionKey() { return sessionKey;	}
	public ArrayList<String> getUsersList() {	return usersList;	}
	public int getPort() { return port;	}
	public String getIP() {	return ip; }
	public ArrayList<String> getIsPeerList() { return isPeerList;	}
	public FileInfo[] getPublishedFiles() {	return files;	}
	public String getFile() {	return filesInString;	}
	public int getNumFiles() { return numFiles;	}
	public String getFileHash() {	return fileHash; }
	public ArrayList<String> getFileServer() {	return fileServers;	}
	
	//Setters
	public void setNickname(String nick) { this.nickname = nick; }
	public void setSessionKey(int sessionKey) {	this.sessionKey = sessionKey;	}
	public void setUsersList(ArrayList<String> nicks) {	this.usersList = nicks;	}
	public void setPort(int port) { this.port = port;	}
	public void setIP(String address) {	this.ip = address; }
	public void setIsPeer(ArrayList<String> bool) {	this.isPeerList = bool;	}
	public void setPublishFiles(FileInfo[] f) {	this.files = f;	}
	public void setNumFiles(int num) { this.numFiles = num;	}
	public void setFilesInString (String f) {	this.filesInString = f;	}
	public void setFileHash(String hash) { this.fileHash = hash; }
	public void setFileServer(ArrayList<String> servers) { this.fileServers = servers; }

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */

		System.out.println("DirMessage read from socket:");
		System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;

		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx);//.toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();
			int i = 0;
			
			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			
			case FIELDNAME_NICKNAME: {
				m.nickname = value;
				break;
			}
			
			case FIELDNAME_SESSIONKEY: {
				m.sessionKey = Integer.parseInt(value);
				break;
			}
			
			case FIELDNAME_USER: {
				m.usersList.add(value);
				break;
			}
			
			case FIELDNAME_PORT:{
				m.port = Integer.parseInt(value);
				break;
			}
			
			case FIELDNAME_IP:{
				m.ip = value;
				break;
			}
			
			case FIELDNAME_PEER:{
				m.isPeerList.add(value);
				break;
			}
			
			case FIELDNAME_PUBLISH:{
				m.files = FileInfo.fromPublish(value, m.numFiles);
				break;
			}
			
			case FIELDNAME_PUBLISHED:{
				m.files = FileInfo.fromFileList(value, m.numFiles);
				break;
			}
			
			case FIELDNAME_NUM_FILES:{
				m.numFiles = Integer.parseInt(value);
				break;
			}
			
			case FIELDNAME_FILE_HASH:{
				m.fileHash = value;
				break;
			}
			
			case FIELDNAME_SERVER:{
				m.fileServers.add(value);
				i++;
				break;
			}

			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}

		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		switch(operation) {
			case DirMessageOps.OPERATION_LOGIN:
			case DirMessageOps.OPERATION_GETADDR_FROM_NICK: {
				sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_LOGINOK:
			case DirMessageOps.OPERATION_LOGINFAIL: {
				sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_LOGOUT:
			case DirMessageOps.OPERATION_STOP_SERVER:{
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_REGISTERED_USERS_RESP: {
				for (int i=0; i< usersList.size(); i++) {
					sb.append(FIELDNAME_USER + DELIMITER + usersList.get(i) + END_LINE);
					sb.append(FIELDNAME_PEER + DELIMITER + isPeerList.get(i) + END_LINE);
				}
				break;
			}
			
			case DirMessageOps.OPERATION_REGISTER_SERVER: {
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
				sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_GET_FILE_LIST:
			case DirMessageOps.OPERATION_PUBLISH_OK:
			case DirMessageOps.OPERATION_PUBLISH_FAIL:
			case DirMessageOps.OPERATION_REGISTERED_USERS:
			case DirMessageOps.OPERATION_REGISTER_SERVER_OK:
			case DirMessageOps.OPERATION_STOP_SERVER_OK:
			case DirMessageOps.OPERATION_STOP_SERVER_FAIL:
			case DirMessageOps.OPERATION_GETADDR_FAIL:
			case DirMessageOps.OPERATION_FILE_LIST_FAIL:
			case DirMessageOps.OPERATION_SEARCHED_FAIL:{ break; }
			
			case DirMessageOps.OPERATION_GETADDR_RESP:{
				sb.append(FIELDNAME_IP + DELIMITER + ip + END_LINE);
				sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_PUBLISH:{
				String totalFiles = "";
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
				sb.append(FIELDNAME_NUM_FILES + DELIMITER + numFiles + END_LINE);
				for (int i=0; i<files.length-1; i++) totalFiles = totalFiles + files[i].toPublish() + ",";
				totalFiles = totalFiles + files[files.length-1].toPublish();
				
				sb.append(FIELDNAME_PUBLISH + DELIMITER + totalFiles + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_FILE_LIST_RESP:{
				sb.append(FIELDNAME_NUM_FILES + DELIMITER + numFiles + END_LINE);
				sb.append(FIELDNAME_PUBLISHED + DELIMITER + filesInString + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_GET_SEARCHED:{
				sb.append(FIELDNAME_FILE_HASH + DELIMITER + fileHash + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_SEARCHED_RESP:{
				for (int i=0; i< fileServers.size(); i++) sb.append(FIELDNAME_SERVER + DELIMITER + fileServers + END_LINE);
				break;
			}
		}
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
