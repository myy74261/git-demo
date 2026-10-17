package com.writingcoach.web;

import com.writingcoach.domain.Document;
import com.writingcoach.domain.DocumentVersion;
import com.writingcoach.service.DocumentDiffService;
import com.writingcoach.service.DocumentService;
import com.writingcoach.web.api.DiffResponse;
import com.writingcoach.web.api.DocumentResponse;
import com.writingcoach.web.api.SaveDocumentRequest;
import com.writingcoach.web.api.VersionDetailResponse;
import com.writingcoach.web.api.VersionSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档保存与版本管理的 REST 接口。
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentApiController {

    private final DocumentService documentService;
    private final DocumentDiffService diffService;

    public DocumentApiController(DocumentService documentService,
                                 DocumentDiffService diffService) {
        this.documentService = documentService;
        this.diffService = diffService;
    }

    @GetMapping("/{documentId}")
    public DocumentResponse getDocument(@PathVariable long documentId) {
        Document document = documentService.requireDocument(documentId);
        return DocumentResponse.from(document, documentService.countVersions(documentId));
    }

    @PutMapping("/{documentId}")
    public DocumentResponse saveDocument(@PathVariable long documentId,
                                         @Valid @RequestBody SaveDocumentRequest request) {
        Document document = documentService.save(
                documentId, request.title(), request.content(), request.baseRevision());
        return DocumentResponse.from(document, documentService.countVersions(documentId));
    }

    @PostMapping("/{documentId}/versions")
    public VersionSummaryResponse createVersion(@PathVariable long documentId) {
        DocumentVersion version = documentService.createVersion(documentId, "MANUAL");
        return VersionSummaryResponse.from(version);
    }

    @GetMapping("/{documentId}/versions")
    public List<VersionSummaryResponse> listVersions(@PathVariable long documentId) {
        return documentService.listVersions(documentId).stream()
                .map(VersionSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{documentId}/versions/{versionId}")
    public VersionDetailResponse getVersion(@PathVariable long documentId,
                                            @PathVariable long versionId) {
        return VersionDetailResponse.from(documentService.requireVersion(documentId, versionId));
    }

    /**
     * 比较历史版本与当前稿，ADD 表示当前稿新增的行。
     */
    @GetMapping("/{documentId}/versions/{versionId}/diff")
    public DiffResponse diffVersion(@PathVariable long documentId,
                                    @PathVariable long versionId) {
        Document document = documentService.requireDocument(documentId);
        DocumentVersion version = documentService.requireVersion(documentId, versionId);
        DocumentDiffService.DiffResult result =
                diffService.diff(version.getContent(), document.getContent());
        return DiffResponse.from(version, result);
    }

    @PostMapping("/{documentId}/versions/{versionId}/restore")
    public DocumentResponse restoreVersion(@PathVariable long documentId,
                                           @PathVariable long versionId) {
        Document document = documentService.restore(documentId, versionId);
        return DocumentResponse.from(document, documentService.countVersions(documentId));
    }
}
