package com.writingcoach.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * 学生正在写作的文档。
 *
 * <p>{@code revision} 在每次保存时递增，用来发现多个页面同时编辑造成的覆盖。</p>
 */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private long revision;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Document() {
        // JPA 需要的无参构造方法
    }

    public Document(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.revision = 0;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 用客户端提交的内容覆盖当前稿。
     *
     * @param title 新标题
     * @param content 新正文
     * @param expectedRevision 客户端读取到的版本号
     * @throws StaleDocumentException 当服务端版本已经变化时
     */
    public void applyUpdate(String title, String content, long expectedRevision) {
        if (expectedRevision != revision) {
            throw new StaleDocumentException(revision);
        }
        this.title = title;
        this.content = content;
        this.revision++;
        this.updatedAt = Instant.now();
    }

    /**
     * 用历史版本覆盖当前稿。恢复本身也是一次新的保存，因此版本号继续递增。
     *
     * @param title 历史版本标题
     * @param content 历史版本正文
     */
    public void applyRestore(String title, String content) {
        this.title = title;
        this.content = content;
        this.revision++;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getRevision() {
        return revision;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
