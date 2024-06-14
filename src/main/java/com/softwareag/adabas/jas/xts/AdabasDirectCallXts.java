package com.softwareag.adabas.jas.xts;

import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Logger;

import com.softwareag.adabas.jas.AdabasBufferX;
import com.softwareag.adabas.jas.AdabasControlBlock;
import com.softwareag.adabas.jas.AdabasControlBlockX;
import com.softwareag.adabas.jas.AdabasDirectCall;
import com.softwareag.adabas.jas.AdabasDirectCallX;
import com.softwareag.adabas.jas.AdabasException;
import com.softwareag.adabas.jas.AdabasTrace;
import com.softwareag.adabas.jas.N1Exception;

import com.softwareag.adabas.xts.Message;
import com.softwareag.adabas.xts.XTS;
import com.softwareag.adabas.xts.XTSException;
import com.softwareag.adabas.xts.XTSSendParameters;
import com.softwareag.adabas.xts.XTSurl;
import com.softwareag.adabas.xts.directory.DefaultDirectory;
import com.softwareag.adabas.xts.directory.Directory;

/**
 * Contains methods related to making Adabas direct calls via XTS.
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

public class AdabasDirectCallXts extends XtsDefinitions {
	
	private static boolean 			initDs			= false;					// directory server init flag
	private boolean					connectedXts	= false;					// connected to XTS server flag
	private byte[] 					xtsBufs 		= new byte[2];				// XTS message buffer in/out array
	private int						xtsCtxId		= 0;						// XTS context ID
	private int						xtsCtxIdVerify	= 0;						// XTS context ID verifier
	private static AtomicInteger	xtsCtxIdVerGen	= new AtomicInteger(0);		// atomic index for generating XTS context ID verifiers
	private static String 			hostName 		= "unknown ";				// default node name     for XTS context
	private String 					userName 		= "unknown ";				// default user name     for XTS context
	private byte[]					pidBA			= new byte[8];				// process ID byte array for XTS context
	private byte[]					timestampBA		= new byte[8];				// timestamp  byte array for XTS context
	
	private static final ReentrantLock	callLock	= new ReentrantLock();		// lock to single thread XTS calls
	private	boolean						A2RetryDone	= false;					// flag for SendAndWait() one time retry done
	
	final static Logger logger = AdabasTrace.getLogger("com.softwareag.adabas.jas.xts.AdabasDirectCallXts");
	
	/**
	 * static block run one time when class is loaded
	 */
	static {
		try {
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();	// get local machine
			hostName = localMachine.getHostName();										// get host name
		} catch (UnknownHostException e) {
			logger.error("static(): getLocalHost(): UnknownHostException: defaulting to unknown ..");
		}
	}
	
	/**
	 * Initialize directory server URL.
	 * 
	 * @throws AdabasException if no DS URL found
	 */
	public static void initDsUrl() throws AdabasException {
	
		String method = "initDsUrl(): ";
		
		logger.trace(method + "> entered");
		
		if (!initDs) {
			Directory adi = XTS.getDirectory();									// get dirserver from XTS
			if (adi == null) {													// if none error
				throw new AdabasException(AdabasException.XTS_NODIRSERVER);
			}
			logger.debug(method + "ADI URL = " + adi.getUrl());
			initDs = true;
		}	
		logger.trace(method + "< exited");
	}
	
	/**
	 * Send a classic direct call to Adabas via XTS using an A1 message.
	 * 
	 * @param adc Direct call with control block and buffers.
	 * 
	 * @throws Exception	General exception. 
	 */
	public void callAdabasXtsA1(AdabasDirectCall adc) throws Exception {
		
		XTSSendParameters	xsp;												// XTS send parameters

		final String method = "callAdabasXtsA1(): ";
		
		logger.trace(method + "> entered");
		logger.trace(method + "attempting callLock.lock() ..");		

		callLock.lock();														// get single thread XTS call lock
																				// TODO determine if this is necessary
		try {
			initDsUrl();														// init directory server
			if (connectedXts == false) {										// connect to Adabas if not yet connected
				connectXts(adc.getAcb().getAcbDBID(),							// using DBID
						   adc.getAcb().getHostName(),							// host name
						   adc.getXtsSendTimeout(),								// XTS send    timeout
						   adc.getXtsConnectTimeout(),							// XTS connect timeout
						   adc.isXtsEBCDIC());									// XTS encoding flag
			}

			int msgLength = A1_HDR_LENGTH + AdabasControlBlock.ACB_LENGTH;		// A1 header + ACB

			xtsBufs[BUFIN] 	= 0;												// reset buffers in/out for new call
			xtsBufs[BUFOUT]	= 0;

			if (adc.getFBArray() != null)	{									// bump message length and
				msgLength += adc.getFBArray().length;							// set buffer bits
				xtsBufs[BUFIN] |= FB;											// for each buffer present
			}
			if (adc.getRBArray() != null)	{
				msgLength += adc.getRBArray().length;
				xtsBufs[BUFIN]  |= RB;
				xtsBufs[BUFOUT] |= RB;											// always add RB to buffers out
			}
			if (adc.getSBArray() != null)	{
				msgLength += adc.getSBArray().length;
				xtsBufs[BUFIN] |= SB;
			}
			if (adc.getVBArray() != null)	{
				msgLength += adc.getVBArray().length;
				xtsBufs[BUFIN] |= VB;
			}
			if (adc.getIBArray() != null)	{
				msgLength += adc.getIBArray().length;
				xtsBufs[BUFIN] 	|= IB;
				xtsBufs[BUFOUT]	|= IB;											// always add IB to buffers out
			}

			Message msg = Message.newMessage(msgLength);						// make a new XTS message
			msg.putBytes(A1_EYECATCHER);										// write the A1 eyecatcher
			msg.put(xtsBufs[BUFIN]);											// write buffer in bits
			msg.put(xtsBufs[BUFOUT]);											// write buffer out  bits
			msg.putInt(xtsCtxId);												// write XTS context ID
			msg.putInt(xtsCtxIdVerify);											// write XTS context ID verifier
			msg.putBytes(adc.getAcb().getACBArray());							// write the ACB

			if (adc.getFBArray() != null)										// check for presence of buffers and for each
				msg.putBytes(adc.getFBArray());									// write buffer to message
			if (adc.getRBArray() != null)
				msg.putBytes(adc.getRBArray());
			if (adc.getSBArray() != null)
				msg.putBytes(adc.getSBArray());
			if (adc.getVBArray() != null)
				msg.putBytes(adc.getVBArray());
			if (adc.getIBArray() != null)
				msg.putBytes(adc.getIBArray());
//			logger.trace(AdabasTrace.dumpBuffer(method + "send XTS A1 direct call message = ", msg.body));
			logger.trace(method + "send XTS A1 direct call message");

			xsp = new XTSSendParameters(adc.getAcb().getAcbDBID(),				// make XTS send parms using DBID
										msg,									// message
										adc.getXtsSendTimeout(),				// XTS timeout
										adc.getAcb().getHostName());			// host name
			
			Message rcvMsg = XTS.sendAndWait(xsp); 								// send message to server

//			logger.trace(AdabasTrace.dumpBuffer(method + "receieved XTS A1 direct call reply = ", rcvMsg.body));
			logger.trace("receieved XTS A1 direct call reply");

			ByteBuffer 	rcvBB 			= ByteBuffer.wrap(rcvMsg.body);			// wrap received msg as a byte buffer
			byte		rcvType			= rcvBB.get(3);							// get the message TYPE byte to check for error
			if ((rcvType & A1_ERRORREPLY) == A1_ERRORREPLY) {					// some error occurred													// error occurred during establish context

				String logMsg = String.format("XTS.sendAndWait() to DBID: %d returned A1 ERRORREPLY rcvType = 0x%02X", adc.getAcb().getAcbDBID(), rcvType);
				logger.error(logMsg); 
				disconnectXts(adc.getAcb().getAcbDBID(),						// disconnect from XTS using DBID
						  	  adc.getAcb().getHostName(),						// host name
						  	  adc.getXtsSendTimeout());							// XTS timeout
				throw new AdabasException(adc.getAcb().getAcbDBID(), "XTS.sendAndWait() returned A1 ERRORREPLY", rcvType);	// throw exception

			}
			int testXtsCtxId		= rcvBB.getInt(0x04);									// get context ID
			int testXtsCtxIdVerify	= rcvBB.getInt(0x08);									// get context ID verifier
			if (testXtsCtxId != xtsCtxId || testXtsCtxIdVerify != xtsCtxIdVerify) {			// not equal to ones we sent?
				String errmsg = String.format("context ID and/or verifier changed by XTS during A1 send! sent ID = 0x%08X; received ID = 0x%08X; sent verifier = 0x%08X; received verifier = 0x%08X", xtsCtxId, testXtsCtxId, xtsCtxIdVerify, testXtsCtxIdVerify);
				logger.error(method + errmsg);
				logger.debug(String.format(method + AdabasTrace.dumpBuffer("rcvMsg", rcvMsg.body)));
				throw new AdabasException(adc.getAcb().getAcbDBID(), errmsg);				// throw exception
			}

			int rcvOffset = A1_HDR_LENGTH;										// start running offset at A1 header length

			System.arraycopy(rcvMsg.body,										// copy from received message 
							 rcvOffset, 										// offset 0x0c (after A1 header)
							 adc.getAcb().getACBArray(),						// to ACB byte array
							 0, 												// offset 0
							 AdabasControlBlock.ACB_LENGTH);					// for length of ACB
			rcvOffset += AdabasControlBlock.ACB_LENGTH;							// bump offset

			if (adc.getAcb().getAcbRSP() == 0) {								// if command successful (rsp = 0)
				
				if ((xtsBufs[BUFOUT] & FB) == FB) {								// for each possible output buffer
					System.arraycopy(rcvMsg.body,								// copy from received message 
									 rcvOffset, 								// running offset
									 adc.getFBArray(), 							// to buffer byte array
									 0, 										// offset 0
									 adc.getFBL());								// for length of buffer
					rcvOffset += adc.getFBL();									// bump offset
				}
				if ((xtsBufs[BUFOUT] & RB) == RB) {
					System.arraycopy(rcvMsg.body, 
									 rcvOffset,
									 adc.getRBArray(),
									 0,
									 adc.getRBL());
					rcvOffset += adc.getRBL();
				}
				if ((xtsBufs[BUFOUT] & SB) == SB) {
					System.arraycopy(rcvMsg.body, 
									 rcvOffset,
									 adc.getSBArray(),
									 0,
									 adc.getSBL());
					rcvOffset += adc.getSBL();
				}
				if ((xtsBufs[BUFOUT] & VB) == VB) {
					System.arraycopy(rcvMsg.body, 
									 rcvOffset,
									 adc.getVBArray(),
									 0,
									 adc.getVBL());
					rcvOffset += adc.getVBL();
				}
				if ((xtsBufs[BUFOUT] & IB) == IB) {
					System.arraycopy(rcvMsg.body, 
									 rcvOffset,
									 adc.getIBArray(),
									 0,
									 adc.getIBL());
					rcvOffset += adc.getIBL();
				}

				if (adc.getAcb().getAcbCMD().compareTo("CL") == 0) {			// if successful CL command
					disconnectXts(adc.getAcb().getAcbDBID(),					// disconnect from XTS using DBID
								  adc.getAcb().getHostName(),					// host name
								  adc.getXtsSendTimeout());						// XTS timeout
				}
			
			}																	// end of rsp = 0 block
			rcvMsg.free(method);												// free return message
		}
		catch (XTSException xe) {												// XTSException thrown?
			logger.trace(method + "XTSException caught: " + xe.getMessage());
			logger.trace(method + "re-throwing XTSException ");
			throw xe;															// re-throw XTSException
		}
		finally {
			callLock.unlock();													// TODO determine necessary XTS lock granularity
			logger.trace(method + "callLock: lock released");
			logger.trace(method + "< exited");
		}
	}

	/**
	 * Connect to Adabas via XTS.
	 * 
	 * @param dbid 				Database ID target.
	 * @param hostName 			Host Name of target.
	 * @param sendTimeout		XTS timeout in milliseconds.
	 * @param connectTimeout	XTS connect timeout in milliseconds.
	 * @param isEBCDIC			EBCDIC encoding flag
	 * @throws Exception 
	 */
	private void connectXts (int dbid, String hostName, long sendTimeout, int connectTimeout, boolean isEBCDIC) throws Exception {
		
		XTSSendParameters	xsp;												// XTS send parameters
		
		final String method = "connectXts(): ";
		
		logger.trace(method + "> entered");
		
		if (connectedXts == false) {
			
			XTS.useOnlyOneConnection = true;									// TODO - revisit per Rich Cole
			
			Message msg = Message.newMessage(93);								// 4  - A1_ESTAB_CONTEXT 							+
																				// 4  - XTS context ID 								+
																				// 4  - XTS context ID verifier 					+
																				// 12 - "ENDIAN  " + length + "BIG" 				+
																				// 15 - "CHARSET " + length + "ASCII" or "EBCDIC"	+
																				// 13 - "FPFORMAT" + length + "IEEE" 				+
																				// 41 - "USERID  " + length + node + user name + PID
			
			msg.putBytes(A1_ESTAB_CONTEXT);										// write 4 byte establish context

			xtsCtxId = 0;
			msg.putInt(xtsCtxId);												// write 0 XTS context ID
			xtsCtxIdVerify = xtsCtxIdVerGen.incrementAndGet();					// get next available XTS context ID verifier
			if (xtsCtxIdVerify == 0x7fffffff)									// if max verifier reached reset to 0
				xtsCtxIdVerGen.set(0);
			msg.putInt(xtsCtxIdVerify);											// write XTS context ID verifier
			
			msg.putBytes("ENDIAN  ".getBytes("ISO-8859-1")); 					// write string "ENDIAN  "
			msg.put((byte) 0x04);												// write inclusive length byte for following string
			msg.putBytes("BIG".getBytes("ISO-8859-1")); 						// write string "BIG"
			
			msg.putBytes("CHARSET ".getBytes("ISO-8859-1")); 					// write string "CHARSET "
			if (isEBCDIC == true) {
				msg.put((byte) 0x07); 											// write inclusive length byte for following string
				msg.putBytes("EBCDIC".getBytes("ISO-8859-1")); 					// write string "EBCDIC"				
			}
			else {
				msg.put((byte) 0x06); 											// write inclusive length byte for following string
				msg.putBytes("ASCII".getBytes("ISO-8859-1")); 					// write string "ASCII"
			}
			
			msg.putBytes("FPFORMAT".getBytes("ISO-8859-1")); 					// write string "FPFORMAT"
			msg.put((byte) 0x05);												// write inclusive length byte for following string
			msg.putBytes("IEEE".getBytes("ISO-8859-1")); 						// write string "IEEE"
			
			pidBA 		= getPID();												// get PID for USERID
			timestampBA = getTimestamp();										// get timestamp for USERID
			
			msg.putBytes("USERID  ".getBytes("ISO-8859-1")); 					// write string "USERID  "
			msg.put((byte) 0x21); 												// write inclusive length byte for following string
			msg.putBytes(getNodeName()); 										// write 8 byte node name
			msg.putBytes(getUserName());										// write 8 byte user name
			msg.putBytes(pidBA);												// write 8 byte PID
			msg.putBytes(timestampBA);											// write 8 byte timestamp
			
			logger.debug(method + "send XTS A1 establish context request: target = " + dbid);
			logger.trace(method + "ENDIAN              = BIG");
			if (isEBCDIC == true) {
				logger.trace(method + "CHARSET             = EBCDIC");					
			}
			else {
				logger.trace(method + "CHARSET             = ASCII");
			}
			logger.trace(method + "FPFORMAT            = IEEE");
			logger.trace(method + "USERID              = " + new String(getNodeName()) + new String(getUserName()) + 
                                                                     String.format("%016x", ByteBuffer.wrap(pidBA).getLong()) + String.format("%016x", ByteBuffer.wrap(timestampBA).getLong()));
			logger.trace(method + "send timeout        = " + sendTimeout);
			logger.trace(method + "connect timeout     = " + connectTimeout);
			logger.trace(method + "context ID verifier = " + String.format("0x%08x", xtsCtxIdVerify));
//			logger.trace(AdabasTrace.dumpBuffer(method + "send XTS A1 establish context message = ", msg.body));
			logger.trace(method + "send XTS A1 establish context message");
			
			xsp = new XTSSendParameters(dbid,									// make XTS send parms with DBID	
										msg,									// message
										sendTimeout,							// send timeout
										hostName,								// host name
										connectTimeout);						// connect timeout

			Message rcvMsg = XTS.sendAndWait(xsp); 								// send connect to server

//			logger.trace(AdabasTrace.dumpBuffer(method + "receieved XTS A1 establish context reply = ", rcvMsg.body));
			logger.trace("receieved XTS A1 establish context reply");
					
			ByteBuffer 	rcvBB 			= ByteBuffer.wrap(rcvMsg.body);			// wrap received msg as a byte buffer
			byte		rcvType			= rcvBB.get(3);							// get the message TYPE byte to check for error
			if ((rcvType & A1_ERRORREPLY) == A1_ERRORREPLY) {					// some error occurred													// error occurred during establish context
				short rcvRspCode = rcvBB.getShort(12);							// get response code
				short rcvSubCode = rcvBB.getShort(14);							// get sub      code
				
				throw new AdabasException(dbid, "XTS A1 establish context", rcvType);	// throw exception
			}
			
			xtsCtxId 				= rcvBB.getInt(4);							// get the context ID generated by XTS
			
			int testXtsCtxIdVerify	= rcvBB.getInt(8);							// get context ID verifier
			if (testXtsCtxIdVerify != xtsCtxIdVerify) {							// not equal to one we sent?
				String errmsg = String.format("context ID verifier changed by XTS during connect! sent = 0x%08X; received = 0x%08X", xtsCtxIdVerify, testXtsCtxIdVerify);
				logger.error(method + errmsg);
				logger.debug(String.format(method + AdabasTrace.dumpBuffer("rcvMsg", rcvMsg.body)));
				throw new AdabasException(dbid, errmsg);						// throw exception
			}
			
			rcvMsg.free(method);												// free return message

			connectedXts = true;
			
			logger.debug(method + "connected to XTS: " + String.format("context ID = 0x%08x; verifier = 0x%08x", xtsCtxId, xtsCtxIdVerify));
			logger.trace(method + "< exited");
			
		}
	}
	
	/**
	 * Disconnect from Adabas via XTS.
	 * 
	 * @param dbid 		Database ID.
	 * @param hostName	Host Name.
	 * @param timeout	XTS timeout in milliseconds.
	 * @throws Exception
	 */
	private void disconnectXts(int dbid, String hostName, long timeout) throws Exception {
		
		XTSSendParameters	xsp;												// XTS send parameters
		
		final String method = "disconnectXts(): ";
		
		logger.trace(method + "> entered");

		if (connectedXts == true) {

			Message msg = Message.newMessage(12);								// 4  - A1_DESTROY_CONTEXT +
																				// 4  - XTS context ID +
																				// 4  - XTS context ID verifier +

			msg.putBytes(A1_DESTROY_CONTEXT);									// write 4 byte destroy context

			msg.putInt(xtsCtxId);												// write current XTS context ID
			msg.putInt(xtsCtxIdVerify);											// write XTS context ID verifier

			logger.debug(method + "send XTS A1 destroy context request: target = " + dbid);
			logger.trace(method + "timeout  = " + timeout);
			logger.trace(method + "context ID          = " + String.format("0x%08x", xtsCtxId));
			logger.trace(method + "context ID verifier = " + String.format("0x%08x", xtsCtxIdVerify));
//			logger.trace(AdabasTrace.dumpBuffer(method + "send XTS A1 destroy context message = ", msg.body));			
			logger.trace(method + "send XTS A1 destroy context message");			
		

			xsp = new XTSSendParameters(dbid,									// make XTS send parms using DBID
										msg,									// message
										timeout,								// XTS timeout
										hostName);								// host name
			
			Message rcvMsg = XTS.sendAndWait(xsp); 								// send disconnect to server

//			logger.trace(AdabasTrace.dumpBuffer(method + "receieved XTS A1 destroy context reply = ", rcvMsg.body));
			logger.trace("receieved XTS A1 destroy context reply");
		
			
			ByteBuffer 	rcvBB 	= ByteBuffer.wrap(rcvMsg.body);					// wrap received msg as a byte buffer
			byte		rcvType	= rcvBB.get(3);									// get the message TYPE byte to check for error
			if ((rcvType & A1_ERRORREPLY) == A1_ERRORREPLY) {					// some error occurred													// error occurred during establish context
				short rcvRspCode = rcvBB.getShort(12);							// get response code
				short rcvSubCode = rcvBB.getShort(14);							// get sub      code
				
				throw new AdabasException(dbid, "XTS A1 destroy context", rcvType);	// throw exception
			}
						
			rcvMsg.free(method);												// free return message
			
			logger.debug(method + "disconnected from XTS");

			connectedXts = false;
		}
		
		logger.trace(method + "< exited");
	}
	
	/**
	 * Get node name for XTS context.
	 * 
	 * @return byte array containing 8 byte node name
	 */
	private byte[] getNodeName() {
		
		byte[] nodeName = "        ".getBytes();								// make 8 byte array of blanks
		if (hostName.getBytes().length >= 8) 									// if long host name
			System.arraycopy(hostName.getBytes(), 0, nodeName, 0, 8);			// use it all
		else																	// else just use its length & leave blanks
			System.arraycopy(hostName.getBytes(), 0, nodeName, 0, hostName.getBytes().length);
		return nodeName;
	}
	
	/**
	 * Get user name for XTS context.
	 * 
	 * @return byte array containing 8 byte user name
	 */
	private byte[] getUserName() {

		byte[] 	userNameBa 	= "        ".getBytes();							// make 8 byte array of blanks
		userName 			= System.getProperty("user.name");					// get user name
		if (userName.getBytes().length >= 8) 									// if long user name
			System.arraycopy(userName.getBytes(), 0, userNameBa, 0, 8);			// use it all
		else																	// else just use its length & leave blanks
			System.arraycopy(userName.getBytes(), 0, userNameBa, 0, userName.getBytes().length);
		return userNameBa;
	}
	
	/**
	 * Get Process ID for XTS context.
	 * 
	 * @return byte array containing 8 byte PID.
	 */
	private byte[] getPID() {
		
		byte[] 		zeroPID = {0,0,0,0,0,0,0,0};								// default PID containing zero if problems
		String		vmName;														// virtual machine name
		int			p;															// offset of @ in virtual machine name
		String		sPid;														// PID as a string
		long		pid;														// PID as a long
		String		hsPid;														// PID as a hex string

		final String method = "getPID(): ";
		
		logger.trace(method + "> entered");

		vmName	= ManagementFactory.getRuntimeMXBean().getName();				// usually pid@hostname but not guaranteed   
		logger.debug(method + "vmName = " + vmName);
		
		p 		= vmName.indexOf('@');
		if (p == -1)
			return zeroPID;
		sPid 	= vmName.substring(0, p);
		logger.debug(method + "PID as String = " + sPid);
		
		try {
			pid	= Long.parseLong(sPid);
		}
		catch (NumberFormatException e) {
			return zeroPID;
		}
		logger.debug(method + "PID as long = " + pid);
	
		hsPid = String.format("%08X", pid);							
		logger.debug(method + "PID as hex string = " + hsPid);
		
		pidBA = hsPid.getBytes();
		
		logger.trace(method + "< exited");

		return pidBA;
	}
	
	/**
	 * Get timestamp for XTS context.
	 * 
	 * @return byte array containing 8 byte timestamp.
	 */
	private byte[] getTimestamp() {
		
		long	lTime;
		
		final String method = "getTimestamp(): ";
		
		logger.trace(method + "> entered");
		
		lTime = System.currentTimeMillis();
		lTime *= 1000;
		logger.debug(method + "Timestamp as long = " + lTime);
		
		ByteBuffer.wrap(timestampBA).putLong(lTime);
		logger.debug(method + AdabasTrace.dumpBuffer("Timestamp as byte array = ", timestampBA));
		
		// Timestamp byte swap here requires ACL fix from RGHADA-4474. usadva 2014.05.30
		
		timestampBA = byteSwap(timestampBA);
		logger.debug(method + AdabasTrace.dumpBuffer("Timestamp as byte array after byte swap = ", timestampBA));


		logger.trace(method + "< exited");

		return timestampBA;
	}

	/**
	 * Send an extended ACBX direct call to Adabas via XTS using an A2 message.
	 * 
	 * @param adcx Direct call with control block and buffers.
	 * 
	 * @throws Exception	General exception. 
	 */
	public void callAdabasXtsA2(AdabasDirectCallX adcx) throws Exception {

		int		numCallersBuffs		= 0;										// total number of caller's buffers
		int		numVariableBuffs	= 0;										// number of variable entries
		int		numSendBuffs 		= 0;										// number of send buffers
		int		offsetACBX			= 0;										// offset to ACBX
		long	totalLength			= 0;										// total message length
		int		streamReqFlag		= 0;										// streaming request flag
		
		XTSSendParameters	xsp;												// XTS send parameters
		
		final String method = "callAdabasXtsA2(): ";
		
		logger.trace(method + "> entered");

		logger.trace(method + "attempting callLock.lock() ..");
		callLock.lock();														// get single thread XTS call lock
																				// TODO determine if this is necessary
		logger.trace(method + "callLock: lock obtained");

		try {
			initDsUrl();														// init directory server

			if (connectedXts == false) {										// connect to Adabas if not yet connected
				connectXts(adcx.getAcbX().getAcbDBID(),							// using DBID
						   adcx.getAcbX().getHostName(),						// host name
						   adcx.getXtsSendTimeout(),							// XTS send 	timeout
						   adcx.getXtsConnectTimeout(),							// XTS connect 	timeout
						   adcx.isXtsEBCDIC());									// XTS encoding flag
			}
			
			int msgLength = A2_HDR_LENGTH + AdabasControlBlockX.ACBX_LENGTH;	// A2 header + ACBX

			xtsBufs[BUFIN] 	= 0;												// buffers in/out not used for A2 message
			xtsBufs[BUFOUT]	= 0;
			
			if (adcx.getFB() != null) {											// for each possible buffer present
				msgLength += AdabasBufferX.ABD_LENGTH + 						// bump msg length by buffer header length
							 adcx.getFB().getDataBuffer().capacity();			// plus user data length
				numVariableBuffs++;												// bump number variable buffers
			}
			if (adcx.getRB() != null) {
				msgLength += AdabasBufferX.ABD_LENGTH +
							 adcx.getRB().getDataBuffer().capacity();
				numVariableBuffs++;
			}
			if (adcx.getSB() != null) {
				msgLength += AdabasBufferX.ABD_LENGTH +
							 adcx.getSB().getDataBuffer().capacity();
				numVariableBuffs++;
			}
			if (adcx.getVB() != null) {
				msgLength += AdabasBufferX.ABD_LENGTH +
							 adcx.getVB().getDataBuffer().capacity();
				numVariableBuffs++;
			}
			if (adcx.getIB() != null) {
				msgLength += AdabasBufferX.ABD_LENGTH +
							 adcx.getIB().getDataBuffer().capacity();
				numVariableBuffs++;
			}
			
			numCallersBuffs = numVariableBuffs + 1;								// callers buffs = user buffs + ACBX
			numSendBuffs	= numCallersBuffs;									// send buffs	 = callers buffs
			offsetACBX		= A2_HDR_LENGTH;									// ACBX starts right after A2 header
			totalLength		= (long) msgLength;									// total length is calculated msg length
			
			Message msg = Message.newMessage(msgLength);						// allocate new message
			
			msg.putBytes(A2_EYECATCHER);										// write A2 eyecatcher				0x00
			msg.putShort(A2_HDR_LENGTH);										// write A2 header length			0x02
			msg.putInt(xtsCtxId);												// write A2 XTS context ID			0x04
			msg.putInt(xtsCtxIdVerify);											// write A2 XTS context ID verifier	0x08
			msg.put(A2_PAYLOAD);												// write message type				0x0C
			
			byte	A2SubType	= A2_ACBXPAYLOAD;								// message sub-type = A2_ACBXPAYLOAD
			if (adcx.getCallSource() != (byte) 0x00) {							// call source set?
				A2SubType	|= adcx.getCallSource();							// yes - OR sub-type with call source
			}
			msg.put(A2SubType);													// write message sub-type			0x0D
			
			if (adcx.isXtsEBCDIC() == true) {
				msg.put((byte) (A2_BIGENDIAN|A2_EBCDIC|A2_IEEE));				// write architecture				0x0E				
			}
			else {
				msg.put((byte) (A2_BIGENDIAN|A2_ASCII7|A2_IEEE));				// write architecture				0x0E
			}
			msg.put((byte) 0x00);												// write A2 response code 0			0x0F
			msg.putInt(adcx.getAcbX().getAcbDBID());							// write A2 DBID					0x10
			msg.putBytes(adcx.getAcbX().getAcbCMD().getBytes());				// write A2 command					0x14
			msg.put(xtsBufs[BUFIN]);											// write A2 buffers in  flags		0x16
			msg.put(xtsBufs[BUFOUT]);											// write A2 buffers out flags		0x17
			msg.putInt(numCallersBuffs);										// write A2 num callers  buffs		0x18
			msg.putInt(numVariableBuffs);										// write A2 num variable buffs		0x1C
			msg.putInt(numSendBuffs);											// write A2 num send	 buffs		0x20
			msg.putInt(offsetACBX);												// write A2 offset to ACBX			0x24
			msg.putLong(totalLength);											// write A2 total msg length		0x28
			msg.putInt(streamReqFlag);											// write A2 stream flag (off)		0x30
			
			msg.putBytes(adcx.getAcbX().getACBArray());							// write A2 ACBX					0x34
			
			CommandTableMF ctmf = CommandTableMF.getInstance();
			ctmf.setBufferUsageFlags(adcx);										// set buffer in/out flags in buffer headers using Legal Command Table
			
			if (adcx.getFB() != null) {											// for each possible buffer present
				if ((adcx.getFB().getAbdBUF() & AdabasBufferX.IN) == 0) {		// if in/out bits from LCT say don't send
					adcx.getFB().setAbdSEND(0);									// clear send length
				}
				msg.putBytes(adcx.getFB().getABDBytes());						// write A2 buffer description header
			}
			if (adcx.getRB() != null) {
				if ((adcx.getRB().getAbdBUF() & AdabasBufferX.IN) == 0) {
					adcx.getRB().setAbdSEND(0);
				}
				msg.putBytes(adcx.getRB().getABDBytes());
			}
			if (adcx.getSB() != null) {
				if ((adcx.getSB().getAbdBUF() & AdabasBufferX.IN) == 0) {
					adcx.getSB().setAbdSEND(0);
				}
				msg.putBytes(adcx.getSB().getABDBytes());
			}
			if (adcx.getVB() != null) {
				if ((adcx.getVB().getAbdBUF() & AdabasBufferX.IN) == 0) {
					adcx.getVB().setAbdSEND(0);
				}
				msg.putBytes(adcx.getVB().getABDBytes());
			}
			if (adcx.getIB() != null) {
				if ((adcx.getIB().getAbdBUF() & AdabasBufferX.IN) == 0) {
					adcx.getIB().setAbdSEND(0);
				}
				msg.putBytes(adcx.getIB().getABDBytes());
			}
			
			if (adcx.getFB() != null) {											// for each possible buffer present
				if ((adcx.getFB().getAbdBUF() & AdabasBufferX.IN) != 0) {		// if in/out bits from LCT say send it
					msg.putBytes(adcx.getFB().getDataBytes());					// write A2 user data buffer
				}
			}
			if (adcx.getRB() != null) {
				if ((adcx.getRB().getAbdBUF() & AdabasBufferX.IN) != 0) {
					msg.putBytes(adcx.getRB().getDataBytes());
				}
			}
			if (adcx.getSB() != null) {
				if ((adcx.getSB().getAbdBUF() & AdabasBufferX.IN) != 0) {
					msg.putBytes(adcx.getSB().getDataBytes());
				}
			}
			if (adcx.getVB() != null) {
				if ((adcx.getVB().getAbdBUF() & AdabasBufferX.IN) != 0) {
					msg.putBytes(adcx.getVB().getDataBytes());
				}
			}
			if (adcx.getIB() != null) {
				if ((adcx.getIB().getAbdBUF() & AdabasBufferX.IN) != 0) {
					msg.putBytes(adcx.getIB().getDataBytes());
				}
			}
			
//			logger.trace(AdabasTrace.dumpBuffer(method + "send XTS A2 direct call message = ", msg.body));
			logger.trace(method + "send XTS A2 direct call message ");
			
			xsp = new XTSSendParameters(adcx.getAcbX().getAcbDBID(),			// make XTS send parms with DBID	
										msg,									// message
										adcx.getXtsSendTimeout(),				// XTS timeout
										adcx.getAcbX().getHostName());			// host name

			Message rcvMsg = XTS.sendAndWait(xsp); 								// send message to server
			
//			logger.trace(AdabasTrace.dumpBuffer(method + "receieved XTS A2 direct call reply = ", rcvMsg.body));
			logger.trace("receieved XTS A2 direct call reply");
			

			ByteBuffer 	rcvBB 			= ByteBuffer.wrap(rcvMsg.body);			// wrap received msg as a byte buffer
			byte		rcvRsp			= rcvBB.get(0x0F);						// get the A2 response byte to check for error
			if ((rcvRsp & A2_ERRORREPLY) == A2_ERRORREPLY) {					// some error occurred

				String logMsg = String.format("XTS.sendAndWait() to DBID: %d returned ERRORREPLY! rcvRsp = 0x%02X", adcx.getAcbX().getAcbDBID(), rcvRsp);
				logger.error(logMsg); 
				disconnectXts(adcx.getAcbX().getAcbDBID(),						// disconnect from XTS using DBID
						  	  adcx.getAcbX().getHostName(),						// host name
						  	  adcx.getXtsSendTimeout());						// XTS timeout
				String expMsg = String.format("XTS.sendAndWait() returned ERRORREPLY! rcvRsp = 0x%02X", rcvRsp);
				throw new AdabasException(adcx.getAcbX().getAcbDBID(), expMsg);	// throw exception
			}
			int testXtsCtxId		= rcvBB.getInt(0x04);							// get context ID
			int testXtsCtxIdVerify	= rcvBB.getInt(0x08);							// get context ID verifier
			if (testXtsCtxId != xtsCtxId || testXtsCtxIdVerify != xtsCtxIdVerify) {	// not equal to ones we sent?
				String errmsg = String.format("context ID and/or verifier changed by XTS during A2 send! sent ID = 0x%08X; received ID = 0x%08X; sent verifier = 0x%08X; received verifier = 0x%08X", xtsCtxId, testXtsCtxId, xtsCtxIdVerify, testXtsCtxIdVerify);
				logger.error(method + errmsg);
				logger.debug(String.format(method + AdabasTrace.dumpBuffer("rcvMsg", rcvMsg.body)));

				throw new AdabasException(adcx.getAcbX().getAcbDBID(), errmsg);		// throw exception
			}

			// TODO add return A2 header validation
			
			rcvMsg.reset();														// point to beginning of A2 message
			rcvMsg.skip(A2_HDR_LENGTH);											// point past A2 header
			rcvMsg.getBytes(adcx.getAcbX().getACBArray());						// copy ACBX
			
			if (adcx.getAcbX().getAcbRSP() == 0) {								// if command successful (rsp = 0)
				
				if (adcx.getFB() != null)										// for each possible buffer present
					rcvMsg.getBytes(adcx.getFB().getABDBytes());				// copy A2 buffer header
				if (adcx.getRB() != null)
					rcvMsg.getBytes(adcx.getRB().getABDBytes());
				if (adcx.getSB() != null)
					rcvMsg.getBytes(adcx.getSB().getABDBytes());
				if (adcx.getVB() != null)
					rcvMsg.getBytes(adcx.getVB().getABDBytes());
				if (adcx.getIB() != null)
					rcvMsg.getBytes(adcx.getIB().getABDBytes());

				if (adcx.getFB() != null) {										// for each possible buffer present
					byte[] ba = rcvMsg.getBytes(adcx.getFB().getAbdRECV());		// get A2 received data as byte array 
					adcx.getFB().getDataBuffer().rewind();						// rewind user data buffer
					adcx.getFB().getDataBuffer().put(ba);						// copy byte array into buffer user data
				}
				if (adcx.getRB() != null) {
					byte[] ba = rcvMsg.getBytes(adcx.getRB().getAbdRECV()); 
					adcx.getRB().getDataBuffer().rewind();
					adcx.getRB().getDataBuffer().put(ba);
				}
				if (adcx.getSB() != null) {
					byte[] ba = rcvMsg.getBytes(adcx.getSB().getAbdRECV()); 
					adcx.getSB().getDataBuffer().rewind();
					adcx.getSB().getDataBuffer().put(ba);
				}
				if (adcx.getVB() != null) {
					byte[] ba = rcvMsg.getBytes(adcx.getVB().getAbdRECV()); 
					adcx.getVB().getDataBuffer().rewind();
					adcx.getVB().getDataBuffer().put(ba);
				}
				if (adcx.getIB() != null) {
					byte[] ba = rcvMsg.getBytes(adcx.getIB().getAbdRECV()); 
					adcx.getIB().getDataBuffer().rewind();
					adcx.getIB().getDataBuffer().put(ba);
				}

				if (adcx.getAcbX().getAcbCMD().compareTo("CL") == 0) {			// if successful CL command
					disconnectXts(adcx.getAcbX().getAcbDBID(),					// disconnect from XTS using DBID
								  adcx.getAcbX().getHostName(),					// host name
								  adcx.getXtsSendTimeout());					// XTS timeout
				}
				
			}																	// end of rsp = 0 block

			rcvMsg.free(method);												// free return message
		}
		catch (XTSException xe) {												// XTSException thrown?
			
			logger.debug(method + "XTSException caught: " + xe.getMessage());
			
			if (xe.xtsResponseCode == XTSException.XTS_TIMEOUT || xe.xtsResponseCode == XTSException.XTS_SEND_RECV_TIMEOUT) {
				
				logger.debug(method + "TIMEOUT Exception rc = " + xe.xtsResponseCode);
				if (A2RetryDone == false) {
					
					logger.debug(method + "Clearing XTS URL cache..");
					DefaultDirectory.clearXtsUrlCache();
					
					logger.debug(method + "Invoking A2 one time retry..");
					A2RetryDone = true;
					
					callAdabasXtsA2(adcx);
				}
				else {
					logger.debug(method + "A2 one time retry already done. Re-throwing XTSException..");
					A2RetryDone = false;
					throw xe;
				}
			}
			else {
				logger.debug(method + "Re-throwing XTSException..");
				throw xe;
			}
		}
		finally {
			if (logger.isTraceEnabled())	logger.trace(method + "attempting callLock.unlock() ..");
			callLock.unlock();													// TODO determine necessary XTS lock granularity
			if (logger.isTraceEnabled())	logger.trace(method + "callLock: lock released");
			logger.trace(method + "< exited");
		}
	}

	/**
	 * Byte swap a byte array.
	 * 
	 * @param in Input byte array
	 * @return Output byte array
	 */
	private static byte[] byteSwap(byte[] in) {
		
		byte[] out = new byte[in.length];
		
		for (int i=0; i<in.length; i++) {
			out[i] = in[in.length-i-1];
		}
		return out;
	}

	/**
	 * Send N1 message to AAS to create a database.
	 * 
	 * @param nodeName			Node name to find AAS.
	 * @param timeout			XTS send timeout.
	 * @param dbid				DBID to create.
	 * @param sLoadDemoFiles	Flag to load demo files.
	 * @param parms				Parms string.
	 * 
	 * @throws Exception	General Exception 
	 */
	public void callAASN1CreateDB(String nodeName, long timeout, int dbid, String sLoadDemoFiles, String parms) throws Exception {
		
		final byte[]	CREATE_DB	= { (byte) 0x8C, 0x00, 0x00, 0x00 };		// XTS N1 create DB request
		Directory	directory	= null;											// XTS dir server directory
		String		oldPartition	= null;										// previous dir server partition
		Message		msg		= null;												// XTS send    message
		Message		rcvMsg		= null;											// XTS receive message
		String		targetname	= null;											// saved AAS URL
		boolean		AASFound	= false;										// AAS found flag
		
		final String method = "callAASN1CreateDB(): ";
		
		if (logger.isTraceEnabled())	logger.trace(method + "> entered");

		callLock.lock();														// get single thread XTS call lock
																				// TODO determine if this is necessary
		if (logger.isTraceEnabled())	logger.trace(method + "callLock: lock obtained");

		try {
			int msgLength = N1_HDR_LENGTH + N1_SEGHDR_LENGTH + 20 + parms.length();

			msg = Message.newMessage(msgLength);								// allocate new message
			
			msg.putBytes(N1_EYECATCHER);										// write N1 eyecatcher				0x00
			msg.putShort(0x00);													// write XTS return code 0			0x02
			msg.putInt(msgLength);												// write total msg length			0x04
			msg.putInt(0x00);													// write create DB return code 0	0x08
			msg.putShort(msgLength - N1_HDR_LENGTH);							// write N1 segment length			0x0C
			msg.putBytes(N1_SEG_VERSION);										// write N1 segment version			0x0E
			msg.putBytes(CREATE_DB);											// write N1 create DB request		0x10
			msg.putInt(dbid);													// write DBID						0x14
			msg.putShort(0x02);													// write type = 2 always pass parms	0x18
			
			if (sLoadDemoFiles.compareToIgnoreCase("YES") == 0) {
				msg.putShort(0x01);												// write load = 1 load demo files	0x1A
			}
			else {
				msg.putShort(0x00);												// write load = 0 no demo load		0x1A
			}
			
			msg.putInt(0x00);													// write AAS return code 0			0x1C
			msg.putInt(0x00);													// write AIF return code 0			0x20
			msg.putInt(0x00);													// write AIF sub    code 0			0x24
			msg.putBytes(parms.getBytes());										// write parms byte array			0x28

//			if (logger.isTraceEnabled())	logger.trace(AdabasTrace.dumpBuffer(method + "N1 message = ", msg.body));
			if (logger.isTraceEnabled())	logger.trace(method + "N1 message");
			
			directory = XTS.getDirectory();										// get directory server
			
			oldPartition = directory.getPartition();							// remember previous partition
			directory.setPartition("");											// look at whole directory
			
			XTSurl[] xtsUrls = directory.retrieve(XTS.XTSACCESS, "*");			// retrieve all access URLs
			
			if (xtsUrls == null) {												// none found
				logger.error(method + "No AAS server found! nodeName = " + nodeName);
				throw new AdabasException(dbid, "CreateDatabase on nodeName " + nodeName, (short) 222, (short) 148);
			}
			
			for (XTSurl url : xtsUrls) {										// loop thru URLs
				if (logger.isTraceEnabled()) logger.trace(method + " URL=" + url.toString(true));
				if (url.getTarget().contains("AAS_SMH")) {
					if (url.getHost().compareToIgnoreCase(nodeName) == 0) {						
						targetname 	= url.getTarget();
						AASFound	= true;
 						if (logger.isTraceEnabled()) logger.trace(method + "Hit!! target=" + targetname);
							
						XTSSendParameters xsp = new XTSSendParameters (targetname, msg, timeout, url);
						try {
							rcvMsg = XTS.sendAndWait(xsp);
						}
						catch (XTSException xe) {
							if (logger.isDebugEnabled()) logger.debug(method + "XTSException: " + xe.getMessage() + ": retrying N1 call ..");
							rcvMsg = XTS.sendAndWait(xsp);
						}
						if (logger.isTraceEnabled())	logger.trace(AdabasTrace.dumpBuffer(method + "N1 message response = ", rcvMsg.body));
//						if (logger.isTraceEnabled())	logger.trace("N1 message response");
						break;
					}
				}
			}
			
			if (AASFound == false) {
				logger.error(method + "No AAS server found! nodeName = " + nodeName);
				throw new AdabasException(dbid, "CreateDatabase on nodeName " + nodeName, (short) 222, (short) 148);
			}
			
			rcvMsg.reset();														// reset to offset 			0x00
			rcvMsg.skip(0x02);													// skip N1 eyecatcher		0x00
			int XTSRC = rcvMsg.getShort();										// get XTS rc				0x02
			rcvMsg.skip(0x04);													// skip msg length			0x04
			int createDBRC = rcvMsg.getInt();									// get create DB rc			0x08
			rcvMsg.skip(0x02);													// skip segment length		0x0C
			rcvMsg.skip(0x02);													// skip segment version		0x0E
			rcvMsg.skip(0x04);													// skip create DB request	0x10
			rcvMsg.skip(0x04);													// skip DBID				0x14
			rcvMsg.skip(0x02);													// skip parms flag			0x18
			rcvMsg.skip(0x02);													// skip demo files flag		0x1A
			int AASRC = rcvMsg.getInt();										// get AAS rc				0x1C
			int AIFRC = rcvMsg.getInt();										// get AAS rc				0x20
			int AIFSC = rcvMsg.getInt();										// get AAS sc				0x24
			
			if (logger.isTraceEnabled())	logger.trace(method + "XTS      rc = " + XTSRC);
			if (logger.isTraceEnabled())	logger.trace(method + "createDB rc = " + createDBRC);
			if (logger.isTraceEnabled())	logger.trace(method + "AAS      rc = " + AASRC);
			if (logger.isTraceEnabled())	logger.trace(method + "AIF      rc = " + AIFRC);
			if (logger.isTraceEnabled())	logger.trace(method + "AIF      sc = " + AIFSC);
			
			if (XTSRC != 0) {
				String logMsg = String.format("N1 create DB %d message to AAS server %s returned XTS error! XTSRC = %d", targetname, dbid, XTSRC);
				logger.error(logMsg); 
				throw new AdabasException(dbid, logMsg);
			}
			if (createDBRC != 0) {
				String errMsg = String.format("CreateDatabase failed! createDBRC = %d; AASRC = %d; AIFRC = %d; AIFSC = %d" , createDBRC, AASRC, AIFRC, AIFSC);
				logger.error(errMsg);
				throw new AdabasException(dbid, "CreateDatabase", (short) 222, (short) AASRC, (short) AIFRC, (short) AIFSC);
			}
		}
		finally {
			directory.setPartition(oldPartition);					// restore previous partition
			if (logger.isTraceEnabled())	logger.trace(method + "attempting callLock.unlock() ..");
			callLock.unlock();										// TODO determine necessary XTS lock granularity
			if (logger.isTraceEnabled())	logger.trace(method + "< exited");
		}
	}

