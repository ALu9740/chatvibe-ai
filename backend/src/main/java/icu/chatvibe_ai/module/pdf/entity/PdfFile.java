package icu.chatvibe_ai.module.pdf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PDF 文件实体。
 *
 * @author Alu
 * @date 2026-07-07
 * @description 对应 pdf_file 表，一个 pdf 会话对应一个文件
 */
@Data
@TableName("pdf_file")
public class PdfFile {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属会话 */
    @TableField("session_id")
    private Long sessionId;

    /** 存储文件名 */
    @TableField("stored_name")
    private String storedName;

    /** 原文件名 */
    @TableField("original_name")
    private String originalName;

    /** 字节数 */
    private Long size;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
