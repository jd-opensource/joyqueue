package io.chubao.joyqueue.monitor;

import java.io.Serializable;

/**
 * RestResponse
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public class RestResponse<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    public static <T> RestResponse<T> build(int code, T data) {
        return build(code, null, data);
    }

    public static <T> RestResponse<T> build(int code, String message) {
        return build(code, message, null);
    }

    public static <T> RestResponse<T> build(int code, String message, T data) {
        return new RestResponse(code, message, data);
    }

    public static <T> RestResponse<T> build(RestResponseCode code, T data) {
        return build(code.getCode(), code.getMessage(), data);
    }

    public static <T> RestResponse<T> build(RestResponseCode code, String message) {
        return build(code, message, null);
    }

    public static <T> RestResponse<T> build(RestResponseCode code, String message, T data) {
        return build(code.getCode(), message, data);
    }

    public static <T> RestResponse<T> success() {
        return success(null);
    }

    public static <T> RestResponse<T> success(T data) {
        return build(RestResponseCode.SUCCESS, RestResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> RestResponse<T> paramError(String message) {
        return build(RestResponseCode.PARAM_ERROR, message, null);
    }

    public static <T> RestResponse<T> notFound(String message) {
        return build(RestResponseCode.NOT_FOUND, message, null);
    }

    public static <T> RestResponse<T> serverError(String message) {
        return build(RestResponseCode.SERVER_ERROR, message, null);
    }

    public RestResponse() {

    }

    public RestResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}