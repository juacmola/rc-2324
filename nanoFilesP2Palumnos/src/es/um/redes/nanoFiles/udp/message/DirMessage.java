package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


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
	private HashSet<String> usersList = new HashSet<>();




	public DirMessage(String op) {
		this.operation = op;
	}
	
	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */
	
	//Getters
	public String getOperation() {
		return operation;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public int getSessionKey() {
		return sessionKey;
	}
	
	public Set<String> getUsersList() {
		return Collections.unmodifiableSet(usersList);
	}
	
	public int getPort() {
		return port;
	}
	
	public String getIP() {
		return ip;
	}
	
	//Setters
	public void setNickname(String nick) {
		this.nickname = nick;
	}

	public void setSessionKey(int sessionKey) {
		this.sessionKey = sessionKey;
	}
	
	public void setUsersList(HashMap<String, Integer> nicks) {
		this.usersList.addAll(nicks.keySet());
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setIP(String address) {
		this.ip = address;
	}



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
			
			case DirMessageOps.OPERATION_REGISTERED_USERS:
			case DirMessageOps.OPERATION_LOGOUT:
			case DirMessageOps.OPERATION_STOP_SERVER:{
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_REGISTERED_USERS_RESP: {
				for (String user : usersList)
					sb.append(FIELDNAME_USER + DELIMITER + user + END_LINE);
				
				break;
			}
			
			case DirMessageOps.OPERATION_REGISTER_SERVER: {
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
				sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
				break;
			}
			
			case DirMessageOps.OPERATION_REGISTER_SERVER_OK:
			case DirMessageOps.OPERATION_STOP_SERVER_OK:
			case DirMessageOps.OPERATION_STOP_SERVER_FAIL:
			case DirMessageOps.OPERATION_GETADDR_FAIL:{ break; }
			
			case DirMessageOps.OPERATION_GETADDR_RESP:{
				sb.append(FIELDNAME_IP + DELIMITER + ip + END_LINE);
				sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
				break;
			}
		
		}



		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
