package com.writingcoach.service;

/**
 * 请求的历史版本不存在，或不属于指定文档。
 */
public class VersionNotFoundException extends RuntimeException {

    public VersionNotFoundException(long documentId, long versionId) {
        super("版本不存在：" + versionId + "（文档 " + documentId + "）");
    }
}
