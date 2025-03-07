package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.io.ObjectOutputStream.PutField;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/**
	 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
	 * 
	 */
	private HashMap<String, Integer> nicks;
	/**
	 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
	 * 
	 */
	private HashMap<Integer, String> sessionKeys;
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc. Luego, inicializarlas.
	 */
	/**
	 * Estructura para guardar las claves de sesión y sus puertos asociados
	 */
	private HashMap<Integer, Integer> peers;	//SessionKey|PORT

	/**
	 * Estructura para guardar los nicknames con sus direcciones IP
	 */
	private HashMap<String, String> address;	//Nick|IP
	
	/**
	 * Estructura para guardar los ficheros publicados por cada servidor
	 */
	private HashMap<Integer, List<FileInfo>> published;	//sessionKey|FileInfo
	
	/**
	 * Estructura para guardar los nick que tienen un archivo
	 */
	private HashMap<String, List<String>> searched;	//Hash|Nick
	
	private String CL="";
	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	Random random = new Random();
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/* Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable) */
		this.messageDiscardProbability = corruptionProbability;
		/* DONE: (Boletín UDP) Inicializar el atributo socket: Crear un socket UDP
		 * ligado al puerto especificado por el argumento directoryPort en la máquina local */
		this.socket = new DatagramSocket(DIRECTORY_PORT);
		
		/* DONE: (Boletín UDP) Inicializar el resto de atributos de esta clase
		 * (estructuras de datos que mantiene el servidor: nicks, sessionKeys, etc.) */
		this.nicks = new HashMap<>();
		this.sessionKeys = new HashMap<>();
		this.peers = new HashMap<>();
		this.address = new HashMap<>();
		this.published = new HashMap<>();
		this.searched = new HashMap<>();

		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public void run() throws IOException {
		byte[] receptionBuffer = null;
		InetSocketAddress clientAddr = null;
		int dataLength = -1;
		
		/* DONE: (Boletín UDP) Crear un búfer para recibir datagramas y un datagrama
		 * asociado al búfer */
		receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
		DatagramPacket packetFromClient = new DatagramPacket(receptionBuffer, receptionBuffer.length);
		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			// DONE: (Boletín UDP) Recibimos a través del socket un datagrama
			System.out.println("Waiting to receive datagram...");						// Opcional
			socket.receive(packetFromClient);
			// DONE: (Boletín UDP) Establecemos dataLength con longitud del datagrama recibido
			dataLength = packetFromClient.getLength();
			// DONE: (Boletín UDP) Establecemos 'clientAddr' con la dirección del cliente,
			// obtenida del datagrama recibido
			clientAddr = (InetSocketAddress) packetFromClient.getSocketAddress();

			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");	
			CL = clientAddr.getHostString();
			
			if (dataLength > 0) {	// Analizamos la solicitud y la procesamos
				String messageFromClient = null;
				
				/* DONE: (Boletín UDP) Construir una cadena a partir de los datos recibidos en
				 * el buffer de recepción */
				messageFromClient = new String(receptionBuffer, 0, packetFromClient.getLength());

				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
					
					/* DONE: (Boletín UDP) Comprobar que se ha recibido un datagrama con la cadena "login" y en 
					 * ese caso enviar como respuesta un mensaje al cliente con la cadena "loginok". Si el 
					 * mensaje recibido no es "login", se informa del error y no se envía ninguna respuesta. */
					if (messageFromClient.equals("login")) {
						String messageToClient = new String("loginok");
						byte[] dataToClient = messageToClient.getBytes();
						System.out.println("Sending datagram with message \"" + messageToClient + "\"");			// Opcional
						System.out.println("Destination is client at addr: " + clientAddr);							// Opcional
						DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
						socket.send(packetToClient);
					}
					else
						System.err.println("The message is not a 'login' message");
					
				} else { 	// Servidor funcionando en modo producción (mensajes bien formados), el codigo comentado servira mas adelante
					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}
					/* DONE: Construir String partir de los datos recibidos en el datagrama. A
					 * continuación, imprimir por pantalla dicha cadena a modo de depuración.
					 * Después, usar la cadena para construir un objeto DirMessage que contenga en
					 * sus atributos los valores del mensaje (fromString).*/
					System.out.println(" Contents interpreted as " + packetFromClient.getLength() + "-byte String: \"" + messageFromClient + "\"");
					DirMessage dirMessageFromClient = DirMessage.fromString(messageFromClient);
					
					/* DONE: Llamar a buildResponseFromRequest para construir, a partir del objeto
					 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
					 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
					 * DirMessage de respuesta deben haber sido establecidos con los valores
					 * adecuados para los diferentes campos del mensaje (operation, etc.)*/
					DirMessage dirMessageToClient = buildResponseFromRequest(dirMessageFromClient, clientAddr);
					
					/* DONE: Convertir en string el objeto DirMessage con el mensaje de respuesta a
					 * enviar, extraer los bytes en que se codifica el string (getBytes), y
					 * finalmente enviarlos en un datagrama*/
					String messageToClient = dirMessageToClient.toString();
					byte[] dataToClient = messageToClient.getBytes();
					DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
					socket.send(packetToClient);
					
				}
				
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		/*
		 * DONE: Construir un DirMessage con la respuesta en función del tipo de mensaje
		 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
		 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
		 * files...)
		 */
		String operation = msg.getOperation();

		DirMessage response = null;

		switch (operation) {
			case DirMessageOps.OPERATION_LOGIN: {
				String username = msg.getNickname();
				/*
				 * DONE: Comprobamos si tenemos dicho usuario registrado (atributo "nicks"). Si
				 * no está, generamos su sessionKey (número aleatorio entre 0 y 1000) y añadimos
				 * el nick y su sessionKey asociada. NOTA: Puedes usar random.nextInt(10000)
				 * para generar la session key
				 */
				/*
				 * DONE: Construimos un mensaje de respuesta que indique el éxito/fracaso del
				 * login y contenga la sessionKey en caso de éxito, y lo devolvemos como
				 * resultado del método.
				 */
			
				if (nicks.containsKey(username)) {
					System.out.println("User " + username + " is already connected.");
					response = new DirMessage(DirMessageOps.OPERATION_LOGINFAIL);
					//response.setNickname(username); //comentar
					response.setSessionKey(DirMessageOps.SESSIONKEY_INVALID);
				}
			
				else {
					address.put(username, CL);
					int sessionKey = random.nextInt(10001);
					nicks.put(username, sessionKey);
					sessionKeys.put(sessionKey, username);
					response = new DirMessage(DirMessageOps.OPERATION_LOGINOK);
					//response.setNickname(username); //comentar
					response.setSessionKey(nicks.get(username));
				}
			
				/*
				 * DONE: Imprimimos por pantalla el resultado de procesar la petición recibida
				 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
				 * servidor
				 */
				System.out.print("operation:" + response.getOperation() +
						//"\nnickname:" + response.getNickname() + //comentar 
						"\nsessionKey:" + response.getSessionKey() + "\n\n");
				break;
			}
			
			case DirMessageOps.OPERATION_REGISTERED_USERS: {
				response = new DirMessage(DirMessageOps.OPERATION_REGISTERED_USERS_RESP);
				ArrayList<String> usuarios = new ArrayList<>();
				ArrayList<String> isPeerList = new ArrayList<>();
				for (Map.Entry<String, Integer> entry : nicks.entrySet()) {
					usuarios.add(entry.getKey());
					if(peers.containsKey(entry.getValue())) isPeerList.add("true");
					else isPeerList.add("false");
				}
				response.setUsersList(usuarios);
				response.setIsPeer(isPeerList);
				
				/*
				 * DONE: Imprimimos por pantalla el resultado de procesar la petición recibida
				 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
				 * servidor
				 */
				System.out.println("operation:" + response.getOperation() + "\n\n");
				break;
			}
			
			case DirMessageOps.OPERATION_LOGOUT: {
				
				if (sessionKeys.containsKey(msg.getSessionKey())) {
					nicks.remove(sessionKeys.remove(msg.getSessionKey()));
					response = new DirMessage(DirMessageOps.OPERATION_LOGOUTOK);
				}
				else {
					response = new DirMessage(DirMessageOps.OPERATION_LOGOUTFAIL);
				}
			
				/*
				 * DONE: Imprimimos por pantalla el resultado de procesar la petición recibida
				 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
				 * servidor
				 */
				System.out.print("operation:" + response.getOperation() + "\n\n");
				break;
			}
			
			case DirMessageOps.OPERATION_REGISTER_SERVER:{
				int sessionKey = msg.getSessionKey();
				int port = msg.getPort();
				
				String nick = sessionKeys.get(sessionKey);
				
				peers.put(sessionKey, port);
				response = new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER_OK);
				
				/*
				 * DONE: Imprimimos por pantalla el resultado de procesar la petición recibida
				 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
				 * servidor
				 */
				System.out.print("operation:" + response.getOperation() + nick + "\n\n");
				break;
			}
			
			case DirMessageOps.OPERATION_STOP_SERVER: {
				if (peers.containsKey(msg.getSessionKey())) {
					peers.remove(msg.getSessionKey());
					published.remove(msg.getSessionKey());			
					response = new DirMessage(DirMessageOps.OPERATION_STOP_SERVER_OK);
				}
				else response = new DirMessage(DirMessageOps.OPERATION_STOP_SERVER_FAIL);
			
				/*
				 * DONE: Imprimimos por pantalla el resultado de procesar la petición recibida
				 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
				 * servidor
				 */
				System.out.print("operation:" + response.getOperation() + "\n\n");
				break;
			}
			
			case DirMessageOps.OPERATION_GETADDR_FROM_NICK:{
				String nick = msg.getNickname();
				try{
					int sessionKey = nicks.get(nick);
					
						String ip = address.get(nick);
						int port = peers.get(sessionKey);
						
						response = new DirMessage(DirMessageOps.OPERATION_GETADDR_RESP);
						response.setIP(ip);
						response.setPort(port);
				}catch (Exception e) {
					response = new DirMessage(DirMessageOps.OPERATION_GETADDR_FAIL);
				}
				
				
				/*
				 * DONE: Imprimimos por pantalla el resultado de procesar la petición recibida
				 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
				 * servidor
				 */
				System.out.print("operation:" + response.getOperation() + "\n\n");
				break;
			}
			
			case DirMessageOps.OPERATION_PUBLISH:{
				int sessionKey = msg.getSessionKey();
//				int numFiles = msg.getNumFiles();
				
				FileInfo[] files = msg.getPublishedFiles();
				if (files==null) response = new DirMessage(DirMessageOps.OPERATION_PUBLISH_FAIL);
				else {
					for (FileInfo file : files) {
						published.computeIfAbsent(sessionKey, k -> new ArrayList<>()).add(file);
						searched.computeIfAbsent(file.getHash().toLowerCase(), k -> new ArrayList<>()).add(sessionKeys.get(sessionKey));
					}

					FileInfo.printToSysout(files);

					response = new DirMessage(DirMessageOps.OPERATION_PUBLISH_OK);
				}
				
				/*
				 * DONE: Imprimimos por pantalla el resultado de procesar la petición recibida
				 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
				 * servidor
				 */
				System.out.print("operation:" + response.getOperation() + "\n\n");
				break;
			}

			case DirMessageOps.OPERATION_GET_FILE_LIST:{
				if (published.isEmpty()) response = new DirMessage(DirMessageOps.OPERATION_FILE_LIST_FAIL);
				else {
					String totalFiles = "";
					int numFiles = 0;
					for (Map.Entry<Integer, List<FileInfo>> entryFiles : published.entrySet()) {
						for (FileInfo entryFile : entryFiles.getValue()) {
							totalFiles = totalFiles + entryFile.toFileList(sessionKeys.get(entryFiles.getKey())) + ",";
							numFiles++;
						}
					}
					System.out.println(totalFiles);
					response = new DirMessage(DirMessageOps.OPERATION_FILE_LIST_RESP);
					response.setFilesInString(totalFiles);
					response.setNumFiles(numFiles);
				}
				
				
				/*
				 * DONE: Imprimimos por pantalla el resultado de procesar la petición recibida
				 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
				 * servidor
				 */
				System.out.print("operation:" + response.getOperation() + "\n\n");
				break;
			}
			
			case DirMessageOps.OPERATION_GET_SEARCHED:{
				String fileHash = msg.getFileHash();
				ArrayList<String> servers = null;
				
				for (String key : searched.keySet()) {
					if (key.contains(fileHash)) {
						servers = (ArrayList<String>) searched.get(key);
						response = new DirMessage(DirMessageOps.OPERATION_SEARCHED_RESP);
		        response.setFileServer(servers);
						break;
					}
				}
		    if (servers == null) response = new DirMessage(DirMessageOps.OPERATION_SEARCHED_FAIL);
				
				/*
				 * DONE: Imprimimos por pantalla el resultado de procesar la petición recibida
				 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
				 * servidor
				 */
				System.out.print("operation:" + response.getOperation() + "\n\n");
				break;
			}

			default:
				System.out.println("Unexpected message operation: \"" + operation + "\"");
			}
		return response;

	}
}
