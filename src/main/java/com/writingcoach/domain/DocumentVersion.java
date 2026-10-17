package com.writingcoach.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * 文档在某一时刻的完整快照，用于回看写作过程和恢复内容。
 */
@Entity
@Table(name = "document_versions", indexes = {
        @Index(name = "idx_document_versions_document",
                columnList = "document_id, revision_number")
})
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private long revisionNumber;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int wordCount;

    @Column(nullable = false, length = 20)
    private String triggerType;

    @Column(nullable = false)
    private Instant createdAt;

    protected DocumentVersion() {
        // JPA 需要的无参构造方法
    }

    public DocumentVersion(Long documentId, long revisionNumber, String title, String content,
                           String triggerType, int wordCount) {
        this.documentId = documentId;
        this.revisionNumber = revisionNumber;
        this.title = title;
        this.content = content;
        this.triggerType = triggerType;
        this.wordCount = wordCount;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public long getRevisionNumber() {
        return revisionNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getWordCount() {
        return wordCount;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
