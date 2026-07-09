package icu.chatvibe_ai.dto;

/**
 * ChatPDF 相关 DTO。
 *
 * @author Alu
 * @date 2026-07-07
 * @description PDF 会话列表 / 上传响应
 */
public final class PdfDtos {

    /** PDF 会话列表项 */
    public record PdfSessionVo(Long id, String fileName) {}

    /** 上传响应 */
    public record PdfUploadResponse(boolean ok, String fileName, Long sessionId) {}

    private PdfDtos() {}
}
