/**
 * 写作工作台的前端交互。
 *
 * 第一阶段只处理排版交互与本机草稿；服务端保存和实时引导将在后续阶段接入。
 */
(() => {
  'use strict';

  const DRAFT_KEY = 'writing-coach:draft:v1';
  const AUTOSAVE_DELAY_MS = 400;

  const editor = document.getElementById('editor');
  const titleInput = document.getElementById('titleInput');
  const charCount = document.getElementById('charCount');
  const paraCount = document.getElementById('paraCount');
  const saveState = document.getElementById('saveState');
  const workspace = document.getElementById('workspace');
  const outlineToggle = document.getElementById('outlineToggle');
  const scrim = document.getElementById('scrim');
  const guideCount = document.getElementById('guideCount');
  const navBadge = document.getElementById('navBadge');
  const navItems = Array.from(document.querySelectorAll('.nav-item'));

  let saveTimer = null;

  /**
   * 让标题输入框随内容换行并自动增高，避免长标题在手机上被截断。
   */
  function resizeTitle() {
    titleInput.style.height = 'auto';
    titleInput.style.height = `${titleInput.scrollHeight}px`;
  }

  /**
   * 恢复本机草稿。没有草稿时保留模板中的示例内容，方便首次打开就看到完整布局。
   */
  function restoreDraft() {
    try {
      const raw = window.localStorage.getItem(DRAFT_KEY);
      if (!raw) {
        return;
      }
      const draft = JSON.parse(raw);
      if (typeof draft.content === 'string') {
        editor.value = draft.content;
      }
      if (typeof draft.title === 'string' && draft.title.trim()) {
        titleInput.value = draft.title;
      }
    } catch (error) {
      // 存储被禁用或数据损坏时退回示例内容，不影响正常写作。
      saveState.textContent = '本机保存不可用';
    }
  }

  /**
   * 统计正文字数（不含空白）与非空段落数。
   */
  function updateCounts() {
    const text = editor.value;
    const characters = text.replace(/\s/g, '').length;
    const paragraphs = text
      .split(/\n\s*\n/)
      .filter((part) => part.trim().length > 0).length;

    charCount.textContent = characters.toLocaleString('zh-CN');
    paraCount.textContent = String(paragraphs);
  }

  /**
   * 写入本机草稿。使用防抖避免每次按键都触发同步存储。
   */
  function saveDraft() {
    try {
      window.localStorage.setItem(
        DRAFT_KEY,
        JSON.stringify({
          title: titleInput.value,
          content: editor.value,
          savedAt: Date.now(),
        })
      );
      saveState.textContent = '已保存到本机';
    } catch (error) {
      saveState.textContent = '本机保存不可用';
    }
  }

  function scheduleSave() {
    saveState.textContent = '保存中…';
    window.clearTimeout(saveTimer);
    saveTimer = window.setTimeout(saveDraft, AUTOSAVE_DELAY_MS);
  }

  /**
   * 切换手机端视图（大纲 / 写作 / 引导）。
   *
   * @param {string} view 视图名称
   */
  function setMobileView(view) {
    document.body.dataset.view = view;
    navItems.forEach((item) => {
      const active = item.dataset.target === view;
      item.classList.toggle('is-active', active);
      item.setAttribute('aria-selected', String(active));
    });

    if (view === 'guide' && navBadge) {
      navBadge.hidden = true;
    }
  }

  /**
   * 打开或关闭平板端的大纲抽屉。
   *
   * @param {boolean} open 是否打开
   */
  function setOutlineOpen(open) {
    workspace.classList.toggle('is-outline-open', open);
    outlineToggle.setAttribute('aria-expanded', String(open));
  }

  /**
   * 学生处理完一条提示后更新计数，保留卡片作为写作记录。
   *
   * @param {HTMLButtonElement} button 被点击的操作按钮
   */
  function resolveCard(button) {
    const card = button.closest('[data-guide-card]');
    if (!card || card.classList.contains('is-resolved')) {
      return;
    }

    card.classList.add('is-resolved');
    const actions = card.querySelector('.card-actions');
    if (actions) {
      actions.remove();
    }

    const note = document.createElement('p');
    note.className = 'card-resolved';
    note.textContent = '已记录你的判断，这条提示不会再出现。';
    card.appendChild(note);

    const remaining = document.querySelectorAll('[data-guide-card]:not(.is-resolved)').length;
    guideCount.textContent = String(remaining);
    guideCount.classList.toggle('is-empty', remaining === 0);
    guideCount.setAttribute('aria-label', `${remaining} 条建议`);

    if (navBadge) {
      navBadge.textContent = String(remaining);
      navBadge.hidden = remaining === 0 || document.body.dataset.view === 'guide';
    }
  }

  editor.addEventListener('input', () => {
    updateCounts();
    scheduleSave();
  });

  titleInput.addEventListener('input', () => {
    resizeTitle();
    scheduleSave();
  });

  navItems.forEach((item) => {
    item.addEventListener('click', () => setMobileView(item.dataset.target));
  });

  outlineToggle.addEventListener('click', () => {
    setOutlineOpen(!workspace.classList.contains('is-outline-open'));
  });

  scrim.addEventListener('click', () => setOutlineOpen(false));

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
      setOutlineOpen(false);
    }
  });

  document.querySelectorAll('[data-action]').forEach((button) => {
    button.addEventListener('click', () => resolveCard(button));
  });

  restoreDraft();
  resizeTitle();
  updateCounts();
})();
