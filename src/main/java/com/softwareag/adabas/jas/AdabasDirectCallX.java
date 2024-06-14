package com.softwareag.adabas.jas;

import org.apache.logging.log4j.Logger;

import com.softwareag.adabas.jas.xts.AdabasDirectCallXts;
import com.softwareag.adabas.jas.xts.XtsDefinitions;

/**
 * Main class for sending extended ACBX direct calls to Adabas.
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

public class AdabasDirectCallX extends XtsDefinitions {
	
	protected AdabasControlBlockX	acbx;									// adabas control block
	protected AdabasBufferX			FB;										// format buffer
	protected AdabasBufferX			RB;										// record buffer
	protected AdabasBufferX			SB;										// search buffer
	protected AdabasBufferX			VB;										// value  buffer
	protected AdabasBufferX			IB;										// ISN    buffer

	protected AdabasDirectCallXts	adcXts;									// direct call via XTS class instance
	protected long					xtsSendTimeout 		= SEND_TIMEOUT;		// XTS send timeout
	protected int					xtsConnectTimeout	= 0;				// XTS connect timeout
	protected boolean				xtsEBCDIC			= false;			// XTS encoding flag

	protected byte					callSource			= 0x00;				// A2 msg call source (e.g. AMN user)
	
	final static Logger logger = AdabasTrace.getLogger("com.softwareag.adabas.jas.AdabasDirectCallX");
	

	/**
	 * Constructor taking ACBX.
	 * 
	 * @param 	acbx	Adabas control block (extended).
	 * 
	 * @throws AdabasException	Adabas specific exception. 
	 */
	public AdabasDirectCallX(AdabasControlBlockX acbx) throws AdabasException {
		
		if (acbx == null) {														// validate ACB provided
			logger.fatal("Constructor: " + "No Adabas control block provided");
			throw new AdabasException("No Adabas control block provided");
		}
		
		this.acbx = acbx;
		this.adcXts = new AdabasDirectCallXts();
	}
	
	/**
	 * Constructor taking ACBX and encoding flag.
	 * 
	 * @param 	acbx 		Adabas control block.
	 * @param	xtsEBCDIC	XTS encoding flag.
	 * 
	 * @throws 	AdabasException		Adabas specific exception. 
	 */
	public AdabasDirectCallX(AdabasControlBlockX acbx, boolean xtsEBCDIC) throws AdabasException {
		
		if (acbx == null) {														// validate ACB provided
			logger.fatal("Constructor: " + "No Adabas control block provided");
			throw new AdabasException("No Adabas control block provided");
		}

		this.acbx 	= acbx;
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

		logger.trace(method + "> entered");
		logger.debug(method + this.acbx.toString());
		logger.trace(method + AdabasTrace.dumpBuffer("Adabas Control Block = ", this.acbx.getACBArray()));		
		if (this.FB != null)	logger.trace(method + AdabasTrace.dumpBuffer("Format Buffer = ", this.FB.getDataBytes()));
		if (this.RB != null)	logger.trace(method + AdabasTrace.dumpBuffer("Record Buffer = ", this.RB.getDataBytes()));
		if (this.SB != null)	logger.trace(method + AdabasTrace.dumpBuffer("Search Buffer = ", this.SB.getDataBytes()));
		if (this.VB != null)	logger.trace(method + AdabasTrace.dumpBuffer("Value  Buffer = ", this.VB.getDataBytes()));
		if (this.IB != null)	logger.trace(method + AdabasTrace.dumpBuffer("ISN    Buffer = ", this.IB.getDataBytes()));
		
		
		// TODO add local / remote logic
		
		adcXts.callAdabasXtsA2(this);											// call it
		
		short rc = acbx.getAcbRSP();
		if (rc > 3) {
			int 	dbid	= acbx.getAcbDBID();
			short 	sc 	= acbx.getAcbERRC();
			short	src	= acbx.getAcbSUBR();
			short	ssc	= acbx.getAcbSUBS();
			String	cmd	= acbx.getAcbCMD();

			logger.debug(method + "DBID = " + dbid + ": cmd = " + cmd + ": rc = " + rc + ": subcode = " + sc + ": subrc = " + src + ": subsc = " + ssc);
			logger.trace(AdabasTrace.dumpBuffer(method + "acbx = ", acbx.acbxBytes));
			
			throw new AdabasException(dbid, cmd, rc, sc, src, ssc);
		}
		
		if (rc == 0)
			logger.info(method + acbx.getAcbCMD() + " call succeeded");
		else
			logger.info(method + acbx.getAcbCMD() + " call rc = " + acbx.getAcbRSP());
		logger.trace(method + AdabasTrace.dumpBuffer("result Adabas Control Block = ", this.acbx.getACBArray()));		
		if (this.FB != null)	logger.trace(method + AdabasTrace.dumpBuffer("result Format Buffer = ", this.FB.getDataBytes()));
		if (this.RB != null)	logger.trace(method + AdabasTrace.dumpBuffer("result Record Buffer = ", this.RB.getDataBytes()));
		if (this.SB != null)	logger.trace(method + AdabasTrace.dumpBuffer("result Search Buffer = ", this.SB.getDataBytes()));
		if (this.VB != null)	logger.trace(method + AdabasTrace.dumpBuffer("result Value  Buffer = ", this.VB.getDataBytes()));
		if (this.IB != null)	logger.trace(method + AdabasTrace.dumpBuffer("result ISN    Buffer = ", this.IB.getDataBytes()));
		logger.trace(method + "< exited");		
	}
	
	/**
	 * Get Adabas control block (extended).
	 * 
	 * @return Adabas control block (extended).
	 */
	public AdabasControlBlockX getAcbX() {
		
		return acbx;
	}
	
	/**
	 * Set Adabas control block (extended).
	 * 
	 * @param acbx Adabas control block (extended) to set.
	 */
	public void setAcbX(AdabasControlBlockX acbx) {
		
		this.acbx = acbx;
	}
	
	/**
	 * Get format buffer (extended).
	 * 
	 * @return format buffer (extended).
	 */
	public AdabasBufferX getFB() {
		
		return FB;
	}
	
	/**
	 * Set format buffer (extended).
	 * 
	 * @param fb format buffer (extended) to set.
	 */
	public void setFB(AdabasBufferX fb) {
		
		this.FB = fb;
	}
	
	/**
	 * Get record buffer (extended).
	 * 
	 * @return record buffer (extended).
	 */
	public AdabasBufferX getRB() {
		
		return RB;
	}
	
	/**
	 * Set record buffer (extended).
	 * 
	 * @param rb record buffer (extended) to set.
	 */
	public void setRB(AdabasBufferX rb) {
		
		this.RB = rb;
	}
	
	/**
	 * Get search buffer (extended).
	 * 
	 * @return search buffer (extended).
	 */
	public AdabasBufferX getSB() {
		
		return SB;
	}
	
	/**
	 * Set search buffer (extended).
	 * 
	 * @param sb record buffer (extended) to set.
	 */
	public void setSB(AdabasBufferX sb) {
		
		this.SB = sb;
	}
	
	/**
	 * Get value buffer (extended).
	 * 
	 * @return value buffer (extended).
	 */
	public AdabasBufferX getVB() {
		
		return VB;
	}
	
	/**
	 * Set value buffer (extended).
	 * 
	 * @param vb value buffer (extended) to set.
	 */
	public void setVB(AdabasBufferX vb) {
		
		this.VB = vb;
	}
	
	/**
	 * Get ISN buffer (extended).
	 * 
	 * @return ISN buffer (extended).
	 */
	public AdabasBufferX getIB() {
		
		return IB;
	}
	
	/**
	 * Set ISN buffer (extended).
	 * 
	 * @param ib ISN buffer (extended) to set.
	 */
	public void setIB(AdabasBufferX ib) {
		
		this.IB = ib;
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
	 * Set XTS send timeout
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

	/**
	 * Get A2 msg call source flag.
	 * 
	 * @return	A2 msg call source flag.
	 */
	public byte getCallSource() {
		return callSource;
	}

	/**
	 * Set A2 msg call source.
	 * 
	 * @param callSource	A2 msg call source (e.g. AMN user)
	 */
	public void setCallSource(byte callSource) {
		this.callSource = callSource;
	}

}
