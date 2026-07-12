package icu.chatvibe_ai.module.pdf.controller;

import icu.chatvibe_ai.common.result.Result;
import icu.chatvibe_ai.module.pdf.dto.PdfDtos;
import icu.chatvibe_ai.module.pdf.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ChatPDF Controller。
 *
 * @author Alu
 * @date 2026-07-07
 * @description PDF 上传 / RAG 问答 / 文件下载 / 会话列表
 */
@Tag(name = "ChatPDF")
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    /**
     * 上传 PDF。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 保存文件 + 解析 + 向量化
     * @param username 用户名
     * @param chatId 会话 ID
     * @param file PDF 文件
     * @return 上传响应
     */
    @Operation(summary = "上传 PDF")
    @PostMapping("/upload/{chatId}")
    public Result<PdfDtos.PdfUploadResponse> upload(@AuthenticationPrincipal String username,
                                                    @PathVariable Long chatId,
                                                    @RequestParam("file") MultipartFile file) {
        return Result.ok(pdfService.upload(username, chatId, file));
    }

    /**
     * RAG 问答（SSE）。
     *
     * @author Alu
     * @date 2026-07-07
     * @description GET 方便 SSE，参数走 query
     * @param username 用户名
     * @param prompt 用户问题
     * @param chatId 会话 ID
     * @return SseEmitter
     */
    @Operation(summary = "PDF 问答（SSE 流）")
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@AuthenticationPrincipal String username,
                           @RequestParam("prompt") String prompt,
                           @RequestParam("chatId") Long chatId) {
        return pdfService.chat(username, prompt, chatId);
    }

    /**
     * 下载原 PDF。
     *
     * @author Alu
     * @date 2026-07-07
     * @description 返回 application/pdf
     * @param username 用户名
     * @param chatId 会话 ID
     * @return 文件流
     */
    @Operation(summary = "下载 PDF")
    @GetMapping("/file/{chatId}")
    public ResponseEntity<FileSystemResource> file(@AuthenticationPrincipal String username,
                                                   @PathVariable Long chatId) {
        File f = pdfService.downloadFile(username, chatId);
        String encoded = URLEncoder.encode(f.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(f));
    }

    /**
     * PDF 会话列表。
     *
     * @author Alu
     * @date 2026-07-07
     * @description type=pdf 的会话
     * @param username 用户名
     * @return 列表
     */
    @Operation(summary = "PDF 会话列表")
    @GetMapping("/sessions")
    public Result<List<PdfDtos.PdfSessionVo>> sessions(@AuthenticationPrincipal String username) {
        return Result.ok(pdfService.sessions(username));
    }
}
