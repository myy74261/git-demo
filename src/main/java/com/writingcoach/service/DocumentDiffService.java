package com.writingcoach.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 按行比较两个版本的正文。
 *
 * <p>使用最长公共子序列生成增删行，并把较长的未变化片段折叠成省略提示，
 * 让版本对比聚焦在真正改动的位置。</p>
 */
@Service
public class DocumentDiffService {

    private static final int CONTEXT_LINES = 2;

    /**
     * 计算旧文本到新文本的行级差异。
     *
     * @param oldText 历史版本正文
     * @param newText 当前正文
     * @return 按顺序排列的差异行
     */
    public DiffResult diff(String oldText, String newText) {
        List<String> oldLines = splitLines(oldText);
        List<String> newLines = splitLines(newText);
        int[][] lcs = buildLcsTable(oldLines, newLines);

        List<DiffLine> allLines = new ArrayList<>();
        int added = 0;
        int removed = 0;
        int oldIndex = 0;
        int newIndex = 0;

        while (oldIndex < oldLines.size() && newIndex < newLines.size()) {
            if (oldLines.get(oldIndex).equals(newLines.get(newIndex))) {
                allLines.add(new DiffLine("EQUAL", oldLines.get(oldIndex)));
                oldIndex++;
                newIndex++;
            } else if (lcs[oldIndex + 1][newIndex] >= lcs[oldIndex][newIndex + 1]) {
                allLines.add(new DiffLine("REMOVE", oldLines.get(oldIndex)));
                removed++;
                oldIndex++;
            } else {
                allLines.add(new DiffLine("ADD", newLines.get(newIndex)));
                added++;
                newIndex++;
            }
        }
        while (oldIndex < oldLines.size()) {
            allLines.add(new DiffLine("REMOVE", oldLines.get(oldIndex)));
            removed++;
            oldIndex++;
        }
        while (newIndex < newLines.size()) {
            allLines.add(new DiffLine("ADD", newLines.get(newIndex)));
            added++;
            newIndex++;
        }

        return new DiffResult(compact(allLines), added, removed);
    }

    private List<String> splitLines(String text) {
        return List.of(text.split("\\R", -1));
    }

    private int[][] buildLcsTable(List<String> oldLines, List<String> newLines) {
        int oldSize = oldLines.size();
        int newSize = newLines.size();
        int[][] lcs = new int[oldSize + 1][newSize + 1];
        for (int oldIndex = oldSize - 1; oldIndex >= 0; oldIndex--) {
            for (int newIndex = newSize - 1; newIndex >= 0; newIndex--) {
                lcs[oldIndex][newIndex] = oldLines.get(oldIndex).equals(newLines.get(newIndex))
                        ? lcs[oldIndex + 1][newIndex + 1] + 1
                        : Math.max(lcs[oldIndex + 1][newIndex], lcs[oldIndex][newIndex + 1]);
            }
        }
        return lcs;
    }

    /**
     * 连续未变化的行只保留少量上下文，避免对比视图被整篇正文淹没。
     */
    private List<DiffLine> compact(List<DiffLine> lines) {
        List<DiffLine> compacted = new ArrayList<>();
        int equalRunStart = -1;

        for (int index = 0; index <= lines.size(); index++) {
            boolean equal = index < lines.size() && "EQUAL".equals(lines.get(index).type());
            if (equal) {
                if (equalRunStart < 0) {
                    equalRunStart = index;
                }
                continue;
            }

            if (equalRunStart >= 0) {
                int runLength = index - equalRunStart;
                if (runLength > CONTEXT_LINES * 2 + 1) {
                    compacted.addAll(lines.subList(equalRunStart, equalRunStart + CONTEXT_LINES));
                    compacted.add(new DiffLine("HUNK", "··· 省略 " + (runLength - CONTEXT_LINES * 2) + " 行 ···"));
                    compacted.addAll(lines.subList(index - CONTEXT_LINES, index));
                } else {
                    compacted.addAll(lines.subList(equalRunStart, index));
                }
                equalRunStart = -1;
            }

            if (index < lines.size()) {
                compacted.add(lines.get(index));
            }
        }
        return compacted;
    }

    /**
     * 一行差异及其类型。
     *
     * @param type EQUAL、ADD、REMOVE 或 HUNK
     * @param text 行内容
     */
    public record DiffLine(String type, String text) {
    }

    /**
     * 完整的比较结果。
     *
     * @param lines 供页面渲染的差异行
     * @param added 新增行数
     * @param removed 删除行数
     */
    public record DiffResult(List<DiffLine> lines, int added, int removed) {
    }
}
