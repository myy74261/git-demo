package com.writingcoach.web.api;

import com.writingcoach.domain.DocumentVersion;
import com.writingcoach.service.DocumentDiffService;
import java.util.List;

/**
 * 历史版本与当前稿的对比结果。
 */
public record DiffResponse(
        long versionId,
        String versionLabel,
        List<DiffLineResponse> lines,
        int added,
        int removed
) {

    public static DiffResponse from(DocumentVersion version,
                                    DocumentDiffService.DiffResult result) {
        return new DiffResponse(
                version.getId(),
                "版本 " + version.getRevisionNumber(),
                result.lines().stream().map(DiffLineResponse::from).toList(),
                result.added(),
                result.removed());
    }
}
