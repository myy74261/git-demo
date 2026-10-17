package com.writingcoach.web.api;

import com.writingcoach.domain.DocumentVersion;
import java.time.Instant;

/**
 * 版本列表中的一项，不包含正文以减小响应体积。
 */
public record VersionSummaryResponse(
        long id,
        long revisionNumber,
        String title,
        int wordCount,
        String triggerType,
        Instant createdAt
) {

    public static VersionSummaryResponse from(DocumentVersion version) {
        return new VersionSummaryResponse(
                version.getId(),
                version.getRevisionNumber(),
                version.getTitle(),
                version.getWordCount(),
                version.getTriggerType(),
                version.getCreatedAt());
    }
}
