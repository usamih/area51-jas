package com.softwareag.adabas.jas.xts;

/**
 * Class containing various bit definitions needed for XTS communication.
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

public class XtsDefinitions {

	public static final byte	A1_CONTROL			= (byte) 0xff;		// control byte
	public static final byte	A1_ESTAB			= (byte) 0x01;		// type establish context
	public static final byte	A1_DESTROY			= (byte) 0x02;		// type destroy   context
	public static final byte	A1_ERRORREPLY		= (byte) 0x40;		// type error reply
	public static final byte	A1_RESPONSE			= (byte) 0x80;		// response
	public static final byte	A1_ERRORRESPONSE	= (byte) 0xc0;		// error + response
	public static final byte	A1_OVERLOAD			= (byte) 0x01; 	    /* Server overloaded          */
	public static final byte	A1_NOCONTEXT		= (byte) 0x02; 	   	/* Context not found          */
	public static final byte	A1_SHUTDOWN			= (byte) 0x03;     	/* Server shutting down       */
	public static final byte	A1_INVALID_DB		= (byte) 0x04;     	/* Invalid DBID               */
	public static final byte	A1_INACTIVE_DB		= (byte) 0x05;     	/* Inactive DBID              */
	public static final byte	A1_INVALID_PROTOCOL	= (byte) 0x06;     	/* Invalid Protocol           */
	public static final byte	A1_MORE				= (byte) 0x07;     	/* Streaming - Expect More    */
	public static final byte	A1_CALLREJECTED		= (byte) 0x08;     	/* Server rejects call        */
	public static final byte	A1_RELAYFAILURE		= (byte) 0x09;     	/* Relay failure              */
	public static final byte 	A1_HDR_LENGTH 		= 0x0c;				// XTS A1 msg header length
	public static final byte[] 	A1_EYECATCHER		= { 'A', '1' };		// XTS A1 msg eyecatcher 
	public static final byte[] 	A1_ESTAB_CONTEXT	= { 'A', '1',		// XTS A1 establish context message 
														A1_CONTROL, 	// control byte
														A1_ESTAB };		// establish context
	public static final byte[] 	A1_DESTROY_CONTEXT	= { 'A', '1',		// XTS A1 destroy   context message 
														A1_CONTROL, 	// control byte
														A1_DESTROY };	// destroy context
	public static final byte[]	A2_EYECATCHER		= { 'A', '2' };		// XTS A2 msg eyecatcher
	public static final short	A2_HDR_LENGTH		= 0x34;				// XTS A2 msg header length
	public static final byte	A2_PAYLOAD			= 0x01;				// XTS A2 message type
	public static final byte	A2_ACBXPAYLOAD		= 0x01;				// XTS A2 message sub-type
	public static final byte	A2_AMNUSER			= 0x02;				// XTS A2 message sub-type
	public static final byte	A2_BIGENDIAN		= 0x00;				// XTS A2 architecture big endian
	public static final byte	A2_ASCII7			= 0x00;				// XTS A2 architecture ASCII 7 bit
	public static final byte	A2_EBCDIC			= 0x04;				// XTS A2 architecture EBCDIC
	public static final byte	A2_IEEE				= 0x20;				// XTS A2 architecture IEEE float
	public static final byte	A2_ERRORREPLY		= (byte) 0xC0;		// XTS A2 rsp reply with error
	public static final byte	A2_OVERLOAD			= (byte) 0x01;		// XTS A2 rsp server overloaded
	public static final byte	A2_NOCONTEXT		= (byte) 0x02;		// XTS A2 rsp context not found
	public static final byte	A2_SHUTDOWN			= (byte) 0x03;		// XTS A2 rsp server shutting down
	public static final byte	A2_INVALID_DB		= (byte) 0x04;		// XTS A2 rsp invalid DBID
	public static final byte	A2_INACTIVE_DB		= (byte) 0x05;		// XTS A2 rsp inactive DBID
	public static final byte	A2_INVALID_PROTOCOL	= (byte) 0x06;		// XTS A2 rsp invalid protocol
	public static final byte	A2_MORE				= (byte) 0x07;		// XTS A2 rsp streaming - expect more
	public static final byte	A2_CALLREJECTED		= (byte) 0x08;		// XTS A2 rsp server rejects call
	public static final byte	A2_RELAYFAILURE		= (byte) 0x09;		// XTS A2 rsp relay failure

	public static final byte 	BUFIN 	= 0;							// XTS message buffer in/out array values
	public static final byte 	BUFOUT 	= 1;
	
	public static final byte 	FB 		= 0x10;							// XTS message buffer in/out array flags
	public static final byte 	RB 		= 0x08;							// bit is turned on if that buffer is present
	public static final byte 	SB 		= 0x04;
	public static final byte 	VB 		= 0x02;
	public static final byte 	IB 		= 0x01;
	
	/** 
	 * These platform architecture byte values were provided by usamih on 2013.01.03.
	 * They correspond to the value returned from an OP command in the high order byte
	 * of ISN lower limit to indicate the target database architecture.
	 */
	
	public static final byte	ARCH_EBCDIC			= 0x04;				// platform architecture EBCDIC
	public static final byte	ARCH_ASCII7			= 0x00;				// platform architecture ASCII7
	public static final byte	ARCH_NONBYTESWAPPED	= 0x00;				// platform architecture non byte swapped
	public static final byte	ARCH_BYTESWAPPED	= 0x01;				// platform architecture byte swapped
	public static final byte	ARCH_FLOATIBM370	= 0x00;				// platform architecture floating point IBM 370
	public static final byte	ARCH_FLOATVAX		= 0x10;				// platform architecture floating point VAX
	public static final byte	ARCH_FLOATIEEE		= 0x20;				// platform architecture floating point IEEE
	
	public static final long	SEND_TIMEOUT		= 30000;			// timeout for sendAndWait()
	
	public static final String	ADI_ADMIN			= "SAGADMIN";		// ADI attribute string for admin partition
	public static final String	ADI_ACTIVE			= "ACTIVE";			// ADI attribute string for database active
	public static final String	ADI_DBNAME			= "DBNAME";			// ADI attribute string for database name
	public static final String	ADI_VERSION			= "Version";		// ADI attribute string for database version
	public static final String	ADI_ADAVERSION		= "Adabas";			// ADI attribute string for Adabas code version
	
	public static final byte[]	N1_EYECATCHER		= { 'N', '1' };		// XTS N1 msg eyecatcher
	public static final byte[]	N1_ZERO				= { 0 };
	public static final short	N1_HDR_LENGTH		= 0x0C;				// XTS N1 msg         header length
	public static final short	N1_SEGHDR_LENGTH	= 0x08;				// XTS N1 msg segment header length
	public static final byte[]	N1_SEG_VERSION		= { 0x00, '1' };	// XTS N1 segment version
	public static final byte[]	N1_SEG_VERSION2		= { 0x00, '2' };	// XTS N1 segment version
	public static final byte[]	N1_SEG_VERSION3		= { 0x00, '3' };	// XTS N1 segment version
	
	public static final byte[]	W2_EYECATCHER		= { 'W', '2'};		// XTS W2 msg eyecatcher
	public static final short	W2_HDR_LENGTH		= 0x0020;			// XTS W2 msg header length
	public static final byte	W2_VERSION			= 0x01;				// XTS W2 msg version
	public static final short	W2_REQ_ZIIP			= 0x0001;			// XTS W2 msg request type zIIP session stats
	public static final short	W2_REQ_DRVSTAT		= 0x0002;			// XTS W2 msg request type driver stats
	public static final short	W2_REQ_LINKSTAT		= 0x0003;			// XTS W2 msg request type link stats
	public static final short	W2_REQ_SESSSTAT		= 0x0004;			// XTS W2 msg request type session stats
	public static final short	W2_REQ_NODELIST		= 0x0005;			// XTS W2 msg request type node   list
	public static final short	W2_REQ_DRVLIST		= 0x0006;			// XTS W2 msg request type driver list
	public static final short	W2_REQ_LINKLIST		= 0x0007;			// XTS W2 msg request type link   list
	public static final short	W2_REQ_PATHLIST		= 0x0008;			// XTS W2 msg request type path   list
	public static final short	W2_REQ_TARGLIST		= 0x0009;			// XTS W2 msg request type target list
	public static final short	W2_REQ_PARMLIST		= 0x000a;			// XTS W2 msg request type parms  list
	public static final short	W2_REQ_DRVPARMS		= 0x000b;			// XTS W2 msg request type drv list
	public static final short	W2_REQ_LNKPARMS		= 0x000c;			// XTS W2 msg request type link list
	public static final short	W2_REQ_SETPARM		= 0x000d;			// XTS W2 msg request type set parmt

	public static final short	W2_REQ_SET_DRV_PARM	= 0x000e;			// Modify Driver parameter
	public static final short	W2_REQ_SET_LNK_PARM	= 0x000f;			// Modify Link parameter
	public static final short	W2_REQ_OP_DRIVER	= 0x0010;			// Open driver
	public static final short	W2_REQ_CL_DRIVER	= 0x0011;			// Close driver
	public static final short	W2_REQ_CON_LINK		= 0x0012;			// Connect link
	public static final short	W2_REQ_DIS_LINK		= 0x0013;			// Disonnect link
	public static final short	W2_REQ_OP_LINK		= 0x0014;			// Open link
	public static final short	W2_REQ_CL_LINK		= 0x0015;			// Close link
	public static final short	W2_REQ_TERMINATE	= 0x0016;			// XTS W2 msg request type terminate
	public static final short	W2_REQ_AUTH			= 0x0017;			// XTS W2 msg request to authenticate

	public static final byte	W2_REQ				= 0x01;				// XTS W2 msg request
 	public static final byte	W2_RPL				= 0x02;				// XTS W2 msg reply
}