/*******************************************************************************************************/
/*************************************** AAS N1 ********************************************************/

	/**
	 * Sends N1 message to AAS 
	 * 
	 * @param nodeName		Node name to find AAS.
	 * @param timeout		XTS send timeout.
	 * @param msg			N1 request
	 * 
	 * @return				N1 reply message.
	 * @throws Exception	General exception. 
 	 */
	public Message makeAASN1Request (String nodeName, long timeout, Message msg) throws Exception {
		Directory		directory		= null;									// XTS dir server directory
		String			oldPartition	= null;									// previous dir server partition
		Message			rcvMsg			= null;									// XTS receive message
		String			fullname		= null;
		String			target			= null;
		String			urlstring		= null;
		boolean			AASFound		= false;								// AAS found flag
		
		final String aasprefix = "AAS_SMH_";
		final String method = "makeAASN1Request(): ";
		
		callLock.lock();														// get single thread XTS call lock
																				// TODO determine if this is necessary
		try {			
			if (logger.isTraceEnabled()) {
					logger.trace(method + "> entered");			
					logger.trace(method + "AAS Server Name=" + nodeName);
//					logger.trace(AdabasTrace.dumpBuffer(method + "N1 message = ", msg.body));
			}			
			fullname = aasprefix + nodeName;
			if (logger.isTraceEnabled()) logger.trace(method + "AAS Service Full Name =" + fullname);

			directory	= XTS.getDirectory();									// get directory server			
			oldPartition 	= directory.getPartition();							// remember previous partition
			directory.setPartition("*");										// look at whole directory
			
			XTSurl[] xtsUrls = directory.retrieve(XTS.XTSACCESS, fullname);		// retrieve all access URLs
			
			if (xtsUrls == null) {												// none found
				logger.error(method + "No URL found for AAS Server=" + fullname);
				throw new AdabasException(0, "Failed to find AAS Server URL for " + nodeName, (short) 222, (short) 148);
			}

			for (XTSurl url : xtsUrls) {										// loop thru URLs
				urlstring = url.toString(true);
				if (logger.isTraceEnabled()) logger.trace(method + "URL=" + urlstring);
				if (urlstring.toLowerCase().contains("aasservice=on")) {
					target = url.getTarget();
					if (logger.isTraceEnabled()) logger.trace(method + "Hit!! target=" + target);
					AASFound = true;
					XTSSendParameters xsp = new XTSSendParameters (fullname, msg, timeout, url);
					rcvMsg = XTS.sendAndWait(xsp);						
//					if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer(method + "N1 message response = ", rcvMsg.body));
					break;
				}
			}
			
			if (AASFound == false) {
				logger.error(method + "No URL found for AAS Server=" + fullname);
				throw new AdabasException(0, "Failed to find AAS Server URL for " + nodeName, (short) 222, (short) 148);
			}
			
			if (logger.isTraceEnabled())	logger.trace(method + "< exited");

                        return rcvMsg;
			
		}
		finally {
			directory.setPartition(oldPartition);								// restore previous partition
			callLock.unlock();													// TODO determine necessary XTS lock granularity
			if (logger.isTraceEnabled()) logger.trace(method + "< exited");
		}
	}

	public Message makeAASN1Request (String nodeName, String hostName, long timeout, Message msg) throws Exception {
		
		Directory		directory		= null;									// XTS dir server directory
		String			oldPartition	= null;									// previous dir server partition
		Message			rcvMsg			= null;									// XTS receive message
		String			fullname		= null;
		String			target			= null;
		String			urlstring		= null;
		boolean			AASFound		= false;								// AAS found flag
		
		final String aasprefix = "AAS_SMH_";
		final String method = "makeAASN1Request(): ";
		
		callLock.lock();														// get single thread XTS call lock
																				// TODO determine if this is necessary
		try {			
			if (logger.isTraceEnabled()) {
					logger.trace(method + "> entered");			
					logger.trace(method + "AAS Server Name=" + nodeName);
					if (hostName != null)
						logger.trace(method + "AAS Server host=" + hostName);
//					logger.trace(AdabasTrace.dumpBuffer(method + "N1 message = ", msg.body));
			}			
			fullname = aasprefix + nodeName;
			if (logger.isTraceEnabled()) logger.trace(method + "AAS Service Full Name =" + fullname);

			directory	= XTS.getDirectory();									// get directory server			
			oldPartition 	= directory.getPartition();							// remember previous partition
			directory.setPartition("*");										// look at whole directory
			
			XTSurl[] xtsUrls = directory.retrieve(XTS.XTSACCESS, fullname);		// retrieve all access URLs
			
			if (xtsUrls == null) {												// none found
				logger.error(method + "No URL found for AAS Server=" + fullname);
				throw new AdabasException(0, "Failed to find AAS Server URL for " + nodeName, (short) 222, (short) 148);
			}
			for (XTSurl url : xtsUrls) {										// loop thru URLs
				urlstring = url.toString(true);
				if (logger.isTraceEnabled()) logger.trace(method + "URL=" + urlstring);
				if (urlstring.toLowerCase().contains("aasservice=on")) {
					if ((hostName == null) || url.getHost().compareToIgnoreCase(hostName) == 0) {						
						target = url.getTarget();
 						if (logger.isTraceEnabled()) logger.trace(method + "Hit!! target=" + target);
						AASFound = true;
						XTSSendParameters xsp = new XTSSendParameters (fullname, msg, timeout, url);
						rcvMsg = XTS.sendAndWait(xsp);						
//						if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer(method + "N1 message response = ", rcvMsg.body));
						break;
					}
				}
			}
			
			if (AASFound == false) {
				logger.error(method + "No URL found for AAS Server=" + fullname);
				throw new AdabasException(0, "Failed to find AAS Server URL for " + nodeName, (short) 222, (short) 148);
			}
			
			if (logger.isTraceEnabled())	logger.trace(method + "< exited");

                        return rcvMsg;
			
		}
		finally {
			directory.setPartition(oldPartition);								// restore previous partition
			callLock.unlock();													// TODO determine necessary XTS lock granularity
			if (logger.isTraceEnabled()) logger.trace(method + "< exited");
		}
	}

	public Message makeAASN1RequestbyHostName (String HostName, long timeout, Message msg) throws Exception {
		
		Directory		directory		= null;									// XTS dir server directory
		String			oldPartition	= null;									// previous dir server partition
		Message			rcvMsg			= null;									// XTS receive message
		String			targetname		= null;									// saved AAS URL
		boolean			AASFound		= false;								// AAS found flag
		
		final String method = "makeAASN1RequestbyHostName(): ";
		
		callLock.lock();														// get single thread XTS call lock
																				// TODO determine if this is necessary
		try {
			
			if (logger.isTraceEnabled()) {
					logger.trace(method + "> entered");			
					logger.trace(method + "AAS Server HostName=" + HostName);
//					logger.trace(AdabasTrace.dumpBuffer(method + "N1 message = ", msg.body));
			}			
			
			directory	= XTS.getDirectory();									// get directory server			
			oldPartition 	= directory.getPartition();							// remember previous partition
			directory.setPartition("*");										// look at whole directory
			
			XTSurl[] xtsUrls = directory.retrieve(XTS.XTSACCESS, "*");			// retrieve all access URLs
			
			if (xtsUrls == null) {								// none found
				logger.error(method + "No URL found for AAS Server on host=" + HostName);
				throw new AdabasException(0, "Failed to find AAS Server URL for host " + HostName, (short) 222, (short) 148);
			}

			for (XTSurl url : xtsUrls) {										// loop thru URLs
				if (logger.isTraceEnabled()) logger.trace(method + " URL=" + url.toString(true));
				if (url.getTarget().contains("AAS_SMH")) {
					if (url.getHost().compareToIgnoreCase(HostName) == 0) {						
						targetname 	= url.getTarget();
						AASFound	= true;
						if (logger.isTraceEnabled()) logger.trace(method + "Hit!! target=" + targetname);						
						XTSSendParameters xsp = new XTSSendParameters (targetname, msg, timeout, url);
						rcvMsg = XTS.sendAndWait(xsp);						
//						if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer(method + "N1 message response = ", rcvMsg.body));
						break;
					}
				}
			}
			
			if (AASFound == false) {
				logger.error(method + "No URL found for AAS Server on host=" + HostName);
				throw new AdabasException(0, "Failed to find AAS Server URL for host " + HostName, (short) 222, (short) 148);
			}
			
			if (logger.isTraceEnabled())	logger.trace(method + "< exited");

                        return rcvMsg;
			
		}
		finally {
			directory.setPartition(oldPartition);								// restore previous partition
			callLock.unlock();													// TODO determine necessary XTS lock granularity
			if (logger.isTraceEnabled()) logger.trace(method + "callLock: lock released");
			if (logger.isTraceEnabled()) logger.trace(method + "< exited");
		}
	}


