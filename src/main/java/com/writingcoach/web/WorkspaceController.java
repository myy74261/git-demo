package com.writingcoach.web;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 渲染写作工作台页面。
 *
 * <p>当前阶段只提供页面骨架与示例内容，写作过程分析与引导卡片将在后续阶段接入。</p>
 */
@Controller
public class WorkspaceController {

    private static final String ASSIGNMENT_TITLE = "短视频使用与大学生注意力恢复的关系研究";

    /**
     * 打开写作工作台。根路径与 {@code /workspace} 指向同一页面，方便直接访问。
     *
     * @param model 视图模型
     * @return 工作台模板名
     */
    @GetMapping({"/", "/workspace"})
    public String workspace(Model model) {
        model.addAttribute("assignmentTitle", ASSIGNMENT_TITLE);
        model.addAttribute("courseName", "传播学方法课");
        model.addAttribute("dueDate", "10 月 30 日");
        model.addAttribute("outline", List.of(
                new OutlineItem("摘要", "150–200 字"),
                new OutlineItem("一、问题提出", "约 600 字"),
                new OutlineItem("二、研究方法", "约 800 字"),
                new OutlineItem("三、结果与分析", "约 900 字"),
                new OutlineItem("四、讨论与结论", "约 600 字"),
                new OutlineItem("参考文献", "至少 5 篇")
        ));
        return "workspace";
    }
}
