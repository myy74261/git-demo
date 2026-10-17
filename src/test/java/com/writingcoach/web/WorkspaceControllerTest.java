package com.writingcoach.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 验证工作台页面可以正常渲染。
 */
@SpringBootTest
@AutoConfigureMockMvc
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void workspacePageRenders() throws Exception {
        mockMvc.perform(get("/workspace"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("写作引导")));
    }
}
