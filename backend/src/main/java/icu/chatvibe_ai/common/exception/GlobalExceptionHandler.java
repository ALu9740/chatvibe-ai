package icu.chatvibe_ai.common.exception;

import icu.chatvibe_ai.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author Alu
 * @date 2026-07-07
 * @description 拦截 Controller 抛出的异常，统一转为 Result 响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 转为对应 HTTP 状态码
     * @param ex 业务异常
     * @return ResponseEntity 包装的 Result
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex) {
        log.warn("业务异常: status={}, msg={}", ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(Result.fail(ex.getStatus(), ex.getMessage()));
    }

    /**
     * 处理认证异常。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 转为 401
     * @param ex 认证异常
     * @return ResponseEntity 包装的 Result
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(401).body(Result.fail(401, "认证失败：" + ex.getMessage()));
    }

    /**
     * 处理参数校验异常。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 转为 400
     * @param ex 校验异常
     * @return ResponseEntity 包装的 Result
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("参数校验失败");
        return ResponseEntity.status(400).body(Result.fail(400, msg));
    }

    /**
     * 兜底异常处理。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 未知异常转为 500，日志记录堆栈
     * @param ex 异常
     * @return ResponseEntity 包装的 Result
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Result<Void>> handleAny(Throwable ex) {
        log.error("未捕获异常", ex);
        return ResponseEntity.status(500).body(Result.fail(500, "服务器内部错误"));
    }
}
