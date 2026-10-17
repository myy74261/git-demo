package com.writingcoach.service;

import com.writingcoach.domain.Document;
import com.writingcoach.domain.DocumentVersion;
import com.writingcoach.repository.DocumentRepository;
import com.writingcoach.repository.DocumentVersionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文档保存、版本快照与恢复的业务入口。
 */
@Service
@Transactional(readOnly = true)
public class DocumentService {

    /** 当前演示版本只有一个文档，后续接入登录后再按学生与作业生成。 */
    public static final long DEMO_DOCUMENT_ID = 1L;

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentVersionRepository versionRepository) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
    }

    public Document requireDocument(long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    public DocumentVersion requireVersion(long documentId, long versionId) {
        return versionRepository.findByIdAndDocumentId(versionId, documentId)
                .orElseThrow(() -> new VersionNotFoundException(documentId, versionId));
    }

    /**
     * 保存当前稿。baseRevision 与服务端不一致时抛出异常，由接口返回 409。
     */
    @Transactional
    public Document save(long documentId, String title, String content, long baseRevision) {
        Document document = requireDocument(documentId);
        document.applyUpdate(title.strip(), content, baseRevision);
        return documentRepository.save(document);
    }

    /**
     * 为当前内容创建快照。
     *
     * @param documentId 文档编号
     * @param triggerType MANUAL、INITIAL 或 RESTORE
     */
    @Transactional
    public DocumentVersion createVersion(long documentId, String triggerType) {
        Document document = requireDocument(documentId);
        return appendVersion(document, triggerType);
    }

    public List<DocumentVersion> listVersions(long documentId) {
        requireDocument(documentId);
        return versionRepository.findByDocumentIdOrderByRevisionNumberDesc(documentId);
    }

    public long countVersions(long documentId) {
        return versionRepository.countByDocumentId(documentId);
    }

    /**
     * 把历史版本恢复为当前稿，并记录一条恢复后的快照，保留完整轨迹。
     */
    @Transactional
    public Document restore(long documentId, long versionId) {
        Document document = requireDocument(documentId);
        DocumentVersion version = requireVersion(documentId, versionId);
        document.applyRestore(version.getTitle(), version.getContent());
        documentRepository.save(document);
        appendVersion(document, "RESTORE");
        return document;
    }

    private DocumentVersion appendVersion(Document document, String triggerType) {
        long nextRevision = versionRepository
                .findFirstByDocumentIdOrderByRevisionNumberDesc(document.getId())
                .map(version -> version.getRevisionNumber() + 1)
                .orElse(1L);
        DocumentVersion version = new DocumentVersion(
                document.getId(),
                nextRevision,
                document.getTitle(),
                document.getContent(),
                triggerType,
                countWords(document.getContent()));
        return versionRepository.save(version);
    }

    /**
     * 统计不含空白字符的字数，与前端展示口径保持一致。
     */
    public static int countWords(String content) {
        return (int) content.codePoints()
                .filter(codePoint -> !Character.isWhitespace(codePoint))
                .count();
    }
}
