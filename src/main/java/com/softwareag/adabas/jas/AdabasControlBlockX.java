package com.softwareag.adabas.jas;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.Logger;

/**
 * Class implementing Adabas eXtended control black for direct calls.
 *
 * @author usadva
 */

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

/*
 * Thanks to Thorsten Knieling whose original JDC package provided many of the
 * ideas used to create JAS.
 */

public class AdabasControlBlockX {

	protected 	byte[]		acbxBytes;					// whole ACBX as a byte array
	protected 	ByteBuffer 	acbxBB;						// whole ACBX as contiguous byte buffer
	private 	byte		acbxTYPE;					// call type						offset x00	length 1
//	private 	byte		acbxReserved;				// reserved							offset x01	length 1
//	private		int			acbxVER;					// version							offset x02	length 2
	private		byte[]		acbxVER = new byte[2];		// version							offset x02	length 2
	private		short		acbxLEN;					// length							offset x04	length 2
	private 	String		acbxCMD;            		// command code			    		offset x06	length 2
//	private 	short		acbxRSV2;					// reserved 2						offset x08	length 2
	private 	short		acbxRSP;					// response code					offset x0A	length 2
	private 	byte[]		acbxCID = new byte[4];		// command ID						offset x0C	length 4
	private 	int			acbxDBID;					// database ID						offset x10	length 4
	private 	int			acbxFNR;					// file number						offset x14	length 4
	private 	long		acbxISN;					// ISN 								offset x18	length 8
	private 	long		acbxISL;					// ISN lower limit 					offset x20	length 8
	private 	long		acbxISQ;					// ISN quantity 					offset x28	length 8
	private 	byte		acbxCOP1;					// command option 1					offset x30	length 1
	private 	byte		acbxCOP2;					// command option 2					offset x31	length 1
	private 	byte		acbxCOP3;					// command option 3					offset x32	length 1
	private 	byte		acbxCOP4;					// command option 4					offset x33	length 1
	private 	byte		acbxCOP5;					// command option 5					offset x34	length 1
	private 	byte		acbxCOP6;					// command option 6					offset x35	length 1
	private 	byte		acbxCOP7;					// command option 7					offset x36	length 1
	private 	byte		acbxCOP8;					// command option 8					offset x37	length 1
	private 	byte[]		acbxADD1 = new byte[8];		// additions 1						offset x38	length 8
	private 	byte[]		acbxADD2 = new byte[4];		// additions 2						offset x40	length 4
	private 	byte[]		acbxADD3 = new byte[8];		// additions 3						offset x44	length 8
	private 	byte[]		acbxADD4 = new byte[8];		// additions 4						offset x4C	length 8
	private 	byte[]		acbxADD5 = new byte[8];		// additions 5						offset x54	length 8
	private 	byte[]		acbxADD6 = new byte[8];		// additions 6						offset x5C	length 8
//	private 	int			acbxRSV3;					// reserved 3						offset x64	length 4
	private		long		acbxERRA;					// error offset in buffer			offset x68	length 8
	private		String		acbxERRB;					// error character field			offset x70	length 2
	private		short		acbxERRC;					// error subcode					offset x72	length 2
	private		byte		acbxERRD;					// error buffer ID					offset x74	length 1
//	private		byte		acbxERRE;					// reserved							offset x75	length 1
	private		short		acbxERRF;					// error buffer sequence number		offset x76	length 2
	private		short		acbxSUBR;					// subcomponent response code		offset x78	length 2
	private		short		acbxSUBS;					// subcomponent response subcode	offset x7A	length 2 
	private		String		acbxSUBT;					// subcomponent error text		    offset x7C	length 4
	private		long		acbxLCMP;					// compressed   record length		offset x80	length 8
	private		long		acbxLDEC;					// decompressed record length		offset x88	length 8
	private		long		acbxCMDT;					// command time						offset x90	length 8
	private		byte[]		acbxUSER = new byte[16];	// user area						offset x98	length 16
//  private		byte[]		acbxRSV4;					// reserved 4						offset xA8	length 24	

	public static final short ACBX_LENGTH 	= 192;		// length of ACBX as a byte array / buffer
	
	private		String		hostName;					// host name of target DBID			NOT part of ACBX byte buffer
	private 	boolean		ebcdic			= false;	// encoding flag
	private		String		encoding;

	final static Logger logger = AdabasTrace.getLogger("com.softwareag.adabas.jas.AdabasControlBlockX");

	private static byte		eSpace			= (byte) 0x40;									// EBCDIC space
	private static byte[]	eVER			= new byte[] {(byte) 0xC6, (byte) 0xF2};		// EBCDIC version 0xC6F2 "F2"
	private static byte[]	aVER			= new byte[] {0x46, 0x32};						// ASCII  version 0x4632 "F2"

	/*
	 * The following array of nulls is used for padding binary byte arrays. 
	 * Newly constructed arrays are initialized to nulls by default.
	 */
	private byte[]          bArrayZeroes = new byte[16];   
	
	/*
	 * The following array of spaces is used for padding alphanumeric byte arrays 
	 */
	private byte[] 			bArraySpaces = new String("                ").getBytes();		// 16 spaces  

	/**
	 * Constructor.
	 * 
	 * @throws	AdabasException		Adabas specific exception.
	 */
	public AdabasControlBlockX() throws AdabasException {
		
		acbxBytes 	= new byte[ACBX_LENGTH];						// allocate byte array
		acbxBB 		= ByteBuffer.wrap(acbxBytes);					// wrap into byte buffer
		
		this.setAcbVER(aVER);										// set version to "F2" (0x4632) to indicate ACBX
		this.setAcbLEN(ACBX_LENGTH);								// set ACBX length
		
		this.ebcdic		= false;
		this.encoding	= "ISO-8859-1";
	}
	
	/**
	 * Constructor taking an EBCDIC encoding flag.
	 * 
	 * @param	e	EBCDIC encoding flag.
	 * 
	 * @throws	AdabasException		Adabas specific exception.
	 */
	public AdabasControlBlockX(boolean e) throws AdabasException {
		
		acbxBytes 	= new byte[ACBX_LENGTH];						// allocate byte array
		acbxBB 		= ByteBuffer.wrap(acbxBytes);					// wrap into byte buffer
		
		this.setAcbLEN(ACBX_LENGTH);								// set ACBX length
		
		this.ebcdic		= e;
		if (e == false) {
			this.setAcbVER(aVER);									// set version to "F2" (0x4632) to indicate ACBX
			this.encoding	= "ISO-8859-1";
		}
		else {
			this.setAcbVER(eVER);									// set version to EBCDIC "F2" (0xC6F2) to indicate ACBX
			this.encoding	= "cp037";
		}
	}
	
	/**
	 * Get whole ACBX as contiguous byte array.
	 *  
	 * @return ACBX byte array.
	 */
	public byte[] getACBArray() {
		
		return acbxBytes;											// return ACBX byte array			
	}
	
	/**
	 * Set whole ACBX byte array.
	 * 
	 * @param 	ba				Byte array containing new ACBX content.
	 * @throws 	AdabasException	Adabas specific exception. 
	 */
	public void setACBArray(byte[] ba) throws AdabasException {
		
		if (ba.length != acbxBytes.length) {
			throw new AdabasException(String.format("setACBArray() error! Input byte array length: %d not equal to acbxBytes length: %d!", ba.length, acbxBytes.length));
		}
		
		System.arraycopy(ba, 0x00, acbxBytes, 0x00, acbxBytes.length);
	}
	
	/**
	 * Get whole ACBX as contiguous byte buffer.
	 * 
	 * @return ACBX byte buffer.
	 */
	public ByteBuffer getACBBuffer() {
		
		return acbxBB;												// return ACBX byte buffer
	}
	
	/**
	 * Get the call type.
	 * 
	 * @return ACBX call type
	 */
	public byte getAcbTYPE() {
		
		this.acbxTYPE = acbxBB.get(0x00);							// call type is		offset x00	length 1
		return this.acbxTYPE;
	}
	
	/**
	 * Set the call type. When issuing an Adabas command, set this field to binary zeros. 
	 * This indicates that a logical user call is being made (ACBXTUSR equate).
	 * 
	 * @param callType ACBX call type
	 */
	public void setAcbTYPE(byte callType) {
		
		this.acbxTYPE = callType;
		acbxBB.put(0x00, callType);									// call type is		offset x00	length 1
	}
	
	/**
	 * Get the version indicator.
	 * 
	 * @return version indicator
	 */
	public byte[] getAcbVER() {
		
		return this.acbxVER;										// version is		offset x02	length 2
	}
	
	/**
	 * Set the version indicator. The version indicator identifies whether 
	 * the Adabas control block uses the new ACBX or the classic ACB format. 
	 * If this field is set to a value starting with the letter "F" 
	 * (for example "F2"), Adabas treats the Adabas control block as though 
	 * it is specified in the ACBX format. If this field is set to any other value, 
	 * Adabas treats the control block as though it is specified in the classic ACB format. 
	 *
	 * @param 	version 		Version indicator.
	 * @throws	AdabasException If version GT 65535.
	 */
	public void setAcbVER(byte[] version) throws AdabasException {

		System.arraycopy(version, 0x00, acbxVER, 	0x00, 2);
		System.arraycopy(version, 0x00, acbxBytes, 	0x02, 2);		// version is		offset x02	length 2
	}
	
	/**
	 * Get the control block length.
	 * 
	 * @return Control block length.
	 */
	public short getAcbLEN() {
		
		this.acbxLEN = acbxBB.getShort(0x04);						// length is		offset x04	length 2
		return this.acbxLEN;
	}
	
