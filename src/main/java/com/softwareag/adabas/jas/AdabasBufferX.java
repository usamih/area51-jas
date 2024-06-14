package com.softwareag.adabas.jas;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Class implementing extended Adabas buffers for use with ACBX direct calls.
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

public class AdabasBufferX {
	
	private short		abdLEN;					// ABD length					offset x00	length 2
	private short		abdVER;					// version indicator			offset x02	length 2
	private byte		abdID;					// buffer type ID				offset x04	length 1
//	private byte		abdRSV1;				// reserved 1					offset x05	length 1
	private byte		abdBUF;					// buffer usage flags from LCT	offset x05	length 1	// see MACLIB(ADABDE)
	private byte		abdLOC;					// buffer location flag			offset x06	length 1
// 	private byte		abdRSV2;				// reserved 2					offset x07	length 1
//	private int			abdRSV3;				// reserved 3					offset x08	length 4
//	private int			abdRSV4;				// reserved 4					offset x0C	length 4	
	private long		abdSIZE;				// buffer size					offset x10	length 8
	private long		abdSEND;				// data length to send			offset x18	length 8
	private long		abdRECV;				// data length received			offset x20	length 8
// 	private int			abdRSV5;				// reserved 5					offset x28	length 4
//	private int			abdADR;					// indirect address not used	offset x2C	length 4
	private byte[]		abdBA;					// buffer description as byte array
	private ByteBuffer	abdBB;					// buffer description as byte buffer
	private byte[]		dataBA;					// user data as byte array
	private ByteBuffer	dataBB;					// user data as byte buffer
	private	boolean		ebcdic;					// EBCDIC flag
	private String		encoding;				// encoding string
	
	public static final int ABD_LENGTH = 0x30;	// ABD length					always 0x30 (48)
	
	public 	static final byte FB	= (byte) 0x46; 	// format         buffer type original
	public 	static final byte RB 	= (byte) 0x52; 	// record         buffer type original
	public 	static final byte SB 	= (byte) 0x53; 	// search         buffer type original
	public 	static final byte VB 	= (byte) 0x56; 	// value          buffer type original
	public 	static final byte IB 	= (byte) 0x49; 	// ISN            buffer type original
	public 	static final byte MB 	= (byte) 0x4D; 	// multifetch     buffer type original
	private	static final byte aFB 	= (byte) 0x46; 	// 'F' format     buffer type ID ASCII
	private static final byte aRB 	= (byte) 0x52; 	// 'R' record     buffer type ID ASCII
	private static final byte aSB 	= (byte) 0x53; 	// 'S' search     buffer type ID ASCII
	private static final byte aVB 	= (byte) 0x56; 	// 'V' value      buffer type ID ASCII
	private static final byte aIB 	= (byte) 0x49; 	// 'I' ISN        buffer type ID ASCII
	private static final byte aMB 	= (byte) 0x4D; 	// 'M' multifetch buffer type ID ASCII
	private static final byte eFB 	= (byte) 0xC6; 	// 'F' format     buffer type ID EBCDIC
	private static final byte eRB 	= (byte) 0xD9; 	// 'R' record     buffer type ID EBCDIC
	private static final byte eSB 	= (byte) 0xE2; 	// 'S' search     buffer type ID EBCDIC
	private static final byte eVB 	= (byte) 0xE5; 	// 'V' value      buffer type ID EBCDIC
	private static final byte eIB 	= (byte) 0xC9; 	// 'I' ISN        buffer type ID EBCDIC
	private static final byte eMB 	= (byte) 0xD4; 	// 'M' multifetch buffer type ID ASCII
	
	// buffer usage flags from LCT Legal Command Table
	// see 'ADABAS.SRC.W.MACLIB(ADABDE)'
	// see 'ADABAS.SRC.W.MACLIB.DEP(LCT)'
	
	public static final byte IN		= (byte) 0x80;	// send buffer on command input
	public static final byte OUT	= (byte) 0x40;	// send buffer on command output

