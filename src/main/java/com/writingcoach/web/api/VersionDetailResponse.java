package com.writingcoach.web.api;

import com.writingcoach.domain.DocumentVersion;
import java.time.Instant;

/**
 * 单个版本详情，包含完整正文。
 */
public record VersionDetailResponse(
        long id,
        long revisionNumber,
        String title,
        String content,
        int wordCount,
        String triggerType,
        Instant createdAt
) {

    public static VersionDetailResponse from(DocumentVersion version) {
        return new VersionDetailResponse(
                version.getId(),
                version.getRevisionNumber(),
                version.getTitle(),
                version.getContent(),
                version.getWordCount(),
                version.getTriggerType(),
                version.getCreatedAt());
    }
}
