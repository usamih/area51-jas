/**
 * Adabas Legal Command Table - Mainframe.
 */
package com.softwareag.adabas.jas.xts;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import com.softwareag.adabas.jas.AdabasBufferX;
import com.softwareag.adabas.jas.AdabasDirectCallX;
import com.softwareag.adabas.jas.AdabasTrace;

/* 
 * Copyright (c) 1998-2021 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, 
 * and/or its subsidiaries and/or its affiliates and/or their licensors. Use, reproduction, transfer, 
 * publication or disclosure is prohibited except as specifically provided for in your License Agreement 
 * with Software AG.
 */

/**
 * Adabas Legal Command Table - Mainframe.
 * 
 * Contains all possible command codes and associated buffers in/out.
 * 
 * @author usadva
 */
public class CommandTableMF {
	
	private	static CommandTableMF	instance	= null;

	public static ArrayList<CommandMF> cmdTable = new ArrayList<CommandMF>();
	
	// The following definitions are taken from 'ADABAS.SRC.W.MACLIB(UB)'.
	
	public static final byte	MM	= (byte) 0x80;		// move multifetch  buffer
	public static final byte	MP	= (byte) 0x40;		// move performance buffer
	public static final byte 	MU	= (byte) 0x20;		// move user info   buffer
	public static final byte	MF	= (byte) 0x10;		// move format      buffer
	public static final byte 	MR	= (byte) 0x08;		// move record      buffer
	public static final byte 	MS	= (byte) 0x04;		// move search      buffer
	public static final byte 	MV	= (byte) 0x02;		// move value       buffer
	public static final byte 	MI	= (byte) 0x01;		// move ISN         buffer
	
	// The following definitions are taken from 'ADABAS.SRC.W.MACLIB.DEP(LCTE)'.
	
	public static final byte	SP1	= (byte) 0x80;		// in:  if ACBCOP2='V',     set SB+VB
														// out: reserved
	public static final byte	SP2	= (byte) 0x40;		// in:  if ACBCOP2='E',     set RB
														// out: reserved
	public static final byte	SP3	= (byte) 0x20;		// in:  if ACBADD4=' ',     set IB
														// out: if ACBCOP2='E',     set RB
	public static final byte	SP4	= (byte) 0x10;		// in:  if ACBCOP1='P|M|O', set IB
														// out: if ACBCOP2='P|M|O', set IB
	public static final byte	SP5	= (byte) 0x08;		// in:  set from ACBCOP1
														// out: set from ACBCOP2
	public static final byte	SP6	= (byte) 0x04;		// in:  if ACBCOP1='P',     set IB
														//		if ACBCOP1='M|O',   set IB+MB
														// out: if ACBCOP1='P',     set IB
														//      if ACBCOP1='M|O',   set IB+MB
	public static final byte	SP7	= (byte) 0x02;		// reserved
	public static final byte	SP8	= (byte) 0x01;		// reserved
	
	final static Logger logger = AdabasTrace.getLogger("com.softwareag.adabas.jas.xts.CommandTableMF");