	/**
	 * Constructor taking a size and type.

	 * @param	size	Buffer size.
	 * @param	type 	One the static defines:
	 * 
	 * AdabasBufferX.FB - format buffer
	 * AdabasBufferX.RB - record buffer
	 * AdabasBufferX.SB - search buffer
	 * AdabasBufferX.VB - value buffer
	 * AdabasBufferX.IB - ISN buffer
	 * AdabasBufferX.MB - multifetch buffer
	 * 
	 * @throws AdabasException	Adabas specific exception.
	 */
	public AdabasBufferX(int size, byte type) throws AdabasException {
		
		this(size, type, false);
	}
	
	/**
	 * Constructor taking a size, a type, and an EBCDIC flag.
	 *
	 * @param	size	Buffer size.
	 * @param	type 	One the static defines:
	 * 
	 * AdabasBufferX.FB - format buffer
	 * AdabasBufferX.RB - record buffer
	 * AdabasBufferX.SB - search buffer
	 * AdabasBufferX.VB - value buffer
	 * AdabasBufferX.IB - ISN buffer
	 * AdabasBufferX.MB - multifetch buffer
	 *
	 * @param	ebcdic	EBCDIC flag.
	 * 
	 * @throws 			AdabasException	Adabas specific exception. 
	 */
	public AdabasBufferX(int size, byte type, boolean ebcdic) throws AdabasException {
	
		if (type != FB && type != RB && type != SB && type != VB && type != IB && type != MB) {
			throw new AdabasException(String.format("Invalid ABD type = 0x%2x", type));
		}
		
		if (ebcdic == false) {
			this.ebcdic 	= false;
			this.encoding 	= "ISO-8859-1";
		}
		else {
			this.ebcdic 	= true;
			this.encoding 	= "cp037";			
		}

		if (size < 0) {
			throw new AdabasException(String.format("SIZE LT 0; SIZE = %d.", size));
		}

		abdLEN 	= ABD_LENGTH;										// header length always 0x30 (48)
		if (ebcdic == false ) 	{abdVER	= (short) 0x4732;}			// 'G2' means new extended ABD structure
		else					{abdVER = (short) 0xC7F2;}

		if (ebcdic == false) {										// set buffer type ID
			if 		(type == FB)	abdID = aFB;
			else if (type == RB)	abdID = aRB;
			else if (type == SB)	abdID = aSB;
			else if (type == VB)	abdID = aVB;
			else if (type == IB)	abdID = aIB;
			else if (type == MB)	abdID = aMB;
		}
		else {
			if 		(type == FB)	abdID = eFB;
			else if (type == RB)	abdID = eRB;
			else if (type == SB)	abdID = eSB;
			else if (type == VB)	abdID = eVB;
			else if (type == IB)	abdID = eIB;
			else if (type == MB)	abdID = eMB;			
		}

		if (ebcdic == false ) 	{abdLOC	= (byte) 0x49;}				// 'I' always indirect
		else					{abdLOC = (byte) 0xC9;}

		abdSIZE	= size;												// set buffer size
		abdSEND	= size;												// send    whole buffer
		abdRECV	= size;												// receive whole buffer
		abdBA	= new byte[abdLEN];									// allocate description byte array
		abdBB	= ByteBuffer.wrap(abdBA);							// wrap     description byte buffer in byte array
		dataBA	= new byte[size];									// allocate user data byte array
		dataBB	= ByteBuffer.wrap(dataBA);							// wrap     user data byte buffer in byte array
		
		abdBB.putShort(0x00, abdLEN);								// ABD length					offset x00	length 2
		abdBB.putShort(0x02, abdVER);								// version indicator			offset x02	length 2
		abdBB.put(0x04, abdID);										// buffer type ID				offset x04	length 1
		abdBB.put(0x06, abdLOC);									// buffer location flag			offset x06	length 1
		abdBB.putLong(0x10, abdSIZE);								// buffer size					offset x10	length 8
		abdBB.putLong(0x18, abdSEND);								// data length to send			offset x18	length 8
		abdBB.putLong(0x20, abdRECV);								// data length received			offset x20	length 8
	}
	