/*******************************************************************************************************/
/*************************************** WCP N1 ********************************************************/
	 /**
	 * Sends N1 message to WCP service 
	 * 
	 * @param 	nodeName	Node name to find WCP.
	 * @param	Partition	ADI partition.
	 * @param	hostName	Host name.
	 * @param	Url			ADI URL.
	 * @param 	timeout		XTS send timeout.
	 * @param 	msg			N1 request
	 * 
	 * @return	N1 reply message.
	 * 
	 * @throws Exception	General exception. 
	 */
	public Message makeWCPServiceN1Request (String nodeName, String Partition, String hostName, String Url, long timeout, Message msg) throws Exception {
		
		Directory		directory		= null;									// XTS dir server directory
		String			oldPartition	= null;									// previous dir server partition
		Message			rcvMsg			= null;									// XTS receive message
		String			fullname		= null;
		String			alias			= null;
		String			target			= null;
		String			urlstring		= null;
		boolean			WCPFound		= false;								// AAS found flag
        XTSurl			resourceUrl     = null;

		final String network75 = "NETWORK75_SMH_";
		final String method = "makeWCPServiceN1Request(): ";
		
		callLock.lock();														// get single thread XTS call lock
																				// TODO determine if this is necessary
		fullname = network75 + nodeName;
		if (logger.isTraceEnabled()) {
			logger.trace(method + "> entered");			
			logger.trace(method + "WCP Service Name=" + nodeName);
			if (Partition != null)
				logger.trace(method + "WCP Service Partition=" + Partition);
			if (hostName != null)
				logger.trace(method + "WCP Service Host=" + hostName);
			if (Url != null)
				logger.trace(method + "WCP Service Url=" + Url);
			logger.trace(method + "WCP Service Full Name=" + fullname);
//			logger.trace(AdabasTrace.dumpBuffer(method + "N1 message = ", msg.body, msg.length));
		}			
		try {			
			if (Url != null) {
				resourceUrl = new XTSurl(Url);
				alias = network75 + nodeName + "_" + Integer.toString(resourceUrl.getPort()); 
				WCPFound = true;
			} else {
				directory	= XTS.getDirectory();								// get directory server			
				oldPartition 	= directory.getPartition();						// remember previous partition
				directory.setPartition(Partition);								// look at whole directory
			
				XTSurl[] xtsUrls = directory.retrieve(XTS.XTSACCESS, fullname, hostName);
				directory.setPartition(oldPartition);							// restore previous partition
				if (xtsUrls == null) {											// none found
					logger.error(method + "No URL found for WCP Service=" + fullname);
					throw new N1Exception ("makeWCPServiceN1Request", "Failed to find WCP Service URL for " + nodeName, 0, 0);
				}
				if (logger.isTraceEnabled()) logger.trace(method + "Search URLS");			
				for (XTSurl url : xtsUrls) {									// loop thru URLs
					urlstring = url.toString(true);
					if (logger.isTraceEnabled()) logger.trace(method + "URL=" + urlstring);
					if (urlstring.toLowerCase().contains("wcpservice=on")) {
						target = url.getTarget();
						if (logger.isTraceEnabled()) logger.trace(method + "Hit!! target=" + target);
						WCPFound = true;
						alias = network75 + nodeName + "_" + Integer.toString(url.getPort()); 
						resourceUrl = url;
						break;
					}
				}
			}			
			if (WCPFound == false) {
				logger.error(method + "No URL found for WCP Service=" + fullname);
				throw new N1Exception ("makeWCPServiceN1Request", "Failed to find WCP Service URL for " + nodeName, 0, 0);
			}			
			XTSSendParameters xsp = new XTSSendParameters (fullname, alias, msg, timeout, resourceUrl);
			try {
				rcvMsg = XTS.sendAndWait(xsp);
			} 
			catch (XTSException exception) {
			        if (logger.isTraceEnabled()) logger.trace("XTSException Message=" + exception.getMessage());
				if (logger.isTraceEnabled()) logger.trace(method + "throw N1Exception");
				throw new N1Exception ("makeWCLServiceN1Request", exception.getMessage(), 0, exception.getResponseCode());
			}
			catch (Exception e) {
			        if (logger.isTraceEnabled()) logger.trace("Unknown Exception caught");
				throw e;
			}
//			if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer(method + "N1 message response = ", rcvMsg.body, rcvMsg.length));
			if (logger.isTraceEnabled()) logger.trace(method + "< exited");
                        return rcvMsg;
			
		}
		finally {
			callLock.unlock();							// TODO determine necessary XTS lock granularity
			if (logger.isTraceEnabled()) logger.trace(method + "< exited");
		}
	}
