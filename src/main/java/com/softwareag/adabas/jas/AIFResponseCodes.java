package com.softwareag.adabas.jas;

/**
 * Class for Adabas Interface (AIF) LUW response code definitions. 
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
 * This class exists solely to create meaningful error texts for Java applications
 * using AIF through Java Adabas Services (JAS) and Adabas Administration Service (AAS).
 * 
 * AIF response code definitions are derived from Adabas LUW source module:
 * 
 * SRV/src/export/inc/adaaif.h
 */
public class AIFResponseCodes {
	
	/**
	 * Response Code Enumeration.
	 * 
	 * Contains the actual definitions for each AIF response code in a
	 * Java enumeration. It is here that response codes are associated with
	 * error texts.
	 * 
	 * The response code constructor is of the form:
	 * 
	 * Name (ResponseCode,	ResponseText)
	 */
	protected enum RC {
		
		SUCCESS							((short) 0, 	"Success."),
		ERROR							((short) 1, 	"Unspecified error."),
		NO_SPACE						((short) 2, 	"No space left."),
		NOT_FOUND						((short) 3, 	"Not found."),
		OPEN_FAILED						((short) 4, 	"Open failed."),
		EOF								((short) 5, 	"Currently not used."),
		NO_DATA							((short) 6, 	"Currently not used."),
		SYNTAX							((short) 7, 	"Syntax error."),
		RESET							((short) 8, 	"Indicates that a reset operation has been performed."),
		ENVERROR						((short) 9, 	"Environment error."),
		STRLMM							((short) 10,	"Structure level mismatch."),
		AUTORESTART						((short) 11, 	"Pending Autorestart."),
		NO_LOBFILE						((short) 12, 	"No LOB file specified."),
		LOBERROR						((short) 13, 	"No file specified."),
		FDT_TOO_BIG						((short) 14, 	"FDT is too big."),
		ISN_REUSE						((short) 15, 	"ISN reusage not permitted."),
		CYPHER_ERROR					((short) 16, 	"Cyphering not allowed."),
		ADAM_ERROR						((short) 17, 	"ADAM error, for example files with ADAM feature can not be refreshed."),
		VAL_EXCEEDED					((short) 18, 	"A value to be returned exceeded the max possible value."),
		UTDM_INIT						((short) 34, 	"Internal initialization failed."),
		GET_FCB							((short) 35,	"Internal error when trying to retrieve the FCB."),
		CONT_EXISTS						((short) 36, 	"Container already exists."),
		DBEXISTS						((short) 37,	"Database already exists."),
		GET_GCB							((short) 38,	"An internal error occurred when trying to retrieve the GCB."),
		GET_FST							((short) 39,	"An internal error occurred when trying to retrieve the FST."),
		GET_FDT							((short) 40,	"An internal error occured when trying to retrieve the FDT."),
		MAP_CSA_FAILED					((short) 41,	"An internal error occurred when mapping to CSA."),
		ADA_SEC							((short) 42,	"Internal error when trying to set security mode."),
		DBNOACT							((short) 148, 	"Database not active."),
		ADAACT							((short) 149,	"Database is active."),
		PLOG_DISABLED					((short) 150, 	"PLOG is disabled."),
		LOCKED							((short) 151, 	"File is locked."),
		INVALID_PARAM					((short) 152, 	"Invalid parameters."),
		INVALID_DBID					((short) 153, 	"Invalid Database ID."),
		INVALID_FNR						((short) 154, 	"Invalid File Number."),
		NO_BASEFILE						((short) 155,	"Base file is missing."),
		NO_FILE							((short) 156, 	"No file specified."),
		LOBFL_EQ_FILE					((short) 157,	"Same file number for file and lobfile not allowed."),
		RO_DB							((short) 158, 	"Readonly database."),
		OPEN_ASSO						((short) 159,	"Open Asso."),
		INSUFF_MEM						((short) 160, 	"Insufficent memory."),
		NO_PERMISSION					((short) 161, 	"No permission."),
		FUNAV							((short) 162, 	"File(s) / Userid  not available at Open."),
		LOBFUNAV						((short) 163, 	"LOB File(s) not available at Open."),
		OVUCB							((short) 164, 	"Utility Communicaton Block (UCB) overflow."),
		FLOADED							((short) 165,	"File is already loaded."),
		FNLOADED						((short) 166, 	"File is not already loaded."),
		FFCB_READ						((short) 167,	"Failed to read FCB."),
		NO_SPACE_AC						((short) 168, 	"No space left for AC."),
		NO_SPACE_DS						((short) 169,	"No space left for DS."),
		OVDS							((short) 170, 	"DS Too many overflow blocks."),
		NO_SPACE_NI						((short) 171, 	"No space left for NI."),
		NO_SPACE_UI						((short) 172, 	"No space left for UI."),
		FUCB_READ						((short) 173, 	"Failed to read UCB."),
		READ_ASSO						((short) 174, 	"Failed to read ASSO block."),
		FMT_DSST						((short) 175,	"Failed to format DSST blocks."),
		DSEXT							((short) 176, 	"Failed to extend FCB extent."),
		FMT_BLK							((short) 177,	"Failed to format range of blocks."),
		FFCB_WRITE						((short) 178, 	"Failed to write FCB."),
		WRITE_ASSO						((short) 179,	"Failed to write ASSO block."),
		OPEN_DATA						((short) 180, 	"Failed to open DATA file."),
		LOCK_SEM						((short) 181, 	"Failed to lock database semaphore."),
		FFST_READ						((short) 182, 	"Failed to read FST."),
		SHUTDOWN						((short) 183, 	"Shutdown or cancel in progress."),
		ACESS_DENIED					((short) 184, 	"Access denied."),
		NOTACT							((short) 185,	"The specified database is not active."),
		HYXNA							((short) 186, 	"Hyper descriptor not available."),
		ASSO77							((short) 187,	"ASSO storage exhausted."),
		ASSO77_NOBLKLT					((short) 188, 	"ASSO storage exhausted, no small blocks available."),
		ASSO77_NOBLKGE					((short) 189,	"ASSO storage exhausted, no large blocks available."),
		DATA77							((short) 190, 	"DATA storage exhausted."),
		NOFIELD							((short) 191, 	"Field not found."),
		FDESC							((short) 192, 	"Field has already descriptor status."),
		NOFSUP							((short) 193, 	"No fields supplied."),
		INVCALL							((short) 194, 	"Invalid function call - coding error."),
		ADAM_OVFL						((short) 195,	"Too many ADAM overflow blocks."),
		INVALID_RABN					((short) 196, 	"Invalid RABN specified."),
		TERMINATE						((short) 197, 	"STOP/TERMINATE detected."),
		INVALID_PATH					((short) 200, 	"Invalid path defined."),
		CREATE_ERROR					((short) 201, 	"Create database/container failed."),
		INVALID_BLOCKSIZE				((short) 202,	"Invalid block size specified / block size out of range."),
		DELETE_ERROR					((short) 203,	"Delete database/container failed."),
		OPEN_WORK						((short) 204,	"Failed to open the WORK file."),
		WRITECHK_ERROR					((short) 205,	"Failed to write entry in checkpoint file."),
		CONT_IN_USE						((short) 206,	"Container cannot be removed because it is in use."),
		NO_SPACE_DEV					((short) 207,   "No space left on device."),
		NO_DEVICE						((short) 208,	"No such file or directory."),
		PATH_NOT_FULL_QUALIFIED			((short) 209,	"The given path for a container is not full qualified."),
		ADADIR_NOT_FOUND				((short) 300, 	"ADADIR environment variable not found."),
		ERROR_HDL_NOT_SET				((short) 301, 	"No error handler was defined."),
		OPEN_INI_FILE_ERROR				((short) 302, 	"Opening the INI file failed."),
		ADADATADIR_NOT_FOUND			((short) 303, 	"The ADADATADIR environment variable could not be found."),
		ADAVERS_NOT_FOUND				((short) 304, 	"The ADAVERS environment variable could not be found."),
		READ_INI_FILE_ERROR				((short) 305, 	"Reading the INI files failed."),
		INI_FILE_ENTRY_ERROR			((short) 306, 	"Missing or inconsistent entries in the INI filed detected."),
		SET_INI_VALUE_ERROR				((short) 307, 	"Failed to set value in INI file."),
		WRITE_INI						((short) 308, 	"Failed to write INI file."),
		ADABAS_INI_DBID_NOT_FOUND		((short) 309, 	"Database not found in ADABAS.INI."),
		ADABAS_INI_STRLVL_NOT_FOUND		((short) 310, 	"No structure level found for the given DBID in ADABAS.INI."),
		IFCTE							((short) 400, 	"Incompatible format conversion or truncation error."),
		FIELD_NOT_FOUND					((short) 500, 	"The given field name is not found in the FDT."),
		FIELD_MODIFICATION_NOT_ALLOWED	((short) 501, 	"The given field is a group or a fixed length field and must not be changed."),
		FILE_NOT_FOUND					((short) 502, 	"The given file is not found."),
		FILE_IS_SYSTEM_FILE				((short) 503, 	"The given file is a system file."),
		FILE_IS_LOB_FILE				((short) 504, 	"The given file is a LOB file."),
		FDT_FIELD_LENGTH_OVERFLOW		((short) 505, 	"Field length is greater than the possible field length."),
		NOT_ALLOWED_OFFLINE				((short) 506, 	"The operation is not allowed offline."),
		NOT_ALLOWED_ONLINE				((short) 507, 	"The operation is not allowed online."),
		DB_READONLY						((short) 508, 	"The database is read-only."),
		FILE_REFERENCED					((short) 509, 	"File is referenced."),
		FILE_NOT_UNLOCKED				((short) 510, 	"Not all files have been unlocked."),
		STRING_LENGTH_OVERFLOW			((short) 600, 	"The string length is greater than the allocated memory."),
		LAUNCH_ADASTART					((short) 651, 	"Failed to launch ADASTART."),
		INVVRS							((short) 700, 	"Invalid version given."),
		NOTIMPL							((short) 701, 	"Function not implemented for the given version."),
		VRS_TOO_HIGH					((short) 702, 	"ADABAS version of database higher than AIF version."),
		AIF_R_VRS_TOO_LOW				((short) 703, 	"ADABAS version of database did not yet include AIF."),
		MEM_ALLOC						((short) 800, 	"Failed to allocate memory."),
		MEM_REALLOC						((short) 801, 	"Failed to reallocate memory."),
		LIC_NOTFOUND					((short) 850, 	"License file not found."),
		INV_FILE						((short) 851, 	"Invalid File."),
		INV_OS							((short) 852, 	"Invalid Operating System."),
		INV_PLATFORM					((short) 853, 	"Invalid Platform."),
		INV_PRODVERSION					((short) 854, 	"Invalid Product Version."),
		INV_PRODUCT						((short) 855, 	"Invalid Product."),
		INV_CPUCNT						((short) 856, 	"Invalid CPU count."),
		INV_VMWARE						((short) 857, 	"Invalid virtual machine."),
		INV_BUCKET						((short) 858, 	"Invalid performance class of the machine."),
		DIFF_USAGE						((short) 859, 	"Invalid usage."),
		DIFF_VERSION					((short) 860, 	"Invalid version."),
		PATH_NOTFOUND					((short) 861, 	"Path not found."),
		COPY_FAILED						((short) 862, 	"Copy failed."),
		WRITE_ERROR						((short) 863, 	"Write error."),
		UNLINK_FAILED					((short) 864, 	"Unlink failed."),
		NO_OVERWRITE					((short) 865, 	"No Overwrite specified, but license fail already present."),
		LICENSE_INVALID					((short) 866, 	"License invalid."),
		BUFFER_TOO_SMALL				((short) 867, 	"Buffer too small."),
		DEMO_NOT_FOUND					((short) 900, 	"Export copy for demo files not found."),
		LOAD_DEMO_ERROR					((short) 901, 	"Loading demo files failed."),
		RI_ERROR						((short) 1000, 	"Referential integrity error."),
		RI_FNLD							((short) 1001, 	"Primary/foreign file not loaded."),
		RI_SYSNPER						((short) 1002, 	"RI not with system file."),
		RI_FNOT							((short) 1003, 	"Primary/foreign key not found."),
		RI_PFNUQ						((short) 1004, 	"Primary not unique."),
		RI_FMTMIS						((short) 1005, 	"Format/length mismatch."),
		RI_CONTNAME						((short) 1006, 	"Error in constraint name or not unique."),
		RI_INVACT						((short) 1007, 	"Invalid/not permitted constr. action."),
		RI_OPTMIS						((short) 1008, 	"SET_NULL and NC/NN Options mismatch."),
		RI_OPTNPER						((short) 1009, 	"Primary/foreign field name is not a descriptor or the descriptor options are not allowed."),
		RI_SELFREF						((short) 1010, 	"Error in self-referencing spec."),
		RI_PFALRR						((short) 1011, 	"Primary/foreign field is already referenced."),
		RI_DINTVIO						((short) 1012, 	"Data integrity violation."),
		RI_SUPMISM						((short) 1013, 	"Invalid superdescriptor reference."),
		RI_NOBTF						((short) 1014, 	"RI not with NOBT files."),
		NOPGMRF							((short) 1015, 	"No primary keys in files with pgm_refresh."),
		RI_REFERENCE					((short) 1016, 	"Reference to missing file detected - Remove the referential integrity constraint first, or specify the referenced file as well."),
		RI_CONTEXIST					((short) 1017, 	"RI constraint already exists."),
		RI_PRIKEY						((short) 1020, 	"Primary key, don't modify."),
		RI_FORKEY						((short) 1021, 	"Foreign key, don't modify."),
		RI_PAFLD						((short) 1022, 	"Parent field, don't modify."),
		RI_RIFND						((short) 1023, 	"Referenced/referencing table."),
		IO_TIMING_NOT_ENABLED			((short) 1050, 	"IO timing is not enabled. See ADAOPR control parameter 'IO_TIME' in the ADABAS documentation."),
		USEREXIT_1_11_MUTUALLY_EXCLUSIV	((short) 1100, 	"User exits 1 and 11 are mutually exclusive."),
		ASSIGN_FILE_CREATE				((short) 2000, 	"Could not create assign file."),
		ASSIGN_FILE_DELETE				((short) 2001, 	"Could not delete assign file."),
		ASSIGN_FILE_CLOSE				((short) 2002, 	"Could not close assign file."),
		ASSIGN_FILE_NOT_OPEN			((short) 2003, 	"Assign file is not open for access."),
		ASSIGN_FILE_NOT_EXISTS			((short) 2004, 	"Assign file does not exist."),
		INTERNAL_ERROR					((short) 9999, 	"This return code indicates an internal error.");
				
