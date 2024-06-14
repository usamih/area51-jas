package com.softwareag.adabas.jas;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.Logger;

import com.softwareag.adabas.jas.xts.AdabasDirectCallXts;
import com.softwareag.adabas.jas.xts.XtsDefinitions;

/**
 * Main class for sending classic direct calls to Adabas.
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

public class AdabasDirectCall extends XtsDefinitions {
	
	protected AdabasControlBlock 	acb;						// adabas control block
	protected ByteBuffer			FB;							// format buffer
	protected ByteBuffer			RB;							// record buffer
	protected ByteBuffer			SB;							// search buffer
	protected ByteBuffer			VB;							// value  buffer
	protected ByteBuffer			IB;							// ISN    buffer
	protected byte[]				FBba;						// format buffer as byte array
	protected byte[]				RBba;						// record buffer as byte array
	protected byte[]				SBba;						// search buffer as byte array
	protected byte[]				VBba;						// value  buffer as byte array
	protected byte[]				IBba;						// ISN    buffer as byte array
	
	protected AdabasDirectCallXts	adcXts;								// direct call via XTS class instance
	protected long					xtsSendTimeout 		= SEND_TIMEOUT;	// XTS send timeout
	protected int					xtsConnectTimeout	= 0;			// XTS connect timeout
	protected boolean				xtsEBCDIC			= false;		// XTS encoding flag
	
	final static Logger logger = AdabasTrace.getLogger("com.softwareag.adabas.jas.AdabasDirectCall");


	/**
	 * Constructor taking ACB.
	 * 
	 * @param	acb	Adabas control block.
	 * 
	 * @throws 	AdabasException		Adabas specific exception. 
	 */
	public AdabasDirectCall(AdabasControlBlock acb) throws AdabasException {
		
		if (acb == null) {														// validate ACB provided
			logger.fatal("Constructor: " + "No Adabas control block provided");
			throw new AdabasException("No Adabas control block provided");
		}

		this.acb 	= acb;
		this.adcXts	= new AdabasDirectCallXts();
	}
	
	/**
	 * Constructor taking ACB and encoding flag.
	 * 
	 * @param 	acb 		Adabas control block.
	 * @param	xtsEBCDIC	XTS encoding flag.
	 * 
	 * @throws 	AdabasException		Adabas specific exception. 
	 */
	public AdabasDirectCall(AdabasControlBlock acb, boolean xtsEBCDIC) throws AdabasException {
		
		if (acb == null) {														// validate ACB provided
			logger.fatal("Constructor: " + "No Adabas control block provided");
			throw new AdabasException("No Adabas control block provided");
		}

		this.acb 	= acb;
		this.setXtsEBCDIC(xtsEBCDIC);
		this.adcXts	= new AdabasDirectCallXts();
	}

	/**
	 * Send a direct call to Adabas.
	 * 
	 * @throws Exception	General exception.
	 */
	public void callAdabas() throws Exception {
		
		final String method = "callAdabas(): ";
		
		if (this.acb == null) {													// validate ACB provided
			logger.fatal(method + "No Adabas control block provided");
			throw new AdabasException("No Adabas control block provided");
		}
		
		logger.trace(method + "> entered");
		logger.debug(method + this.acb.toString());
		logger.trace(method + AdabasTrace.dumpBuffer("Adabas Control Block = ", this.acb.getACBArray()));		
		if (this.FBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("Format Buffer = ", this.FBba));
		if (this.RBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("Record Buffer = ", this.RBba));
		if (this.SBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("Search Buffer = ", this.SBba));
		if (this.VBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("Value  Buffer = ", this.VBba));
		if (this.IBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("ISN    Buffer = ", this.IBba));
		
		// TODO add local / remote logic
		
		adcXts.callAdabasXtsA1(this);						// call it
		
		short rc = acb.getAcbRSP();
		if (rc > 3) {
			byte[]		baSC 	= new byte[2];
			System.arraycopy(acb.acbBytes, 0x2E, baSC, 0x00, 2);
			ByteBuffer	bbSC 	= ByteBuffer.wrap(baSC);
			short		sc		= bbSC.getShort();
			logger.error(method + "DBID = " + acb.getAcbDBID() + ": rc = " + rc + "; sc = " + sc);
			logger.trace(AdabasTrace.dumpBuffer(method + "acb = ", acb.acbBytes));
			
			throw new AdabasException(acb.getAcbDBID(), rc, sc);
		}
		
		if (rc == 0)
			logger.info(method + acb.getAcbCMD() + " call succeeded");
		else
			logger.info(method + acb.getAcbCMD() + " call rc = " + acb.getAcbRSP());
		logger.trace(method + AdabasTrace.dumpBuffer("result Adabas Control Block = ", this.acb.getACBArray()));			
		if (this.FBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("result Format Buffer = ", this.FBba));
		if (this.RBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("result Record Buffer = ", this.RBba));
		if (this.SBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("result Search Buffer = ", this.SBba));
		if (this.VBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("result Value  Buffer = ", this.VBba));
		if (this.IBba != null)	logger.trace(method + AdabasTrace.dumpBuffer("result ISN    Buffer = ", this.IBba));
		logger.trace(method + "< exited");
		
	}

	/**
	 * Get Adabas control block.
	 * 
	 * @return the acb
	 */
	public AdabasControlBlock getAcb() {
		return acb;
	}

	/**
	 * Set Adabas control block.
	 * 
	 * @param acb the acb to set
	 */
	public void setAcb(AdabasControlBlock acb) {
		this.acb = acb;
	}

	/**
	 * Get format buffer.
	 * 
	 * @return the FB
	 */
	public ByteBuffer getFB() {
		return this.FB;
	}

	/**
	 * Get the format buffer byte array.
	 * 
	 * @return the FB byte array.
	 */
	public byte[] getFBArray() {
		return this.FBba;
	}

	/**
	 * Set format buffer.
	 * 
	 * @param fb	The FB to set.
	 */
	public void setFB(byte[] fb) {
		
		this.FBba 	= fb;											// set  FB byte array
		if (fb != null)
			this.FB = ByteBuffer.wrap(fb);							// wrap FB byte array in FB byte buffer
		else
			this.FB	= null;
	}

	/**
	 * Get the record buffer.
	 * 
	 * @return the RB
	 */
	public ByteBuffer getRB() {
		return this.RB;
	}
	
	/**
	 * Get the record buffer byte array.
	 * 
	 * @return the RB byte array.
	 */
	public byte[] getRBArray() {
		return this.RBba;
	}

	/**
	 * Set the record buffer.
	 * 
	 * @param rb 	The RB to set
	 */
	public void setRB(byte[] rb) {
		
		this.RBba	= rb;											// set  RB byte array
		if (rb != null)
			this.RB = ByteBuffer.wrap(rb);							// wrap RB byte array into RB byte buffer
		else
			this.RB = null;
	}

	/**
	 * Get the search buffer.
	 * 
	 * @return the SB
	 */
	public ByteBuffer getSB() {
		return this.SB;
	}

	/**
	 * Get the search buffer byte array.
	 * 
	 * @return the SB byte array.
	 */
	public byte[] getSBArray() {
		return this.SBba;
	}

	/**
	 * Set the search buffer.
	 * 
	 * @param sb 	The SB to set
	 */
	public void setSB(byte[] sb) {

		this.SBba	= sb;											// set  SB byte array
		if (sb != null)
			this.SB = ByteBuffer.wrap(sb);							// wrap SB byte array into SB byte buffer
		else
			this.SB = null;
	}

	/**
	 * Get the value buffer.
	 * 
	 * @return the VB
	 */
	public ByteBuffer getVB() {
		return this.VB;
	}

	/**
	 * Get the value buffer byte array.
	 * 
	 * @return the VB byte array.
	 */
	public byte[] getVBArray() {
		return this.VBba;
	}

	/**
	 * Set the value buffer.
	 * 
	 * @param vb 	The VB to set
	 */
	public void setVB(byte[] vb) {

		this.VBba 	= vb;											// set  VB byte array
		if (vb != null)
			this.VB	= ByteBuffer.wrap(vb);							// wrap VB byte array into VB byte buffer
		else 
			this.VB = null;
	}

	/**
	 * Get the ISN buffer.
	 * 
	 * @return the IB
	 */
	public ByteBuffer getIB() {
		return IB;
	}

	/**
	 * Get the ISN buffer byte array.
	 * 
	 * @return the IB byte array.
	 */
	public byte[] getIBArray() {
		return this.IBba;
	}

	/**
	 * Set the ISN buffer.
	 * 
	 * @param ib 	The IB to set
	 */
	public void setIB(byte[] ib) {

		this.IBba 	= ib;											// set  IB byte array
		if (ib != null)
			this.IB	= ByteBuffer.wrap(ib);							// wrap IB byte array into IB byte buffer
		else
			this.IB = null;
	}
	
	/**
	 * Get format buffer length.
	 * 
	 * @return format buffer length.
	 */
	public short getFBL() {
		
		if (this.acb instanceof AdabasControlBlock)					// classic ACB
			return ((AdabasControlBlock) this.acb).getAcbFBL();		// FBL comes from ACB
		else
			return 0;
	}

	/**
	 * Get record buffer length.
	 * 
	 * @return record buffer length.
	 */
	public short getRBL() {
		
		if (this.acb instanceof AdabasControlBlock)					// classic ACB
			return ((AdabasControlBlock) this.acb).getAcbRBL();		// RBL comes from ACB
		else
			return 0;
	}

	/**
	 * Get search buffer length.
	 * 
	 * @return search buffer length.
	 */
	public short getSBL() {
		
		if (this.acb instanceof AdabasControlBlock)					// classic ACB
			return ((AdabasControlBlock) this.acb).getAcbSBL();		// SBL comes from ACB
		else
			return 0;
	}

	/**
	 * Get value buffer length.
	 * 
	 * @return value buffer length.
	 */
	public short getVBL() {
		
		if (this.acb instanceof AdabasControlBlock)					// classic ACB
			return ((AdabasControlBlock) this.acb).getAcbVBL();		// VBL comes from ACB
		else
			return 0;
	}

	/**
	 * Get ISN buffer length.
	 * 
	 * @return ISN buffer length.
	 */
	public short getIBL() {
		
		if (this.acb instanceof AdabasControlBlock)					// classic ACB
			return ((AdabasControlBlock) this.acb).getAcbIBL();		// IBL comes from ACB
		else
			return 0;
	}

	/**
	 * Get XTS send timeout.
	 * 
	 * @return xtsSendTimeout.
	 */
	public long getXtsSendTimeout() {
		
		return xtsSendTimeout;
	}

	/**
	 * Set XTS send timeout.
	 * 
	 * @param xtsSendTimeout xtsSendTimeout to set.
	 */
	public void setXtsSendTimeout(long xtsSendTimeout) {
		
		this.xtsSendTimeout = xtsSendTimeout;
	}

	/**
	 * Get XTS connect timeout.
	 * 
	 * @return xtsConnectTimeout.
	 */
	public int getXtsConnectTimeout() {
		
		return xtsConnectTimeout;
	}

	/**
	 * Set XTS connect timeout.
	 * 
	 * @param xtsConnectTimeout xtsConnectTimeout to set.
	 */
	public void setXtsConnectTimeout(int xtsConnectTimeout) {
		
		this.xtsConnectTimeout = xtsConnectTimeout;
	}

	/**
	 * Get XTS encoding flag.
	 * 
	 * @return	XTS encoding flag.
	 */
	public boolean isXtsEBCDIC() {
		return xtsEBCDIC;
	}

	/**
	 * Set XTS encoding flag.
	 * 
	 * @param xtsEBCDIC	XTS encoding flag.
	 */
	public void setXtsEBCDIC(boolean xtsEBCDIC) {
		this.xtsEBCDIC = xtsEBCDIC;
	}

}