	/**
	 * Constructor taking a byte array content initializer and a type.
	 * 
	 * @param contentBA 	Content initializer.
	 * @param type 			One the static defines:
	 * 
	 * AdabasBufferX.FB - format buffer
	 * AdabasBufferX.RB - record buffer
	 * AdabasBufferX.SB - search buffer
	 * AdabasBufferX.VB - value buffer
	 * AdabasBufferX.IB - ISN buffer
	 * AdabasBufferX.MB - multifetch buffer
	 *
	 * @throws	AdabasException	Adabas specific exception.
	 */
	public AdabasBufferX(byte[] contentBA, byte type) throws AdabasException {

		this(contentBA, type, false);
	}
	
	/**
	 * Constructor taking a byte array content initializer, a type, and an EBCDIC flag.
	 * 
	 * @param contentBA 	Content initializer.
	 * @param type 			One the static defines:
	 * 
	 * AdabasBufferX.FB - format buffer
	 * AdabasBufferX.RB - record buffer
	 * AdabasBufferX.SB - search buffer
	 * AdabasBufferX.VB - value buffer
	 * AdabasBufferX.IB - ISN buffer
	 * AdabasBufferX.MB - multifetch buffer
	 * 
	 * @param ebcdic	EBCDIC flag.
	 * 
	 * @throws			AdabasException	Adabas specific exception.

	 */
	public AdabasBufferX(byte[] contentBA, byte type, boolean ebcdic) throws AdabasException {
		
		int		size		= contentBA.length;						// get size of content
		
		if (type != FB && type != RB && type != SB && type != VB && type != IB && type != MB) {
			throw new AdabasException(String.format("Invalid ABD type = 0x%2x", type));
		}
		
		if (ebcdic == false) {
			this.ebcdic 	= false;
			this.encoding 	= "ISO-8859-1";
		}
		else {
			this.ebcdic 	= true;
			this.encoding 	= "cp037";			
		}

		abdLEN 	= ABD_LENGTH;										// header length always 0x30 (48)
		if (ebcdic == false ) 	{abdVER	= (short) 0x4732;}			// 'G2' means new extended ABD structure
		else					{abdVER = (short) 0xC7F2;}

		if (ebcdic == false) {										// set buffer type ID
			if 		(type == FB)	abdID = aFB;
			else if (type == RB)	abdID = aRB;
			else if (type == SB)	abdID = aSB;
			else if (type == VB)	abdID = aVB;
			else if (type == IB)	abdID = aIB;
			else if (type == MB)	abdID = aMB;
		}
		else {
			if 		(type == FB)	abdID = eFB;
			else if (type == RB)	abdID = eRB;
			else if (type == SB)	abdID = eSB;
			else if (type == VB)	abdID = eVB;
			else if (type == IB)	abdID = eIB;
			else if (type == MB)	abdID = eMB;			
		}
		
		if (ebcdic == false ) 	{abdLOC	= (byte) 0x49;}				// 'I' always indirect
		else					{abdLOC = (byte) 0xC9;}
		abdSIZE	= size;												// set buffer size
		abdSEND	= size;												// send    whole buffer
		abdRECV	= size;												// receive whole buffer
		abdBA	= new byte[abdLEN];									// allocate description byte array
		abdBB	= ByteBuffer.wrap(abdBA);							// wrap     description byte buffer in byte array
		dataBA	= new byte[size];									// allocate user data byte array
		dataBB	= ByteBuffer.wrap(dataBA);							// wrap     user data byte buffer in byte array
		
		abdBB.putShort(0x00, abdLEN);								// ABD length					offset x00	length 2
		abdBB.putShort(0x02, abdVER);								// version indicator			offset x02	length 2
		abdBB.put(0x04, abdID);										// buffer type ID				offset x04	length 1
		abdBB.put(0x06, abdLOC);									// buffer location flag			offset x06	length 1
		abdBB.putLong(0x10, abdSIZE);								// buffer size					offset x10	length 8
		abdBB.putLong(0x18, abdSEND);								// data length to send			offset x18	length 8
		abdBB.putLong(0x20, abdRECV);								// data length received			offset x20	length 8
		
		System.arraycopy(contentBA, 0, dataBA, 0, size);			// copy content to user data
	}
	
