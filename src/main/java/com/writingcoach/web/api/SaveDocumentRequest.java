package com.writingcoach.web.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 保存文档时提交的数据。
 *
 * <p>{@code baseRevision} 是客户端读取到的版本号，用于避免静默覆盖。</p>
 */
public record SaveDocumentRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题不能超过 200 个字符")
        String title,

        @NotNull(message = "正文不能为空")
        @Size(max = 200_000, message = "正文不能超过 200000 个字符")
        String content,

        @Min(value = 0, message = "版本号无效")
        long baseRevision
) {
}
