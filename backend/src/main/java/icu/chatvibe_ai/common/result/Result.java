package icu.chatvibe_ai.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应包装类。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 统一返回结构 { code, message, data }
 * @param <T> 数据载荷类型
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 构造成功 Result
     * @param data 载荷
     * @return Result.ok(data)
     * @param <T> 载荷类型
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "ok", data);
    }

    /**
     * 成功响应（无载荷）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 构造空成功 Result
     * @return Result.ok(null)
     */
    public static <T> Result<T> ok() {
        return new Result<>(0, "ok", null);
    }

    /**
     * 失败响应。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 构造失败 Result
     * @param code 错误码
     * @param message 错误消息
     * @return Result.fail(code, message)
     * @param <T> 载荷类型
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
