package com.jd.journalq.model.exception;

/**
 * @author tianya
 *
 */
public class AssociateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4062983781140204113L;

	/**
	 * 
	 */
	public AssociateException() {
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AssociateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public AssociateException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AssociateException(Throwable cause) {
		super(cause);
	}
}
