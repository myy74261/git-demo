package com.writingcoach.service;

/**
 * 请求的文档不存在。
 */
public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(long documentId) {
        super("文档不存在：" + documentId);
    }
}
