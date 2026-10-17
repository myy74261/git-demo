package com.writingcoach.repository;

import com.writingcoach.domain.DocumentVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 文档版本快照的数据访问入口。
 */
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocumentIdOrderByRevisionNumberDesc(Long documentId);

    Optional<DocumentVersion> findFirstByDocumentIdOrderByRevisionNumberDesc(Long documentId);

    Optional<DocumentVersion> findByIdAndDocumentId(Long id, Long documentId);

    long countByDocumentId(Long documentId);
}
