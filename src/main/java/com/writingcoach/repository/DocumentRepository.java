package com.writingcoach.repository;

import com.writingcoach.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 文档聚合根的数据访问入口。
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {
}