	/**
	 * Constructor.
	 * 
	 * Private contructor makes this a singleton class.
	 */
	private CommandTableMF() {
		
		String method = "CommandTableMF(): ";
		
		if (logger.isTraceEnabled())	logger.trace(method + "> entered");
		
		// The following definitions are taken from 'ADABAS.SRC.W.MACLIB.DEP(LCT)'.
		
		cmdTable.add(new CommandMF("L1", (byte) (MF), 				(byte) (MR),				(byte) (0x00),		(byte) (SP6)));
		cmdTable.add(new CommandMF("S1", (byte) (MF+MS+MV),			(byte) (MR+MI),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("S4", (byte) (MF+MS+MV), 		(byte) (MR+MI),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("L2", (byte) (MF), 				(byte) (MR),				(byte) (0x00),		(byte) (SP6)));
		cmdTable.add(new CommandMF("L4", (byte) (MF), 				(byte) (MR),				(byte) (0x00),		(byte) (SP6)));
		cmdTable.add(new CommandMF("L5", (byte) (MF), 				(byte) (MR),				(byte) (0x00),		(byte) (SP6)));
		cmdTable.add(new CommandMF("L3", (byte) (MF), 				(byte) (MR),				(byte) (SP1),		(byte) (SP6)));
		cmdTable.add(new CommandMF("L6", (byte) (MF), 				(byte) (MR),				(byte) (SP1),		(byte) (SP6)));
		cmdTable.add(new CommandMF("N1", (byte) (MF+MR), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("N2", (byte) (MF+MR), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("A1", (byte) (MF+MR), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("S9", (byte) (0x00), 			(byte) (MI),				(byte) (SP3),		(byte) (0x00)));
		cmdTable.add(new CommandMF("A4", (byte) (MF+MR), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("RC", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("E1", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("E4", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("ET", (byte) (0x00), 			(byte) (0x00),				(byte) (SP2+SP4),	(byte) (0x00)));
		cmdTable.add(new CommandMF("HI", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("RI", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("OP", (byte) (MR), 				(byte) (0x00),				(byte) (0x00),		(byte) (SP3)));
		cmdTable.add(new CommandMF("CL", (byte) (0x00), 			(byte) (0x00),				(byte) (SP2+SP4),	(byte) (0x00)));
		cmdTable.add(new CommandMF("A9", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("LC", (byte) (MF), 				(byte) (MR),				(byte) (0x00),		(byte) (SP4)));
		cmdTable.add(new CommandMF("L7", (byte) (0x00), 			(byte) (MR),				(byte) (0x00),		(byte) (SP4)));
		cmdTable.add(new CommandMF("L8", (byte) (0x00), 			(byte) (MR),				(byte) (0x00),		(byte) (SP4)));
		cmdTable.add(new CommandMF("LA", (byte) (0x00), 			(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("LB", (byte) (0x00), 			(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("LD", (byte) (0x00), 			(byte) (MR),				(byte) (0x00),		(byte) (SP4)));
		cmdTable.add(new CommandMF("SP", (byte) (MR), 				(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("LF", (byte) (0x00), 			(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("L9", (byte) (MF+MS+MV), 		(byte) (MR),				(byte) (0x00),		(byte) (SP6)));
		cmdTable.add(new CommandMF("S2", (byte) (MF+MS+MV), 		(byte) (MR+MI),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("S5", (byte) (0x00), 			(byte) (MI),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("S8", (byte) (0x00), 			(byte) (MI),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("C1", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("C2", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("C3", (byte) (0x00), 			(byte) (0x00),				(byte) (SP2+SP4),	(byte) (0x00)));
		cmdTable.add(new CommandMF("BT", (byte) (0x00), 			(byte) (0x00),				(byte) (SP4),		(byte) (0x00)));
		cmdTable.add(new CommandMF("C5", (byte) (MR), 				(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("RE", (byte) (0x00), 			(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("U0", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("U1", (byte) (0x00), 			(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("U2", (byte) (MR), 				(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("U3", (byte) (MR), 				(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("X0", (byte) (0x00), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("X1", (byte) (0x00), 			(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("X2", (byte) (MF), 				(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("X3", (byte) (MF), 				(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("NQ", (byte) (MF), 				(byte) (MR),				(byte) (0x00),		(byte) (SP4)));
		cmdTable.add(new CommandMF("PC", (byte) (MF+MR), 			(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("YA", (byte) (MR+MV), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("YB", (byte) (MV+MI), 			(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("YD", (byte) (MV), 				(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("YE", (byte) (MV), 				(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("YF", (byte) (MR+MV+MI), 		(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("YP", (byte) (MV), 				(byte) (0x00),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("YR", (byte) (MV), 				(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("V1", (byte) (MR+MS+MV+MI), 		(byte) (MR+MI),				(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("V2", (byte) (MR+MS+MV+MF+MI),	(byte) (MR+MS+MV+MF+MI),	(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("V3", (byte) (MR+MS+MV+MF+MI),	(byte) (MR+MS+MV+MF+MI),	(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("V4", (byte) (MR+MS+MV+MF+MI),	(byte) (MR+MV+MI),			(byte) (0x00),		(byte) (0x00)));
		cmdTable.add(new CommandMF("MC", (byte) (MR),				(byte) (MR),				(byte) (0x00),		(byte) (0x00)));
	
		if (logger.isTraceEnabled())	logger.trace(method + "< exited");
	}
	
	/**
	 * Get singleton instance of this class.
	 * 
	 * @return	Singleton instance.
	 */
	public static CommandTableMF getInstance() {
		
		if (instance == null) {
			instance = new CommandTableMF();
		}
		
		return instance;
	}

	/**
	 * Inner class describing a single legal command.
	 * 
	 * @author usadva
	 *
	 */
	public class CommandMF {
		
		public String	commandCode;
		
		public byte	buffersIn;
		public byte	buffersOut;
		public byte	specialIn;
		public byte	specialOut;
		
		/**
		 * Constructor.
		 * 
		 * @param commandCode	Two character command code.
		 * @param buffersIn		Buffers sent on command input.
		 * @param buffersOut	Buffers returned on command output.
		 * @param specialIn		Special case buffers sent on command input.
		 * @param specialOut	Special case buffers returned on command output.
		 */
		public CommandMF(String commandCode, byte buffersIn, byte buffersOut, byte specialIn, byte specialOut) {

			this.commandCode	= commandCode;
			this.buffersIn		= buffersIn;
			this.buffersOut		= buffersOut;
			this.specialIn		= specialIn;
			this.specialOut		= specialOut;
		}
	}
	
	/**
	 * Set buffer usage (in/out) flags for an AdabasDirectCallX direct call per the Legal Command Table.
	 * 
	 * @param adcx	Direct call containing buffers and control block.
	 */
	public void setBufferUsageFlags(AdabasDirectCallX adcx) {
		
		String method = "setBufferUsageFlags(): ";
		
		if (logger.isTraceEnabled())	logger.trace(method + "> entered");
		
		String cmdCode	= adcx.getAcbX().getAcbCMD();
		
		for (CommandMF cmd : cmdTable) {
			
			if (cmd.commandCode.compareTo(cmdCode) == 0) {
				
				if (logger.isDebugEnabled())	
					logger.debug(method + String.format("command found: code = %s; in = 0x%02X out = 0x%02X; spin = 0x%02X; spout = 0x%02X", 
							     cmd.commandCode, cmd.buffersIn, cmd.buffersOut, cmd.specialIn, cmd.specialOut));
				
				byte	bufInOut = 0x00;
				
				if (adcx.getFB() != null) {
					if ((cmd.buffersIn  & MF) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.IN);
					if ((cmd.buffersOut & MF) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.OUT);
					adcx.getFB().setAbdBUF(bufInOut);
					if (logger.isTraceEnabled())	logger.trace(AdabasTrace.dumpBuffer("format buffer header =", adcx.getFB().getABDBytes()));
				}
				bufInOut = 0x00;
				if (adcx.getRB() != null) {
					if ((cmd.buffersIn   & MR) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.IN); 
					if ((cmd.buffersOut  & MR) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.OUT);
					if (((cmd.specialIn  & SP2) != 0) && (adcx.getAcbX().getAcbCOP2() == 'E')) bufInOut = (byte) (bufInOut | AdabasBufferX.IN); 
					if (((cmd.specialOut & SP3) != 0) && (adcx.getAcbX().getAcbCOP2() == 'E')) bufInOut = (byte) (bufInOut | AdabasBufferX.OUT); 
					adcx.getRB().setAbdBUF(bufInOut);
					if (logger.isTraceEnabled())	logger.trace(AdabasTrace.dumpBuffer("record buffer header =", adcx.getRB().getABDBytes()));
				}
				bufInOut = 0x00;
				if (adcx.getSB() != null) {
					if ((cmd.buffersIn  & MS) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.IN); 
					if ((cmd.buffersOut & MS) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.OUT);
					if (((cmd.specialIn & SP1) != 0) && (adcx.getAcbX().getAcbCOP2() == 'V')) bufInOut = (byte) (bufInOut | AdabasBufferX.IN); 
					adcx.getSB().setAbdBUF(bufInOut);
					if (logger.isTraceEnabled())	logger.trace(AdabasTrace.dumpBuffer("search buffer header =", adcx.getSB().getABDBytes()));
				}
				bufInOut = 0x00;
				if (adcx.getVB() != null) {
					if ((cmd.buffersIn  & MV) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.IN); 
					if ((cmd.buffersOut & MV) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.OUT);
					if (((cmd.specialIn & SP1) != 0) && (adcx.getAcbX().getAcbCOP2() == 'V')) bufInOut = (byte) (bufInOut | AdabasBufferX.IN); 
					adcx.getVB().setAbdBUF(bufInOut);
					if (logger.isTraceEnabled())	logger.trace(AdabasTrace.dumpBuffer("value buffer header =", adcx.getVB().getABDBytes()));
				}
				bufInOut = 0x00;
				if (adcx.getIB() != null) {
					if ((cmd.buffersIn  & MI) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.IN); 
					if ((cmd.buffersOut & MI) != 0) bufInOut = (byte) (bufInOut | AdabasBufferX.OUT);
					if ((cmd.specialIn  & SP3) != 0) {
						String sADD4 = new String(adcx.getAcbX().getAcbADD4());
						if (sADD4.compareTo("        ") == 0) bufInOut = (byte) (bufInOut | AdabasBufferX.IN); 
					}
					if ((cmd.specialIn  & SP4) != 0) {
						byte cop1 = adcx.getAcbX().getAcbCOP1();
						if (cop1 == 'P' || cop1 == 'M' || cop1 == 'O') bufInOut = (byte) (bufInOut | AdabasBufferX.IN);
					}
					if ((cmd.specialOut & SP4) != 0) {
						byte cop1 = adcx.getAcbX().getAcbCOP1();
						if (cop1 == 'P' || cop1 == 'M' || cop1 == 'O') bufInOut = (byte) (bufInOut | AdabasBufferX.OUT);
					}
					if ((cmd.specialIn  & SP6) != 0) {
						byte cop1 = adcx.getAcbX().getAcbCOP1();
						if (cop1 == 'P' || cop1 == 'M' || cop1 == 'O') bufInOut = (byte) (bufInOut | AdabasBufferX.IN);
					}
					if ((cmd.specialOut & SP6) != 0) {
						byte cop1 = adcx.getAcbX().getAcbCOP1();
						if (cop1 == 'P' || cop1 == 'M' || cop1 == 'O') bufInOut = (byte) (bufInOut | AdabasBufferX.OUT);
					}
					adcx.getIB().setAbdBUF(bufInOut);
					if (logger.isTraceEnabled())	logger.trace(AdabasTrace.dumpBuffer("ISN buffer header =", adcx.getIB().getABDBytes()));
				}
				
				break;
			}
		}

		if (logger.isTraceEnabled())	logger.trace(method + "< exited");
	}
	
}

