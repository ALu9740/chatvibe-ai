package icu.chatvibe_ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * SSE 流式助手。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 订阅 Spring AI Flux，将分块通过 SseEmitter 推送；完成时回调累积内容
 */
@Slf4j
@Component
public class SseStreamingHelper {

    /**
     * 流式推送（普通模式）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 完成后发 [DONE]，再回调 onComplete
     * @param flux Spring AI 文本分块流
     * @param sanitizer 输出过滤函数（可为 null）
     * @param onComplete 完成回调，参数为累积全文
     * @return SseEmitter
     */
    public SseEmitter stream(Flux<String> flux,
                              UnaryOperator<String> sanitizer,
                              Consumer<String> onComplete) {
        return streamWithMeta(flux, sanitizer, (content, emitter) -> {
            if (onComplete != null) {
                onComplete.accept(content);
            }
        });
    }

    /**
     * 流式推送（带元信息）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 完成后先回调 beforeDone（可向 emitter 发送 meta 事件），再发 [DONE]
     * @param flux Spring AI 文本分块流
     * @param sanitizer 输出过滤函数（可为 null）
     * @param beforeDone 完成前回调，参数为 (累积全文, emitter)
     * @return SseEmitter
     */
    public SseEmitter streamWithMeta(Flux<String> flux,
                                      UnaryOperator<String> sanitizer,
                                      BiConsumer<String, SseEmitter> beforeDone) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 分钟超时
        StringBuilder accumulated = new StringBuilder();
        AtomicReference<Disposable> ref = new AtomicReference<>();

        Disposable subscription = flux.subscribe(
                chunk -> {
                    String safe = sanitizer == null ? chunk : sanitizer.apply(chunk);
                    if (safe != null && !safe.isEmpty()) {
                        accumulated.append(safe);
                        try {
                            emitter.send(SseEmitter.event().data(safe));
                        } catch (IOException e) {
                            log.debug("SSE 发送失败: {}", e.getMessage());
                            emitter.completeWithError(e);
                        }
                    }
                },
                err -> {
                    log.error("AI 流式错误", err);
                    try {
                        emitter.send(SseEmitter.event().data("[ERROR] " + err.getMessage()));
                    } catch (IOException ignored) {
                    }
                    emitter.completeWithError(err);
                },
                () -> {
                    try {
                        if (beforeDone != null) {
                            beforeDone.accept(accumulated.toString(), emitter);
                        }
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }
        );
        ref.set(subscription);
        emitter.onTimeout(() -> {
            subscription.dispose();
            emitter.complete();
        });
        emitter.onError(t -> {
            subscription.dispose();
            log.debug("SSE 客户端断开: {}", t.getMessage());
        });
        return emitter;
    }
}
