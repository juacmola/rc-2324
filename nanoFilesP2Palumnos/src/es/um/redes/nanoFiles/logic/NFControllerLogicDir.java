package es.um.redes.nanoFiles.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicDir {

	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;

	/**
	 * Método para comprobar que la comunicación con el directorio es exitosa (se
	 * pueden enviar y recibir datagramas) haciendo uso de la clase
	 * DirectoryConnector
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	protected void testCommunicationWithDirectory(String directoryHostname) throws IOException {
		assert (NanoFiles.testMode);
		System.out.println("[testMode] Testing communication with directory...");
		/*
		 * Crea un objeto DirectoryConnector a partir del parámetro directoryHostname y
		 * lo utiliza para hacer una prueba de comunicación con el directorio.
		 */
		DirectoryConnector directoryConnector = new DirectoryConnector(directoryHostname);
		if (directoryConnector.testSendAndReceive()) {
			System.out.println("[testMode] Test PASSED!");
		} else {
			System.err.println("[testMode] Test FAILED!");
		}
	}

	/**
	 * Método para conectar con el directorio y obtener la "sessionKey" que se
	 * deberá utilizar en lo sucesivo para identificar a este cliente ante el
	 * directorio
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	protected boolean doLogin(String directoryHostname, String nickname) throws IOException{
		/* DONE: Debe crear un objeto DirectoryConnector a partir del parámetro
		 * directoryHostname y guardarlo en el atributo correspondiente para que pueda
		 * ser utilizado por el resto de métodos de esta clase. A continuación,
		 * utilizarlo para comunicarse con el directorio y tratar de realizar el
		 * "login", informar por pantalla del éxito/fracaso e imprimir la clave de
		 * sesión asignada por el directorio. Devolver éxito/fracaso de la operación.*/
		boolean result = false;
		
		try{
			this.directoryConnector = new DirectoryConnector(directoryHostname); 
		}catch (Exception e) {
			System.err.println("The host you wrote is not known");
			return result=false;
		}
		
		result = directoryConnector.logIntoDirectory(nickname);
		
		if (result)
			System.out.println("Login was successful with " + directoryConnector.getSessionKey() + " key");
		else System.err.println("Login was unsuccessful");
		
		return result;
	}

	/**
	 * Método para desconectarse del directorio: cerrar sesión y dar de baja el
	 * nombre de usuario registrado
	 */
	public boolean doLogout() {
		/*
		 * DONE: Comunicarse con el directorio (a través del directoryConnector) para
		 * dar de baja a este usuario. Se debe enviar la clave de sesión para
		 * identificarse. Devolver éxito/fracaso de la operación.
		 */
		boolean result = false;
		result = directoryConnector.logoutFromDirectory();
		
		if (result) System.out.println("Logout was successful");
		else System.err.println("Logout was unsuccessful");

		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de nicks registrados en el directorio
	 */
	protected boolean getAndPrintUserList() {
		/*
		 * DONE: Obtener la lista de usuarios registrados. Comunicarse con el directorio
		 * (a través del directoryConnector) para obtener la lista de nicks registrados
		 * e imprimirla por pantalla. Devolver éxito/fracaso de la operación.
		 */
		boolean result = false;
		ArrayList<String> userList = null;
		try{
			userList = directoryConnector.getUserList();
//			for (int i=0; i< userList.size(); i++) {
//				System.out.println("user:" + userList.get(i));
//				System.out.println("peer:" + isPeerList.get(i));
//			}
			result = true;
			
		}catch (NullPointerException e) {
			System.err.println("There are no users using NanoFiles."); 
			result = false;
		}
		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de ficheros que los peer servidores
	 * han publicado al directorio
	 */
	protected boolean getAndPrintFileList() {
		/*
		 * DONE: Obtener la lista de ficheros servidos. Comunicarse con el directorio (a
		 * través del directoryConnector) para obtener la lista de ficheros e imprimirla
		 * por pantalla (método FileInfo.printToSysout). Devolver éxito/fracaso de la
		 * operación.
		 */
		boolean result = false;
		FileInfo[] files = null;		
		
		files = directoryConnector.getFileList();
		if (files != null){
			FileInfo.printToSysoutFileList(files);
			result = true;
		}else {
			System.out.println("There are no files published."); 
			result = false;
		}
		
		return result;
//		try {
//			files = directoryConnector.getFileList();
////			FileInfo.printToSysout(files);
//			return result = true;
//		}catch (NullPointerException e) {
//				System.out.println("There are no files published."); 
//				return result = false;
//		}
	}

	/**
	 * Método para registrarse en el directorio como servidor de ficheros en un
	 * puerto determinado
	 * 
	 * @param serverPort el puerto en el que está escuchando nuestro servidor de
	 *                   ficheros
	 */

	public boolean registerFileServer(int serverPort) {
		/*DONE: Darse de alta en el directorio como servidor. Comunicarse con el
		 * directorio (a través del directoryConnector) para enviar el número de puerto
		 * TCP en el que escucha el servidor de ficheros que habremos arrancado
		 * previamente. Se debe enviar la clave de sesión para identificarse. Devolver
		 * éxito/fracaso de la operación.
		 */
		boolean result = false;

		if (directoryConnector.registerServerPort(serverPort)) result = true;
		return result;
	}

	/**
	 * Método para enviar al directorio la lista de ficheros que este peer servidor
	 * comparte con el resto (ver método filelist).
	 * 
	 */
	protected boolean publishLocalFiles() {
		/*
		 * DONE: Comunicarse con el directorio (a través del directoryConnector) para
		 * enviar la lista de ficheros servidos por este peer. Los ficheros de la
		 * carpeta local compartida están disponibles en NanoFiles.db). Se debe enviar
		 * la clave de sesión para identificarse. Devolver éxito/fracaso de la
		 * operación.
		 */
		boolean result = false;

		FileInfo[] files = NanoFiles.db.getFiles();
		if (files.length == 0) System.err.println("No files found in folder nf-shared");
		else {
			result = directoryConnector.publishLocalFiles(files);
			
			if (result) System.out.println("The files were published");
			else System.err.println("Could not publish your files");
		}
		
		return result;
	}

	/**
	 * Método para consultar al directorio el nick de un peer servidor y obtener
	 * como respuesta la dirección de socket IP:puerto asociada a dicho servidor
	 * 
	 * @param nickname el nick del servidor por cuya IP:puerto se pregunta
	 * @return La dirección de socket del servidor identificado por dich nick, o
	 *         null si no se encuentra ningún usuario con ese nick que esté
	 *         sirviendo ficheros.
	 */
	private InetSocketAddress lookupServerAddrByUsername(String nickname) {
		InetSocketAddress serverAddr = null;
		/*DONE: Obtener IP:puerto de un servidor de ficheros a partir de su nickname.
		 * Comunicarse con el directorio (a través del directoryConnector) para
		 * preguntar la dirección de socket en la que el usuario con 'nickname' está
		 * sirviendo ficheros. Si la operación fracasa (no se obtiene una respuesta con
		 * IP:puerto válidos), se debe devolver null.
		 */
		try {
			serverAddr = directoryConnector.lookupServerAddrByUsername(nickname);
		} catch (UnknownHostException e) {
			System.err.println("There was an error finding peer");
			e.printStackTrace();
		}

		return serverAddr;
	}

	/**
	 * Método para obtener la dirección de socket asociada a un servidor a partir de
	 * una cadena de caracteres que contenga: i) el nick del servidor, o ii)
	 * directamente una IP:puerto.
	 * 
	 * @param serverNicknameOrSocketAddr El nick o IP:puerto del servidor por el que
	 *                                   preguntamos
	 * @return La dirección de socket del peer identificado por dicho nick, o null
	 *         si no se encuentra ningún peer con ese nick.
	 * @throws IOException 
	 */
	public InetSocketAddress getServerAddress(String serverNicknameOrSocketAddr) throws IOException {
		InetSocketAddress fserverAddr = null;
		InetAddress serverIP = null;
		/* DONE: Averiguar si el nickname es en realidad una cadena "IP:puerto", en cuyo
		 * caso no es necesario comunicarse con el directorio (simplemente se devuelve
		 * un InetSocketAddress); en otro caso, utilizar el método
		 * lookupServerAddrByUsername de esta clase para comunicarse con el directorio y
		 * obtener la IP:puerto del servidor con dicho nickname. Devolver null si la
		 * operación fracasa. */
		if (serverNicknameOrSocketAddr.contains(":")) { // Then it has to be a socket address (IP:port)
			/* DONE: Extraer la dirección IP y el puerto de la cadena y devolver un
			 * InetSocketAddress. Para convertir un string con la IP a un objeto InetAddress
			 * se debe usar InetAddress.getByName()*/
			int idx = serverNicknameOrSocketAddr.indexOf(":"); // Posición del delimitador
			
			String address = serverNicknameOrSocketAddr.substring(0, idx);
			try{ serverIP = InetAddress.getByName(address);
			}catch (Exception e) {
				System.err.println("The host you wrote is not known");
				return fserverAddr;
			}
			
			String serverPort = serverNicknameOrSocketAddr.substring(idx + 1).trim();
			fserverAddr = new InetSocketAddress(serverIP, Integer.parseInt(serverPort));
		} else {
			/* DONE: Si es un nickname, preguntar al directorio la IP:puerto asociada a
			 * dicho peer servidor. */
			fserverAddr = lookupServerAddrByUsername(serverNicknameOrSocketAddr);
		}
		return fserverAddr;
	}

	/**
	 * Método para consultar al directorio los nicknames de los servidores que
	 * tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 */
	public boolean getAndPrintServersNicknamesSharingThisFile(String fileHashSubstring) {
		/* DONE: Comunicarse con el directorio (a través del directoryConnector) para
		 * preguntar por aquellos servidores que están sirviendo un determinado fichero,
		 * y obtener una lista con sus nicknames. Devolver éxito/fracaso de la
		 * operación.
		 */
		boolean result = false;
		String[] servers= null;		
		
		servers = directoryConnector.getServerNicknamesSharingThisFile(fileHashSubstring);
		if (servers != null){
			System.out.print("Lista de servidores que comparten dicho hash: ");
			System.out.println(servers[servers.length-1] + ".");
//			for (String str : servers) {
//        System.out.println(str + ", ");
//			}
			result = true;
		}else {
			System.out.println("This file was nos published yet"); 
			result = false;
		}

		return result;
	}

	/**
	 * Método para consultar al directorio las direcciones de socket de los
	 * servidores que tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 * @return Una lista de direcciones de socket de los servidores que comparten
	 *         dicho fichero, o null si dicha subcadena del hash no identifica
	 *         ningún fichero concreto (no existe o es una subcadena ambigua)
	 * 
	 */
	public LinkedList<InetSocketAddress> getServerAddressesSharingThisFile(String downloadTargetFileHash) {
		LinkedList<InetSocketAddress> serverAddressList = null;
		/*
		 * TODO: Comunicarse con el directorio (a través del directoryConnector) para
		 * preguntar por aquellos servidores que están sirviendo un determinado fichero,
		 * y obtener una lista con sus nicknames (método
		 * getServerNicknamesSharingThisFile). A continuación, obtener la dirección de
		 * socket de cada servidor a partir de su nickname (método getServerAddress), y
		 * devolver una lista con dichas direcciones. Devolver null si la operación
		 * fracasa.
		 * 
		 */




		return serverAddressList;
	}

	/**
	 * Método para dar de baja a nuestro servidor de ficheros en el directorio.
	 * 
	 * @return Éxito o fracaso de la operación
	 */
	public boolean unregisterFileServer() {
		/*DONE: Comunicarse con el directorio (a través del directoryConnector) para
		 * darse de baja como servidor de ficheros. Se debe enviar la clave de sesión
		 * para identificarse.
		 */
		boolean result = false;

		result = directoryConnector.stopBackGroundServer();
		
		if (result) System.out.println("The background server was stopped");
		else System.err.println("Could not stop background server");

		return result;
	}

	protected InetSocketAddress getDirectoryAddress() {
		return directoryConnector.getDirectoryAddress();
	}

}