	/**
	 * Constructor taking a string content initializer and a type.
	 * 
	 * @param content content initializer.
	 * @param type is one the static defines:
	 * 
	 * AdabasBufferX.FB - format buffer
	 * AdabasBufferX.RB - record buffer
	 * AdabasBufferX.SB - search buffer
	 * AdabasBufferX.VB - value buffer
	 * AdabasBufferX.IB - ISN buffer
	 * AdabasBufferX.MB - multifetch buffer
	 *
	 * @throws	AdabasException	Adabas specific exception.
	 */
	public AdabasBufferX(String content, byte type) throws AdabasException {
		
		this(content.getBytes(), type, false);						// convert string to byte array and invoke constructor
	}
	
	/**
	 * Constructor taking a string content initializer, a type, and an EBCDIC flag.
	 * 
	 * @param content 	Content initializer.
	 * @param type 		One the static defines:
	 * 
	 * AdabasBufferX.FB - format buffer
	 * AdabasBufferX.RB - record buffer
	 * AdabasBufferX.SB - search buffer
	 * AdabasBufferX.VB - value buffer
	 * AdabasBufferX.IB - ISN buffer
	 * AdabasBufferX.MB - multifetch buffer
	 * 
	 * @param 	ebcdic	EBCDIC flag
	 * 
	 * @throws	AdabasException	Adabas specific exception.
	 */
	public AdabasBufferX(String content, byte type, boolean ebcdic) throws AdabasException {
		
		byte[] 	contentBA 	= new byte[content.length()];
		
		int		size		= contentBA.length;						// get size of content
		
		if (type != FB && type != RB && type != SB && type != VB && type != IB && type != MB) {
			throw new AdabasException(String.format("Invalid ABD type = 0x%2x", type));
		}
		
		if (ebcdic == false) {
			this.ebcdic 	= false;
			this.encoding 	= "ISO-8859-1";
		}
		else {
			this.ebcdic 	= true;
			this.encoding 	= "cp037";			
		}
		
		try {
			contentBA = content.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {}

		abdLEN 	= ABD_LENGTH;										// header length always 0x30 (48)
		if (ebcdic == false ) 	{abdVER	= (short) 0x4732;}			// 'G2' means new extended ABD structure
		else					{abdVER = (short) 0xC7F2;}

		if (ebcdic == false) {										// set buffer type ID
			if 		(type == FB)	abdID = aFB;
			else if (type == RB)	abdID = aRB;
			else if (type == SB)	abdID = aSB;
			else if (type == VB)	abdID = aVB;
			else if (type == IB)	abdID = aIB;
			else if (type == MB)	abdID = aMB;
		}
		else {
			if 		(type == FB)	abdID = eFB;
			else if (type == RB)	abdID = eRB;
			else if (type == SB)	abdID = eSB;
			else if (type == VB)	abdID = eVB;
			else if (type == IB)	abdID = eIB;
			else if (type == MB)	abdID = eMB;			
		}
				
		if (ebcdic == false ) 	{abdLOC	= (byte) 0x49;}				// 'I' always indirect
		else					{abdLOC = (byte) 0xC9;}
		
		abdSIZE	= size;												// set buffer size
		abdSEND	= size;												// send    whole buffer
		abdRECV	= size;												// receive whole buffer
		abdBA	= new byte[abdLEN];									// allocate description byte array
		abdBB	= ByteBuffer.wrap(abdBA);							// wrap     description byte buffer in byte array
		dataBA	= new byte[size];									// allocate user data byte array
		dataBB	= ByteBuffer.wrap(dataBA);							// wrap     user data byte buffer in byte array
		
		abdBB.putShort(0x00, abdLEN);								// ABD length					offset x00	length 2
		abdBB.putShort(0x02, abdVER);								// version indicator			offset x02	length 2
		abdBB.put(0x04, abdID);										// buffer type ID				offset x04	length 1
		abdBB.put(0x06, abdLOC);									// buffer location flag			offset x06	length 1
		abdBB.putLong(0x10, abdSIZE);								// buffer size					offset x10	length 8
		abdBB.putLong(0x18, abdSEND);								// data length to send			offset x18	length 8
		abdBB.putLong(0x20, abdRECV);								// data length received			offset x20	length 8
		
		System.arraycopy(contentBA, 0, dataBA, 0, size);			// copy content to user data
	}
	
	/**
	 * Get buffer description header length.
	 * 
	 * @return buffer header length.
	 */
	public short getAbdLEN() {
		
		this.abdLEN = abdBB.getShort(0x00);							// ABD length					offset x00	length 2
		return abdLEN;
	}
	
	/**
	 * Get buffer description version.
	 * 
	 * @return buffer description version.
	 */
	public short getAbdVER() {
		
		abdVER = abdBB.getShort(0x02);								// version indicator			offset x02	length 2
		return abdVER;
	}
	
	/**
	 * Get buffer type ID.
	 * 
	 * Buffer type ID is one the static defines:
	 * 
	 * AdabasBufferX.FB - format buffer
	 * AdabasBufferX.RB - record buffer
	 * AdabasBufferX.SB - search buffer
	 * AdabasBufferX.VB - value buffer
	 * AdabasBufferX.IB - ISN buffer
	 * AdabasBufferX.MB - multifetch buffer
	 * 
	 * @return buffer type ID.
	 */
	public byte getAbdID() {
		
		abdID = abdBB.get(0x04);									// buffer type ID				offset x04	length 1
		return abdID;
	}
	
	/**
	 * Set buffer type ID.
	 * 
	 * Buffer type ID is one the static defines:
	 * 
	 * AdabasBufferX.FB - format buffer
	 * AdabasBufferX.RB - record buffer
	 * AdabasBufferX.SB - search buffer
	 * AdabasBufferX.VB - value buffer
	 * AdabasBufferX.IB - ISN buffer
	 * AdabasBufferX.MB - multifetch buffer
	 * 
	 * @param id 	Buffer type ID.
	 * 
	 * @throws		AdabasException	Adabas specific exception.
	 */
	public void setAbdID(byte id) throws AdabasException {
		
		if (id != FB && id != RB && id != SB && id != VB && id != IB && id != MB) {
			throw new AdabasException(String.format("Invalid ABD type = 0x%2x", id));
		}
	
		abdID = id;
		abdBB.put(0x04, abdID);										// buffer type ID				offset x04	length 1
	}
	
	/**
	 * Get buffer usage flags.
	 * 
	 * Buffer usage flags can be one or more of the static defines:
	 * 
	 *  AdabasBufferX.IN 	- send buffer on input.
	 *  AdabasBufferX.OUT	- send buffer on output
	 *  
	 * @return buffer usage flags
	 */
	public byte getAbdBUF() {
		
		abdBUF = abdBB.get(0x05);
		return abdBUF;
	}

	/**
	 * Set buffer usage flags.
	 * 
	 * Buffer usage flags can be one or more of the static defines:
	 * 
	 *  AdabasBufferX.IN 	- send buffer on input.
	 *  AdabasBufferX.OUT	- send buffer on output
	 *  
	 * @param buf buffer usage flags
	 */
	public void setAbdBUF(byte buf) {
		
		abdBUF = buf;
		abdBB.put(0x05, abdBUF);
	}

	/**
	 * Get buffer location flag.
	 * 
	 * @return buffer location flag.
	 */
	public byte getAbdLOC() {
		
		abdLOC = abdBB.get(0x06);									// buffer location flag			offset x06	length 1
		return abdLOC;
	}
	
	/**
	 * Get buffer size.
	 * 
	 * @return buffer size.
	 */
	public int getAbdSIZE() {
		
		abdSIZE = abdBB.getLong(0x10);								// buffer size					offset x10	length 8
		return (int) abdSIZE;										// TODO make sure it fits
	}
	
	/**
	 * Set buffer size.
	 * 
	 * @param size 	Buffer size.
	 * 
	 * @throws		AdabasException	Adabas specific exception.
	 */
	public void setAbdSIZE(int size) throws AdabasException {
		
		if (size < 0) {
			throw new AdabasException(String.format("SIZE LT 0; SIZE = %d.", size));	
		}
		
		if (size > abdSIZE) {
			throw new AdabasException(String.format("SIZE GT allocated buffer length, SIZE = %d; Allocated buffer length = %d.", size, abdSIZE));	
		}

		abdSIZE = size;
		abdBB.putLong(0x10, abdSIZE);								// buffer size					offset x10	length 8
	}
	
	/**
	 * Get data length to send.
	 * 
	 * @return data length to send.
	 */
	public int getAbdSEND() {
		
		abdSEND = abdBB.getLong(0x18);								// data length to send			offset x18	length 8
		return (int) abdSEND;										// TODO make sure it fits
	}
	
	/**
	 * Set data length to send.
	 * 
	 * @param send 	Data length to send.
	 * 
	 * @throws		AdabasException	Adabas specific exception.
	 */
	public void setAbdSEND(int send) throws AdabasException {
		
		if (send < 0) {
			throw new AdabasException(String.format("SEND LT 0; SEND = %d.", send));	
		}
		
		if (send > abdSIZE) {
			throw new AdabasException(String.format("SEND GT allocated buffer length, SEND = %d; Allocated buffer length = %d.", send, abdSIZE));	
		}

		abdSEND = send;
		abdBB.putLong(0x18, abdSEND);								// data length to send			offset x18	length 8
	}
	
	/**
	 * Get data length received.
	 * 
	 * @return data length received.
	 */
	public int getAbdRECV() {
		
		abdRECV = abdBB.getLong(0x20);								// data length received			offset x20	length 8
		return (int) abdRECV;										// TODO make sure it fits
	}
	
	/**
	 * Set data length received.
	 * 
	 * @param recv 	Data length received.
	 * 
	 * @throws		AdabasException	Adabas specific exception.
	 */
	public void setAbdRECV(int recv) throws AdabasException {
		
		if (recv < 0) {
			throw new AdabasException(String.format("RECV LT 0; RECV = %d.", recv));	
		}
		
		if (recv > abdSIZE) {
			throw new AdabasException(String.format("RECV GT allocated buffer length, RECV = %d; Allocated buffer length = %d.", recv, abdSIZE));	
		}

		abdRECV = recv;
		abdBB.putLong(0x20, abdRECV);								// data length received			offset x20	length 8
	}
	
	/**
	 * Get Adabas Buffer Description (ABD) byte array.
	 * 
	 * @return ABD byte array.
	 */
	public byte[] getABDBytes() {
		
		return abdBA;												// buffer description as byte array
	}
	
	/**
	 * Get Adabas Buffer Description (ABD) byte buffer.
	 * 
	 * @return ABD byte buffer.
	 */
	public ByteBuffer getABDBuffer() {
		
		return abdBB;												// buffer description as byte buffer
	}
	
	/**
	 * Get user data byte array.
	 * 
	 * @return user data byte array.
	 */
	public byte[] getDataBytes() {
		
		return dataBA;												// user data as byte array
	}
	
	/**
	 * Get user data byte buffer.
	 * 
	 * @return user data byte buffer.
	 */
	public ByteBuffer getDataBuffer() {
		
		return dataBB;												// user data as byte buffer
	}

	/**
	 * Get EBCDIC flag.
	 * 
	 * @return	EBCDIC flag.
	 */
	public boolean isEbcdic() {
		return ebcdic;
	}

	/**
	 * Set EBCDIC flag.
	 * 
	 * @param ebcdic	EBCDIC flag.
	 */
	public void setEbcdic(boolean ebcdic) {
		
		this.ebcdic = ebcdic;
		
		// TODO	set already defined instance values per encoding.
		
		if (ebcdic == false) {
			encoding = "ISO-8859-1";
		}
		else {
			encoding = "cp037";
		}
		
	}
}
