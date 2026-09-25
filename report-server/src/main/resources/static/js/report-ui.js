(function () {
  const byId = (id) => document.getElementById(id);
  const statusGroups = Array.from(document.querySelectorAll('[data-status-control]'));
  statusGroups.forEach((group) => {
    const control = byId(group.dataset.statusControl);
    group.querySelectorAll('[data-status]').forEach((button) => {
      button.addEventListener('click', () => {
        control.value = button.dataset.status;
        control.dispatchEvent(new Event('change', { bubbles: true }));
      });
    });
  });


  const serviceTable = byId('servicesTable');
  if (serviceTable) {
    const rows = Array.from(serviceTable.querySelectorAll('tbody tr'));
    const search = byId('serviceSearch');
    const org = byId('orgFilter');
    byId('failedServiceCount').textContent = rows.filter((row) => row.dataset.status === 'FAIL').length;
    byId('unrunServiceCount').textContent = rows.filter((row) => !row.dataset.status).length;
    Array.from(new Set(rows.map((row) => row.dataset.org))).sort().forEach((name) => {
      const option = document.createElement('option');
      option.value = name;
      option.textContent = name;
      org.appendChild(option);
    });
    const filterServices = () => {
      const query = search.value.trim().toLowerCase();
      let count = 0;
      rows.forEach((row) => {
        row.hidden = !(row.dataset.service.toLowerCase().includes(query)
          && (!org.value || row.dataset.org === org.value));
        if (!row.hidden) count += 1;
      });
      serviceTable.closest('.table-wrapper').hidden = count === 0;
      byId('serviceEmptyState').hidden = count !== 0;
      byId('serviceEmptyState').textContent = rows.length === 0
        ? '등록된 서비스가 없습니다. Gateway 계약 카탈로그를 확인하세요.'
        : '조건에 맞는 서비스가 없습니다. 검색어나 기관을 변경해 보세요.';
      byId('serviceResultCount').textContent = count + '개 서비스 · 최근 결과는 각 서비스의 마지막 실행 기준입니다.';
    };
    search.addEventListener('input', filterServices);
    org.addEventListener('change', filterServices);
    filterServices();
  }

  const period = byId('historyPeriod');
  const from = byId('historyFromFilter');
  const to = byId('historyToFilter');
  if (period) {
    const localDate = (date) => {
      const pad = (value) => String(value).padStart(2, '0');
      return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
        + 'T' + pad(date.getHours()) + ':' + pad(date.getMinutes());
    };
    period.value = from.value || to.value ? 'custom' : '';
    period.addEventListener('change', () => {
      if (period.value === 'custom') {
        byId('historyAdvanced').open = true;
        from.focus();
        return;
      }
      const end = new Date();
      const start = new Date(end);
      start.setDate(start.getDate() - Number(period.value));
      from.value = period.value ? localDate(start) : '';
      to.value = period.value ? localDate(end) : '';
      from.dispatchEvent(new Event('change', { bubbles: true }));
    });
    [from, to].forEach((input) => input.addEventListener('change', (event) => {
      if (event.isTrusted) period.value = input.value || from.value || to.value ? 'custom' : '';
    }));
    byId('historyResetFilterBtn').addEventListener('click', () => { period.value = ''; });
    const first = document.querySelector('#historyRunsTable tbody tr');
    if (first) {
      byId('latestRunSummary').textContent = '최근 실행 ' + first.dataset.status + ' · '
        + (first.dataset.startedAt.replace('T', ' ') || '시각 정보 없음') + ' · ' + first.dataset.source;
    }
  }

  ['historyAdvanced', 'caseAdvanced'].forEach((id) => {
    const details = byId(id);
    if (details && Array.from(details.querySelectorAll('input,select')).some((input) => input.value)) {
      details.open = true;
    }
  });

  const cases = Array.from(document.querySelectorAll('#casesTable tbody tr'));
  let selectedCase = null;
  const selectCase = (row) => {
    selectedCase = row;
    cases.forEach((candidate) => {
      candidate.classList.toggle('is-selected', candidate === row);
      candidate.querySelector('.case-select').setAttribute('aria-pressed', String(candidate === row));
    });
    const inspector = byId('caseInspector');
    if (!inspector) return;
    inspector.hidden = !row;
    if (!row) return;
    const data = row.dataset;
    const fields = {
      inspectorName: data.name || '이름 없는 케이스', inspectorKind: data.kindLabel,
      inspectorApi: data.api, inspectorScenario: data.scenarioName,
      inspectorMethod: data.method, inspectorHttp: data.httpStatus, inspectorEndpoint: data.endpoint,
      inspectorFailure: data.failure || '실패 메시지가 없습니다.'
    };
    Object.entries(fields).forEach(([id, value]) => { byId(id).textContent = value || '—'; });
    byId('inspectorKind').hidden = !data.kindLabel;
    byId('inspectorScenario').dataset.empty = String(!data.scenarioName || data.scenarioName === data.name);
    byId('copyFailure').disabled = !data.failure;
    byId('copyStatus').textContent = '';
  };
  cases.forEach((row) => row.querySelector('.case-select').addEventListener('click', () => selectCase(row)));
  if (byId('copyFailure')) {
    byId('copyFailure').addEventListener('click', async () => {
      const message = selectedCase ? selectedCase.dataset.failure : '';
      if (!message) return;
      try {
        await navigator.clipboard.writeText(message);
        byId('copyStatus').textContent = '실패 메시지를 복사했습니다.';
      } catch (error) {
        byId('copyStatus').textContent = '복사할 수 없습니다. 메시지를 선택해 직접 복사해 주세요.';
      }
    });
  }

  document.addEventListener('report:filtered', (event) => {
    const { control, statuses } = event.detail;
    const group = statusGroups.find((item) => item.dataset.statusControl === control);
    if (group) {
      group.querySelectorAll('[data-status]').forEach((button) => {
        const value = button.dataset.status;
        button.setAttribute('aria-pressed', String(byId(control).value === value));
        button.querySelector('.tab-count').textContent = statuses.filter((status) => !value || status === value).length;
      });
    }
    if (control === 'statusFilter') {
      const visible = cases.filter((row) => row.style.display !== 'none');
      selectCase(visible.includes(selectedCase) ? selectedCase : visible[0] || null);
      const advanced = byId('caseAdvanced');
      if (advanced && Array.from(advanced.querySelectorAll('input,select')).some((input) => input.value)) advanced.open = true;
    }
  });
})();
