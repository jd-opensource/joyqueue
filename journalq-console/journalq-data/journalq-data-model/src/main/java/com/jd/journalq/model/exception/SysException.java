/**
 * 
 */
package com.jd.journalq.model.exception;

/**
 * 系统异常基础类，记录日至，提示用户
 * 
 * @author 何小锋
 * 
 */
public class SysException extends RuntimeException {

	private static final long serialVersionUID = -7934290491194646404L;

	public SysException() {
		super();
	}

	public SysException(String message, Throwable cause) {
		super(message, cause);
	}

	public SysException(String message) {
		super(message);
	}

	public SysException(Throwable cause) {
		super(cause);
	}

}
