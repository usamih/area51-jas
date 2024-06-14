package com.softwareag.adabas.jas;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.Logger;

/**
 * Base class implementing classic/original Adabas control block for direct calls.
 *
 * @author usadva
 * @author usarc
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
public class AdabasControlBlock {
	
	protected byte[]		acbBytes;				// whole ACB as a byte array
	protected ByteBuffer 	acbBB;					// whole ACB as contiguous byte buffer
	private byte			acbTYPE;				// call type				offset x00	length 1
//	private byte			acbReserved;			// reserved					offset x01	length 1
	private String			acbCMD;					// command code  			offset x02	length 2
	private byte[]			acbCID = new byte[4];	// command ID				offset x04	length 4
	private short			acbDBID;				// database ID				offset x08	length 1 or offset x0A	length 2 if DBID >  255
	private short			acbFNR;					// file number				offset x09	length 1 or offset x08	length 2 if DBID <= 255
	private short			acbRSP;					// response code			offset x0A	length 2
	private byte[]			acbISN = new byte[4]; 	// ISN						offset x0C	length 4
	private byte[]			acbISL = new byte[4]; 	// ISN lower limit			offset x10	length 4
	private byte[]			acbISQ = new byte[4]; 	// ISN quantity				offset x14	length 4
	private short			acbFBL;					// format buffer length		offset x18	length 2
	private short			acbRBL;					// record buffer length		offset x1A	length 2
	private short			acbSBL;					// search buffer length		offset x1C	length 2
	private short			acbVBL;					// value  buffer length		offset x1E	length 2
	private short			acbIBL;					// ISN    buffer length		offset x20	length 2
	private byte			acbCOP1;				// command option 1			offset x22	length 1
	private byte			acbCOP2;				// command option 2			offset x23	length 1
	private byte[]			acbADD1 = new byte[8]; 	// additions 1				offset x24	length 8
	private byte[]			acbADD2 = new byte[4]; 	// additions 2				offset x2C	length 4
	private byte[]			acbADD3 = new byte[8]; 	// additions 3				offset x30	length 8
	private byte[]			acbADD4 = new byte[8]; 	// additions 4				offset x38	length 8
	private byte[]			acbADD5 = new byte[8]; 	// additions 5				offset x40	length 8
	private byte[]			acbCMDT = new byte[4]; 	// command time				offset x48	length 4
	private byte[]			acbUSER = new byte[4]; 	// user area				offset x4C	length 4
	
	public static final int ACB_LENGTH 	= 80;		// length of ACB as a byte array / buffer

	private		String		hostName;				// host name of target DBID	NOT part of ACB byte buffer
	private 	boolean		ebcdic		= false;	// encoding flag
	private		String		encoding;

	final static Logger logger = AdabasTrace.getLogger("com.softwareag.adabas.jas.AdabasControlBlock");

	private static byte		eSpace		=	(byte) 0x40;	// EBCDIC space

	/*
	 * The following array of nulls is used for padding binary byte arrays. 
	 * Newly constructed arrays are initialized to nulls by default.
	 */
	private byte[]          bArrayZeroes = new byte[8];
	
	/*
	 * The following array of spaces is used for padding alphanumeric byte arrays. 
	 */
	private byte[] 			bArraySpaces = new String("        ").getBytes();  

	/**
	 * Constructor.
	 */
	public AdabasControlBlock() {
		
		acbBytes	= new byte[ACB_LENGTH];							// allocate byte array
		acbBB 		= ByteBuffer.wrap(acbBytes);					// wrap into byte buffer
		ebcdic		= false;
		encoding	= "ISO-8859-1";
	}
	
	/**
	 * Constructor taking an EBCDIC encoding flag.
	 * 
	 * @param	e	EBCDIC encoding used flag: true if EBCDIC used, false if not.
	 */
	public AdabasControlBlock(boolean e) {
		
		acbBytes	= new byte[ACB_LENGTH];							// allocate byte array
		acbBB 		= ByteBuffer.wrap(acbBytes);					// wrap into byte buffer
		ebcdic		= e;
		if (e == false) {
			encoding = "ISO-8859-1";
		}
		else {
			encoding = "cp037";
		}
		
	}

	/**
	 * Get whole ACB as contiguous byte array.
	 *  
	 * @return ACB byte array.
	 */
	public byte[] getACBArray() {
		
		return acbBytes;											// return ACB byte array			
	}
	
	/**
	 * Set whole ACB byte array.
	 * 
	 * @param 	ba				Byte array containing new ACB content.
	 * @throws 	AdabasException	Adabas specific exception. 
	 */
	public void setACBArray(byte[] ba) throws AdabasException {
		
		if (ba.length != acbBytes.length) {
			throw new AdabasException(String.format("setACBArray() error! Input byte array length: %d not equal to acbBytes length: %d!", ba.length, acbBytes.length));
		}
		
		System.arraycopy(ba, 0x00, acbBytes, 0x00, acbBytes.length);
	}
	
	/**
	 * Get whole ACB as contiguous byte buffer.
	 * 
	 * @return ACB byte buffer.
	 */
	public ByteBuffer getACBBuffer() {
		
		return acbBB;												// return ACB byte buffer
	}

	/**
	 * Get the call type.
	 * 
	 * Documented values: 	x00	-	1 byte file number	1-255;
	 * 						x30	-	2 byte file number	1-65535;
	 * 						x40	-	logical application call (compatibility earlier releases mainframe).
	 * 
	 * @return the acbTYPE.
	 */
	protected byte getAcbTYPE() {
		
		this.acbTYPE = acbBB.get(0);								// call type is		offset x00	length 1
		return this.acbTYPE;
	}

	/**
	 * Set the call type.
	 * 
	 * Documented values: 	x00	-	1 byte file number	1-255;
	 * 						x30	-	2 byte file number	1-65535;
	 * 						x40	-	logical application call (compatibility earlier releases mainframe).
	 * 
	 * @param 	acbTYPE 			the acbTYPE to set.
	 * @throws 	AdabasException  	If acbType != one of these values (0x00, 0x30, or 0x40).  
	 */
	protected void setAcbTYPE(byte acbTYPE) throws AdabasException {
		
		if (acbTYPE != 0x00 && acbTYPE != 0x30 && acbTYPE != 0x40) {
			throw new AdabasException("setAcbTYPE(): Invalid acbTYPE = " + String.format("0x%02X", acbTYPE));
		}
		
		this.acbTYPE = acbTYPE;
		acbBB.put(0, acbTYPE);										// call type is		offset x00	length 1
	}

	/**
	 * Get the command code.
	 * 
	 * @return the acbCMD.
	 */
	public String getAcbCMD() {
		
		try {
			this.acbCMD = new String(acbBytes, 0x02, 2, encoding);	// command code is	offset x02	length 2 		
		}
		catch (UnsupportedEncodingException e) {}
		return acbCMD;
	}

	/**
	 * Set the command code as a String.
	 * 
	 * @param 	acbCMD 			The acbCMD to set.
	 *        					If acbCMD is null or an empty string then it will be filled
	 *        					with spaces. If acbCMD is less than 2 bytes, then
	 *        					the remaining byte will be filled with a space.  
	 * @throws 	AdabasException	If acbCMD length GT 2.
	 */
	public void setAcbCMD(String acbCMD) throws AdabasException {
		
		// TODO validate content of command code
		
		if (acbCMD == null || acbCMD.isEmpty()) {
			try {
				this.acbCMD = new String("  ".getBytes(encoding));
			} catch (UnsupportedEncodingException e) {}
		}
		else {
			int acbCMDLen = acbCMD.length();
			if (acbCMDLen == 1) {
				try {
					this.acbCMD = acbCMD + new String(" ".getBytes(encoding));
				} catch (UnsupportedEncodingException e) {}
			}
			else if (acbCMDLen == 2) { 
				this.acbCMD = acbCMD;
			}
			else {
				throw new AdabasException(acbDBID, String.format("setAcbCMD(): acbCMD length GT 2: acbCMD length = %d", acbCMDLen));
			}
		}
		
		byte[] cmd = new byte[2];
		try {
			cmd		= this.acbCMD.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {}
		System.arraycopy(cmd, 0, acbBytes, 0x02, 2);				// command code is	offset x02	length 2
	}
	
	
	/**
	 * Get command code as a byte array.
	 * 
	 * @return	Command code byte array.
	 */
	public byte[] getAcbBaCMD() {
		
		byte[] baCMD	= new byte[2];
		
		System.arraycopy(acbBytes, 0x02, baCMD, 0x00, 2);
		
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
				this.acbCMD = new String("  ".getBytes(encoding));
			} catch (UnsupportedEncodingException e) {}
		}
		else {
			int baCMDLen = baCMD.length;
			if (baCMDLen == 1) {
				System.arraycopy(baCMD, 0x00, acbBytes, 0x02, 1);
				if (ebcdic == false) {
					acbBytes[0x03] = ' ';
				}
				else {
					acbBytes[0x03] = eSpace;
				}
			}
			else if (baCMDLen == 2) {
				System.arraycopy(baCMD, 0x00, acbBytes, 0x02, 2);
			}
			else {
				throw new AdabasException(acbDBID, String.format("setAcbCMD(): baCMD length GT 2: baCMD length = %d", baCMDLen));
			}
		}
		
		byte[]	newBaCMD	= new byte[2];
		System.arraycopy(acbBytes, 0x02, newBaCMD, 0x00, 2);
		try {
			this.acbCMD = new String(newBaCMD, encoding);
		} catch (UnsupportedEncodingException e) {}
	}

	/**
	 * Get the command ID.
	 * 
	 * @return the acbCID as a byte array.
	 */
	public byte[] getAcbCID() {
		
		System.arraycopy(acbBytes, 0x04, acbCID, 0, 4);             // command ID is	offset x04	length 4
		return acbCID;
	}

	/**
	 * Set the command ID.
	 * 
	 * @param	acbCID      	The acbCID to set.
	 *        					If acbCID is null then it will be filled with nulls(0x00).  
	 *        					If acbCID is less than 4 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbCID length GT 4.
	 */
	public void setAcbCID(byte [] acbCID) throws AdabasException {
		
		int acbCIDLen = 0;
		
		if (acbCID != null) {
			acbCIDLen = acbCID.length;
			if (acbCIDLen > 4) {
				throw new AdabasException(acbDBID, String.format("setAcbCID(): acbCID length GT 4: acbCID length = %d", acbCIDLen));
			}
		}

		/*
		 * If the acbCID has a length > 0
		 * 	Copy all the bytes in the byte array to this.acbCID array starting at offset 0x00.
		 */
		if (acbCIDLen > 0) {
			System.arraycopy(acbCID, 0, this.acbCID, 0x00, acbCIDLen);	   		
		}
		
		/*
		 * If the acbCID byte array was LT 4 bytes. 
		 *  then the remaining bytes in this.acbCID will be padded with zeroes. 
		 */
		if (acbCIDLen < 4) {
			System.arraycopy(bArrayZeroes, 0, this.acbCID, acbCIDLen, 4-acbCIDLen);
		}
	
		System.arraycopy(this.acbCID, 0, acbBytes, 0x04, 4);	   		// command ID is	offset x04	length 4	
	}

	/**
	 * Get the command ID as a String.
	 * 
	 * @return the acbCID as a String.
	 */
	public String getAcbCIDString() {
		
		byte[] acbCIDba = getAcbCID();									// get CID as byte array
		return new String(acbCIDba);									// return String from byte array
	}

	/**
	 * Set the command ID as a String.
	 * 
	 * @param	acbCID 			The acbCID to set.
	 *        					If acbCID is null or an empty string then it will be filled
	 *        					with spaces. If acbCID is less than 4 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbCID length GT 4.
	 */
	public void setAcbCID(String acbCID) throws AdabasException {
		
		int acbCIDLen = 0;
		
		if (acbCID != null) {
			if (!acbCID.isEmpty()) {
				acbCIDLen = acbCID.length();
				if (acbCIDLen > 4) {
					throw new AdabasException(acbDBID, String.format("setAcbCID(): acbCID length GT 4: acbCID length = %d", acbCIDLen));
				}
			}
		}

		/*
		 * If the acbCID has a length > 0
		 * 	Generate a byte array from the acbCID string.
		 *  Copy all the bytes in the byte array to this.acbCID array starting at offset 0x00.
		 */
		if (acbCIDLen > 0) {
			byte[] cid = acbCID.getBytes();								   
			System.arraycopy(cid, 0, this.acbCID, 0x00, acbCIDLen);	   		
		}
		
		/*
		 * If the acbCID string generated byte array was LT 4 bytes. 
		 *  then the remaining bytes in this.acbCID will be padded with spaces. 
		 */
		if (acbCIDLen < 4) {
			System.arraycopy(bArraySpaces, 0, this.acbCID, acbCIDLen, 4-acbCIDLen);
		}
	
		System.arraycopy(this.acbCID, 0, acbBytes, 0x04, 4);	   	// command ID is	offset x04	length 4	
	}
	
	/**
	 * Get the database ID.
	 * 
	 * DBID is really an unsigned short (2 bytes) with a value between 1 and 65535.
	 * Because Java unfortunately does not support any unsigned data types we must
	 * use the next large type (4 byte int) to correctly support positive values
	 * between 32768 and 65535. We actually store the DBID in this ACB class in a
	 * 2 byte short and perform Adabas calls using a byte array / ByteBuffer.
	 * But the user API sends a receives a 4 byte int with the correct positive values.
	 * 
	 * @return Database ID as an int.
	 */
	public int getAcbDBID() {
		
		// Because DBID's > 255 are put into the acbRSP field and are then overwritten on the return trip and
		// because Java XTS overwrites even DBID's < 255 in A1 return messages (as of 2012.12.11 - usadva)
		// we save acbDBID whenever it is set in a short and always just return that.
		// This can be different than what's in the ACB byte buffer and byte array.
		// Use ACBX to avoid that.
		
		return (int) this.acbDBID & 0x0000FFFF;						// make int with mask for correct positive values
	}
	
	/**
	 * Set the database ID.
	 * 
	 * If the DBID is GT 255 you will have to call this method again after each Adabas call 
	 * (because large DBID's are stored in the acbRSP field). Consider using the new ACBX
	 * control block to avoid this restriction.
	 * 
	 * DBID is really an unsigned short (2 bytes) with a value between 1 and 65535.
	 * Because Java unfortunately does not support any unsigned data types we must
	 * use the next larger type (4 byte int) to correctly support positive values
	 * between 32768 and 65535. We actually store the DBID in this ACB class in a
	 * 2 byte short and perform Adabas calls using a byte array / ByteBuffer.
	 * But the user API sends a receives a 4 byte int with the correct positive values.
	 * 
	 * @param 	acbDBID 		Database ID (target) to set.
	 * @throws 	AdabasException If acbDBID LT 1 or GT 65535.
	 */
	public void setAcbDBID(int acbDBID) throws AdabasException {
		
		if (acbDBID < 1 || acbDBID > 65535) {
			throw new AdabasException(acbDBID, "DBID LT 1 or GT 65535.");
		}
		
		short shortDBID = (short) (acbDBID & 0xFFFF);				// assign DBID to short with mask for large positive values
		
		this.acbDBID = shortDBID;
		
		if (acbDBID > 255) {										// if large DBID
			try {setAcbTYPE((byte) 0x30);}							// set call type 0x30
			catch (AdabasException ae) {}							// we won't get any error
			acbBB.putShort(0x0A, shortDBID);						// store 2 byte DBID at 0x0A (acbRSP)
		}
		else {														// else normal 1 byte DBID
			ByteBuffer 	bb 	= null;									// make a byte buffer
			byte[]		ba 	= new byte[2];							// make a byte array
			bb = ByteBuffer.wrap(ba);								// wrap buffer with array
			bb.putShort(shortDBID);									// write dbid into buffer / array
			System.arraycopy(ba, 1, acbBytes, 0x08, 1);				// copy right byte of short int to offset 0x08 (acbFNR)
		}
	}
	
	/**
	 * Get the file number.
	 * 
	 * @return File number.
	 */
	public int getAcbFNR() {
		
		if (acbTYPE == 0x30) {										// large DBID > 255 is in use
			this.acbFNR = acbBB.getShort(0x08);						// file number is	offset x08	length 2
		}
		else {														// else normal 1 byte file number at offset 0x09 (acbFNR + 1)
			this.acbFNR = acbBB.get(0x09);							// copy 1 byte to short int
		}
		return (int) acbFNR;
	}

	/**
	 * Set the file number.
	 * 
	 * @param 	acbFNR 			File number to set.
	 * @throws 	AdabasException If acbFNR LT 0 or GT 32000.
	 */
	public void setAcbFNR(int acbFNR) throws AdabasException {
		
		if (acbFNR < 0 || acbFNR > 32000) {
			throw new AdabasException(acbDBID, String.format("file number = %d; FNR LT 0 or GT 32000.", acbFNR));
		}
		
		short shortFNR	= (short) acbFNR;							// convert FNR to short
		this.acbFNR 	= shortFNR;									// assign  FNR in ACB
		
		if (acbTYPE == 0x30) { 										// large DBID > 255 is in use
			acbBB.putShort(0x08, shortFNR);							// file number is	offset x08	length 2
		}
		else {														// else normal 1 byte file number at offset 0x09 (acbFNR + 1)
			ByteBuffer 	bb 	= null;									// make a byte buffer
			byte[]		ba 	= new byte[2];							// make a byte array
			bb = ByteBuffer.wrap(ba);								// wrap buffer with array
			bb.putShort(shortFNR);									// write acbFNR into buffer / array
			System.arraycopy(ba, 1, acbBytes, 0x09, 1);				// copy 1 byte from right byte of short int
		}
	}

	/**
	 * Get the response code.
	 * 
	 * @return response code.
	 */
	public short getAcbRSP() {
		
		this.acbRSP = acbBB.getShort(0x0A);							// response code is	offset x0A	length 2
		return acbRSP;
	}

	/**
	 * Set the response code.
	 * 
	 * @param acbRSP response code to set.
	 */
	public void setAcbRSP(short acbRSP) {
		
		this.acbRSP = acbRSP;
		acbBB.putShort(0x0A, acbRSP);								// response code is	offset x0A	length 2
	}

	/**
	 * Get the ISN.
	 * 
	 * @return the acbISN.
	 */
	public long getAcbISN() {
	
		System.arraycopy(acbBytes, 0x0C, this.acbISN, 0, 4);	   	// ISN is	offset x0C	length 4
	    return ByteBuffer.wrap(this.acbISN).getInt() & 0xffffffffL;	// return ISN as a Long

	}

	/**
	 * Set the ISN.
	 * 
	 * @param 	acbISN        	The acbISN to set.
	 */
	public void setAcbISN(long acbISN) {
	
	    this.acbISN = convertLongTo4ByteArray(acbISN);
		System.arraycopy(this.acbISN, 0, acbBytes, 0x0C, 4);		// ISN is	offset x0C	length 4
	}

	/**
	 * Get the ISN lower limit.
	 * 
	 * @return the acbISL.
	 */
	public long getAcbISL() {
		
		System.arraycopy(acbBytes, 0x10, this.acbISL, 0, 4);	   	// ISL is	offset x10	length 4
	    return ByteBuffer.wrap(this.acbISL).getInt() & 0xffffffffL;	// return ISL as a Long
	}

	/**
	 * Set the ISN lower limit.
	 * 
	 * @param 	acbISL        	The acbISL to set.
	 * @throws 	AdabasException	If acbISL LT 0 or GT 4294967295.
	 */
	public void setAcbISL(long acbISL) throws AdabasException {
		
		if (acbISL < 0 || acbISL > 4294967295L) {
			throw new AdabasException(acbDBID, String.format("setAcbISL(): acbISL LT 0 or GT 4294967295: acbISL = %d", acbISL));
		}
		
        this.acbISL = convertLongTo4ByteArray(acbISL);
		System.arraycopy(this.acbISL, 0, acbBytes, 0x10, 4);		// ISL is	offset x10	length 4
	}

	/**
	 * Get the ISN quantity.
	 * 
	 * @return the acbISQ.
	 */
	public long getAcbISQ() {
		
		System.arraycopy(acbBytes, 0x14, this.acbISQ, 0, 4);	   	// ISQ is	offset x14	length 4
	    return ByteBuffer.wrap(this.acbISQ).getInt() & 0xffffffffL;	// return ISQ as a Long
	}

	/**
	 * Set the ISN quantity.
	 * 
	 * @param 	acbISQ        	The acbISQ to set.
	 * @throws 	AdabasException	If acbISQ LT 0 or GT 4294967295.
	 */
	public void setAcbISQ(long acbISQ) throws AdabasException {
		
		if (acbISQ < 0 || acbISQ > 4294967295l) {
			throw new AdabasException(acbDBID, String.format("setAcbISQ(): acbISQ LT 0 or GT 4294967295: acbISQ = %d", acbISQ));
		}

        this.acbISQ = convertLongTo4ByteArray(acbISQ);
		System.arraycopy(this.acbISQ, 0, acbBytes, 0x14, 4);		// ISQ is	offset x14	length 4
	}
	
	/**
	 * Get the format buffer length.
	 * 
	 * @return the acbFBL.
	 */
	public short getAcbFBL() {
		
		this.acbFBL = acbBB.getShort(0x18);							// FBL is			offset x18	length 2
		return acbFBL;
	}

	/**
	 * Set the format buffer length.
	 * 
	 * @param 	acbFBL       	The acbFBL to set.
	 * @throws 	AdabasException	If acbFBL LT 0.
	 */
	public void setAcbFBL(short acbFBL) throws AdabasException {
		
		if (acbFBL < 0) {
			throw new AdabasException(acbDBID, String.format("setAcbFBL(): acbFBL LT 0: acbFBL = %d", acbFBL));
		}

		this.acbFBL = acbFBL;
		acbBB.putShort(0x18, acbFBL);								// FBL is			offset x18	length 2
	}

	/**
	 * Get the record buffer length.
	 * 
	 * @return the acbRBL.
	 */
	public short getAcbRBL() {
		
		this.acbRBL = acbBB.getShort(0x1A);							// RBL is			offset x1A	length 2
		return acbRBL;
	}

	/**
	 * Set the record buffer length.
	 * 
	 * @param 	acbRBL       	The acbRBL to set.
	 * @throws 	AdabasException	If acbRBL LT 0.
	 */
	public void setAcbRBL(short acbRBL) throws AdabasException {

		if (acbRBL < 0) {
			throw new AdabasException(acbDBID, String.format("setAcbRBL(): acbRBL LT 0: acbRBL = %d", acbRBL));
		}

		this.acbRBL = acbRBL;
		acbBB.putShort(0x1A, acbRBL);								// RBL is			offset x1A	length 2
	}

	/**
	 * Get the search buffer length.
	 * 
	 * @return the acbSBL.
	 */
	public short getAcbSBL() {

		this.acbSBL = acbBB.getShort(0x1C);							// SBL is			offset x1C	length 2
		return acbSBL;
	}

	/**
	 * Set the search buffer length.
	 * 
	 * @param 	acbSBL       	The acbSBL to set.
	 * @throws 	AdabasException	If acbSBL LT 0.
	 */
	public void setAcbSBL(short acbSBL) throws AdabasException {
	
		if (acbSBL < 0) {
			throw new AdabasException(acbDBID, String.format("setAcbSBL(): acbSBL LT 0: acbSBL = %d", acbSBL));
		}

		this.acbSBL = acbSBL;
		acbBB.putShort(0x1C, acbSBL);								// SBL is			offset x1C	length 2
	}

	/**
	 * Get the value buffer length.
	 * 
	 * @return the acbVBL.
	 */
	public short getAcbVBL() {
		
		this.acbVBL = acbBB.getShort(0x1E);							// VBL is			offset x1E	length 2
		return acbVBL;
	}

	/**
	 * Set the value buffer length. 
	 * 
	 * @param 	acbVBL       	The acbVBL to set.
	 * @throws 	AdabasException	If acbVBL LT 0.
	 */
	public void setAcbVBL(short acbVBL) throws AdabasException {
	
		if (acbVBL < 0) {
			throw new AdabasException(acbDBID, String.format("setAcbVBL(): acbVBL LT 0: acbVBL = %d", acbVBL));
		}

		this.acbVBL = acbVBL;
		acbBB.putShort(0x1E, acbVBL);								// VBL is			offset x1E	length 2
	}

	/**
	 * Get the ISN buffer length.
	 * 
	 * @return the acbIBL.
	 */
	public short getAcbIBL() {
		
		this.acbIBL = acbBB.getShort(0x20);							// IBL is			offset x20	length 2
		return acbIBL;
	}

	/**
	 * Set the ISN buffer length.
	 * 
	 * @param 	acbIBL       	The acbIBL to set.
	 * @throws 	AdabasException	If acbIBL LT 0.
	 */
	public void setAcbIBL(short acbIBL) throws AdabasException {
		
		if (acbIBL < 0) {
			throw new AdabasException(acbDBID, String.format("setAcbIBL(): acbIBL LT 0: acbIBL = %d", acbIBL));
		}

		this.acbIBL = acbIBL;
		acbBB.putShort(0x20, acbIBL);								// IBL is			offset x20	length 2
	}

	/**
	 * Get command options 1.
	 * 
	 * @return command options 1.
	 */
	public byte getAcbCOP1() {
		
		this.acbCOP1 = acbBB.get(0x22);								// COP1 is			offset x22	length 1
		return acbCOP1;
	}

	/**
	 * Set command options 1.
	 * 
	 * @param acbCOP1 command options 1 to set.
	 */
	public void setAcbCOP1(byte acbCOP1) {
		
		this.acbCOP1 = acbCOP1;
		acbBB.put(0x22, acbCOP1);									// COP1 is			offset x22	length 1
	}

	/**
	 * Get command options 2.
	 * 
	 * @return command options 2.
	 */
	public byte getAcbCOP2() {
		
		this.acbCOP2 = acbBB.get(0x23);								// COP2 is			offset x23	length 1
		return acbCOP2;
	}

	/**
	 * Set command options 2.
	 * 
	 * @param acbCOP2 command options 2 to set.
	 */
	public void setAcbCOP2(byte acbCOP2) {
		
		this.acbCOP2 = acbCOP2;
		acbBB.put(0x23, acbCOP2);									// COP2 is			offset x23	length 1
	}

	/**
	 * Get additions 1.
	 * 
	 * @return acbADD1 as a byte array.
	 */
	public byte[] getAcbADD1() {
		
		System.arraycopy(acbBytes, 0x24, this.acbADD1, 0, 8);       // additions 1 is	offset x24	length 8
		return acbADD1;
	}

	/**
	 * Set additions 1.
	 * 
	 * @param 	acbADD1 		additions 1 to set.
	 *        					If acbADD1 is null then it will be set to all nulls(0x00).
	 *        					If acbADD1 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws 	AdabasException	If acbADD1 length GT 8
	 */
	public void setAcbADD1(byte[] acbADD1) throws AdabasException {

		int acbADD1Len = 0;
		
		if (acbADD1 != null) {
			acbADD1Len = acbADD1.length;
			if (acbADD1Len > 8) {
				throw new AdabasException(acbDBID, String.format("setAcbADD1(): acbADD1 length GT 8: acbADD1 length = %d", acbADD1Len));
			}
		}

		/*
		 * If the acbADD1 has a length > 0
		 * 	Copy all the bytes in the byte array to this.acbADD1 array starting at offset 0x00.
		 */
		if (acbADD1Len > 0) {
			System.arraycopy(acbADD1, 0, this.acbADD1, 0x00, acbADD1Len);	   		
		}
		
		/*
		 * If the acbADD1 byte array was LT 8 bytes. 
		 * 	then the remaining bytes in this.acbADD1 will be padded with zeroes. 
		 */
		if (acbADD1Len < 8) {
			System.arraycopy(bArrayZeroes, 0, this.acbADD1, acbADD1Len, 8-acbADD1Len);
		}
	
		System.arraycopy(this.acbADD1, 0, acbBytes, 0x24, 8);		// additions 1 is	offset x24	length 8
	}

	/**
	 * Get additions 1 as a String.
	 * 
	 * @return acbADD1 as a string.
	 */
	public String getAcbADD1String() {
		
		byte[]	acbADD1ba = getAcbADD1();							// get ADD1 byte array
		return 	new String(acbADD1ba);								// make new String from ADD1 byte array
	}

	/**
	 * Set additions 1 as a String.
	 * 
	 * @param	acbADD1 		Additions 1 to set.
	 *        					If acbADD1 is null or an empty string then it will be filled
	 *        					with spaces. If acbADD1 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws 	AdabasException	If acbADD1 length GT 8.
	 */
	public void setAcbADD1(String acbADD1) throws AdabasException {

		int acbADD1Len = 0;
		
		if (acbADD1 != null) {
			if (!acbADD1.isEmpty()) {
				acbADD1Len = acbADD1.length();
				if (acbADD1Len > 8) {
					throw new AdabasException(acbDBID, String.format("setAcbADD1(): acbADD1 length GT 8: acbADD1 length = %d", acbADD1Len));
				}
			}
		}

		/*
		 * If the acbADD1 has a length > 0
		 * 	Generate a byte array from the acbADD1 string.
		 * 	Copy all the bytes in the byte array to this.acbADD1 array starting at offset 0x00.
		 */
		if (acbADD1Len > 0) {
			byte[] add1 = acbADD1.getBytes();								   
			System.arraycopy(add1, 0, this.acbADD1, 0x00, acbADD1Len);	   		
		}
		
		/*
		 * If the acbADD1 string generated byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbADD1 will be padded with spaces. 
		 */
		if (acbADD1Len < 8) {
			System.arraycopy(bArraySpaces, 0, this.acbADD1, acbADD1Len, 8-acbADD1Len);
		}
		
		System.arraycopy(this.acbADD1, 0, acbBytes, 0x24, 8);		// additions 1 is	offset x24	length 8
	}

	/**
	 * Get additions 2.
	 * 
	 * @return additions 2.
	 */
	public byte[] getAcbADD2() {
		
		System.arraycopy(acbBytes, 0x2C, this.acbADD2, 0, 4);       // additions 2 is	offset x2C	length 4
		return acbADD2;
	}

	/**
	 * Set additions 2.
	 * 
	 * @param 	acbADD2  		additions 2 to set.
	 *        					If acbADD2 is null then it will be set to all nulls(0x00).
	 *        					If acbADD2 is less than 4 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws 	AdabasException	If acbADD2 length GT 4
	 */
	public void setAcbADD2(byte[] acbADD2) throws AdabasException {

		int acbADD2Len = 0;
		
		if (acbADD2 != null) {
			acbADD2Len = acbADD2.length;
			if (acbADD2Len > 4) {
				throw new AdabasException(acbDBID, String.format("setAcbADD2(): acbADD2 length GT 4: acbADD2 length = %d", acbADD2Len));
			}
		}

		/*
		 * If the acbADD2 has a length > 0
		 * 	Copy all the bytes in the byte array to this.acbADD2 array starting at offset 0x00.
		 */
		if (acbADD2Len > 0) {
			System.arraycopy(acbADD2, 0, this.acbADD2, 0x00, acbADD2Len);	   		
		}
		
		/*
		 * If the acbADD2 byte array was LT 4 bytes. 
		 * 	then the remaining bytes in this.acbADD2 will be padded with zeroes. 
		 */
		if (acbADD2Len < 4) {
			System.arraycopy(bArrayZeroes, 0, this.acbADD2, acbADD2Len, 4-acbADD2Len);
		}
	
		System.arraycopy(this.acbADD2, 0, acbBytes, 0x2C, 4);		// additions 2 is	offset x2C	length 4
	}

	/**
	 * Get additions 2 as a String.
	 * 
	 * @return additions 2 as a String.
	 */
	public String getAcbADD2String() {

		byte[]	acbADD2ba = getAcbADD2();							// get ADD2 byte array
		return 	new String(acbADD2ba);								// make new String from ADD2 byte array
	}

	/**
	 * Set additions 2 as a String.
	 * 
	 * @param 	acbADD2   		Additions 2 to set.
	 *        					If acbADD2 is null or an empty string then it will be filled
	 *        					with spaces. If acbADD2 is less than 4 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbADD2 length GT 4.
	 */
	public void setAcbADD2(String acbADD2) throws AdabasException {

		int acbADD2Len = 0;
		
		if (acbADD2 != null) {
			if (!acbADD2.isEmpty()) {
				acbADD2Len = acbADD2.length();
				if (acbADD2Len > 4) {
					throw new AdabasException(acbDBID, String.format("setAcbADD2(): acbADD2 length GT 4: acbADD2 length = %d", acbADD2Len));
				}
			}
		}

		/*
		 * If the acbADD2 has a length > 0
		 * 	Generate a byte array from the acbADD2 string.
		 * 	Copy all the bytes in the byte array to this.acbADD2 array starting at offset 0x00.
		 */
		if (acbADD2Len > 0) {
			byte[] add2 = acbADD2.getBytes();								   
			System.arraycopy(add2, 0, this.acbADD2, 0x00, acbADD2Len);	   		
		}
		
		/*
		 * If the acbADD2 string generated byte array was LT 4 bytes. 
		 *  then the remaining bytes in this.acbADD2 will be padded with spaces. 
		 */
		if (acbADD2Len < 4) {
			System.arraycopy(bArraySpaces, 0, this.acbADD2, acbADD2Len, 4-acbADD2Len);
		}
		
		System.arraycopy(this.acbADD2, 0, acbBytes, 0x2C, 4);		// additions 2 is	offset x2C	length 4
	}

	/**
	 * Get additions 2 as a long.
	 * 
	 * @return additions 2 as a a long.
	 */
	public long getAcbADD2Long() {
		
		int 	iAcbADD2 	= acbBB.getInt(0x2C);					// additions 2 is	offset x2C	length 4
		long	lAcbADD2	= iAcbADD2 & 0x00000000FFFFFFFFL;		// convert to long correcting for sign
		return	lAcbADD2;
	}
	
	/**
	 * Set additions 2 as a long.
	 * 
	 * @param	acbADD2			Additions 2 to set.
	 * @throws 	AdabasException	If acbxADD2 LT 0 or GT 4294967295.
	 */
	public void setAcbADD2(long acbADD2) throws AdabasException {
		
		if (acbADD2 < 0 || acbADD2 > 4294967295L) {
			throw new AdabasException(acbDBID, String.format("setAcbADD2(): acbADD2 LT 0 or GT 4294967295: acbADD2 = %d", acbADD2));
		}
		
		int iAcbADD2 = (int) (acbADD2 & 0x00000000FFFFFFFFL);		// capture lower 4 bytes of input ADD2
		acbBB.putInt(0x2C, iAcbADD2);								// additions 2 is	offset x2C	length 4
	}

	/**
	 * Get additions 3.
	 * 
	 * @return acbADD3 as a byte array.
	 */
	public byte[] getAcbADD3() {
		
		System.arraycopy(acbBytes, 0x30, this.acbADD3, 0, 8);       // additions 3 is	offset x30	length 8
		return acbADD3;
	}

	/**
	 * Set additions 3.
	 * 
	 * @param 	acbADD3 		additions 3 to set.
	 *        					If acbADD3 is null then it will be set to all nulls(0x00).
	 *        					If acbADD3 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbADD3 length GT 8.
	 */
	public void setAcbADD3(byte[] acbADD3) throws AdabasException {

		int acbADD3Len = 0;
		
		if (acbADD3 != null) {
			acbADD3Len = acbADD3.length;
			if (acbADD3Len > 8) {
				throw new AdabasException(acbDBID, String.format("setAcbADD3(): acbADD3 length GT 8: acbADD3 length = %d", acbADD3Len));
			}
		}

		/*
		 * If the acbADD3 has a length > 0
		 * 	Copy all the bytes in the byte array to this.acbADD3 array starting at offset 0x00.
		 */
		if (acbADD3Len > 0) {
			System.arraycopy(acbADD3, 0, this.acbADD3, 0x00, acbADD3Len);	   		
		}
		
		/*
		 * If the acbADD3 byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbADD3 will be padded with zeroes. 
		 */
		if (acbADD3Len < 8) {
			System.arraycopy(bArrayZeroes, 0, this.acbADD3, acbADD3Len, 8-acbADD3Len);
		}
	
		System.arraycopy(this.acbADD3, 0, acbBytes, 0x30, 8);		// additions 3 is	offset x30	length 8
	}

	/**
	 * Get additions 3 as a String.
	 * 
	 * @return acbADD3 as a String.
	 */
	public String getAcbADD3String() {
		
		byte[]	acbADD3ba = getAcbADD3();							// get ADD3 byte array
		return 	new String(acbADD3ba);								// make new String from ADD3 byte array
	}

	/**
	 * Set additions 3 as a String.
	 * 
	 * @param	acbADD3 		Additions 3 to set.
	 *        					If acbADD3 is null or an empty string then it will be filled
	 *        					with spaces. If acbADD3 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbADD3 length GT 8
	 */
	public void setAcbADD3(String acbADD3) throws AdabasException {

		int acbADD3Len = 0;
		
		if (acbADD3 != null) {
			if (!acbADD3.isEmpty()) {
				acbADD3Len = acbADD3.length();
				if (acbADD3Len > 8) {
					throw new AdabasException(acbDBID, String.format("setAcbADD3(): acbADD3 length GT 8: acbADD3 length = %d", acbADD3Len));
				}
			}
		}

		/*
		 * If the acbADD3 has a length > 0
		 * 	Generate a byte array from the acbADD3 string.
		 * 	Copy all the bytes in the byte array to this.acbADD3 array starting at offset 0x00.
		 */
		if (acbADD3Len > 0) {
			byte[] add3 = acbADD3.getBytes();								   
			System.arraycopy(add3, 0, this.acbADD3, 0x00, acbADD3Len);	   		
		}
		
		/*
		 * If the acbADD3 string generated byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbADD3 will be padded with spaces. 
		 */
		if (acbADD3Len < 8) {
			System.arraycopy(bArraySpaces, 0, this.acbADD3, acbADD3Len, 8-acbADD3Len);
		}
		
		System.arraycopy(this.acbADD3, 0, acbBytes, 0x30, 8);		// additions 3 is	offset x30	length 8
	}

	/**
	 * Get additions 4.
	 * 
	 * @return acbADD4 as a byte array.
	 */
	public byte[] getAcbADD4() {
		
		System.arraycopy(acbBytes, 0x38, this.acbADD4, 0, 8);       // additions 4 is	offset x38	length 8
		return acbADD4;
	}

	/**
	 * Set additions 4.
	 * 
	 * @param 	acbADD4 		additions 4 to set.
	 *        					If acbADD4 is null then it will be set to all nulls(0x00).
	 *        					If acbADD4 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbADD4 length GT 8.
	 */
	public void setAcbADD4(byte[] acbADD4) throws AdabasException {

		int acbADD4Len = 0;
		
		if (acbADD4 != null) {
			acbADD4Len = acbADD4.length;
			if (acbADD4Len > 8) {
				throw new AdabasException(acbDBID, String.format("setAcbADD4(): acbADD4 length GT 8: acbADD4 length = %d", acbADD4Len));
			}
		}

		/*
		 * If the acbADD4 has a length > 0
		 * 	Copy all the bytes in the byte array to this.acbADD4 array starting at offset 0x00.
		 */
		if (acbADD4Len > 0) {
			System.arraycopy(acbADD4, 0, this.acbADD4, 0x00, acbADD4Len);	   		
		}
		
		/*
		 * If the acbADD4 byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbADD4 will be padded with zeroes. 
		 */
		if (acbADD4Len < 8) {
			System.arraycopy(bArrayZeroes, 0, this.acbADD4, acbADD4Len, 8-acbADD4Len);
		}
	
		System.arraycopy(this.acbADD4, 0, acbBytes, 0x38, 8);		// additions 4 is	offset x38	length 8
	}

	/**
	 * Get additions 4 as a String.
	 * 
	 * @return acbADD4 as a String.
	 */
	public String getAcbADD4String() {

		byte[]	acbADD4ba = getAcbADD4();							// get ADD4 byte array
		return 	new String(acbADD4ba);								// make new String from ADD4 byte array
	}

	/**
	 * Set additions 4 as a String.
	 * 
	 * @param 	acbADD4 		Additions 4 to set.
	 *        					If acbADD4 is null or an empty string then it will be filled
	 *        					with spaces. If acbADD4 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbADD4 length GT 8.
	 */
	public void setAcbADD4(String acbADD4) throws AdabasException {

		int acbADD4Len = 0;
		
		if (acbADD4 != null) {
			if (!acbADD4.isEmpty()) {
				acbADD4Len = acbADD4.length();
				if (acbADD4Len > 8) {
					throw new AdabasException(acbDBID, String.format("setAcbADD4(): acbADD4 length GT 8: acbADD4 length = %d", acbADD4Len));
				}
			}
		}

		/*
		 * If the acbADD4 has a length > 0
		 * 	Generate a byte array from the acbADD4 string.
		 * 	Copy all the bytes in the byte array to this.acbADD4 array starting at offset 0x00.
		 */
		if (acbADD4Len > 0) {
			byte[] add4 = acbADD4.getBytes();								   
			System.arraycopy(add4, 0, this.acbADD4, 0x00, acbADD4Len);	   		
		}
		
		/*
		 * If the acbADD4 string generated byte array was LT 8 bytes. 
		 * 	then the remaining bytes in this.acbADD4 will be padded with spaces. 
		 */
		if (acbADD4Len < 8) {
			System.arraycopy(bArraySpaces, 0, this.acbADD4, acbADD4Len, 8-acbADD4Len);
		}
		
		System.arraycopy(this.acbADD4, 0, acbBytes, 0x38, 8);		// additions 4 is	offset x38	length 8
	}

	/**
	 * Get additions 5.
	 * 
	 * @return acbADD5 as a byte array.
	 */
	public byte[] getAcbADD5() {
		
		System.arraycopy(acbBytes, 0x40, acbADD5, 0, 8);			// additions 5 is	offset x40	length 8
		return acbADD5;
	}

	/**
	 * Set additions 5.
	 * 
	 * @param 	acbADD5 		Additions 5 to set.
	 *        					If acbADD5 is null then it will be set to all nulls(0x00).
	 *        					If acbADD5 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with nulls(0x00).  
	 * @throws	AdabasException	If acbADD5 length GT 8.
	 */
	public void setAcbADD5(byte[] acbADD5) throws AdabasException {

		int acbADD5Len = 0;
		
		if (acbADD5 != null) {
			acbADD5Len = acbADD5.length;
			if (acbADD5Len > 8) {
				throw new AdabasException(acbDBID, String.format("setAcbADD5(): acbADD5 length GT 8: acbADD5 length = %d", acbADD5Len));
			}
		}

		/*
		 * If the acbADD5 has a length > 0
		 *   Copy all the bytes in the byte array to this.acbADD5 array starting at offset 0x00.
		 */
		if (acbADD5Len > 0) {
			System.arraycopy(acbADD5, 0, this.acbADD5, 0x00, acbADD5Len);	   		
		}
		
		/*
		 * If the acbADD5 byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbADD5 will be padded with nulls(0x00). 
		 */
		if (acbADD5Len < 8) {
			System.arraycopy(bArrayZeroes, 0, this.acbADD5, acbADD5Len, 8-acbADD5Len);
		}
	
		System.arraycopy(this.acbADD5, 0, acbBytes,	0x40, 8);		// additions 5 is	offset x40	length 8
	}

	/**
	 * Get additions 5 as a String.
	 * 
	 * @return acbADD5 as a String.
	 */
	public String getAcbADD5String() {
		
		byte[]	acbADD5ba = getAcbADD5();							// get ADD5 byte array
		return 	new String(acbADD5ba);								// make new String from ADD5 byte array
	}

	/**
	 * Set additions 5 as a String.
	 * 
	 * @param 	acbADD5 		Additions 5 to set.
	 *        					If acbADD5 is null or an empty string then it will be filled
	 *        					with spaces. If acbADD5 is less than 8 bytes, then
	 *        					the remaining bytes will be filled with spaces.  
	 * @throws	AdabasException	If acbADD5 length GT 8.
	 */
	public void setAcbADD5(String acbADD5) throws AdabasException {

		int acbADD5Len = 0;
		
		if (acbADD5 != null) {
			if (!acbADD5.isEmpty()) {
				acbADD5Len = acbADD5.length();
				if (acbADD5Len > 8) {
					throw new AdabasException(acbDBID, String.format("setAcbADD5(): acbADD5 length GT 8: acbADD5 length = %d", acbADD5Len));
				}
			}
		}

		/*
		 * If the acbADD5 has a length > 0
		 *   Generate a byte array from the acbADD5 string.
		 *   Copy all the bytes in the byte array to this.acbADD5 array starting at offset 0x00.
		 */
		if (acbADD5Len > 0) {
			byte[] add5 = acbADD5.getBytes();								   
			System.arraycopy(add5, 0, this.acbADD5, 0x00, acbADD5Len);	   		
		}
		
		/*
		 * If the acbADD5 string generated byte array was LT 8 bytes. 
		 *  then the remaining bytes in this.acbADD5 will be padded with spaces. 
		 */
		if (acbADD5Len < 8) {
			System.arraycopy(bArraySpaces, 0, this.acbADD5, acbADD5Len, 8-acbADD5Len);
		}
		
		System.arraycopy(this.acbADD5, 0, acbBytes,	0x40, 8);		// additions 5 is	offset x40	length 8
	}

	/**
	 * Get the command time.
	 * 
	 * @return the acbCMDT.
	 */
	public long getAcbCMDT() {
	
		System.arraycopy(acbBytes, 0x48, this.acbCMDT, 0, 4);	   		// CMDT is		offset x48	length 4
	    return ByteBuffer.wrap(this.acbCMDT).getInt() & 0xffffffffL;	// return CMDT as a Long
	}

	/**
	 * Set the command time.
	 * 
	 * @param 	acbCMDT 		the acbCMDT to set.
	 * @throws	AdabasException	If acbCMDT LT 0 or GT 4294967295.
 	 */
	public void setAcbCMDT(long acbCMDT) throws AdabasException {
		
		if (acbCMDT < 0 || acbCMDT > 4294967295l) {
			throw new AdabasException(acbDBID, String.format("setAcbCMDT(): acbCMDT LT 0 or GT 4294967295: acbCMDT = %d", acbCMDT));
		}
		
        this.acbCMDT = convertLongTo4ByteArray(acbCMDT);
		System.arraycopy(this.acbCMDT, 0, acbBytes, 0x48, 4);		// CMDT is			offset x48	length 4
	}

	/**
	 * Get the user area.
	 * 
	 * @return the acbUSER.
	 */
	public byte[] getAcbUSER() {
		
		System.arraycopy(acbBytes, 0x4C, acbUSER, 0, 4);			// user area is		offset x4C	length 4
		return acbUSER;
	}

	/**
	 * Set the user area.
	 * 
	 * @param 	acbUSER 		the acbUSER to set.
 	 * @throws	AdabasException	If acbUSER length GT 4.
	 */
	public void setAcbUSER(byte[] acbUSER) throws AdabasException {
		
		int acbUSERLen = 0;
		
		if (acbUSER != null) {
			acbUSERLen = acbUSER.length;
			if (acbUSERLen > 4) {
				throw new AdabasException(acbDBID, String.format("setAcbUSER(): acbUSER length GT 4: acbUSER length = %d", acbUSERLen));
			}
		}
			
		/*
		 * If the acbUSER has a length > 0
		 * 	Copy all the bytes in the byte array to this.acbUSER array starting at offset 0x00.
		 */
		if (acbUSERLen > 0) {
			System.arraycopy(acbUSER, 0, this.acbUSER, 0x00, acbUSERLen);	   		
		}

		/*
		 * If the acbUSER byte array was LT 4 bytes. 
		 *  then the remaining bytes in this.acbUSER will be padded with nulls. 
		 */
		if (acbUSERLen < 4)
			System.arraycopy(bArrayZeroes, 0, this.acbUSER, acbUSERLen, 4-acbUSERLen);	 
	
		System.arraycopy(this.acbUSER, 0, acbBytes, 0x4C, 4);	   	// user area is		offset x4C	length 4
	}
	
	/**
	 * Get host name.
	 * 
	 * Host name is an additional value beyond the traditional ACB. It is used
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
	 * Host name is an additional value beyond the traditional ACB. It is used
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
	 * Get EBCDIC encoding flag.
	 * 
	 * @return	True if EBCDIC encoding used, false if not.
	 */
	public boolean isEbcdic() {
		return ebcdic;
	}

	/**
	 * Set EBCDIC encoding flag.
	 * 
	 * @param ebcdic	EBCDIC encoding flag value: true if EBCDIC used, false if not.
	 */
	public void setEbcdic(boolean ebcdic) {
		this.ebcdic = ebcdic;
	}

	/**
	 * Get the whole ACB as a formatted / displayable string.
	 * 
	 * @return the ACB
	 */
	public String toString() {
		
		String result = "Adabas Control Block = \n";
		
		result += "   acbTYPE  = " 	+ String.format("0x%02x\n", this.acbTYPE);
		result += "   acbCMD   = " 	+ this.acbCMD  												+ "\n";
		result += "   acbCID   = 0x"	+ byteArrayToHexString(this.acbCID)  						+ "\n";
		result += "   acbDBID  = " 	+ this.getAcbDBID()											+ "\n";
		result += "   acbFNR   = " 	+ this.acbFNR  												+ "\n";
		result += "   acbRSP   = " 	+ this.acbRSP  												+ "\n";
		result += "   acbISN   = " 	+ (ByteBuffer.wrap(this.acbISQ).getInt() & 0xffffffffL)		+ "\n";	// return ISN 4 byte array as unsigned int 
		result += "   acbISL   = " 	+ (ByteBuffer.wrap(this.acbISL).getInt() & 0xffffffffL)		+ "\n";	// return ISL 4 byte array as unsigned int 
		result += "   acbISQ   = " 	+ (ByteBuffer.wrap(this.acbISQ).getInt() & 0xffffffffL)		+ "\n";	// return ISQ 4 byte array as unsigned int
		result += "   acbFBL   = " 	+ this.acbFBL  												+ "\n";
		result += "   acbRBL   = " 	+ this.acbRBL  												+ "\n";
		result += "   acbSBL   = " 	+ this.acbSBL  												+ "\n";
		result += "   acbVBL   = " 	+ this.acbVBL  												+ "\n";
		result += "   acbIBL   = " 	+ this.acbIBL  												+ "\n";
		result += "   acbCOP1  = 0x" + String.format("%02X",this.acbCOP1) 						+ "\n";
		result += "   acbCOP2  = 0x" + String.format("%02X",this.acbCOP2)						+ "\n";
		result += "   acbADD1  = 0x" + byteArrayToHexString(this.acbADD1) 						+ "\n";
		result += "   acbADD2  = 0x" + byteArrayToHexString(this.acbADD2) 						+ "\n";
		result += "   acbADD3  = 0x" + byteArrayToHexString(this.acbADD3) 						+ "\n";
		result += "   acbADD4  = 0x" + byteArrayToHexString(this.acbADD4) 						+ "\n";
		result += "   acbADD5  = 0x" + byteArrayToHexString(this.acbADD5) 						+ "\n";
		result += "   acbCMDT  = " 	+ (ByteBuffer.wrap(this.acbCMDT).getInt() & 0xffffffffL)	+ "\n";	// return CMDT 4 byte array as unsigned int
		result += "   acbUSER  = 0x" + byteArrayToHexString(this.acbUSER)        				+ "\n";
		
		if (this.hostName == null) 	{result += "   hostName = null"								+ "\n";}
		else 						{result += "   hostname = " + this.hostName					+ "\n";}
		
		result += "   ebcdic   = " + String.format("%b", ebcdic)								+ "\n";
		result += "   encoding = " + encoding;

		return result;
	}
	
	/**
	 * Convert a byte array to a string of display hex characters.
	 * 
	 * @param 	ba byte array
	 * @return 	String of hex characters
	 */
	public String byteArrayToHexString(byte[] ba) {
	   
		StringBuilder sb = new StringBuilder();						// make a string builder
		
		for(byte b : ba) {											// loop through bytes
			sb.append(String.format("%02X", b&0xff));				// format each as hex char and append
		}
		
		return sb.toString();
	}
	
	/**
	 * Convert the low order 4 bytes of a long to a 4 byte array.
	 * 
	 * @param 	l long value to convert
	 * @return 	byte array
	 */
	public static byte[] convertLongTo4ByteArray(long l) {
		
		byte[] b = new byte [4];
		
		b[0] = (byte)(l >>> 24);
		b[1] = (byte)(l >>> 16);
		b[2] = (byte)(l >>> 8);
		b[3] = (byte)(l >>> 0);
	    
		return b;
	}
	
	/**
	 * Reset ACB to binary zeroes.
	 */
	public void resetACB() {

		for (int i=0; i < ACB_LENGTH; i++) {
			acbBytes[i] = 0x00;
		}
		
		this.getAcbTYPE();
		this.getAcbCMD();
		this.getAcbCID();
		this.getAcbDBID();
		this.getAcbFNR();
		this.getAcbRSP();
		this.getAcbISN();
		this.getAcbISL();
		this.getAcbISQ();
		this.getAcbFBL();
		this.getAcbRBL();
		this.getAcbSBL();
		this.getAcbVBL();
		this.getAcbIBL();
		this.getAcbCOP1();
		this.getAcbCOP2();
		this.getAcbADD1();
		this.getAcbADD2();
		this.getAcbADD3();
		this.getAcbADD4();
		this.getAcbADD5();
		this.getAcbCMDT();
		this.getAcbUSER();
		
		this.hostName = null;
	}
}