	/**
	 * Set the control block length. The ACBX length field should be 
	 * set to the length of the ACBX structure passed to Adabas 
	 * (the ACBXQLL equate, currently 192). 
	 * 
	 * @param 	length 			Control block length.
	 * @throws	AdabasException	If length length LT 0.
	 */
	public void setAcbLEN(short length) throws AdabasException {
		
		if (length < 0) {
			throw new AdabasException("ACBX length LT 0. Length = " + length);
		}

		this.acbxLEN = length;
		acbxBB.putShort(0x04, length);								// length is		offset x04	length 2
	}

	/**
	 * Get the command code.
	 * 
	 * @return the acbxCMD
	 */
	public String getAcbCMD() {
		
		try {
			this.acbxCMD = new String(acbxBytes, 0x06, 2, encoding);	// command code is	offset x06	length 2 		
		}
		catch (UnsupportedEncodingException e) {}
		return this.acbxCMD;
	}

	/**
	 * Set the command code.
	 * 
	 * @param 	acbxCMD			Command code to set.
	 *        					If acbxCMD is null or an empty string then it will be filled
	 *        					with spaces. If acbxCMD is less than 2 bytes, then
	 *        					the remaining byte will be filled with a space.  
	 * @throws	AdabasException  If acbxCMD length GT 2.
	 */
	public void setAcbCMD(String acbxCMD) throws AdabasException {
		
		// TODO validate content of command code
		
		if (acbxCMD == null || acbxCMD.isEmpty()) {
			try {
				this.acbxCMD = new String("  ".getBytes(encoding));
			} catch (UnsupportedEncodingException e) {}
		}
		else {
			int acbxCMDLen = acbxCMD.length();
			if (acbxCMDLen == 2) { 
				this.acbxCMD = acbxCMD;
			}
			else if (acbxCMDLen == 1) {
				try {
					this.acbxCMD = acbxCMD + new String(" ".getBytes(encoding));
				} catch (UnsupportedEncodingException e) {}
			}
			else {
				throw new AdabasException(acbxDBID, String.format("setAcbCMD(): acbxCMD length GT 2: acbxCMD length = %d", acbxCMDLen));
			}
		}
		
		byte[] cmd = new byte[2];
		try {
			cmd	= this.acbxCMD.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {}
		System.arraycopy(cmd, 0, acbxBytes, 0x06, 2);				// command code is	offset x02	length 2
	}

	/**
	 * Get command code as a byte array.
	 * 
	 * @return	Command code byte array.
	 */
	public byte[] getAcbBaCMD() {
		
		byte[] baCMD	= new byte[2];
		
		System.arraycopy(acbxBytes, 0x06, baCMD, 0x00, 2);						// command code is offset x06 length 2
		
		return baCMD;
	}
	
	/**
	 * Set the command code as a byte array.
	 * 
	 * @param 	baCMD 			The baCMD to set.
	 *        					If baCMD is null or 0 length then it will be filled
	 *        					with spaces. If baCMD is less than 2 bytes, then
	 *        					the remaining byte will be filled with a space.  
	 * @throws 	AdabasException	If baCMD length GT 2.
	 */
	public void setAcbBaCMD(byte[] baCMD) throws AdabasException {
		
		if (baCMD == null || baCMD.length == 0) {
			try {
				this.acbxCMD = new String("  ".getBytes(encoding));
			} catch (UnsupportedEncodingException e) {}
		}
		else {
			int baCMDLen = baCMD.length;
			if (baCMDLen == 1) {
				System.arraycopy(baCMD, 0x00, acbxBytes, 0x06, 1);				// command code is offset x06 length 2
				if (ebcdic == false) {
					acbxBytes[0x07] = ' ';
				}
				else {
					acbxBytes[0x07] = eSpace;
				}
			}
			else if (baCMDLen == 2) {
				System.arraycopy(baCMD, 0x00, acbxBytes, 0x06, 2);
			}
			else {
				throw new AdabasException(acbxDBID, String.format("setAcbCMD(): baCMD length GT 2: baCMD length = %d", baCMDLen));
			}
		}
		
		byte[]	newBaCMD	= new byte[2];
		System.arraycopy(acbxBytes, 0x06, newBaCMD, 0x00, 2);
		try {
			this.acbxCMD = new String(newBaCMD, encoding);
		} catch (UnsupportedEncodingException e) {}
	}

	/**
	 * Get the response code.
	 * 
	 * @return response code.
	 */
	public short getAcbRSP() {
		
		this.acbxRSP = acbxBB.getShort(0x0A);						// response code is	offset x0A	length 2
		return this.acbxRSP;
	}

	/**
	 * Set the response code.
	 * 
	 * @param acbxRSP response code to set.
	 */
	public void setAcbRSP(short acbxRSP) {
		
		this.acbxRSP = acbxRSP;
		acbxBB.putShort(0x0A, acbxRSP);								// response code is	offset x0A	length 2
	}

	/**
	 * Get the command ID.
	 * 
	 * @return the acbxCID as a byte array.
	 */
	public byte[] getAcbCID() {
		
		System.arraycopy(acbxBytes, 0x0C, acbxCID, 0, 4);           // command ID is	offset x0C	length 4
		return acbxCID;
	}

	/**
	 * Set the command ID.
	 * 
	 * @param 	acbxCID byte[]	The acbxCID to set.
	 *        					If acbxCID is null then it will be filled with nulls(0x00).  
	 *        					If acbxCID is less than 4 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbxCID length GT 4.
	 */
	public void setAcbCID(byte [] acbxCID) throws AdabasException {
		
		int acbxCIDLen = 0;
		
		if (acbxCID != null) {
			acbxCIDLen = acbxCID.length;
			if (acbxCIDLen > 4) {
				throw new AdabasException(acbxDBID, String.format("setAcbCID(): acbxCID length GT 4: acbxCID length = %d", acbxCIDLen));
			}
		}

		/*
		 * If the acbxCID has a length > 0
		 * 	Copy all the bytes in the byte array to this.acbxCID array starting at offset 0x00.
		 */
		if (acbxCIDLen > 0) {
			System.arraycopy(acbxCID, 0, this.acbxCID, 0x00, acbxCIDLen);	   		
		}
		
		/**
		 * If the acbxCID byte array was LT 4 bytes. 
		 * 	then the remaining bytes in this.acbxCID will be padded with zeroes. 
		 */
		if (acbxCIDLen < 4) {
			System.arraycopy(bArrayZeroes, 0, this.acbxCID, acbxCIDLen, 4-acbxCIDLen);
		}
	
		System.arraycopy(this.acbxCID, 0, acbxBytes, 0x0C, 4);	   	// command ID is	offset x0C	length 4	
	}

	/**
	 * Get the command ID as a String.
	 * 
	 * @return the acbxCID as a string.
	 */
	public String getAcbCIDString() {
		
		byte[]	acbCIDba = getAcbCID();								// get CID byte array
		return 	new String(acbCIDba);								// make new String from CID byte array
	}

	/**
	 * Set the command ID as a String.
	 * 
	 * @param 	acbxCID 		The acbxCID to set.
	 *        					If acbxCID is null or an empty string then it will be filled
	 *        					with spaces. If acbxCID is less than 4 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbxCID length GT 4.
	 */
	public void setAcbCID(String acbxCID) throws AdabasException {
		
		int acbxCIDLen = 0;
		
		if (acbxCID != null) {
			if (!acbxCID.isEmpty()) {
				acbxCIDLen = acbxCID.length();
				if (acbxCIDLen > 4) {
					throw new AdabasException(acbxDBID, String.format("setAcbCID(): acbxCID length GT 4: acbxCID length = %d", acbxCIDLen));
				}
			}
		}

		/*
		 * If the acbxCID has a length > 0
		 * 	Generate a byte array from the acbxCID string.
		 *  Copy all the bytes in the byte array to this.acbxCID array starting at offset 0x00.
		 */
		if (acbxCIDLen > 0) {
			byte[] cid = acbxCID.getBytes();								   
			System.arraycopy(cid, 0, this.acbxCID, 0x00, acbxCIDLen);	   		
		}
		
		/*
		 * If the acbxCID string generated byte array was LT 4 bytes. 
		 *  then the remaining bytes in this.acbxCID will be padded with spaces. 
		 */
		if (acbxCIDLen < 4) {
			System.arraycopy(bArraySpaces, 0, this.acbxCID, acbxCIDLen, 4-acbxCIDLen);
		}

		System.arraycopy(this.acbxCID, 0, acbxBytes, 0x0C, 4);	   	// command ID is	offset x0C	length 4	
	}
	
	/**
	 * Get the Database ID. DBID in the ACBX is a proper 4 byte 
	 * integer. Valid values are between 1 and 65535.
	 * 
	 * @return Database ID.
	 */
	public int getAcbDBID() {
		
		this.acbxDBID = acbxBB.getInt(0x10);						// DBID is			offset x10	length 4
		return this.acbxDBID;
	}
	
	/**
	 * Set the database ID. DBID in the ACBX is a proper 4 byte 
	 * integer. Valid values are between 1 and 65535.
	 * 
	 * @param 	acbxDBID 		Database ID (target) to set.
	 * @throws	AdabasException	If acbxDBID LT 1 or GT 65535.
	 */
	public void setAcbDBID(int acbxDBID) throws AdabasException {
		
		if (acbxDBID < 1 || acbxDBID > 65535) {
			throw new AdabasException(acbxDBID, "DBID LT 1 or GT 65535.");
		}
		
		this.acbxDBID = acbxDBID;
		acbxBB.putInt(0x10, acbxDBID);								// DBID is			offset x10	length 4
	}
	
	/**
	 * Get the file number.
	 * 
	 * @return file number.
	 */
	public int getAcbFNR() {
		
		this.acbxFNR = acbxBB.getInt(0x14);							// file number is	offset x14	length 4
		return this.acbxFNR;
	}

	/**
	 * Set the file number.
	 * 
	 * @param 	acbxFNR 		File number to set.
	 * @throws	AdabasException If acbxFNR LT 0 or GT 32000. 
	 */
	public void setAcbFNR(int acbxFNR) throws AdabasException {
		
		if (acbxFNR < 0 || acbxFNR > 32000) {
			throw new AdabasException(acbxDBID, String.format("file number = %d; FNR LT 0 or GT 32000.", acbxFNR));
		}

		this.acbxFNR = acbxFNR;
		acbxBB.putInt(0x14, acbxFNR);								// file number is	offset x14	length 4
	}

	/**
	 * Get the ISN.
	 * 
	 * @return ISN.
	 */
	public long getAcbISN() {
		
		this.acbxISN = acbxBB.getLong(0x18);						// ISN is 			offset x18	length 8
		return this.acbxISN;
	}

	/**
	 * Set the ISN.
	 *  
	 * @param 	acbxISN 		ISN to set.
	 */
	public void setAcbISN(long acbxISN) {
		
		this.acbxISN = acbxISN;
		acbxBB.putLong(0x18, acbxISN);								// ISN is 			offset x18	length 8
	}

	/**
	 * Get the ISN lower limit.
	 * 
	 * @return ISN lower limit.
	 */
	public long getAcbISL() {
		
		this.acbxISL = acbxBB.getLong(0x20);						// ISL is 			offset x20	length 8
		return this.acbxISL;
	}

	/**
	 * Set the ISN lower limit. The ACBXISL field is an eight-byte field, 
	 * which is not yet used, but only 4-byte values are allowed. 
	 * The high-order part of the ACBXISL field must contain binary zeros. 
	 * 
	 * @param 	acbxISL 		ISN lower limit to set.
	 * @throws	AdabasException	If acbxISL LT 0 or GT 4294967295.
	 */
	public void setAcbISL(long acbxISL) throws AdabasException {
		
		if (acbxISL < 0 || acbxISL > 4294967295L) {
			throw new AdabasException(acbxDBID, String.format("setAcbISL(): acbxISL LT 0 or GT 4294967295: acbxISL = %d", acbxISL));
		}
		
		this.acbxISL = acbxISL;
		acbxBB.putLong(0x20, acbxISL);								// ISL is 			offset x20	length 8
	}

	/**
	 * Get the ISN quantity.
	 * 
	 * @return ISN quantity.
	 */
	public long getAcbISQ() {
		
		this.acbxISQ = acbxBB.getLong(0x28);						// ISQ is 			offset x28	length 8
		return this.acbxISQ;
	}

	/**
	 * Set the ISN quantity. The ACBXISQ field is an eight-byte field, 
	 * which is not yet used, but only 4-byte values are allowed. 
	 * The high-order part of the ACBXISQ field must contain binary zeros. 
	 * 
	 * @param 	acbxISQ 		ISN quantity to set.
 	 * @throws	AdabasException	If acbxISQ LT 0 or GT 4294967295.
	 */
	public void setAcbISQ(long acbxISQ) throws AdabasException {
		
		if (acbxISQ < 0 || acbxISQ > 4294967295L) {
			throw new AdabasException(acbxDBID, String.format("setAcbISQ(): acbxISQ LT 0 or GT 4294967295: acbxISQ = %d", acbxISQ));
		}
	
		this.acbxISQ = acbxISQ;
		acbxBB.putLong(0x28, acbxISQ);								// ISQ is 			offset x28	length 8
	}

	/**
	 * Get command options 1.
	 * 
	 * @return command options 1.
	 */
	public byte getAcbCOP1() {
		
		this.acbxCOP1 = acbxBB.get(0x30);							// COP1 is			offset x30	length 1
		return acbxCOP1;
	}

	/**
	 * Set command options 1.
	 * 
	 * @param acbxCOP1 command options 1 to set.
	 */
	public void setAcbCOP1(byte acbxCOP1) {
		
		this.acbxCOP1 = acbxCOP1;
		acbxBB.put(0x30, acbxCOP1);									// COP1 is			offset x30	length 1
	}

	/**
	 * Get command options 2.
	 * 
	 * @return command options 2.
	 */
	public byte getAcbCOP2() {
		
		this.acbxCOP2 = acbxBB.get(0x31);							// COP2 is			offset x31	length 1
		return acbxCOP2;
	}

	/**
	 * Set command options 2.
	 * 
	 * @param acbxCOP2 command options 2 to set.
	 */
	public void setAcbCOP2(byte acbxCOP2) {
		
		this.acbxCOP2 = acbxCOP2;
		acbxBB.put(0x31, acbxCOP2);									// COP2 is			offset x31	length 1
	}

	/**
	 * Get command options 3.
	 * 
	 * @return command options 3.
	 */
	public byte getAcbCOP3() {
		
		this.acbxCOP3 = acbxBB.get(0x32);							// COP3 is			offset x32	length 1
		return acbxCOP3;
	}

	/**
	 * Set command options 3.
	 * 
	 * @param acbxCOP3 command options 3 to set.
	 */
	public void setAcbCOP3(byte acbxCOP3) {
		
		this.acbxCOP3 = acbxCOP3;
		acbxBB.put(0x32, acbxCOP3);									// COP3 is			offset x32	length 1
	}

	/**
	 * Get command options 4.
	 * 
	 * @return command options 4.
	 */
	public byte getAcbCOP4() {
		
		this.acbxCOP4 = acbxBB.get(0x33);							// COP4 is			offset x33	length 1
		return acbxCOP4;
	}

	/**
	 * Set command options 4.
	 * 
	 * @param acbxCOP4 command options 4 to set.
	 */
	public void setAcbCOP4(byte acbxCOP4) {
		
		this.acbxCOP4 = acbxCOP4;
		acbxBB.put(0x33, acbxCOP4);									// COP4 is			offset x33	length 1
	}

	/**
	 * Get command options 5.
	 * 
	 * @return command options 5.
	 */
	public byte getAcbCOP5() {
		
		this.acbxCOP5 = acbxBB.get(0x34);							// COP5 is			offset x34	length 1
		return acbxCOP5;
	}

	/**
	 * Set command options 5.
	 * 
	 * @param acbxCOP5 command options 5 to set.
	 */
	public void setAcbCOP5(byte acbxCOP5) {
		
		this.acbxCOP5 = acbxCOP5;
		acbxBB.put(0x34, acbxCOP5);									// COP5 is			offset x34	length 1
	}

	/**
	 * Get command options 6.
	 * 
	 * @return command options 6.
	 */
	public byte getAcbCOP6() {
		
		this.acbxCOP6 = acbxBB.get(0x35);							// COP6 is			offset x35	length 1
		return acbxCOP6;
	}

	/**
	 * Set command options 6.
	 * 
	 * @param acbxCOP6 command options 6 to set.
	 */
	public void setAcbCOP6(byte acbxCOP6) {
		
		this.acbxCOP6 = acbxCOP6;
		acbxBB.put(0x35, acbxCOP6);									// COP6 is			offset x35	length 1
	}

	/**
	 * Get command options 7.
	 * 
	 * @return command options 7.
	 */
	public byte getAcbCOP7() {
		
		this.acbxCOP7 = acbxBB.get(0x36);							// COP7 is			offset x36	length 1
		return acbxCOP7;
	}

	/**
	 * Set command options 7.
	 * 
	 * @param acbxCOP7 command options 7 to set.
	 */
	public void setAcbCOP7(byte acbxCOP7) {
		
		this.acbxCOP7 = acbxCOP7;
		acbxBB.put(0x36, acbxCOP7);									// COP7 is			offset x36	length 1
	}

	/**
	 * Get command options 8.
	 * 
	 * @return command options 8.
	 */
	public byte getAcbCOP8() {
		
		this.acbxCOP8 = acbxBB.get(0x37);							// COP8 is			offset x37	length 1
		return acbxCOP8;
	}

	/**
	 * Set command options 8.
	 * 
	 * @param acbxCOP8 command options 8 to set.
	 */
	public void setAcbCOP8(byte acbxCOP8) {
		
		this.acbxCOP8 = acbxCOP8;
		acbxBB.put(0x37, acbxCOP8);									// COP8 is			offset x37	length 1
	}

	/**
	 * Get additions 1.
	 * 
	 * @return acbxADD1 as a byte array.
	 */
	public byte[] getAcbADD1() {
		
		System.arraycopy(acbxBytes, 0x38, this.acbxADD1, 0, 8);     // additions 1 is	offset x38	length 8
		return acbxADD1;
	}

	/**
	 * Set additions 1.
	 * 
	 * @param 	acbxADD1		Additions 1 to set.
	 *        					If acbxADD1 is null then it will be set to all nulls(0x00).
	 *        					If acbxADD1 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbxADD1 length GT 8.
	 */
	public void setAcbADD1(byte[] acbxADD1) throws AdabasException {

		int acbxADD1Len = 0;
		
		if (acbxADD1 != null) {
			acbxADD1Len = acbxADD1.length;
			if (acbxADD1Len > 8) {
				throw new AdabasException(acbxDBID, String.format("setAcbADD1(): acbxADD1 length GT 8: acbxADD1 length = %d", acbxADD1Len));
			}
		}

		/*
		 * If the acbxADD1 has a length > 0
		 * 	Copy all the bytes in the byte array to this.acbxADD1 array starting at offset 0x00.
		 */
		if (acbxADD1Len > 0) {
			System.arraycopy(acbxADD1, 0, this.acbxADD1, 0x00, acbxADD1Len);	   		
		}
		
		/*
		 * If the acbxADD1 byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD1 will be padded with zeroes. 
		 */
		if (acbxADD1Len < 8) {
			System.arraycopy(bArrayZeroes, 0, this.acbxADD1, acbxADD1Len, 8-acbxADD1Len);
		}
	
		System.arraycopy(this.acbxADD1, 0, acbxBytes, 0x38, 8);		// additions 1 is	offset x38	length 8
	}

	/**
	 * Get additions 1 as a String.
	 * 
	 * @return acbxADD1 as a String.
	 */
	public String getAcbADD1String() {
		
		byte[]	acbADD1ba = getAcbADD1();							// get ADD1 byte array
		return new String(acbADD1ba);								// make new String from ADD1 byte array
	}

	/**
	 * Set additions 1 as a String.
	 * 
	 * @param 	acbxADD1 		Additions 1 to set.
	 *        					If acbxADD1 is null or an empty string then it will be filled
	 *        					with spaces. If acbxADD1 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbxADD1 length GT 8.
	 */
	public void setAcbADD1(String acbxADD1) throws AdabasException {

		int acbxADD1Len = 0;
		
		if (acbxADD1 != null) {
			if (!acbxADD1.isEmpty()) {
				acbxADD1Len = acbxADD1.length();
				if (acbxADD1Len > 8) {
					throw new AdabasException(acbxDBID, String.format("setAcbADD1(): acbxADD1 length GT 8: acbxADD1 length = %d", acbxADD1Len));
				}
			}
		}

		/*
		 * If the acbxADD1 has a length > 0
		 * 	Generate a byte array from the acbxADD1 string.
		 *  Copy all the bytes in the byte array to this.acbxADD1 array starting at offset 0x00.
		 */
		if (acbxADD1Len > 0) {
			byte[] add1 = acbxADD1.getBytes();								   
			System.arraycopy(add1, 0, this.acbxADD1, 0x00, acbxADD1Len);	   		
		}
		
		/*
		 * If the acbxADD1 string generated byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD1 will be padded with spaces. 
		 */
		if (acbxADD1Len < 8) {
			System.arraycopy(bArraySpaces, 0, this.acbxADD1, acbxADD1Len, 8-acbxADD1Len);
		}
		
		System.arraycopy(this.acbxADD1, 0, acbxBytes, 0x38, 8);		// additions 1 is	offset x38	length 8
	}

	/**
	 * Get additions 2.
	 * 
	 * @return acbxADD2 as a byte array.
	 */
	public byte[] getAcbADD2() {
		
		System.arraycopy(acbxBytes, 0x40, this.acbxADD2, 0, 4);     // additions 2 is	offset x40	length 4
		return this.acbxADD2;
	}

	/**
	 * Set additions 2.
	 * 
	 * @param 	acbxADD2 		Additions 2 to set.
	 *        					If acbxADD2 is null then it will be set to all nulls(0x00).
	 *        					If acbxADD2 is less than 4 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbxADD2 length GT 4.
	 */
	public void setAcbADD2(byte[] acbxADD2) throws AdabasException {

		int acbxADD2Len = 0;
		
		if (acbxADD2 != null) {
			acbxADD2Len = acbxADD2.length;
			if (acbxADD2Len > 4) {
				throw new AdabasException(acbxDBID, String.format("setAcbADD2(): acbxADD2 length GT 4: acbxADD2 length = %d", acbxADD2Len));
			}
		}

		/*
		 * If the acbxADD2 has a length > 0
		 *  Copy all the bytes in the byte array to this.acbxADD2 array starting at offset 0x00.
		 */
		if (acbxADD2Len > 0) {
			System.arraycopy(acbxADD2, 0, this.acbxADD2, 0x00, acbxADD2Len);	   		
		}
		
		/*
		 * If the acbxADD2 byte array was LT 4 bytes. 
		 *  then the remaining bytes in this.acbxADD2 will be padded with zeroes. 
		 */
		if (acbxADD2Len < 4) {
			System.arraycopy(bArrayZeroes, 0, this.acbxADD2, acbxADD2Len, 4-acbxADD2Len);
		}
	
		System.arraycopy(this.acbxADD2, 0, acbxBytes, 0x40, 4);		// additions 2 is	offset x40	length 4
	}

	/**
	 * Get additions 2 as a String.
	 * 
	 * @return acbxADD2 as a String.
	 */
	public String getAcbADD2String() {
		
		byte[]	acbADD2ba = getAcbADD2();							// get ADD2 byte array
		return	new String(acbADD2ba);								// make new String from ADD2 byte arry
	}

	/**
	 * Set additions 2 as a String.
	 * 
	 * @param 	acbxADD2 		Additions 2 to set.
	 *        					If acbxADD2 is null or an empty string then it will be filled
	 *        					with spaces. If acbxADD2 is less than 4 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbxADD2 length GT 4.
	 */
	public void setAcbADD2(String acbxADD2) throws AdabasException {

		int acbxADD2Len = 0;
		
		if (acbxADD2 != null) {
			if (!acbxADD2.isEmpty()) {
				acbxADD2Len = acbxADD2.length();
				if (acbxADD2Len > 4) {
					throw new AdabasException(acbxDBID, String.format("setAcbADD2(): acbxADD2 length GT 4: acbxADD2 length = %d", acbxADD2Len));
				}
			}
		}

		/*
		 * If the acbxADD2 has a length > 0
		 *  Generate a byte array from the acbxADD2 string.
		 *  Copy all the bytes in the byte array to this.acbxADD2 array starting at offset 0x00.
		 */
		if (acbxADD2Len > 0) {
			byte[] add2 = acbxADD2.getBytes();								   
			System.arraycopy(add2, 0, this.acbxADD2, 0x00, acbxADD2Len);	   		
		}
		
		/*
		 * If the acbxADD2 string generated byte array was LT 4 bytes. 
		 *  then the remaining bytes in this.acbxADD2 will be padded with spaces. 
		 */
		if (acbxADD2Len < 4) {
			System.arraycopy(bArraySpaces, 0, this.acbxADD2, acbxADD2Len, 4-acbxADD2Len);
		}
		
		System.arraycopy(this.acbxADD2, 0, acbxBytes, 0x40, 4);		// additions 2 is	offset x40	length 4
	}
	
	/**
	 * Get additions 2 as a long.
	 * 
	 * @return acbxADD2 as a a long.
	 */
	public long getAcbADD2Long() {
		
		int 	iAcbxADD2 	= acbxBB.getInt(0x40);				// additions 2 is	offset x40	length 4
		long	lAcbxADD2	= iAcbxADD2 & 0x00000000FFFFFFFFL;		// convert to long correcting for sign
		return	lAcbxADD2;
	}
	

	/**
	 * Set additions 2 as a long.
	 * 
	 * @param	acbxADD2		Additions 2 to set.
	 * @throws 	AdabasException	If acbxADD2 LT 0 or GT 4294967295.
	 */
	public void setAcbADD2(long acbxADD2) throws AdabasException {
		
		if (acbxADD2 < 0 || acbxADD2 > 4294967295L) {
			throw new AdabasException(acbxDBID, String.format("setAcbADD2(): acbxADD2 LT 0 or GT 4294967295: acbxADD2 = %d", acbxADD2));
		}
		
		int iAcbxADD2 = (int) (acbxADD2 & 0x00000000FFFFFFFFL);		// capture lower 4 bytes of input ADD2
		acbxBB.putInt(0x40, iAcbxADD2);								// additions 2 is	offset x40	length 4
	}

	/**
	 * Get additions 3.
	 * 
	 * @return acbxADD3 as a byte array.
	 */
	public byte[] getAcbADD3() {
		
		System.arraycopy(acbxBytes, 0x44, this.acbxADD3, 0, 8);     // additions 3 is	offset x44	length 8
		return acbxADD3;
	}

	/**
	 * Set additions 3.
	 * 
	 * @param 	acbxADD3 		Additions 3 to set.
	 *        					If acbxADD3 is null then it will be set to all nulls(0x00).
	 *        					If acbxADD3 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbxADD3 length GT 8.
	 */
	public void setAcbADD3(byte[] acbxADD3) throws AdabasException {

		int acbxADD3Len = 0;
		
		if (acbxADD3 != null) {
			acbxADD3Len = acbxADD3.length;
			if (acbxADD3Len > 8) {
				throw new AdabasException(acbxDBID, String.format("setAcbADD3(): acbxADD3 length GT 8: acbxADD3 length = %d", acbxADD3Len));
			}
		}

		/*
		 * If the acbxADD3 has a length > 0
		 * 	Copy all the bytes in the byte array to this.acbxADD3 array starting at offset 0x00.
		 */
		if (acbxADD3Len > 0) {
			System.arraycopy(acbxADD3, 0, this.acbxADD3, 0x00, acbxADD3Len);	   		
		}
		
		/*
		 * If the acbxADD3 byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD3 will be padded with zeroes. 
		 */
		if (acbxADD3Len < 8) {
			System.arraycopy(bArrayZeroes, 0, this.acbxADD3, acbxADD3Len, 8-acbxADD3Len);
		}
	
		System.arraycopy(this.acbxADD3, 0, acbxBytes, 0x44, 8);		// additions 3 is	offset x44	length 8
	}

	/**
	 * Get additions 3 as a String.
	 * 
	 * @return acbxADD3 as a String.
	 */
	public String getAcbADD3String() {
		
		byte[]	acbxADD3ba = getAcbADD3();							// get ADD3 byte array
		return	new String(acbxADD3ba);								// make new String from ADD3 byte array
	}

	/**
	 * Set additions 3 as a String.
	 * 
	 * @param 	acbxADD3 		Additions 3 to set.
	 *        					If acbxADD3 is null or an empty string then it will be filled
	 *        					with spaces. If acbxADD3 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbxADD3 length GT 8.
	 */
	public void setAcbADD3(String acbxADD3) throws AdabasException {

		int acbxADD3Len = 0;
		
		if (acbxADD3 != null) {
			if (!acbxADD3.isEmpty()) {
				acbxADD3Len = acbxADD3.length();
				if (acbxADD3Len > 8) {
					throw new AdabasException(acbxDBID, String.format("setAcbADD3(): acbxADD3 length GT 8: acbxADD3 length = %d", acbxADD3Len));
				}
			}
		}

		/*
		 * If the acbxADD3 has a length > 0
		 * 	Generate a byte array from the acbxADD3 string.
		 *  Copy all the bytes in the byte array to this.acbxADD3 array starting at offset 0x00.
		 */
		if (acbxADD3Len > 0) {
			byte[] add3 = acbxADD3.getBytes();								   
			System.arraycopy(add3, 0, this.acbxADD3, 0x00, acbxADD3Len);	   		
		}
		
		/*
		 * If the acbxADD3 string generated byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD3 will be padded with spaces. 
		 */
		if (acbxADD3Len < 8) {
			System.arraycopy(bArraySpaces, 0, this.acbxADD3, acbxADD3Len, 8-acbxADD3Len);
		}
		
		System.arraycopy(this.acbxADD3, 0, acbxBytes, 0x44, 8);		// additions 3 is	offset x44	length 8
	}

	/**
	 * Get additions 4.
	 * 
	 * @return acbxADD4 as a byte array.
	 */
	public byte[] getAcbADD4() {
		
		System.arraycopy(acbxBytes, 0x4C, this.acbxADD4, 0, 8);     // additions 4 is	offset x4C	length 8
		return acbxADD4;
	}

	/**
	 * Set additions 4.
	 * 
	 * @param 	acbxADD4 		Additions 4 to set.
	 *        					If acbxADD4 is null then it will be set to all nulls(0x00).
	 *        					If acbxADD4 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbxADD4 length GT 8.
	 */
	public void setAcbADD4(byte[] acbxADD4) throws AdabasException {

		int acbxADD4Len = 0;
		
		if (acbxADD4 != null) {
			acbxADD4Len = acbxADD4.length;
			if (acbxADD4Len > 8) {
				throw new AdabasException(acbxDBID, String.format("setAcbADD4(): acbxADD4 length GT 8: acbxADD4 length = %d", acbxADD4Len));
			}
		}

		/*
		 * If the acbxADD4 has a length > 0
		 *  Copy all the bytes in the byte array to this.acbxADD4 array starting at offset 0x00.
		 */
		if (acbxADD4Len > 0) {
			System.arraycopy(acbxADD4, 0, this.acbxADD4, 0x00, acbxADD4Len);	   		
		}
		
		/*
		 * If the acbxADD4 byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD4 will be padded with zeroes. 
		 */
		if (acbxADD4Len < 8) {
			System.arraycopy(bArrayZeroes, 0, this.acbxADD4, acbxADD4Len, 8-acbxADD4Len);
		}
	
		System.arraycopy(this.acbxADD4, 0, acbxBytes, 0x4C, 8);		// additions 4 is	offset x4C	length 8
	}

	/**
	 * Get additions 4 as a String.
	 * 
	 * @return acbxADD4 as a String.
	 */
	public String getAcbADD4String() {
		
		byte[]	acbxADD4ba = getAcbADD4();							// get ADD4 byte array
		return	new String(acbxADD4ba);								// make new String from ADD4 byte array
	}

	/**
	 * Set additions 4 as a String.
	 * 
	 * @param 	acbxADD4		Additions 4 to set.
	 *        					If acbxADD4 is null or an empty string then it will be filled
	 *        					with spaces. If acbxADD4 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbxADD4 length GT 8.
	 */
	public void setAcbADD4(String acbxADD4) throws AdabasException {

		int acbxADD4Len = 0;
		
		if (acbxADD4 != null) {
			if (!acbxADD4.isEmpty()) {
				acbxADD4Len = acbxADD4.length();
				if (acbxADD4Len > 8) {
					throw new AdabasException(acbxDBID, String.format("setAcbADD4(): acbxADD4 length GT 8: acbxADD4 length = %d", acbxADD4Len));
				}
			}
		}

		/*
		 * If the acbxADD4 has a length > 0
		 *  Generate a byte array from the acbxADD4 string.
		 *  Copy all the bytes in the byte array to this.acbxADD4 array starting at offset 0x00.
		 */
		if (acbxADD4Len > 0) {
			byte[] add4 = acbxADD4.getBytes();								   
			System.arraycopy(add4, 0, this.acbxADD4, 0x00, acbxADD4Len);	   		
		}
		
		/*
		 * If the acbxADD4 string generated byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD4 will be padded with spaces. 
		 */
		if (acbxADD4Len < 8) {
			System.arraycopy(bArraySpaces, 0, this.acbxADD4, acbxADD4Len, 8-acbxADD4Len);
		}
		
		System.arraycopy(this.acbxADD4, 0, acbxBytes, 0x4C, 8);		// additions 4 is	offset x4C	length 8
	}

	/**
	 * Get additions 5.
	 * 
	 * @return acbxADD5 as a byte array.
	 */
	public byte[] getAcbADD5() {
		
		System.arraycopy(acbxBytes, 0x54, acbxADD5, 0, 8);			// additions 5 is	offset x54	length 8
		return acbxADD5;
	}

	/**
	 * Set additions 5.
	 * 
	 * @param 	acbxADD5		Additions 5 to set.
	 *        					If acbxADD5 is null then it will be set to all nulls(0x00).
	 *        					If acbxADD5 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbxADD5 length GT 8.
	 */
	public void setAcbADD5(byte[] acbxADD5) throws AdabasException {

		int acbxADD5Len = 0;
		
		if (acbxADD5 != null) {
			acbxADD5Len = acbxADD5.length;
			if (acbxADD5Len > 8) {
				throw new AdabasException(acbxDBID, String.format("setAcbADD5(): acbxADD5 length GT 8: acbxADD5 length = %d", acbxADD5Len));
			}
		}

		/*
		 * If the acbxADD5 has a length > 0
		 *  Copy all the bytes in the byte array to this.acbxADD5 array starting at offset 0x00.
		 */
		if (acbxADD5Len > 0) {
			System.arraycopy(acbxADD5, 0, this.acbxADD5, 0x00, acbxADD5Len);	   		
		}
		
		/*
		 * If the acbxADD5 byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD5 will be padded with nulls(0x00). 
		 */
		if (acbxADD5Len < 8) {
			System.arraycopy(bArrayZeroes, 0, this.acbxADD5, acbxADD5Len, 8-acbxADD5Len);
		}
	
		System.arraycopy(this.acbxADD5, 0, acbxBytes,	0x54, 8);	// additions 5 is	offset x54	length 8
	}

	/**
	 * Get additions 5 as a String.
	 * 
	 * @return acbxADD5 as a String.
	 */
	public String getAcbADD5String() {
		
		byte[]	acbxADD5ba = getAcbADD5();							// get ADD5 byte array
		return	new String(acbxADD5ba);								// make new String from ADD5 byte array
	}

	/**
	 * Set additions 5 as a String.
	 * 
	 * @param 	acbxADD5 		Additions 5 to set.
	 *        					If acbxADD5 is null or an empty string then it will be filled
	 *        					with spaces. If acbxADD5 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbxADD5 length GT 8.
	 */
	public void setAcbADD5(String acbxADD5) throws AdabasException {

		int acbxADD5Len = 0;
		
		if (acbxADD5 != null) {
			if (!acbxADD5.isEmpty()) {
				acbxADD5Len = acbxADD5.length();
				if (acbxADD5Len > 8) {
					throw new AdabasException(acbxDBID, String.format("setAcbADD5(): acbxADD5 length GT 8: acbxADD5 length = %d", acbxADD5Len));
				}
			}
		}

		/*
		 * If the acbxADD5 has a length > 0
		 *  Generate a byte array from the acbxADD5 string.
		 *  Copy all the bytes in the byte array to this.acbxADD5 array starting at offset 0x00.
		 */
		if (acbxADD5Len > 0) {
			byte[] add5 = acbxADD5.getBytes();								   
			System.arraycopy(add5, 0, this.acbxADD5, 0x00, acbxADD5Len);	   		
		}
		
		/*
		 * If the acbxADD5 string generated byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD5 will be padded with spaces. 
		 */
		if (acbxADD5Len < 8) {
			System.arraycopy(bArraySpaces, 0, this.acbxADD5, acbxADD5Len, 8-acbxADD5Len);
		}
		
		System.arraycopy(this.acbxADD5, 0, acbxBytes,	0x54, 8);	// additions 5 is	offset x54	length 8
	}

	/**
	 * Get additions 6.
	 * 
	 * @return acbxADD6 as a byte array.
	 */
	public byte[] getAcbADD6() {
		
		System.arraycopy(acbxBytes, 0x5C, acbxADD6, 0, 8);			// additions 6 is	offset x5C	length 8
		return acbxADD6;
	}

	/**
	 * Set additions 6.
	 * 
	 * @param 	acbxADD6		Additions 6 to set.
	 *        					If acbxADD6 is null then it will be set to all nulls(0x00).
	 *        					If acbxADD6 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbxADD6 length GT 8.
	 */
	public void setAcbADD6(byte[] acbxADD6) throws AdabasException {

		int acbxADD6Len = 0;
		
		if (acbxADD6 != null) {
			acbxADD6Len = acbxADD6.length;
			if (acbxADD6Len > 8) {
				throw new AdabasException(acbxDBID, String.format("setAcbADD6(): acbxADD6 length GT 8: acbxADD6 length = %d", acbxADD6Len));
			}
		}

		/*
		 * If the acbxADD6 has a length > 0
		 *  Copy all the bytes in the byte array to this.acbxADD6 array starting at offset 0x00.
		 */
		if (acbxADD6Len > 0) {
			System.arraycopy(acbxADD6, 0, this.acbxADD6, 0x00, acbxADD6Len);	   		
		}
		
		/*
		 * If the acbxADD6 byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD6 will be padded with nulls(0x00). 
		 */
		if (acbxADD6Len < 8) {
			System.arraycopy(bArrayZeroes, 0, this.acbxADD6, acbxADD6Len, 8-acbxADD6Len);
		}
	
		System.arraycopy(this.acbxADD6, 0, acbxBytes,	0x5C, 8);	// additions 6 is	offset x5C	length 8
	}
	
	/**
	 * Get additions 6 as a String.
	 * 
	 * @return acbxADD6 as a String.
	 */
	public String getAcbADD6String() {
		
		byte[]	acbxADD6ba = getAcbADD6();							// get ADD6 byte array
		return	new String(acbxADD6ba);								// make new String from ADD6 byte array
	}

	/**
	 * Set additions 6 as a String.
	 * 
	 * @param 	acbxADD6 		Additions 6 to set.
	 *        					If acbxADD6 is null or an empty string then it will be filled
	 *        					with spaces. If acbxADD6 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbxADD6 length GT 8.
	 */
	public void setAcbADD6(String acbxADD6) throws AdabasException {

		int acbxADD6Len = 0;
		
		if (acbxADD6 != null) {
			if (!acbxADD6.isEmpty()) {
				acbxADD6Len = acbxADD6.length();
				if (acbxADD6Len > 8) {
					throw new AdabasException(acbxDBID, String.format("setAcbADD6(): acbxADD6 length GT 8: acbxADD6 length = %d", acbxADD6Len));
				}
			}
		}

		/*
		 * If the acbxADD6 has a length > 0
		 *  Generate a byte array from the acbxADD6 string.
		 *  Copy all the bytes in the byte array to this.acbxADD6 array starting at offset 0x00.
		 */
		if (acbxADD6Len > 0) {
			byte[] add6 = acbxADD6.getBytes();								   
			System.arraycopy(add6, 0, this.acbxADD6, 0x00, acbxADD6Len);	   		
		}
		
		/*
		 * If the acbxADD6 string generated byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbxADD6 will be padded with spaces. 
		 */
		if (acbxADD6Len < 8) {
			System.arraycopy(bArraySpaces, 0, this.acbxADD6, acbxADD6Len, 8-acbxADD6Len);
		}
		
		System.arraycopy(this.acbxADD6, 0, acbxBytes,	0x5C, 8);	// additions 6 is	offset x5C	length 8
	}

	/**
	 * Get error offset in buffer. The Error Offset in Buffer specifies 
	 * the offset in the buffer, if any, where the error is detected 
	 * during the direct call. The ACBXERRx fields are only set when a 
	 * response code is returned from a direct call. The ACBXERRA, 
	 * ACBXERRD, and ACBXERRE fields are only set when the response code 
	 * is related to buffer processing. 
	 *
	 * @return acbxERRA error offset in buffer.
	 */
	public long getAcbERRA() {
		
		this.acbxERRA = acbxBB.getLong(0x68);						// error offset is	offset x68	length 8
		return this.acbxERRA;
	}
	
	/**
	 * Set error offset in buffer. The Error Offset in Buffer specifies 
	 * the offset in the buffer, if any, where the error is detected 
	 * during the direct call. The ACBXERRx fields are only set when a 
	 * response code is returned from a direct call. The ACBXERRA, 
	 * ACBXERRD, and ACBXERRE fields are only set when the response code 
	 * is related to buffer processing.
	 * 
	 * @param acbxERRA error offset in buffer to set.
	 */
	public void setAcbERRA(long acbxERRA) {
		
		this.acbxERRA = acbxERRA;
		acbxBB.putLong(0x68, acbxERRA);								// error offset is	offset x68	length 8
	}

	/**
	 * Get error character field. This field identifies the two-byte 
	 * Adabas short name of the field, if any, that was being processed 
	 * when the error was detected. The ACBXERRx fields are only set 
	 * when a response code is returned from a direct call.
	 *
	 * @return acbxERRB error character field.
	 */
	public String getAcbERRB() {
		
		try {
			this.acbxERRB = new String(acbxBytes, 0x70, 2, encoding);	// error char is	offset x70	length 2 		
		}
		catch (UnsupportedEncodingException e) {}
		return this.acbxERRB;
	}
	
	/**
	 * Set error character field. This field identifies the two-byte 
	 * Adabas short name of the field, if any, that was being processed 
	 * when the error was detected. The ACBXERRx fields are only set 
	 * when a response code is returned from a direct call.
	 *
	 * @param 	acbxERRB 		Error character field to set.
	 *        					If acbxERRB is null or an empty string then it will be filled
	 *        					with spaces. If acbxERRB is less than 2 bytes, then
	 *        					the remaining byte will be filled with a space.  
	 * @throws	AdabasException	If acbxERRB length GT 2.
	 */
	public void setAcbERRB(String acbxERRB) throws AdabasException {
		
		if (acbxERRB == null || acbxERRB.isEmpty()) {
			try {
				this.acbxERRB = new String("  ".getBytes(encoding));
			} catch (UnsupportedEncodingException e) {}
		}
		else {
			int acbxERRBLen = acbxERRB.length();
			if (acbxERRBLen == 2) { 
				this.acbxERRB = acbxERRB;
			}
			else if (acbxERRBLen == 1) {
				try {
					this.acbxERRB = acbxERRB + new String(" ".getBytes(encoding));
				} catch (UnsupportedEncodingException e) {}
			}
			else {
				throw new AdabasException(acbxDBID, String.format("setAcbERRB(): acbxERRB length GT 2: acbxERRB length = %d", acbxERRBLen));
			}
		}
		
		byte[] errb	= new byte[2];
		try {
			errb	= this.acbxERRB.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {}
		System.arraycopy(errb, 0, acbxBytes, 0x70, 2);				// error char is	offset x70	length 2
	}
	
	/**
	 * Get error subcode. This field stores the subcode of the error 
	 * that occurred during direct call processing. The ACBXERRx fields 
	 * are only set when a response code is returned from a direct call. 
	 * If Entire Net-work is installed, some response codes return the 
	 * node ID of the problem node in this field.
	 *
	 * @return acbxERRC error subcode.
	 */
	public short getAcbERRC() {
		
		this.acbxERRC = acbxBB.getShort(0x72);						// error subcode is	offset x72	length 2
		return this.acbxERRC;
	}
	
	/**
	 * Set error subcode. This field stores the subcode of the error 
	 * that occurred during direct call processing. The ACBXERRx fields 
	 * are only set when a response code is returned from a direct call. 
	 * If Entire Net-work is installed, some response codes return the 
	 * node ID of the problem node in this field.
	 *
	 * @param acbxERRC error subcode to set.
	 */
	public void setAcbERRC(short acbxERRC) {
		
		this.acbxERRC = acbxERRC;
		acbxBB.putShort(0x72, acbxERRC);								// error subcode is	offset x72	length 2
	}
	
	/**
	 * Get error buffer ID. This field contains the ID (from the ABDID 
	 * field) of the buffer referred to by the ACBXERRA field, so that 
	 * the buffer causing the error can be identified, when multiple 
	 * buffers are involved. The ACBXERRx fields are only set when a 
	 * response code is returned from a direct call. The ACBXERRA, 
	 * ACBXERRD, and ACBXERRE fields are only set when the response 
	 * code is related to buffer processing.
	 *
	 * @return acbxERRD error buffer ID.
	 */
	public byte getAcbERRD() {
		
		this.acbxERRD = acbxBB.get(0x74);							// err buffer ID is	offset x74	length 1
		return this.acbxERRD;
	}
	
	/**
	 * Set error buffer ID. This field contains the ID (from the ABDID 
	 * field) of the buffer referred to by the ACBXERRA field, so that 
	 * the buffer causing the error can be identified, when multiple 
	 * buffers are involved. The ACBXERRx fields are only set when a 
	 * response code is returned from a direct call. The ACBXERRA, 
	 * ACBXERRD, and ACBXERRE fields are only set when the response 
	 * code is related to buffer processing.
	 *
	 * @param acbxERRD error buffer ID to set.
	 */
	public void setAcbERRD(byte acbxERRD) {
		
		this.acbxERRD = acbxERRD;
		acbxBB.put(0x74, acbxERRD);									// err buffer ID is	offset x74	length 1
	}
	
	/**
	 * Get error buffer sequence number. This field contains the sequence 
	 * number of the buffer referred to by the ACBXERRA and ACBXERRD fields. 
	 * The ACBXERRx fields are only set when a response code is returned 
	 * from a direct call. The ACBXERRA, ACBXERRD, and ACBXERRE fields are 
	 * only set when the response code is related to buffer processing. 
	 *
	 * @return acbxERRF error buffer seqence number.
	 */
	public short getAcbERRF() {
		
		this.acbxERRF = acbxBB.getShort(0x76);						// err buf seq # is	offset x76	length 2
		return this.acbxERRF;
	}
	
	/**
	 * Set error buffer sequence number. This field contains the sequence 
	 * number of the buffer referred to by the ACBXERRA and ACBXERRD fields. 
	 * The ACBXERRx fields are only set when a response code is returned 
	 * from a direct call. The ACBXERRA, ACBXERRD, and ACBXERRE fields are 
	 * only set when the response code is related to buffer processing. 
	 *
	 * @param acbxERRF error buffer sequence number.
	 */
	public void setAcbERRF(short acbxERRF) {
		
		this.acbxERRF = acbxERRF;
		acbxBB.putShort(0x76, acbxERRF);							// err bug seq # is offset x76	length 2
	}
	
	/**
	 * Get subcomponent response code. This field contains the response 
	 * code from any error that occurred when an Adabas add-on product 
	 * intercepts the Adabas command.
	 * 
	 * @return acbxSUBR subcomponent response code.
	 */
	public short getAcbSUBR() {
		
		this.acbxSUBR = acbxBB.getShort(0x78);						// subcomp rc is 	offset x78	length 2
		return this.acbxSUBR;
	}
	
	/**
	 * Set subcomponent response code. This field contains the response 
	 * code from any error that occurred when an Adabas add-on product 
	 * intercepts the Adabas command.
	 * 
	 * @param acbxSUBR subcomponent response code.
	 */
	public void setAcbSUBR(short acbxSUBR) {
		
		this.acbxSUBR = acbxSUBR;
		acbxBB.putShort(0x78, acbxSUBR);							// subcomp rc is 	offset x78	length 2
	}
	
	/**
	 * Get subcomponent response subcode. This field contains the response 
	 * subcode from any error that occurred when an Adabas add-on product 
	 * intercepts the Adabas command.
	 * 
	 * @return acbxSUBS subcomponent response subcode.
	 */
	public short getAcbSUBS() {
		
		this.acbxSUBS = acbxBB.getShort(0x7A);						// subcomp rsc is 	offset x7A	length 2
		return this.acbxSUBS;
	}
	
	/**
	 * Set subcomponent response subcode. This field contains the response 
	 * subcode from any error that occurred when an Adabas add-on product 
	 * intercepts the Adabas command.
	 * 
	 * @param acbxSUBS subcomponent response code.
	 */
	public void setAcbSUBS(short acbxSUBS) {
		
		this.acbxSUBS = acbxSUBS;
		acbxBB.putShort(0x7A, acbxSUBS);							// subcomp rsc is 	offset x7A	length 2
	}
	
	/**
	 * Get subcomponent error text. This field contains the error text 
	 * of any error that occurred when an Adabas add-on product intercepts 
	 * the Adabas command.
	 * 
	 * @return acbxSUBT subcomponent error text.
	 */
	public String getAcbSUBT() {
		
		try {
			this.acbxSUBT = new String(acbxBytes, 0x7C, 4, encoding);	// subcomp text is	offset x7C	length 4 		
		}
		catch (UnsupportedEncodingException e) {}
		return this.acbxSUBT;
	}
	
	/**
	 * Set subcomponent error text. This field contains the error text 
	 * of any error that occurred when an Adabas add-on product intercepts 
	 * the Adabas command.
	 *
	 * @param 	acbxSUBT		error text to set.
	 *        					If acbxSUBT is null or an empty string, then it will be filled
	 *        					with spaces. If acbxSUBT is less than 4 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbxSUBT length GT 4.
	 * 
	 */
	public void setAcbSUBT(String acbxSUBT) throws AdabasException {
		
		try {
			if (acbxSUBT == null || acbxSUBT.isEmpty()) {
				this.acbxSUBT = new String("    ".getBytes(encoding));
			}
			else {
				int acbxSUBTLen = acbxSUBT.length();
				if (acbxSUBTLen == 4) {
					this.acbxSUBT = acbxSUBT;
				}
				else if (acbxSUBTLen == 3) {
					this.acbxSUBT = acbxSUBT + new String(" ".getBytes(encoding));
				}
				else if (acbxSUBTLen == 2) {
					this.acbxSUBT = acbxSUBT + new String("  ".getBytes(encoding));
				}
				else if (acbxSUBTLen == 1) {
					this.acbxSUBT = acbxSUBT + new String("   ".getBytes(encoding));
				}
				else { 
					throw new AdabasException(acbxDBID, String.format("setAcbSUBT(): acbxSUBT length GT 4: acbxSUBT length = %d", acbxSUBTLen));
				}
			}

			byte[] subt	= new byte[4];									// make byte array
			subt		= this.acbxSUBT.getBytes(encoding);
			System.arraycopy(subt, 0, acbxBytes, 0x7C, 4);				// subcomponent error text  offset x7C	length 4
		}
		catch (UnsupportedEncodingException e) {}
	}
	
	/**
	 * Get compressed record length. This field returns the compressed 
	 * record length when a record was read or written. This is the length 
	 * of the compressed data processed by the successful Adabas call. 
	 * If the logical data storage record spans multiple physical data 
	 * records, the combined length of all associated physical records 
	 * may not be known. In this case, Adabas returns high values in the 
	 * low-order word of this field. 
	 *
	 * @return acbxLCMP compressed record length.
	 */
	public long getAcbLCMP() {
		
		this.acbxLCMP = acbxBB.getLong(0x80);						// comp rec lng is	offset x80	length 8
		return this.acbxLCMP;
	}
	
	/**
	 * Get compressed record length. This field returns the compressed 
	 * record length when a record was read or written. This is the length 
	 * of the compressed data processed by the successful Adabas call. 
	 * If the logical data storage record spans multiple physical data 
	 * records, the combined length of all associated physical records 
	 * may not be known. In this case, Adabas returns high values in the 
	 * low-order word of this field.
	 *  
	 * @param acbxLCMP compressed record length to set.
	 */
	public void setAcbLCMP(long acbxLCMP) {
		
		this.acbxLCMP = acbxLCMP;
		acbxBB.putLong(0x80, acbxLCMP);								// comp rec lng is	offset x80 	length 8
	}
	
	/**
	 * Get decompressed record length. This field returns the decompressed 
	 * record length. This is the length of the decompressed data processed 
	 * by the successful call. If multiple record buffer segments are specified, 
	 * this reflects the total length across all buffer segments. 
	 *
	 * @return acbxLDEC decompressed record length.
	 */
	public long getAcbLDEC() {
		
		this.acbxLDEC = acbxBB.getLong(0x88);						// dec rec lng is	offset x88	length 8
		return this.acbxLDEC;
	}
	
	/**
	 * Set decompressed record length. This field returns the decompressed 
	 * record length. This is the length of the decompressed data processed 
	 * by the successful call. If multiple record buffer segments are specified, 
	 * this reflects the total length across all buffer segments. 
	 *
	 * @param acbxLDEC decompressed record length to set.
	 */
	public void setAcbLDEC(long acbxLDEC) {
		
		this.acbxLDEC = acbxLDEC;
		acbxBB.putLong(0x88, acbxLDEC);								// dec rec lng is	offset x88	length 8
	}
	
	/**
	 * Get command time. The command time field is used by Adabas to return 
	 * the elapsed time that was needed by the nucleus to process the command. 
	 * In contrast to the mainframe, where this field is always filled by Adabas, 
	 * it is only filled on open systems platforms if Command Logging is switched 
	 * on or if the nucleus is started with the environment variable ADA_CMD_TIME 
	 * set (the value is irrelevant).
	 * 
	 * @return acbxCMDT command time
	 */
	public long getAcbCMDT() {
		
		this.acbxCMDT = acbxBB.getLong(0x90);						// command time is	offset x90	length 8
		return this.acbxCMDT;
	}
	
	/**
	 * Set command time. The command time field is used by Adabas to return 
	 * the elapsed time that was needed by the nucleus to process the command. 
	 * In contrast to the mainframe, where this field is always filled by Adabas, 
	 * it is only filled on open systems platforms if Command Logging is switched 
	 * on or if the nucleus is started with the environment variable ADA_CMD_TIME 
	 * set (the value is irrelevant).
	 * 
	 * @param acbxCMDT command time to set.
	 */
	public void setAcbCMDT(long acbxCMDT) {
		
		this.acbxCMDT = acbxCMDT;
		acbxBB.putLong(0x90, acbxCMDT);								// command time is	offset x90	length 8
	}
	
	/**
	 * Get the user area.
	 * 
	 * @return the acbxUSER
	 */
	public byte[] getAcbUSER() {
		
		System.arraycopy(acbxBytes, 0x98, acbxUSER, 0, 16);			// user area is		offset x98	length 16
		return acbxUSER;
	}

	/**
	 * Set the user area.
	 * 
	 * @param 	acbxUSER 		The acbUSER to set.
 	 * @throws	AdabasException	If acbUSER length GT 16.
	 */
	public void setAcbUSER(byte[] acbxUSER) throws AdabasException {
		
		int acbxUSERLen = 0;
		
		if (acbxUSER != null) {
			acbxUSERLen = acbxUSER.length;
			if (acbxUSERLen > 16) {
				throw new AdabasException(acbxDBID, String.format("setAcbUSER(): acbxUSER length GT 16: acbxUSER length = %d", acbxUSERLen));
			}
		}
			
		/*
		 * If the acbxUSER has a length > 0
		 *  Copy all the bytes in the byte array to this.acbxUSER array starting at offset 0x00.
		 */
		if (acbxUSERLen > 0) {
			System.arraycopy(acbxUSER, 0, this.acbxUSER, 0x00, acbxUSERLen);	   		
		}

		/*
		 * If the acbxUSER byte array was LT 16 bytes. 
		 *  then the remaining bytes in this.acbxUSER will be padded with nulls. 
		 */
		if (acbxUSERLen < 16) {
			System.arraycopy(bArrayZeroes, 0, this.acbxUSER, acbxUSERLen, 16-acbxUSERLen);
		}
	
		System.arraycopy(this.acbxUSER, 0, acbxBytes, 0x98, 16);	// user area is		offset x98	length 16
	}
	
	/**
	 * Get host name.
	 * 
	 * Host name is an additional value beyond the traditional ACBX. It is used
	 * when more than one instance of a DBID exist in the network to explicitly
	 * tell XTS which one to use. Its value can be null, in which case XTS will
	 * decide which instance of the DBID to use.
	 * 
	 * @return the hostName
	 */
	public String getHostName() {
		
		return hostName;
	}

	/**
	 * Set host name.
	 * 
	 * Host name is an additional value beyond the traditional ACBX. It is used
	 * when more than one instance of a DBID exist in the network to explicitly
	 * tell XTS which one to use. Its value can be null, in which case XTS will
	 * decide which instance of the DBID to use.
	 * 
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		
		this.hostName = hostName;
	}

	/**
	 * Get the whole ACBX as a formatted / displayable string.
	 * 
	 * @return the ACBX
	 */
	public String toString() {
		
		StringBuffer sb = new StringBuffer("Adabas Control Block = \n");
		
		sb.append("   acbxTYPE = " + String.format("0x%02x\n", this.acbxTYPE));
		sb.append("   acbxVER  = " 		+ String.format("0x%02X%02X\n", acbxVER[0],acbxVER[1]));
		sb.append("   acbxLEN  = " 		+ this.acbxLEN  + "\n");
		sb.append("   acbxCMD  = " 		+ this.acbxCMD  + "\n");
		sb.append("   acbxRSP  = "		+ this.acbxRSP  + "\n");
		sb.append("   acbxCID  = 0x"	+ byteArrayToHexString(this.acbxCID) + "\n");
		sb.append("   acbxDBID = " 		+ this.acbxDBID + "\n");
		sb.append("   acbxFNR  = " 		+ this.acbxFNR  + "\n");
		sb.append("   acbxISN  = " 		+ this.acbxISN  + "\n");
		sb.append("   acbxISL  = " 		+ this.acbxISL  + "\n");
		sb.append("   acbxISQ  = " 		+ this.acbxISQ  + "\n");
		sb.append("   acbxCOP1 = " 		+ String.format("0x%02X\n", this.acbxCOP1));
		sb.append("   acbxCOP2 = " 		+ String.format("0x%02X\n", this.acbxCOP2));
		sb.append("   acbxCOP3 = " 		+ String.format("0x%02X\n", this.acbxCOP3));
		sb.append("   acbxCOP4 = " 		+ String.format("0x%02X\n", this.acbxCOP4));
		sb.append("   acbxCOP5 = " 		+ String.format("0x%02X\n", this.acbxCOP5));
		sb.append("   acbxCOP6 = " 		+ String.format("0x%02X\n", this.acbxCOP6));
		sb.append("   acbxCOP7 = " 		+ String.format("0x%02X\n", this.acbxCOP7));
		sb.append("   acbxCOP8 = " 		+ String.format("0x%02X\n", this.acbxCOP8));
		sb.append("   acbxADD1 = 0x" 	+ byteArrayToHexString(this.acbxADD1) + "\n");
		sb.append("   acbxADD2 = 0x" 	+ byteArrayToHexString(this.acbxADD2) + "\n");
		sb.append("   acbxADD3 = 0x" 	+ byteArrayToHexString(this.acbxADD3) + "\n");
		sb.append("   acbxADD4 = 0x" 	+ byteArrayToHexString(this.acbxADD4) + "\n");
		sb.append("   acbxADD5 = 0x" 	+ byteArrayToHexString(this.acbxADD5) + "\n");
		sb.append("   acbxADD6 = 0x" 	+ byteArrayToHexString(this.acbxADD6) + "\n");
		sb.append("   acbxERRA = " 		+ this.acbxERRA  + "\n");
		sb.append("   acbxERRB = " 		+ this.acbxERRB  + "\n");
		sb.append("   acbxERRC = " 		+ this.acbxERRC  + "\n");
		sb.append("   acbxERRD = " 		+ String.format("0x%02x\n", this.acbxERRD));
		sb.append("   acbxERRF = " 		+ this.acbxERRF  + "\n");
		sb.append("   acbxSUBR = " 		+ this.acbxSUBR  + "\n");
		sb.append("   acbxSUBS = " 		+ this.acbxSUBS  + "\n");
		sb.append("   acbxSUBT = " 		+ this.acbxSUBT  + "\n");
		sb.append("   acbxLCMP = " 		+ this.acbxLCMP  + "\n");
		sb.append("   acbxLDEC = " 		+ this.acbxLDEC  + "\n");
		sb.append("   acbxCMDT = " 		+ this.acbxCMDT  + "\n");
		sb.append("   acbxUSER = 0x" 	+ byteArrayToHexString(this.acbxUSER) + "\n");
		if (this.hostName == null) 	{sb.append("   hostName = null" + "\n");}
		else 						{sb.append("   hostname = " + this.hostName + "\n");}
		sb.append("   ebcdic   = "		+ String.format("%b", ebcdic) + "\n");
		sb.append("   encoding = "		+ encoding);
		
		return sb.toString();
	}
	
	/**
	 * Get the abbreviated ACBX as a formatted / displayable string.
	 * 
	 * @return the ACBX
	 */
	public String toStringBrief() {
		
		StringBuffer sb = new StringBuffer("Adabas Control Block = \n");
		sb.append("   acbxCMD  = " 		+ this.acbxCMD  + "\n");
		sb.append("   acbxRSP  = " 		+ this.acbxRSP  + "\n");
		sb.append("   acbxCID  = 0x"	+ byteArrayToHexString(this.acbxCID)  + "\n");
		sb.append("   acbxDBID = " 		+ this.acbxDBID + "\n");
		sb.append("   acbxFNR  = " 		+ this.acbxFNR  + "\n");
		sb.append("   acbxISN  = " 		+ this.acbxISN  + "\n");
		sb.append("   acbxISL  = " 		+ this.acbxISL  + "\n");
		sb.append("   acbxISQ  = " 		+ this.acbxISQ  + "\n");
		sb.append("   acbxCOP1 = " 		+ String.format("0x%02X\n", this.acbxCOP1));
		sb.append("   acbxCOP2 = " 		+ String.format("0x%02X\n", this.acbxCOP2));
		sb.append("   acbxADD1 = 0x" 	+ byteArrayToHexString(this.acbxADD1)  + "\n");
		sb.append("   acbxERRA = " 		+ this.acbxERRA  + "\n");
		sb.append("   acbxERRB = " 		+ this.acbxERRB  + "\n");
		sb.append("   acbxERRC = " 		+ this.acbxERRC  + "\n");
		sb.append("   acbxERRD = " 		+ String.format("0x%02X\n", this.acbxERRD));
		sb.append("   acbxERRF = " 		+ this.acbxERRF  + "\n");
		sb.append("   acbxLCMP = " 		+ this.acbxLCMP  + "\n");
		sb.append("   acbxLDEC = " 		+ this.acbxLDEC  + "\n");
		if (this.hostName == null) 	{sb.append("   hostName = null");}
		else 						{sb.append("   hostname = " + this.hostName);}
		sb.append("   encoding = "		+ encoding);
		
		return sb.toString();
	}
	
	/**
	 * Convert a byte array to a string of display hex characters
	 * 
	 * @param 	ba byte array
	 * @return 	String of hex characters
	 */
	public String byteArrayToHexString(byte[] ba) {
		
	   StringBuilder sb = new StringBuilder();
	   
	   for(byte b : ba) {
	      sb.append(String.format("%02X", b&0xff));
	   }
	   
	   return sb.toString();
	}
	
	/**
	 * Reset ACBX content to empty/default values.
	 * 
	 * @throws AdabasException	Adabas specific exception.
	 */
	public void resetACBX() throws AdabasException {

		for (int i=0; i < acbxBytes.length; i++) {
			acbxBytes[i] = 0x00;
		}
		
		if (ebcdic == false) {
		this.setAcbVER(aVER);										// set version to "F2" (0x4632) to indicate ACBX
		}
		else {
			this.setAcbVER(eVER);
		}
		this.setAcbLEN(ACBX_LENGTH);								// set ACBX length
		
		this.getAcbTYPE();
		this.getAcbVER();
		this.getAcbLEN();
		this.getAcbCMD();
		this.getAcbRSP();
		this.getAcbCID();
		this.getAcbDBID();
		this.getAcbFNR();
		this.getAcbISN();
		this.getAcbISL();
		this.getAcbISQ();
		this.getAcbCOP1();
		this.getAcbCOP2();
		this.getAcbCOP3();
		this.getAcbCOP4();
		this.getAcbCOP5();
		this.getAcbCOP6();
		this.getAcbCOP7();
		this.getAcbCOP8();
		this.getAcbADD1();
		this.getAcbADD2();
		this.getAcbADD3();
		this.getAcbADD4();
		this.getAcbADD5();
		this.getAcbADD6();
		this.getAcbERRA();
		this.getAcbERRB();
		this.getAcbERRC();
		this.getAcbERRD();
		this.getAcbERRF();
		this.getAcbSUBR();
		this.getAcbSUBS();
		this.getAcbSUBT();
		this.getAcbLCMP();
		this.getAcbLDEC();
		this.getAcbCMDT();
		this.getAcbUSER();
		
		this.hostName = null;

	}

}