		protected short		rc;										// decimal response code
		protected short		sc;										// subcode
		protected String	rcText;									// response code error text
		protected String	scText;									// subcode 		 error text
		protected String	fullText;								// combined		 error text
		
		/**
		 * Constructor taking response code and meaning.
		 * Subcode is assumed to be zero.
		 * 
		 * @param rc	Response code.
		 * @param text	Meaning.
		 */
		private RC(short rc, String text) {
			
			this.rc 	= rc;
			this.rcText	= text;
			this.fullText	= String.format("JASAIF%04d%04d : rc = %04d : sc = %04d : %s", 
											 this.rc, this.sc, this.rc, this.sc, this.rcText);
		}
	}

	/**
	 * Get response code text from enumeration using only rc.
	 * Returns 1st match on rc only.
	 * Returns fully formatted response code text with identifier, rc, and sc. Example:
	 * 
	 * JASAIF00020000 : rc = 0002 : sc = 0000 : No space left.
	 *  
	 * @param rc Response code.
	 * @return   Error text.
	 */
	public static String getRCText(int rc) {
		
		for (RC responseCode:RC.values()) {							// loop thru enumeration values
			if (responseCode.rc == rc) {							// rc value match?
				return responseCode.fullText;						// return associated text
			}
		}
		
		return "Unknown";											// not found
	}
	
	/**
	 * Get response code short text from enumeration using only rc.
	 * Returns 1st match on rc only.
	 * Returns only response code short text. Example:
	 * 
	 * No space left.
	 *  
	 * @param rc Response code.
	 * @return   Error text.
	 */
	public static String getRCShortText(int rc) {
		
		for (RC responseCode:RC.values()) {							// loop thru enumeration values
			if (responseCode.rc == rc) {							// rc value match?
				return responseCode.rcText;							// return associated short text
			}
		}
		
		return "Unknown";											// not found
	}
	
	/**
	 * Print all response code texts.
	 * 
	 * Used for documentation feeds.
	 */
	
	public static void printAllRCTexts() {

		for (RC responseCode : RC.values()) {						// loop thru enum values
			System.out.println(responseCode.fullText);				// print full response & subcode text
		}
		System.out.println(String.format("Total number of ResponseCodes = %d", RC.values().length));
	}
	
}
