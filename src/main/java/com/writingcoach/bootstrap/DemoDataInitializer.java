package com.writingcoach.bootstrap;

import com.writingcoach.domain.Document;
import com.writingcoach.repository.DocumentRepository;
import com.writingcoach.service.DocumentService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 首次启动时写入演示文档和初始版本，保证打开页面就能看到完整工作台。
 */
@Component
public class DemoDataInitializer implements ApplicationRunner {

    private static final String SAMPLE_TITLE = "短视频使用与大学生注意力恢复的关系研究";

    private static final String SAMPLE_CONTENT = """
            摘要

            本研究探讨短视频使用强度与大学生注意力恢复之间的关系。通过对在校学生进行问卷调查，研究发现短视频使用强度与注意力恢复水平之间存在显著相关。这一结果说明短视频使用对注意力恢复具有重要影响。

            一、问题提出

            随着移动互联网的普及，短视频已经成为大学生日常生活中的重要组成部分。已有研究表明，短视频使用会影响个体的注意力水平，但关于其恢复机制的研究仍然不足。因此，本研究拟从注意力恢复理论出发，考察短视频使用强度与注意力恢复之间的关系。

            二、研究方法

            本研究采用问卷调查法，以某高校在校本科生为研究对象。问卷包括短视频使用强度量表、注意力恢复量表和基本信息三部分。数据收集完成后，使用统计软件进行分析。
            """;

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    public DemoDataInitializer(DocumentRepository documentRepository,
                               DocumentService documentService) {
        this.documentRepository = documentRepository;
        this.documentService = documentService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (documentRepository.existsById(DocumentService.DEMO_DOCUMENT_ID)) {
            return;
        }
        Document document = new Document(
                DocumentService.DEMO_DOCUMENT_ID, SAMPLE_TITLE, SAMPLE_CONTENT);
        documentRepository.save(document);
        documentService.createVersion(document.getId(), "INITIAL");
    }
}