/*******************************************************************************************************/
/*************************************** WCL N1 ********************************************************/
	/**
	 * Sends N1 message to WCL service 
	 * 
	 * @param 	nodeName	Node name to find WCL.
	 * @param	Partition	ADI partition.
	 * @param	hostName	Host name.
	 * @param	Url			ADI URL.
	 * @param 	timeout		XTS send timeout.
	 * @param 	msg			N1 request
	 * 
	 * @return	N1 reply message.
	 * 
	 * @throws Exception	General exception. 
	 */
	public Message makeWCLServiceN1Request (String nodeName, String Partition, String hostName, String Url, long timeout, Message msg) throws Exception {
		
		Directory		directory		= null;									// XTS dir server directory
		String			oldPartition	= null;									// previous dir server partition
		Message			rcvMsg			= null;									// XTS receive message
		String			fullname		= null;
		String			alias			= null;
		String			target			= null;
		String			urlstring		= null;
		boolean			WCLFound		= false;								// AAS found flag
        XTSurl			resourceUrl     = null;
		
		final String network75 = "NETWORK75_SMH_";
		final String method = "makeWCLServiceN1Request(): ";
		
		callLock.lock();														// get single thread XTS call lock
																				// TODO determine if this is necessary
		fullname = network75 + nodeName;
		if (logger.isTraceEnabled()) {
			logger.trace(method + "> entered");			
			logger.trace(method + "WCL Service Name=" + nodeName);
			if (Partition != null)
				logger.trace(method + "WCL Service Partition=" + Partition);
			if (hostName != null)
				logger.trace(method + "WCL Service Host=" + hostName);
			if (Url != null)
				logger.trace(method + "WCL Service Url=" + Url);
			logger.trace(method + "WCL Service Full Name=" + fullname);
//			logger.trace(AdabasTrace.dumpBuffer(method + "N1 message = ", msg.body, msg.length));
		}			
		try {			
			if (Url != null) {
				resourceUrl = new XTSurl(Url);
				alias = network75 + nodeName + "_" + Integer.toString(resourceUrl.getPort()); 
				WCLFound = true;
			} else {
				directory	= XTS.getDirectory();								// get directory server			
				oldPartition 	= directory.getPartition();						// remember previous partition
				directory.setPartition(Partition);								// look at whole directory
			
				XTSurl[] xtsUrls = directory.retrieve(XTS.XTSACCESS, fullname, hostName);
				directory.setPartition(oldPartition);							// restore previous partition
				if (xtsUrls == null) {											// none found
					logger.error(method + "No URL found for WCL Service=" + fullname);
					throw new N1Exception ("makeWCLServiceN1Request", "Failed to find WCL Service URL for " + nodeName, 0, 0);
				}
				if (logger.isTraceEnabled())	logger.trace(method + "Search URLS");			
				for (XTSurl url : xtsUrls) {									// loop thru URLs
					urlstring = url.toString(true);
					if (logger.isTraceEnabled()) logger.trace(method + "URL=" + urlstring);
					if (urlstring.toLowerCase().contains("wclservice=on")) {
						target = url.getTarget();
						if (logger.isTraceEnabled()) logger.trace(method + "Hit!! target=" + target);
						WCLFound = true;
						alias = network75 + nodeName + "_" + Integer.toString(url.getPort()); 
						resourceUrl = url;
						break;
					}
				}
			}			
			if (WCLFound == false) {
				logger.error(method + "No URL found for WCL Service=" + fullname);
				throw new N1Exception ("makeWCLServiceN1Request", "Failed to find WCL Service URL for " + nodeName, 0, 0);
			}			

			XTSSendParameters xsp = new XTSSendParameters (fullname, alias, msg, timeout, resourceUrl);
			try {
				rcvMsg = XTS.sendAndWait(xsp);
			} 
			catch (XTSException exception) {
			        if (logger.isTraceEnabled()) logger.trace("XTSException Message=" + exception.getMessage());
				if (logger.isTraceEnabled()) logger.trace(method + "throw N1Exception");
					throw new N1Exception ("makeWCLServiceN1Request", exception.getMessage(), 0, exception.getResponseCode());
				}
			catch (Exception e) {
			        if (logger.isTraceEnabled()) logger.trace("Unknown Exception caught");
				throw e;
			}
//			if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer(method + "N1 message response = ", rcvMsg.body, rcvMsg.length));
			if (logger.isTraceEnabled()) logger.trace(method + "< exited");
                        return rcvMsg;			
		}
		finally {
			callLock.unlock();							// TODO determine necessary XTS lock granularity
			if (logger.isTraceEnabled()) logger.trace(method + "< exited");
		}
	}

