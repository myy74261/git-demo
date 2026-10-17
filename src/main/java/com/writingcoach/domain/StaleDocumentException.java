package com.writingcoach.domain;

/**
 * 客户端保存时使用的版本号已经落后于服务端。
 */
public class StaleDocumentException extends RuntimeException {

    private final long currentRevision;

    public StaleDocumentException(long currentRevision) {
        super("文档已在其他位置更新，当前版本号为 " + currentRevision);
        this.currentRevision = currentRevision;
    }

    public long getCurrentRevision() {
        return currentRevision;
    }
}
