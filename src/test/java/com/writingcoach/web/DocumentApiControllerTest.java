package com.writingcoach.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文档保存、版本快照与恢复的接口测试。
 *
 * <p>每个测试结束后回滚，避免修改演示数据影响其他测试。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DocumentApiControllerTest {

    private static final String DOCUMENT_URL = "/api/documents/1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void seededDocumentIsAvailable() throws Exception {
        mockMvc.perform(get(DOCUMENT_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", containsString("短视频")))
                .andExpect(jsonPath("$.revision").isNumber())
                .andExpect(jsonPath("$.versionCount").value(1));
    }

    @Test
    void saveIncrementsRevision() throws Exception {
        long revision = currentRevision();

        mockMvc.perform(put(DOCUMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveRequest("修改后的标题", "修改后的正文", revision)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value((int) (revision + 1)))
                .andExpect(jsonPath("$.title").value("修改后的标题"))
                .andExpect(jsonPath("$.content").value("修改后的正文"));
    }

    @Test
    void staleRevisionIsRejected() throws Exception {
        long revision = currentRevision();

        mockMvc.perform(put(DOCUMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveRequest("标题", "正文", revision + 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STALE_REVISION"))
                .andExpect(jsonPath("$.currentRevision").value((int) revision));
    }

    @Test
    void versionCanBeCreatedListedAndDiffed() throws Exception {
        long versionId = createVersion();

        long revision = currentRevision();
        mockMvc.perform(put(DOCUMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveRequest("修订后的标题", "第一段。\n\n新增的第二段。", revision)))
                .andExpect(status().isOk());

        mockMvc.perform(get(DOCUMENT_URL + "/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get(DOCUMENT_URL + "/versions/" + versionId + "/diff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added", greaterThan(0)));
    }

    @Test
    void restoreBringsBackVersionContent() throws Exception {
        String originalContent = readDocument().get("content").asText();
        long versionId = createVersion();

        long revision = currentRevision();
        mockMvc.perform(put(DOCUMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveRequest("临时标题", "临时内容", revision)))
                .andExpect(status().isOk());

        mockMvc.perform(post(DOCUMENT_URL + "/versions/" + versionId + "/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(originalContent));

        mockMvc.perform(get(DOCUMENT_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(originalContent));
    }

    private String saveRequest(String title, String content, long baseRevision) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", title,
                "content", content,
                "baseRevision", baseRevision));
    }

    private JsonNode readDocument() throws Exception {
        String body = mockMvc.perform(get(DOCUMENT_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private long currentRevision() throws Exception {
        return readDocument().get("revision").asLong();
    }

    private long createVersion() throws Exception {
        MvcResult result = mockMvc.perform(post(DOCUMENT_URL + "/versions"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(
                        result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("id")
                .asLong();
    }
}
