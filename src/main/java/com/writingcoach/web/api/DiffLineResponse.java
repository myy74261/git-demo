package com.writingcoach.web.api;

import com.writingcoach.service.DocumentDiffService;

/**
 * 版本对比中的一行。
 *
 * @param type EQUAL、ADD、REMOVE 或 HUNK
 * @param text 行内容
 */
public record DiffLineResponse(String type, String text) {

    public static DiffLineResponse from(DocumentDiffService.DiffLine line) {
        return new DiffLineResponse(line.type(), line.text());
    }
}
