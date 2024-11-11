package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

	public static final byte OPCODE_INVALID_OPCODE = 0;
	public static final byte OPCODE_DOWNLOAD_FROM = 1;
	//public static final byte OPCODE_DOWNLOAD_OK = 2;
	public static final byte OPCODE_DOWNLOAD_FROM_RESP = 2;
	public static final byte OPCODE_DOWNLOAD_FROM_RESP_HS = 3;
	//public static final byte OPCODE_FILE_NOT_FOUND = 3;
	public static final byte OPCODE_DOWNLOAD_FROM_WHICH = 4;
	//public static final byte OPCODE_AMBIGUOUS_HASH = 4;
	public static final byte OPCODE_DOWNLOAD_FROM_FAIL = 5;
	//public static final byte OPCODE_END_OF_FILE = 5;
	public static final byte OPCODE_TEST = 9;
	

	/**
	 * TODO: Definir constantes con nuevos opcodes de mensajes
	 * definidos, añadirlos al array "valid_opcodes" y añadir su
	 * representación textual a "valid_operations_str" en el mismo orden
	 */
	private static final Byte[] _valid_opcodes = {
			OPCODE_INVALID_OPCODE,
			OPCODE_DOWNLOAD_FROM,
			//OPCODE_DOWNLOAD_OK,
			OPCODE_DOWNLOAD_FROM_RESP,
			OPCODE_DOWNLOAD_FROM_RESP_HS,
			//OPCODE_FILE_NOT_FOUND,
			OPCODE_DOWNLOAD_FROM_WHICH,
			//OPCODE_AMBIGUOUS_HASH,
			OPCODE_DOWNLOAD_FROM_FAIL,
			//OPCODE_END_OF_FILE,
			OPCODE_TEST,
			
			};
	private static final String[] _valid_operations_str = {
			"INVALID_OPCODE",
			"DOWNLOAD_FROM",
			//"DOWNLOAD_OK",
			"DOWNLOAD_FROM_RESP",
			"DOWNLOAD_FROM_RESP_HS",
			//"FILE_NOT_FOUND",
			"DOWNLOAD_FROM_WHICH",
			//"AMBIGUOUS_HASH",
			"DOWNLOAD_FROM_FAIL",
			//"END_OF_FILE",
			"TEST"
			};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}
	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	public static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OPCODE_INVALID_OPCODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
