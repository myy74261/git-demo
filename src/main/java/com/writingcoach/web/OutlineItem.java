package com.writingcoach.web;

/**
 * 写作大纲中的一个条目，仅用于页面渲染。
 */
public final class OutlineItem {

    private final String title;
    private final String hint;

    public OutlineItem(String title, String hint) {
        this.title = title;
        this.hint = hint;
    }

    public String getTitle() {
        return title;
    }

    public String getHint() {
        return hint;
    }
}
