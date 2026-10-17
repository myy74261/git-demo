package com.writingcoach.web.api;

import com.writingcoach.domain.Document;
import com.writingcoach.service.DocumentService;
import java.time.Instant;

/**
 * 文档的接口返回结构。
 */
public record DocumentResponse(
        long id,
        String title,
        String content,
        long revision,
        Instant updatedAt,
        int wordCount,
        long versionCount
) {

    public static DocumentResponse from(Document document, long versionCount) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getRevision(),
                document.getUpdatedAt(),
                DocumentService.countWords(document.getContent()),
                versionCount);
    }
}
