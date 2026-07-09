package icu.chatvibe_ai.common;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author Alu
 * @date 2026-07-07
 * @description 携带 HTTP 状态码的业务异常，由 GlobalExceptionHandler 统一处理
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int status;

    /**
     * 构造业务异常。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 指定 HTTP 状态码与消息
     * @param status HTTP 状态码
     * @param message 异常消息
     */
    public BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * 构造 400 业务异常。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 默认 400 Bad Request
     * @param message 异常消息
     * @return BusinessException
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 构造 401 未认证异常。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 401 Unauthorized
     * @param message 异常消息
     * @return BusinessException
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    /**
     * 构造 403 禁止访问异常。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 403 Forbidden
     * @param message 异常消息
     * @return BusinessException
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 构造 429 限流异常。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 429 Too Many Requests
     * @param message 异常消息
     * @return BusinessException
     */
    public static BusinessException tooManyRequests(String message) {
        return new BusinessException(429, message);
    }
}