/*******************************************************************************************************/
/*************************************** Kernel N1 ********************************************************/
	/**
	 * Sends N1 message to WCP Kernel Server 
	 * 
	 * @param	serverName	Server name.
	 * @param 	nodeName	Node name to find Kernel.
	 * @param	Partition	ADI partition.
	 * @param	hostName	Host name.
	 * @param	Url			ADI URL.
	 * @param 	timeout		XTS send timeout.
	 * @param 	msg			N1 request
	 * 
	 * @return	N1 reply message.
	 * 
	 * @throws 	Exception	General exception. 
	 */
	public Message makeKernelN1Request (String serverName, String nodeName, String Partition, String hostName, String Url, long timeout, Message msg) throws Exception {
		
		Directory		directory		= null;									// XTS dir server directory
		String			oldPartition	= null;									// previous dir server partition
		Message			rcvMsg			= null;									// XTS receive message
		String			fullname		= null;
		String			alias			= null;
		String			target			= null;
		String			urlstring		= null;
		boolean			KernelFound		= false;								// AAS found flag
        XTSurl			resourceUrl     = null;
		
		final String network75 = "NETWORK75_SMH_";
		final String method = "makeKernelN1Request(): ";
		
		callLock.lock();														// get single thread XTS call lock
																				// TODO determine if this is necessary
		fullname = network75 + nodeName; 
		if (logger.isTraceEnabled()) {
			logger.trace(method + "> entered");			
			logger.trace(method + "WCP Kernel Server Name=" + nodeName);
			if (Partition != null)
				logger.trace(method + "WCP Kernel Server Partition=" + Partition);
			if (hostName != null)
				logger.trace(method + "WCP Kernel Server Host=" + hostName);
			if (serverName != null)
				logger.trace(method + "WCP Server Name=" + serverName);
			if (Url != null)
				logger.trace(method + "WCP Kernel Url=" + Url);
			logger.trace(method + "WCP Kernel Server Full Name=" + fullname);
//			logger.trace(AdabasTrace.dumpBuffer(method + "N1 message = ", msg.body, msg.length));
		}			
		try {			
			if (Url != null) {
				resourceUrl = new XTSurl(Url);
				alias = network75 + nodeName + "_" + Integer.toString(resourceUrl.getPort()); 
				KernelFound = true;
			} else {
				directory		= XTS.getDirectory();							// get directory server			
				oldPartition 	= directory.getPartition();						// remember previous partition
				directory.setPartition(Partition);								// look at whole directory
			
				XTSurl[] xtsUrls = directory.retrieve(XTS.XTSACCESS, fullname, hostName);
				directory.setPartition(oldPartition);							// restore previous partition
				if (xtsUrls == null) {											// none found
				logger.error(method + "No URL found for WCP Kernel Server=" + fullname);
				throw new N1Exception ("makeKernelN1Request", "Failed to find WCP Kernel Server URL for " + nodeName, 0, 0);
				}
				if (logger.isTraceEnabled())	logger.trace(method + "Search URLS");			
				for (XTSurl url : xtsUrls) {									// loop thru URLs
					urlstring = url.toString(true);
					if (logger.isTraceEnabled()) logger.trace(method + "URL=" + urlstring);
					if (urlstring.toLowerCase().contains("wcpkernel=on")) {
						if (serverName != null && !urlstring.toLowerCase().contains("server=" + serverName.toLowerCase()))
							continue;
						target = url.getTarget();
						if (logger.isTraceEnabled()) logger.trace(method + "Hit!! target=" + target);
						KernelFound = true;
						alias = network75 + nodeName + "_" + Integer.toString(url.getPort()); 
						resourceUrl = url;
						break;
					}
				}
			}			
			if (KernelFound == false) {
				logger.error(method + "No URL found for WCP Kernel Server=" + fullname);
				throw new N1Exception ("makeKernelN1Request", "Failed to find WCP Kernel Server URL for " + nodeName, 0, 0);
			}			
			XTSSendParameters xsp = new XTSSendParameters (fullname, alias, msg, timeout, resourceUrl);
			try {
				rcvMsg = XTS.sendAndWait(xsp);
			} 
			catch (XTSException exception) {
			        if (logger.isTraceEnabled()) logger.trace("XTSException Message=" + exception.getMessage());
				if (logger.isTraceEnabled()) logger.trace(method + "throw N1Exception");
				throw new N1Exception ("makeWCLServiceN1Request", exception.getMessage(), 0, exception.getResponseCode());
			}
			catch (Exception e) {
			        if (logger.isTraceEnabled()) logger.trace("Unknown Exception caught");
				throw e;
			}
//			if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer(method + "N1 message response = ", rcvMsg.body, rcvMsg.length));
			if (logger.isTraceEnabled()) logger.trace(method + "< exited");
                        return rcvMsg;			
		}
		finally {
			callLock.unlock();						// TODO determine necessary XTS lock granularity
			if (logger.isTraceEnabled()) logger.trace(method + "< exited");
		}
	}

