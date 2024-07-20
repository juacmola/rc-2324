package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.sound.midi.SysexMessage;

import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.udp.server.NFDirectoryServer;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/** Puerto en el que atienden los servidores de directorio */
	private static final int DIRECTORY_PORT = 6868;
	
	/** Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control */
	private static final int TIMEOUT = 1000;
	
	/** Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento. */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Valor inválido de la clave de sesión, antes de ser obtenida del directorio al loguearse
	 */
	public static final int INVALID_SESSION_KEY = -1;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;

	private int sessionKey = INVALID_SESSION_KEY;
	private boolean successfulResponseStatus;
	private String errorDescription;

	public DirectoryConnector(String address) throws IOException {
		/* DONE: Convertir el nombre de host 'address' a InetAddress y guardar la dirección de socket (address:DIRECTORY_PORT) 
		 * del directorio en el atributo directoryAddress, para poder enviar datagramas a dicho destino. */
		InetAddress directoryIp = InetAddress.getByName(address);
		this.directoryAddress = new InetSocketAddress(directoryIp, NFDirectoryServer.DIRECTORY_PORT);
		
		/* DONE: Crea el socket UDP en cualquier puerto para enviar datagramas al directorio */
		this.socket = new DatagramSocket();
		System.out.println("Created UDP socket at local addresss " + socket.getLocalSocketAddress()); 		// Opcional
	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 * @throws IOException 
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) throws IOException {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		
		boolean paqueteRecibido=false;
		int numeroIntentos=0;
		
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/* DONE: Enviar datos en un datagrama al directorio y recibir una respuesta. El
		 * array devuelto debe contener únicamente los datos recibidos, *NO* el búfer de
		 * recepción al completo. */
		DatagramPacket packetToDirectory = new DatagramPacket(requestData, requestData.length, directoryAddress);
		socket.send(packetToDirectory);
		
		DatagramPacket packetFromDirectory = new DatagramPacket(responseData, responseData.length);
		
		/* DONE: Una vez el envío y recepción asumiendo un canal confiable (sin
		 * pérdidas) esté terminado y probado, debe implementarse un mecanismo de
		 * retransmisión usando temporizador, en caso de que no se reciba respuesta en
		 * el plazo de TIMEOUT. En caso de salte el timeout, se debe reintentar como
		 * máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones. */
		/* DONE: Las excepciones que puedan lanzarse al leer/escribir en el socket deben
		 * ser capturadas y tratadas en este método. Si se produce una excepción de
		 * entrada/salida (error del que no es posible recuperarse), se debe informar y
		 * terminar el programa.*/
		/* NOTA: Las excepciones deben tratarse de la más concreta a la más genérica.
		 * SocketTimeoutException es más concreta que IOException.*/
		
//		socket.setSoTimeout(TIMEOUT);	
		while(!paqueteRecibido && numeroIntentos < MAX_NUMBER_OF_ATTEMPTS) {
	
			try{
				socket.receive(packetFromDirectory);
				String messageFromDirectory = new String(responseData, 0, packetFromDirectory.getLength());
				response = messageFromDirectory.getBytes();
				paqueteRecibido = true;
				
			}catch(SocketTimeoutException e) {
				System.err.println("Timeout is over. Trying again... ");
				socket.send(packetToDirectory);
				numeroIntentos++;
				
			}catch(IOException e) {
				System.err.println("There was an IOException" + e.getMessage());
				System.exit(1);
			}
		}
		
		if (numeroIntentos == MAX_NUMBER_OF_ATTEMPTS) {
			System.err.println("The maximum number of resend attempts has been exceeded. Aborting communication with the directory...");
			System.exit(1);
		}

		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 * @throws IOException 
	 */
	public boolean testSendAndReceive() throws IOException {
		/* DONE: Probar el correcto funcionamiento de sendAndReceiveDatagrams. Se debe enviar un datagrama con 
		 * la cadena "login" y comprobar que la respuesta recibida es "loginok". En tal caso, devuelve verdadero, 
		 * falso si la respuesta no contiene los datos esperados.*/
		boolean success = false;
		
		String messageToDirectory = DirMessageOps.OPERATION_LOGIN;
		byte[] login = messageToDirectory.getBytes();
		System.out.println("Sending message to directory: \"" + messageToDirectory + "\"");	// Opcional
		
		byte[] datafromDirectory = sendAndReceiveDatagrams(login);
		String messageFromDirectory = new String(datafromDirectory);
		
		System.out.println(" Reception buffer contents: " + messageFromDirectory);			// Opcional
		if (messageFromDirectory.equals(DirMessageOps.OPERATION_LOGINOK))
			success = true;

		return success;
	}

	public InetSocketAddress getDirectoryAddress() {
		return directoryAddress;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	/**
	 * Método para "iniciar sesión" en el directorio, comprobar que está operativo y
	 * obtener la clave de sesión asociada a este usuario.
	 * 
	 * @param nickname El nickname del usuario a registrar
	 * @return La clave de sesión asignada al usuario que acaba de loguearse, o -1 en caso de error
	 */
	//El codigo comentado servira mas adelante
	public boolean logIntoDirectory(String nickname) {
		assert (sessionKey == INVALID_SESSION_KEY);
		boolean success = false;
		// DONE: 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
		// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la clase DirMessageOps
		// DONE: 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		// DONE: 3.Crear un datagrama con los bytes en que se codifica la cadena
		// DONE: 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		// DONE: 5.Convertir respuesta recibida en un objeto DirMessage (método DirMessage.fromString)
		// DONE: 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
		// DONE: 7.Devolver éxito/fracaso de la operación
		
		DirMessage dirMessageToDirectory = new DirMessage(DirMessageOps.OPERATION_LOGIN);
		dirMessageToDirectory.setNickname(nickname);
		String messageToDirectory = dirMessageToDirectory.toString();
		byte[] requestData = messageToDirectory.getBytes();
		byte response[] = null;
		try {
			response = sendAndReceiveDatagrams(requestData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String responseFromDirectory = new String(response);
		DirMessage dirMessageFromDirectory = DirMessage.fromString(responseFromDirectory);
		
		String loginOp = dirMessageFromDirectory.getOperation();
		int num = dirMessageFromDirectory.getSessionKey();
		
		if (loginOp.equals("loginOK") && num >= 0 && num <= 10000) {
			success = true;
			sessionKey = num;
			System.out.println(nickname + " has logged in successfully (" + sessionKey + ").");
		}
		
		else {
			System.err.println(nickname + " couldn't log in correctly.");
		}
		
		return success;
	}

	/**
	 * Método para obtener la lista de "nicknames" registrados en el directorio.
	 * Opcionalmente, la respuesta puede indicar para cada nickname si dicho peer
	 * está sirviendo ficheros en este instante.
	 * 
	 * @return La lista de nombres de usuario registrados, o null si el directorio
	 *         no pudo satisfacer nuestra solicitud
	 */
	public String[] getUserList() {
		String[] userlist = null;
		// DONE: Ver TODOs en logIntoDirectory y seguir esquema similar
		DirMessage dirMessageToDirectory = new DirMessage(DirMessageOps.OPERATION_REGISTERED_USERS);
		dirMessageToDirectory.setSessionKey(sessionKey);
		String messageToDirectory = dirMessageToDirectory.toString();
		byte[] requestData = messageToDirectory.getBytes();
		byte response[] = null;
		try {
			response = sendAndReceiveDatagrams(requestData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String responseFromDirectory = new String(response);
		DirMessage dirMessageFromDirectory = DirMessage.fromString(responseFromDirectory);
		userlist = dirMessageFromDirectory.getUsersList().toArray(new String[dirMessageFromDirectory.getUsersList().size()]);
		
		if (userlist.length == 0)
			userlist = null;

		return userlist;
	}

	/**
	 * Método para "cerrar sesión" en el directorio
	 * 
	 * @return Verdadero si el directorio eliminó a este usuario exitosamente
	 */
	public boolean logoutFromDirectory() {
		// DONE: Ver TODOs en logIntoDirectory y seguir esquema similar
		boolean success = false;
		
		DirMessage dirMessageToDirectory = new DirMessage(DirMessageOps.OPERATION_LOGOUT);
		dirMessageToDirectory.setSessionKey(sessionKey);
		String messageToDirectory = dirMessageToDirectory.toString();
		byte[] requestData = messageToDirectory.getBytes();
		byte response[] = null;
		try {
			response = sendAndReceiveDatagrams(requestData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String responseFromDirectory = new String(response);
		DirMessage dirMessageFromDirectory = DirMessage.fromString(responseFromDirectory);

		String logoutOp = dirMessageFromDirectory.getOperation();
		if (logoutOp.equals("logoutOK")) {
			success = true;
		}
		
		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 */
	public boolean registerServerPort(int serverPort) {
		// DONE: Ver TODOs en logIntoDirectory y seguir esquema similar
		boolean success = false;
		
		DirMessage dirMessageToDirectory = new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER);
		dirMessageToDirectory.setSessionKey(sessionKey);
		dirMessageToDirectory.setPort(serverPort);
		
		String messageToDirectory = dirMessageToDirectory.toString();
		
		byte[] requestData = messageToDirectory.getBytes();
		byte response[] = null;
		try { response = sendAndReceiveDatagrams(requestData);
		} catch (IOException e) { e.printStackTrace(); }
		String responseFromDirectory = new String(response);
		DirMessage dirMessageFromDirectory = DirMessage.fromString(responseFromDirectory);
		
		String confirmation = dirMessageFromDirectory.getOperation();
		if (confirmation.equals("registerServerOK")) {
			success = true;
			System.out.println("File Server was set in port " + serverPort);
		}else {
			System.err.println("Couldn't set File Server in port " + serverPort);
		}
		
		return success;
	}

	/**
	 * Método para obtener del directorio la dirección de socket (IP:puerto)
	 * asociada a un determinado nickname.
	 * 
	 * @param nick El nickname del servidor de ficheros por el que se pregunta
	 * @return La dirección de socket del servidor en caso de que haya algún
	 *         servidor dado de alta en el directorio con ese nick, o null en caso
	 *         contrario.
	 * @throws UnknownHostException 
	 */
	public InetSocketAddress lookupServerAddrByUsername(String nick) throws UnknownHostException {
		InetSocketAddress serverAddr = null;
		// DONE: Ver TODOs en logIntoDirectory y seguir esquema similar
			DirMessage dirMessageToDirectory = new DirMessage(DirMessageOps.OPERATION_GETADDR_FROM_NICK);
			dirMessageToDirectory.setNickname(nick);
			
			String messageToDirectory = dirMessageToDirectory.toString();
			byte[] requestData = messageToDirectory.getBytes();
			byte response[] = null;
			
			try {
				response = sendAndReceiveDatagrams(requestData);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String responseFromDirectory = new String(response);
			DirMessage dirMessageFromDirectory = DirMessage.fromString(responseFromDirectory);
			
			String confirmation = dirMessageFromDirectory.getOperation();
			if (confirmation.equals("getAddrResp") ) {
				String address = dirMessageFromDirectory.getIP();
				int port = dirMessageFromDirectory.getPort();
				InetAddress ip = InetAddress.getByName(address);
				serverAddr = new InetSocketAddress(ip, port);
				System.out.println("The address sent by Directory is " + ip + ":" + port);
			}else {
				System.err.println("There was an error sending the address");
			}

		return serverAddr;
	}

	/**
	 * Método para publicar ficheros que este peer servidor de ficheros están
	 * compartiendo.
	 * 
	 * @param files La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean publishLocalFiles(FileInfo[] files) {
		boolean success = false;

		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar



		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar



		return filelist;
	}

	/**
	 * Método para obtener la lista de nicknames de los peers servidores que tienen
	 * un fichero identificado por su hash. Opcionalmente, puede aceptar también
	 * buscar por una subcadena del hash, en vez de por el hash completo.
	 * 
	 * @return La lista de nicknames de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public String[] getServerNicknamesSharingThisFile(String fileHash) {
		String[] nicklist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar



		return nicklist;
	}
	
	/**
	 * Método para cerrar el servidor en segundo plano del peer
	 * 
	 * @return Verdadero si el directorio eliminó el servidor en segundo plano
	 */
	public boolean stopBackGroundServer() {
		// DONE: Ver TODOs en logIntoDirectory y seguir esquema similar
		boolean success = false;
		
		DirMessage dirMessageToDirectory = new DirMessage(DirMessageOps.OPERATION_STOP_SERVER);
		dirMessageToDirectory.setSessionKey(sessionKey);
		
		String messageToDirectory = dirMessageToDirectory.toString();
		byte[] requestData = messageToDirectory.getBytes();
		byte response[] = null;
		try {
			response = sendAndReceiveDatagrams(requestData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String responseFromDirectory = new String(response);
		DirMessage dirMessageFromDirectory = DirMessage.fromString(responseFromDirectory);

		String logoutOp = dirMessageFromDirectory.getOperation();
		if (logoutOp.equals("stopServerOK")) {
			success = true;
		}
		
		return success;
	}




}
