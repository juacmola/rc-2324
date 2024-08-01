package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;

public class DirMessageOps {

	/*
	 * TODO: Añadir aquí todas las constantes que definen los diferentes tipos de
	 * mensajes del protocolo de comunicación con el directorio.
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	
	public static final String OPERATION_LOGIN = "login";
	public static final String OPERATION_LOGINOK = "loginOK";
	public static final String OPERATION_LOGINFAIL = "loginFAIL";
	
	public static final String OPERATION_LOGOUT = "logout";
	public static final String OPERATION_LOGOUTOK = "logoutOK";
	public static final String OPERATION_LOGOUTFAIL = "logoutFAIL";
	
	public static final String OPERATION_REGISTERED_USERS = "registeredUsers";
	public static final String OPERATION_REGISTERED_USERS_RESP = "registeredUsersResp";
	
	public static final String OPERATION_REGISTER_SERVER = "registerServer";
	public static final String OPERATION_REGISTER_SERVER_OK = "registerServerOK";
	public static final String OPERATION_STOP_SERVER = "stopServer";
	public static final String OPERATION_STOP_SERVER_OK = "stopServerOK";
	public static final String OPERATION_STOP_SERVER_FAIL = "stopServerFAIL";
	
	public static final String OPERATION_GETADDR_FROM_NICK = "getAddrFromNick";
	public static final String OPERATION_GETADDR_RESP = "getAddrResp";
	public static final String OPERATION_GETADDR_FAIL = "getAddrFAIL";
	
	public static final String OPERATION_PUBLISH = "publish";
	public static final String OPERATION_PUBLISH_OK = "publishOK";
	public static final String OPERATION_PUBLISH_FAIL = "publishFAIL";
	
	public static final String OPERATION_GET_FILE_LIST = "getFileList";
	public static final String OPERATION_FILE_LIST_RESP = "getFileListResp";
	public static final String OPERATION_FILE_LIST_FAIL = "getFileListFAIL";
	
	public static final String OPERATION_GET_SEARCHED = "getSearched";
	public static final String OPERATION_SEARCHED_RESP = "getSearchedResp";
	public static final String OPERATION_SEARCHED_FAIL = "getSearchedFAIL";
	
	public static final String NICKNAME_INVALID = "invalid_nickname";
	public static final int PORT_INVALID = -1;
	public static final int SESSIONKEY_INVALID = -1;
	public static final String IP_INVALID = null;
	public static final String HASH_INVALID = null;
	public static final String FILES_IN_STRING_INVALID = null;

}