/*******************************************************************************************************/
/*************************************** W2 ********************************************************/

	/**
	 * Sends W2 message to mainframe WCP
	 * 
	 * @param msg			W2 request.
	 * @param kernelName    Kernel name (node name).
	 *
	 * @return	W2 reply message.
	 * 
	 * @throws XTSException	XTS specific exception. 
	*/
	public Message callAASW2(Message msg, String kernelName) throws XTSException {
		
		String method = "callAASW2()";
    
		if (logger.isTraceEnabled()) logger.trace("================= " + method + " =================" + "> entered");
		if (logger.isTraceEnabled()) logger.trace("kernelName = " + kernelName);
//		if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer("W2 request message body", msg.body, msg.length));
		
		String targetName	= "NETWORK75_SMH_" + kernelName;
		if (logger.isTraceEnabled()) logger.trace("targetName = " + targetName);

		XTSSendParameters	xsp	= new XTSSendParameters(targetName, msg, 30000);
		
		Message	rplMsg	= null;
		try {
			rplMsg	= XTS.sendAndWait(xsp);
			
//			if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer("W2 reply message body", rplMsg.body, rplMsg.length));
		}
		catch (XTSException xe) {
			logger.error("XTSException = " + xe.getMessage());
			throw xe;
		}
		
// 		below is a sample W2 reply message for zIIP stats provided by usarc for testing

//		byte[]	baRpl	= {(byte) 0x57, (byte) 0x32, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xC8, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xAD, (byte) 0x9F, (byte) 0x02, (byte) 0x00,	// 0x0000
//						   (byte) 0x00, (byte) 0x00, (byte) 0xD4, (byte) 0xD6, (byte) 0xC4, (byte) 0xC5, (byte) 0xD3, (byte) 0xF0, (byte) 0xF0, (byte) 0xF1, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0010
//						   (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xA8, (byte) 0x02, (byte) 0xE2, (byte) 0x0A, (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA8, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40,	// 0x0020
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA8, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C,	// 0x0030
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07,	// 0x0040
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,	// 0x0050
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18,	// 0x0060
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x54, (byte) 0xAB, (byte) 0x9B,	// 0x0070
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xB7, (byte) 0x47, (byte) 0x92, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xCF, (byte) 0x10, (byte) 0x03,	// 0x0080
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x86, (byte) 0x57, (byte) 0x95, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0090
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x02,	// 0x00A0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1A, (byte) 0x5A, (byte) 0x9D, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x9D, (byte) 0x64, (byte) 0x09,	// 0x00B0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xB7, (byte) 0x47, (byte) 0x92, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,	// 0x00C0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x00D0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x00E0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x14,	// 0x00F0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0100
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0110
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0120
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0130
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0140
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10,	// 0x0150
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0160
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0170
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0180
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0190
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x01A0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x01B0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x01C0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x01D0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x01E0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x01F0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0200
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0210
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0220
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0230
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0240
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0250
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0260
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0270
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0280
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x0290
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x02A0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	// 0x02B0
//						   (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};																																								// 0x02C0
		
//						          0x00,        0x01,        0x02,        0x03,        0x04,        0x05,        0x06,        0x07,        0x08,        0x09,        0x0A,        0x0B,        0x0C,        0x0D,        0x0E,        0x0F};

//		Message	rplMsg	= Message.newMessage(baRpl.length);
						   
//		rplMsg.putBytes(baRpl);

	    if (logger.isTraceEnabled()) logger.trace("================= " + method + " =================" + "< exited");
		
		return rplMsg;
	}

	/**
	 * Sends W2 message to mainframe Adabas
	 * 
	 * @param msg			W2 request.
	 * @param dbid    		Database ID.
	 * @param hostName		Host name.
	 *
	 * @return	W2 reply message.
	 * 
	 * @throws XTSException	XTS specific exception. 
	*/
	public Message callADAMFW2(Message msg, int dbid, String hostName) throws XTSException {
		
		String method = "callADAMFW2()";
    
		if (logger.isTraceEnabled()) logger.trace("================= " + method + " =================" + "> entered");
		if (logger.isTraceEnabled()) logger.trace("dbid     = " + dbid);
		if (logger.isTraceEnabled()) logger.trace("hostName = " + hostName);
//		if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer("W2 request message body", msg.body, msg.length));
		
		XTSSendParameters	xsp	= new XTSSendParameters(dbid, msg, 30000, hostName);
		
		Message	rplMsg	= null;
		try {
			rplMsg	= XTS.sendAndWait(xsp);
			
//			if (logger.isTraceEnabled()) logger.trace(AdabasTrace.dumpBuffer("W2 reply message body", rplMsg.body, rplMsg.length));
		}
		catch (XTSException xe) {
			logger.error("XTSException = " + xe.getMessage());
			throw xe;
		}
		
	    if (logger.isTraceEnabled()) logger.trace("================= " + method + " =================" + "< exited");
		
		return rplMsg;
	}

}
