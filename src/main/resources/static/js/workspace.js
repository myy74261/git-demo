/**
 * 写作工作台的前端交互。
 *
 * 负责本机草稿、服务端自动保存、版本历史、版本对比和响应式导航。
 * 本机草稿只作为断网兜底，服务端文档才是页面加载时的数据源。
 */
(() => {
  'use strict';

  const SAVE_DEBOUNCE_MS = 900;
  const LOCAL_DEBOUNCE_MS = 300;
  const TRIGGER_LABELS = {
    INITIAL: '初始版本',
    MANUAL: '手动保存',
    RESTORE: '恢复后快照',
  };

  const body = document.body;
  const documentId = body.dataset.documentId || '1';
  const DRAFT_KEY = `writing-coach:draft:${documentId}`;

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

  const saveVersionButton = document.getElementById('saveVersionButton');
  const versionButton = document.getElementById('versionButton');
  const versionCountElement = document.getElementById('versionCount');
  const versionDrawer = document.getElementById('versionDrawer');
  const versionScrim = document.getElementById('versionScrim');
  const versionClose = document.getElementById('versionClose');
  const versionListWrap = document.getElementById('versionListWrap');
  const versionList = document.getElementById('versionList');
  const versionEmpty = document.getElementById('versionEmpty');
  const versionDetail = document.getElementById('versionDetail');
  const versionBack = document.getElementById('versionBack');
  const detailTitle = document.getElementById('detailTitle');
  const detailMeta = document.getElementById('detailMeta');
  const diffView = document.getElementById('diffView');
  const restoreButton = document.getElementById('restoreButton');
  const toast = document.getElementById('toast');

  let revision = Number(body.dataset.revision || 0);
  let versionCount = Number(body.dataset.versionCount || 0);
  let localTimer = null;
  let serverTimer = null;
  let saveInFlight = null;
  let pendingSave = false;
  let unsynced = false;
  let toastTimer = null;
  let versionDrawerOpen = false;
  let selectedVersionId = null;
  let restoreArmed = false;

  /**
   * 标题输入框随内容换行并自动增高，避免长标题在手机上被截断。
   */
  function resizeTitle() {
    titleInput.style.height = 'auto';
    titleInput.style.height = `${titleInput.scrollHeight}px`;
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

  function setSaveState(text, tone) {
    saveState.textContent = text;
    saveState.classList.toggle('is-error', tone === 'error');
    saveState.classList.toggle('is-warn', tone === 'warn');
  }

  function persistLocalDraft() {
    try {
      window.localStorage.setItem(DRAFT_KEY, JSON.stringify({
        title: titleInput.value,
        content: editor.value,
        revision,
        pending: unsynced,
        savedAt: Date.now(),
      }));
    } catch (error) {
      // 本机存储被禁用时仍可继续写作，服务端保存不受影响。
    }
  }

  function scheduleLocalSave() {
    window.clearTimeout(localTimer);
    localTimer = window.setTimeout(persistLocalDraft, LOCAL_DEBOUNCE_MS);
  }

  /**
   * 如果上次服务端保存失败，用本机草稿恢复未同步的文字。
   * 服务端内容正常时以页面渲染的内容为准，避免旧草稿覆盖新版本。
   */
  function restoreLocalDraft() {
    try {
      const raw = window.localStorage.getItem(DRAFT_KEY);
      if (!raw) {
        return;
      }
      const draft = JSON.parse(raw);
      if (draft.pending === true && typeof draft.content === 'string') {
        editor.value = draft.content;
        if (typeof draft.title === 'string' && draft.title.trim()) {
          titleInput.value = draft.title;
        }
        if (Number.isFinite(Number(draft.revision))) {
          revision = Number(draft.revision);
        }
        unsynced = true;
        setSaveState('有未同步内容', 'warn');
      }
    } catch (error) {
      setSaveState('本机草稿不可用', 'warn');
    }
  }

  function scheduleServerSave(delay = SAVE_DEBOUNCE_MS) {
    window.clearTimeout(serverTimer);
    serverTimer = window.setTimeout(() => {
      void saveToServer();
    }, delay);
  }

  /**
   * 保存当前稿。同一时间只允许一个请求在途，编辑期间到达的改动会排队再存。
   *
   * @returns {Promise<boolean>} 是否保存成功
   */
  function saveToServer() {
    if (saveInFlight) {
      pendingSave = true;
      return saveInFlight;
    }

    setSaveState('保存中…');
    saveInFlight = (async () => {
      try {
        const saved = await putDocumentWithRetry();
        revision = saved.revision;
        unsynced = false;
        persistLocalDraft();
        setSaveState('已保存');
        return true;
      } catch (error) {
        unsynced = true;
        persistLocalDraft();
        setSaveState('保存失败，已存本机', 'error');
        return false;
      } finally {
        saveInFlight = null;
        if (pendingSave) {
          pendingSave = false;
          scheduleServerSave(200);
        }
      }
    })();
    return saveInFlight;
  }

  /**
   * 遇到版本冲突时采用服务端最新版本号重试一次，保留当前页面上的文字。
   */
  async function putDocumentWithRetry() {
    for (let attempt = 0; attempt < 2; attempt++) {
      const response = await fetch(`/api/documents/${documentId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: titleInput.value,
          content: editor.value,
          baseRevision: revision,
        }),
      });

      if (response.ok) {
        return response.json();
      }
      if (response.status === 409 && attempt === 0) {
        const conflict = await response.json();
        if (typeof conflict.currentRevision === 'number') {
          revision = conflict.currentRevision;
          continue;
        }
      }
      throw new Error(`保存失败：${response.status}`);
    }
    throw new Error('保存失败：重试后仍未成功');
  }

  function showToast(message) {
    toast.textContent = message;
    toast.classList.add('is-visible');
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(() => {
      toast.classList.remove('is-visible');
    }, 2400);
  }

  function updateVersionBadge() {
    versionCountElement.textContent = String(versionCount);
  }

  function formatTime(isoTime) {
    try {
      return new Intl.DateTimeFormat('zh-CN', {
        month: 'numeric',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(new Date(isoTime));
    } catch (error) {
      return isoTime;
    }
  }

  function triggerLabel(triggerType) {
    return TRIGGER_LABELS[triggerType] || '版本快照';
  }

  function setVersionMode(mode) {
    versionDrawer.dataset.mode = mode;
    versionListWrap.hidden = mode !== 'list';
    versionDetail.hidden = mode !== 'detail';
  }

  function openVersionDrawer() {
    versionDrawerOpen = true;
    versionDrawer.inert = false;
    versionDrawer.classList.add('is-open');
    versionScrim.classList.add('is-open');
    versionDrawer.setAttribute('aria-hidden', 'false');
    setVersionMode('list');
    void loadVersions();
    versionClose.focus();
  }

  function closeVersionDrawer() {
    versionDrawerOpen = false;
    versionDrawer.classList.remove('is-open');
    versionScrim.classList.remove('is-open');
    versionDrawer.setAttribute('aria-hidden', 'true');
    versionDrawer.inert = true;
    versionButton.focus();
  }

  async function loadVersions() {
    versionList.innerHTML = '';
    versionEmpty.hidden = true;
    versionEmpty.textContent = '还没有版本记录，点击“存为版本”创建第一条。';

    const loading = document.createElement('li');
    loading.className = 'drawer-empty';
    loading.textContent = '正在加载版本…';
    versionList.appendChild(loading);

    try {
      const response = await fetch(`/api/documents/${documentId}/versions`);
      if (!response.ok) {
        throw new Error(`加载版本失败：${response.status}`);
      }
      const versions = await response.json();
      versionCount = versions.length;
      updateVersionBadge();
      renderVersionList(versions);
    } catch (error) {
      versionList.innerHTML = '';
      versionEmpty.hidden = false;
      versionEmpty.textContent = '版本加载失败，请稍后再试。';
    }
  }

  function renderVersionList(versions) {
    versionList.innerHTML = '';
    versionEmpty.hidden = versions.length > 0;
    if (versions.length === 0) {
      return;
    }

    versions.forEach((version) => {
      const item = document.createElement('li');
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'version-item';

      const badge = document.createElement('span');
      badge.className = 'version-badge';
      badge.textContent = `V${version.revisionNumber}`;

      const main = document.createElement('span');
      main.className = 'version-main';
      const title = document.createElement('strong');
      title.textContent = triggerLabel(version.triggerType);
      const meta = document.createElement('small');
      meta.textContent =
        `${formatTime(version.createdAt)} · ${version.wordCount.toLocaleString('zh-CN')} 字`;
      main.append(title, meta);

      const chevron = document.createElement('span');
      chevron.className = 'version-chevron';
      chevron.setAttribute('aria-hidden', 'true');
      chevron.textContent = '›';

      button.append(badge, main, chevron);
      button.addEventListener('click', () => {
        void openVersionDetail(version.id);
      });
      item.appendChild(button);
      versionList.appendChild(item);
    });
  }

  async function openVersionDetail(versionId) {
    selectedVersionId = versionId;
    restoreArmed = false;
    restoreButton.disabled = false;
    restoreButton.textContent = '恢复此版本';
    setVersionMode('detail');

    detailTitle.textContent = '正在加载…';
    detailMeta.textContent = '';
    diffView.innerHTML = '';
    diffView.textContent = '正在生成对比…';

    try {
      const [detail, diff] = await Promise.all([
        fetch(`/api/documents/${documentId}/versions/${versionId}`).then(readJson),
        fetch(`/api/documents/${documentId}/versions/${versionId}/diff`).then(readJson),
      ]);

      detailTitle.textContent = `版本 ${detail.revisionNumber}`;
      detailMeta.textContent =
        `${triggerLabel(detail.triggerType)} · ${formatTime(detail.createdAt)} · ` +
        `${detail.wordCount.toLocaleString('zh-CN')} 字 · +${diff.added} −${diff.removed}`;
      renderDiff(diff);
    } catch (error) {
      detailTitle.textContent = '版本加载失败';
      detailMeta.textContent = '';
      diffView.textContent = '请稍后再试。';
    }
  }

  async function readJson(response) {
    if (!response.ok) {
      throw new Error(`请求失败：${response.status}`);
    }
    return response.json();
  }

  function renderDiff(diff) {
    diffView.innerHTML = '';
    if (diff.added === 0 && diff.removed === 0) {
      const same = document.createElement('p');
      same.className = 'drawer-empty';
      same.textContent = '当前稿与这个版本内容一致。';
      diffView.appendChild(same);
      return;
    }

    diff.lines.forEach((line) => {
      const row = document.createElement('div');
      row.className = `diff-line is-${line.type.toLowerCase()}`;

      const marker = document.createElement('span');
      marker.className = 'marker';
      marker.textContent = line.type === 'ADD' ? '+' : line.type === 'REMOVE' ? '−' : '';

      const text = document.createElement('span');
      text.textContent = line.text || ' ';

      row.append(marker, text);
      diffView.appendChild(row);
    });
  }

  async function saveVersion() {
    saveVersionButton.disabled = true;
    try {
      if (unsynced) {
        const saved = await saveToServer();
        if (!saved) {
          throw new Error('保存未完成');
        }
      }
      const response = await fetch(`/api/documents/${documentId}/versions`, {
        method: 'POST',
      });
      if (!response.ok) {
        throw new Error(`保存版本失败：${response.status}`);
      }
      const version = await response.json();
      versionCount += 1;
      updateVersionBadge();
      showToast(`已保存为版本 ${version.revisionNumber}`);
      if (versionDrawerOpen) {
        await loadVersions();
      }
    } catch (error) {
      showToast('保存版本失败，请稍后再试');
    } finally {
      saveVersionButton.disabled = false;
    }
  }

  async function restoreSelectedVersion() {
    if (!selectedVersionId) {
      return;
    }
    if (!restoreArmed) {
      restoreArmed = true;
      restoreButton.textContent = '确认恢复？';
      return;
    }

    restoreButton.disabled = true;
    restoreButton.textContent = '正在恢复…';
    try {
      const response = await fetch(
        `/api/documents/${documentId}/versions/${selectedVersionId}/restore`,
        { method: 'POST' }
      );
      if (!response.ok) {
        throw new Error(`恢复失败：${response.status}`);
      }
      window.location.reload();
    } catch (error) {
      restoreButton.disabled = false;
      restoreButton.textContent = '恢复此版本';
      restoreArmed = false;
      showToast('恢复失败，请稍后再试');
    }
  }

  /**
   * 切换手机端视图（大纲 / 写作 / 引导）。
   */
  function setMobileView(view) {
    body.dataset.view = view;
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
   */
  function setOutlineOpen(open) {
    workspace.classList.toggle('is-outline-open', open);
    outlineToggle.setAttribute('aria-expanded', String(open));
  }

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
      navBadge.hidden = remaining === 0 || body.dataset.view === 'guide';
    }
  }

  editor.addEventListener('input', () => {
    unsynced = true;
    updateCounts();
    scheduleLocalSave();
    scheduleServerSave();
  });

  titleInput.addEventListener('input', () => {
    resizeTitle();
    unsynced = true;
    scheduleLocalSave();
    scheduleServerSave();
  });

  navItems.forEach((item) => {
    item.addEventListener('click', () => setMobileView(item.dataset.target));
  });

  outlineToggle.addEventListener('click', () => {
    setOutlineOpen(!workspace.classList.contains('is-outline-open'));
  });

  scrim.addEventListener('click', () => setOutlineOpen(false));

  versionButton.addEventListener('click', openVersionDrawer);
  versionClose.addEventListener('click', closeVersionDrawer);
  versionScrim.addEventListener('click', closeVersionDrawer);
  versionBack.addEventListener('click', () => setVersionMode('list'));
  saveVersionButton.addEventListener('click', () => {
    void saveVersion();
  });
  restoreButton.addEventListener('click', () => {
    void restoreSelectedVersion();
  });

  document.addEventListener('keydown', (event) => {
    if (event.key !== 'Escape') {
      return;
    }
    if (versionDrawerOpen) {
      closeVersionDrawer();
      return;
    }
    setOutlineOpen(false);
  });

  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'hidden' && unsynced) {
      persistLocalDraft();
    }
  });

  document.querySelectorAll('[data-action]').forEach((button) => {
    button.addEventListener('click', () => resolveCard(button));
  });

  restoreLocalDraft();
  resizeTitle();
  updateCounts();
  updateVersionBadge();
})();
