// ---------------------------------------------------------------------------
// Catálogo estático (espelha os enums e portas dos microsserviços)
// ---------------------------------------------------------------------------

const SERVICES = [
    { key: 'identity', name: 'Identity', port: 8080, role: 'Usuários, papéis e permissões', arch: 'clean', view: 'identity', deps: [] },
    { key: 'catalog', name: 'Catalog', port: 8082, role: 'Laboratórios e horários de funcionamento', arch: 'clean', view: 'catalog', deps: ['identity'] },
    { key: 'scheduling-clean', name: 'Scheduling', port: 8081, role: 'Reservas — variante Clean Architecture', arch: 'clean', view: 'scheduling', deps: ['identity', 'catalog'] },
    { key: 'scheduling-layered', name: 'Scheduling', port: 8083, role: 'Reservas — variante em camadas (controle)', arch: 'layered', view: 'scheduling', deps: ['identity', 'catalog'] },
    { key: 'inventory', name: 'Inventory', port: 8084, role: 'Consumíveis e movimentações de estoque', arch: 'clean', view: 'inventory', deps: ['identity'] },
];

const ROLES = { STUDENT: 'Aluno', PROFESSOR: 'Professor', TECHNICIAN: 'Técnico', ADMIN: 'Administrador' };

const PERMISSIONS = {
    RESERVATION_REQUEST: 'Solicitar reserva',
    LABORATORY_MANAGE: 'Gerir laboratórios',
    INVENTORY_MANAGE: 'Gerir inventário',
    USER_MANAGE: 'Gerir usuários',
};

const ROLE_PERMISSIONS = {
    STUDENT: ['RESERVATION_REQUEST'],
    PROFESSOR: ['RESERVATION_REQUEST'],
    TECHNICIAN: ['RESERVATION_REQUEST', 'LABORATORY_MANAGE', 'INVENTORY_MANAGE'],
    ADMIN: Object.keys(PERMISSIONS),
};

const DAYS = [
    ['MONDAY', 'Seg'], ['TUESDAY', 'Ter'], ['WEDNESDAY', 'Qua'], ['THURSDAY', 'Qui'],
    ['FRIDAY', 'Sex'], ['SATURDAY', 'Sáb'], ['SUNDAY', 'Dom'],
];
const JS_DAY_TO_ENUM = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

const VIEWS = {
    overview: ['Visão geral', 'Estado dos microsserviços e dependências entre eles'],
    identity: ['Usuários', 'identity-service · :8080 — cadastro de usuários e autorização'],
    catalog: ['Laboratórios', 'catalog-service · :8082 — laboratórios e horários de funcionamento'],
    scheduling: ['Reservas', 'scheduling-service · :8081 (Clean) e :8083 (Layered) — mesmo contrato REST'],
    inventory: ['Inventário', 'inventory-service · :8084 — itens consumíveis, entradas e saídas'],
    activity: ['Atividade', 'Requisições enviadas pelo console e respostas recebidas'],
};

const ICONS = {
    copy: '<svg viewBox="0 0 24 24"><rect x="9" y="9" width="12" height="12" rx="2"/><path d="M5 15V5a2 2 0 0 1 2-2h10"/></svg>',
    trash: '<svg viewBox="0 0 24 24"><path d="M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14"/></svg>',
    refresh: '<svg viewBox="0 0 24 24"><path d="M21 12a9 9 0 1 1-3-6.7L21 8"/><path d="M21 3v5h-5"/></svg>',
    clock: '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>',
    users: '<svg viewBox="0 0 24 24"><circle cx="9" cy="8" r="4"/><path d="M2 21a7 7 0 0 1 14 0"/><path d="M16 4a4 4 0 0 1 0 8M22 21a7 7 0 0 0-4-6.3"/></svg>',
    check: '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M8 12l3 3 5-6"/></svg>',
    x: '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M9 9l6 6M15 9l-6 6"/></svg>',
    user: '<svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg>',
};

// ---------------------------------------------------------------------------
// Estado local (as APIs não oferecem listagens)
// ---------------------------------------------------------------------------

const STORAGE_KEY = 'labmanager.console.v1';

const emptyState = () => ({ users: [], labs: [], reservations: [], items: [], movements: [], balances: {}, activeUser: '' });

const state = (() => {
    try {
        return { ...emptyState(), ...JSON.parse(localStorage.getItem(STORAGE_KEY)) };
    } catch {
        return emptyState();
    }
})();

const save = () => localStorage.setItem(STORAGE_KEY, JSON.stringify(state));

function upsert(list, entry, key = 'id') {
    const index = list.findIndex(existing => existing[key] === entry[key]);
    if (index >= 0) list[index] = { ...list[index], ...entry };
    else list.unshift(entry);
}

let serviceStatus = {};
let activity = [];
let reservationVariant = 'scheduling-clean';
let movementType = 'IN';

// ---------------------------------------------------------------------------
// Utilitários
// ---------------------------------------------------------------------------

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];

const esc = value => String(value ?? '').replace(/[&<>"']/g, ch => (
    { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]
));

const shortId = id => (id ? `${String(id).slice(0, 8)}…` : '—');
const hhmm = time => (time ? String(time).slice(0, 5) : '—');
const withSeconds = time => (time && time.length === 5 ? `${time}:00` : time);
const pad = n => String(n).padStart(2, '0');
const toLocalInput = date => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;

const fmtDateTime = value => (value
    ? new Date(value).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
    : '—');

const fmtHours = (start, end) => {
    const hours = (new Date(end) - new Date(start)) / 3_600_000;
    return Number.isFinite(hours) ? `${hours.toLocaleString('pt-BR', { maximumFractionDigits: 1 })} h` : '—';
};

const serviceLabel = key => {
    const service = SERVICES.find(s => s.key === key);
    if (!service) return key;
    return service.key.startsWith('scheduling') ? `${service.name} (${service.arch === 'clean' ? 'Clean' : 'Layered'})` : service.name;
};

function toast(title, message = '', kind = 'ok') {
    const element = document.createElement('div');
    element.className = `toast ${kind}`;
    element.innerHTML = `<div><b>${esc(title)}</b>${message ? `<span>${esc(message)}</span>` : ''}</div>`;
    $('#toasts').appendChild(element);
    setTimeout(() => element.remove(), kind === 'err' ? 6500 : 3500);
}

function errorMessage({ status, data }) {
    if (data && typeof data === 'object') {
        if (data.detail) return data.detail;
        if (data.error && data.status === undefined) return data.error;
        if (data.error) return `${data.status} ${data.error}`;
    }
    if (typeof data === 'string' && data.trim()) return data.slice(0, 200);
    return `HTTP ${status}`;
}

async function copy(text) {
    try {
        await navigator.clipboard.writeText(text);
        toast('ID copiado', text);
    } catch {
        toast('Não foi possível copiar', text, 'err');
    }
}

async function withBusy(button, task) {
    if (button) button.disabled = true;
    try {
        return await task();
    } finally {
        if (button) button.disabled = false;
    }
}

// ---------------------------------------------------------------------------
// Acesso às APIs (via proxy do console)
// ---------------------------------------------------------------------------

async function api(service, method, path, body) {
    const startedAt = performance.now();
    const result = { ok: false, status: 0, data: null, duration: 0 };
    try {
        const response = await fetch(`/proxy/${service}${path}`, {
            method,
            headers: body === undefined
                ? { Accept: 'application/json' }
                : { 'Content-Type': 'application/json', Accept: 'application/json' },
            body: body === undefined ? undefined : JSON.stringify(body),
        });
        result.status = response.status;
        result.ok = response.ok;
        const text = await response.text();
        try {
            result.data = text ? JSON.parse(text) : null;
        } catch {
            result.data = text;
        }
    } catch (error) {
        result.data = { error: `Console inacessível: ${error.message}` };
    }
    result.duration = Math.round(performance.now() - startedAt);

    activity.unshift({ id: crypto.randomUUID(), at: new Date(), service, method, path, request: body, ...result });
    activity = activity.slice(0, 200);
    renderActivity();
    return result;
}

async function refreshStatus() {
    try {
        const response = await fetch('/console/services');
        const list = await response.json();
        serviceStatus = Object.fromEntries(list.map(entry => [entry.name, entry]));
    } catch {
        serviceStatus = {};
    }
    renderServices();
    renderTopology();
    renderHealthPill();
}

// ---------------------------------------------------------------------------
// Navegação
// ---------------------------------------------------------------------------

function route() {
    const view = VIEWS[location.hash.slice(1)] ? location.hash.slice(1) : 'overview';
    $$('.view').forEach(section => { section.hidden = section.dataset.view !== view; });
    $$('[data-nav]').forEach(link => link.classList.toggle('active', link.dataset.nav === view));
    const [title, subtitle] = VIEWS[view];
    $('#page-title').textContent = title;
    $('#page-subtitle').textContent = subtitle;
    document.title = `${title} · LabManager`;
}

// ---------------------------------------------------------------------------
// Renderização — visão geral
// ---------------------------------------------------------------------------

function statusBadge(key) {
    const status = serviceStatus[key];
    if (!status) return '<span class="status"><span class="dot"></span>Desconhecido</span>';
    return status.up
        ? `<span class="status up"><span class="dot"></span>Online${status.latencyMs != null ? ` · ${status.latencyMs} ms` : ''}</span>`
        : '<span class="status down"><span class="dot"></span>Offline</span>';
}

function renderServices() {
    $('#service-grid').innerHTML = SERVICES.map(service => {
        const status = serviceStatus[service.key];
        const port = status ? new URL(status.baseUrl).port : service.port;
        const deps = service.deps.length
            ? `Chama ${service.deps.map(serviceLabel).join(' e ')}`
            : 'Sem dependências remotas';
        return `
            <article class="card service" data-go="${service.view}">
                <div class="service-top">
                    <h4>${esc(service.name)}</h4>
                    <span class="badge ${service.arch}">${service.arch === 'clean' ? 'Clean' : 'Layered'}</span>
                </div>
                <p>${esc(service.role)}</p>
                ${statusBadge(service.key)}
                <div class="service-meta"><code>:${esc(port)}</code><span>${esc(deps)}</span></div>
            </article>`;
    }).join('');
}

function renderTopology() {
    $$('#topology .node').forEach(node => {
        const status = serviceStatus[node.dataset.node];
        node.classList.toggle('up', Boolean(status?.up));
        node.classList.toggle('down', Boolean(status && !status.up));
    });
    $$('#topology .edges path').forEach(edge => {
        const live = serviceStatus[edge.dataset.from]?.up && serviceStatus[edge.dataset.to]?.up;
        edge.classList.toggle('live', Boolean(live));
    });
}

function renderHealthPill() {
    const pill = $('#health-pill');
    const values = Object.values(serviceStatus);
    const up = values.filter(entry => entry.up).length;
    pill.classList.remove('all-up', 'partial', 'all-down');
    if (!values.length) {
        pill.classList.add('all-down');
        pill.lastElementChild.textContent = 'Console sem resposta';
        return;
    }
    pill.classList.add(up === values.length ? 'all-up' : up === 0 ? 'all-down' : 'partial');
    pill.lastElementChild.textContent = `${up}/${values.length} serviços online`;
}

function renderStats() {
    const stats = [
        [state.users.length, 'Usuários'],
        [state.labs.length, 'Laboratórios'],
        [state.reservations.length, 'Reservas'],
        [state.items.length, 'Itens de inventário'],
    ];
    $('#local-stats').innerHTML = stats.map(([value, label]) => `<div class="stat"><b>${value}</b><span>${label}</span></div>`).join('');
}

// ---------------------------------------------------------------------------
// Renderização — identity
// ---------------------------------------------------------------------------

const roleBadges = roles => (roles ?? []).map(role => `<span class="badge role">${esc(ROLES[role] ?? role)}</span>`).join(' ');

function renderUsers() {
    $('#users-count').textContent = `${state.users.length} registrado(s)`;
    $('#users-datalist').innerHTML = state.users.map(user => `<option value="${esc(user.userId)}">${esc(user.name)}</option>`).join('');

    $('#active-user').innerHTML = ['<option value="">— nenhum —</option>']
        .concat(state.users.map(user => `<option value="${esc(user.userId)}" ${user.userId === state.activeUser ? 'selected' : ''}>${esc(user.name)} (${esc(user.userId)})</option>`))
        .join('');

    if (!state.users.length) {
        $('#users-list').innerHTML = '<div class="empty"><b>Nenhum usuário ainda</b>Cadastre um usuário ou use “Popular dados de exemplo”.</div>';
        return;
    }
    $('#users-list').innerHTML = `
        <div class="table-wrap"><table>
            <thead><tr><th>Identificador</th><th>Nome</th><th>Papéis</th><th></th></tr></thead>
            <tbody>${state.users.map(user => `
                <tr>
                    <td class="mono">${esc(user.userId)}</td>
                    <td>${esc(user.name)}</td>
                    <td>${roleBadges(user.roles)}</td>
                    <td style="text-align:right;white-space:nowrap">
                        <button class="btn btn-ghost btn-sm" data-act-as="${esc(user.userId)}" ${user.userId === state.activeUser ? 'disabled' : ''}>
                            ${user.userId === state.activeUser ? 'Ativo' : 'Agir como'}
                        </button>
                        <button class="icon-btn" title="Remover do histórico local" data-remove-user="${esc(user.userId)}">${ICONS.trash}</button>
                    </td>
                </tr>`).join('')}
            </tbody>
        </table></div>`;
}

function renderRoleMatrix() {
    const permissions = Object.keys(PERMISSIONS);
    $('#role-matrix').innerHTML = `
        <thead><tr><th>Papel</th>${permissions.map(p => `<th title="${p}">${PERMISSIONS[p]}</th>`).join('')}</tr></thead>
        <tbody>${Object.keys(ROLES).map(role => `
            <tr><td><span class="badge role">${ROLES[role]}</span></td>
            ${permissions.map(p => ROLE_PERMISSIONS[role].includes(p) ? '<td class="check">✓</td>' : '<td class="cross">—</td>').join('')}</tr>`).join('')}
        </tbody>`;
}

function fillRequesterFields(force = false) {
    $$('[data-requester]').forEach(input => {
        if (force || !input.value) input.value = state.activeUser;
    });
}

function setActiveUser(userId) {
    state.activeUser = userId;
    save();
    fillRequesterFields(true);
    renderUsers();
}

// ---------------------------------------------------------------------------
// Renderização — catalog
// ---------------------------------------------------------------------------

const daysStrip = openDays => `<div class="days">${DAYS.map(([day, label]) => `<span class="${(openDays ?? []).includes(day) ? 'on' : ''}" title="${day}">${label[0]}</span>`).join('')}</div>`;

function labTile(lab, actions = true) {
    return `
        <div class="tile">
            <div class="tile-head">
                <h4>${esc(lab.name)}</h4>
                ${actions ? `<button class="icon-btn" title="Remover do histórico local" data-remove-lab="${esc(lab.id)}">${ICONS.trash}</button>` : ''}
            </div>
            <div class="tile-meta">
                <span>${ICONS.users}${esc(lab.capacity)} lugares</span>
                <span>${ICONS.clock}${hhmm(lab.opensAt)} – ${hhmm(lab.closesAt)}</span>
            </div>
            ${daysStrip(lab.openDays)}
            <div class="tile-foot">
                <span class="id-copy mono">${shortId(lab.id)}<button class="icon-btn" title="Copiar ID" data-copy="${esc(lab.id)}">${ICONS.copy}</button></span>
                ${actions ? `<span>
                    <button class="icon-btn" title="Consultar no catálogo" data-check-lab="${esc(lab.id)}">${ICONS.refresh}</button>
                    <button class="btn btn-primary btn-sm" data-reserve-lab="${esc(lab.id)}">Reservar</button>
                </span>` : ''}
            </div>
        </div>`;
}

function renderLabs() {
    $('#labs-count').textContent = `${state.labs.length} registrado(s)`;
    $('#labs-list').innerHTML = state.labs.length
        ? state.labs.map(lab => labTile(lab)).join('')
        : '<div class="empty"><b>Nenhum laboratório ainda</b>Cadastre um laboratório com um usuário técnico ou administrador.</div>';

    const select = $('#reservation-lab');
    const current = select.value;
    select.innerHTML = state.labs.map(lab => `<option value="${esc(lab.id)}">${esc(lab.name)} · ${hhmm(lab.opensAt)}–${hhmm(lab.closesAt)}</option>`).join('')
        + '<option value="__manual">Outro (informar UUID)…</option>';
    if ([...select.options].some(option => option.value === current)) select.value = current;
    $('#manual-lab-field').hidden = select.value !== '__manual';
}

// ---------------------------------------------------------------------------
// Renderização — scheduling
// ---------------------------------------------------------------------------

function suggestSlot() {
    const lab = state.labs.find(entry => entry.id === $('#reservation-lab').value);
    const [openHour, openMinute] = (lab?.opensAt ?? '09:00').split(':').map(Number);
    const [closeHour, closeMinute] = (lab?.closesAt ?? '18:00').split(':').map(Number);
    const openDays = lab?.openDays?.length ? lab.openDays : JS_DAY_TO_ENUM;

    const start = new Date();
    start.setDate(start.getDate() + 2);
    for (let i = 0; i < 7 && !openDays.includes(JS_DAY_TO_ENUM[start.getDay()]); i++) {
        start.setDate(start.getDate() + 1);
    }
    start.setHours(openHour, openMinute, 0, 0);

    const end = new Date(start.getTime() + 2 * 3_600_000);
    const closing = new Date(start);
    closing.setHours(closeHour, closeMinute, 0, 0);

    const form = $('#form-reservation');
    form.start.value = toLocalInput(start);
    form.end.value = toLocalInput(end > closing ? closing : end);
}

function responseCard(title, result) {
    return `
        <div class="response ${result.ok ? 'ok' : 'fail'}">
            <div class="response-head">
                <span>${title}</span>
                <span><span class="${result.ok ? 'code-ok' : 'code-err'}">${result.status || 'ERR'}</span> · ${result.duration} ms</span>
            </div>
            <pre class="json">${esc(JSON.stringify(result.data, null, 2) ?? '')}</pre>
        </div>`;
}

function renderReservations() {
    $('#reservations-count').textContent = `${state.reservations.length} registrada(s)`;
    if (!state.reservations.length) {
        $('#reservations-list').innerHTML = '<div class="empty"><b>Nenhuma reserva ainda</b>Escolha um laboratório e solicite um horário com pelo menos 24 h de antecedência.</div>';
        return;
    }
    $('#reservations-list').innerHTML = `
        <div class="table-wrap"><table>
            <thead><tr><th>Variante</th><th>Laboratório</th><th>Solicitante</th><th>Início</th><th>Fim</th><th>Duração</th><th>Status</th><th>ID</th><th></th></tr></thead>
            <tbody>${state.reservations.map(reservation => `
                <tr>
                    <td><span class="badge ${reservation.variant === 'scheduling-clean' ? 'clean' : 'layered'}">${reservation.variant === 'scheduling-clean' ? 'Clean' : 'Layered'}</span></td>
                    <td>${esc(reservation.labName ?? shortId(reservation.laboratoryId))}</td>
                    <td class="mono">${esc(reservation.requesterId)}</td>
                    <td>${fmtDateTime(reservation.start)}</td>
                    <td>${fmtDateTime(reservation.end)}</td>
                    <td>${fmtHours(reservation.start, reservation.end)}</td>
                    <td><span class="badge ${esc(reservation.status)}">${esc(reservation.status)}</span></td>
                    <td><span class="id-copy mono">${shortId(reservation.id)}<button class="icon-btn" title="Copiar ID" data-copy="${esc(reservation.id)}">${ICONS.copy}</button></span></td>
                    <td><button class="icon-btn" title="Remover do histórico local" data-remove-reservation="${esc(reservation.id)}">${ICONS.trash}</button></td>
                </tr>`).join('')}
            </tbody>
        </table></div>`;
}

// ---------------------------------------------------------------------------
// Renderização — inventory
// ---------------------------------------------------------------------------

function renderItems() {
    $('#items-count').textContent = `${state.items.length} registrado(s)`;

    const select = $('#movement-item');
    const current = select.value;
    select.innerHTML = state.items.length
        ? state.items.map(item => `<option value="${esc(item.id)}">${esc(item.name)} (${esc(item.unit)})</option>`).join('')
        : '<option value="">Cadastre um item primeiro</option>';
    if ([...select.options].some(option => option.value === current)) select.value = current;

    if (!state.items.length) {
        $('#items-list').innerHTML = '<div class="empty"><b>Nenhum item ainda</b>Cadastre um consumível e registre entradas e saídas.</div>';
        return;
    }
    $('#items-list').innerHTML = state.items.map(item => {
        const movements = state.movements.filter(movement => movement.itemId === item.id).slice(0, 6);
        const balance = state.balances[item.id];
        return `
            <div class="tile">
                <div class="tile-head">
                    <h4>${esc(item.name)}</h4>
                    <button class="icon-btn" title="Remover do histórico local" data-remove-item="${esc(item.id)}">${ICONS.trash}</button>
                </div>
                <div class="balance"><b>${balance ?? '—'}</b><span>${esc(item.unit)} em estoque</span></div>
                <div class="movements">${movements.length
                    ? movements.map(movement => `
                        <div class="movement">
                            <span class="${movement.type === 'IN' ? 'in' : 'out'}">${movement.type === 'IN' ? '+' : '−'}${esc(movement.quantity)}</span>
                            <span>${esc(movement.performedBy)} · ${fmtDateTime(movement.occurredAt)}</span>
                        </div>`).join('')
                    : '<span class="muted">Sem movimentações registradas pelo console.</span>'}
                </div>
                <div class="tile-foot">
                    <span class="id-copy mono">${shortId(item.id)}<button class="icon-btn" title="Copiar ID" data-copy="${esc(item.id)}">${ICONS.copy}</button></span>
                    <button class="btn btn-ghost btn-sm" data-refresh-balance="${esc(item.id)}">${ICONS.refresh} Saldo</button>
                </div>
            </div>`;
    }).join('');
}

async function refreshBalance(itemId, { silent = false } = {}) {
    const result = await api('inventory', 'GET', `/api/items/${itemId}/balance`);
    if (result.ok) {
        state.balances[itemId] = result.data.balance;
        save();
        renderItems();
    } else if (!silent) {
        toast('Falha ao consultar saldo', errorMessage(result), 'err');
    }
}

// ---------------------------------------------------------------------------
// Renderização — atividade
// ---------------------------------------------------------------------------

function renderActivity() {
    $('#activity-count').textContent = activity.length;
    if (!activity.length) {
        $('#activity-list').innerHTML = '<div class="empty"><b>Nenhuma requisição ainda</b>As chamadas feitas pelo console aparecem aqui, com corpo e tempo de resposta.</div>';
        return;
    }
    $('#activity-list').innerHTML = `
        <div class="table-wrap"><table>
            <thead><tr><th>Hora</th><th>Serviço</th><th>Método</th><th>Caminho</th><th>Status</th><th>Tempo</th></tr></thead>
            <tbody>${activity.map(entry => `
                <tr class="activity-row" data-activity="${entry.id}">
                    <td class="mono">${entry.at.toLocaleTimeString('pt-BR')}</td>
                    <td>${esc(serviceLabel(entry.service))}</td>
                    <td><span class="method ${entry.method}">${entry.method}</span></td>
                    <td class="mono">${esc(entry.path)}</td>
                    <td class="${entry.ok ? 'code-ok' : 'code-err'}">${entry.status || 'ERR'}</td>
                    <td>${entry.duration} ms</td>
                </tr>
                <tr class="activity-detail" data-detail="${entry.id}" hidden>
                    <td colspan="6"><div class="responses">
                        ${entry.request !== undefined ? `<div class="response"><div class="response-head">Requisição</div><pre class="json">${esc(JSON.stringify(entry.request, null, 2))}</pre></div>` : ''}
                        ${responseCard('Resposta', entry)}
                    </div></td>
                </tr>`).join('')}
            </tbody>
        </table></div>`;
}

function renderAll() {
    renderStats();
    renderUsers();
    renderLabs();
    renderReservations();
    renderItems();
    fillRequesterFields();
}

// ---------------------------------------------------------------------------
// Ações
// ---------------------------------------------------------------------------

async function registerUser(body) {
    const result = await api('identity', 'POST', '/api/users', body);
    if (result.ok) {
        upsert(state.users, result.data, 'userId');
    } else if (result.status === 422 && /já existe/i.test(errorMessage(result))) {
        upsert(state.users, body, 'userId');
    }
    return result;
}

async function registerLab(body) {
    const result = await api('catalog', 'POST', '/api/laboratories', body);
    if (result.ok) upsert(state.labs, result.data);
    return result;
}

async function registerItem(body) {
    const result = await api('inventory', 'POST', '/api/items', body);
    if (result.ok) {
        upsert(state.items, result.data);
        state.balances[result.data.id] = 0;
    }
    return result;
}

async function registerMovement(itemId, body) {
    const result = await api('inventory', 'POST', `/api/items/${itemId}/movements`, body);
    if (result.ok) state.movements.unshift(result.data);
    return result;
}

async function requestReservation(variant, body) {
    const result = await api(variant, 'POST', '/api/reservations', body);
    if (result.ok) {
        const lab = state.labs.find(entry => entry.id === body.laboratoryId);
        upsert(state.reservations, {
            ...result.data,
            variant,
            laboratoryId: body.laboratoryId,
            labName: lab?.name,
            requesterId: body.requesterId,
        });
    }
    return result;
}

async function seed(button) {
    await withBusy(button, async () => {
        const users = [
            { userId: 'admin-1', name: 'Ana Administradora', roles: ['ADMIN'] },
            { userId: 'tec-1', name: 'Tiago Técnico', roles: ['TECHNICIAN'] },
            { userId: 'prof-1', name: 'Paula Professora', roles: ['PROFESSOR'] },
            { userId: 'aluno-1', name: 'Lucas Aluno', roles: ['STUDENT'] },
        ];
        for (const user of users) {
            const result = await registerUser(user);
            if (!result.ok && !state.users.some(entry => entry.userId === user.userId)) {
                toast('Falha ao criar usuários', errorMessage(result), 'err');
                return;
            }
        }
        if (!state.activeUser) state.activeUser = 'tec-1';

        const weekdays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
        const labs = [
            { requesterId: 'tec-1', name: 'Laboratório de Redes', capacity: 30, opensAt: '07:30:00', closesAt: '22:00:00', openDays: weekdays },
            { requesterId: 'tec-1', name: 'Laboratório de Eletrônica', capacity: 20, opensAt: '08:00:00', closesAt: '18:00:00', openDays: [...weekdays, 'SATURDAY'] },
        ];
        const labResults = await Promise.all(labs.map(registerLab));
        const labFailure = labResults.find(result => !result.ok);

        const items = [
            { requesterId: 'tec-1', name: 'Resistor 1kΩ', unit: 'un', initial: 200 },
            { requesterId: 'tec-1', name: 'Álcool isopropílico', unit: 'L', initial: 5 },
        ];
        let itemFailure;
        for (const { initial, ...item } of items) {
            const result = await registerItem(item);
            if (!result.ok) {
                itemFailure = result;
                continue;
            }
            await registerMovement(result.data.id, { requesterId: 'tec-1', type: 'IN', quantity: initial });
            await refreshBalance(result.data.id, { silent: true });
        }

        save();
        renderAll();
        fillRequesterFields(true);

        if (labFailure) toast('Catálogo não populado', errorMessage(labFailure), 'err');
        if (itemFailure) toast('Inventário não populado', errorMessage(itemFailure), 'err');
        if (!labFailure && !itemFailure) toast('Dados de exemplo criados', '4 usuários, 2 laboratórios e 2 itens com estoque inicial.');
    });
}

// ---------------------------------------------------------------------------
// Eventos
// ---------------------------------------------------------------------------

function bindForms() {
    $('#form-user').addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const roles = $$('input[name="roles"]:checked', form).map(input => input.value);
        if (!roles.length) {
            toast('Selecione ao menos um papel', '', 'err');
            return;
        }
        const body = { userId: form.userId.value.trim(), name: form.name.value.trim(), roles };
        await withBusy(event.submitter, async () => {
            const result = await api('identity', 'POST', '/api/users', body);
            if (!result.ok) {
                toast('Usuário não cadastrado', errorMessage(result), 'err');
                return;
            }
            upsert(state.users, result.data, 'userId');
            if (!state.activeUser) state.activeUser = result.data.userId;
            save();
            renderAll();
            form.reset();
            toast('Usuário cadastrado', `${result.data.name} (${result.data.userId})`);
        });
    });

    $('#form-permission').addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const panel = $('#permission-result');
        await withBusy(event.submitter, async () => {
            const result = await api('identity', 'POST', '/api/permissions/check', {
                userId: form.userId.value.trim(),
                permission: form.permission.value,
            });
            panel.className = 'permission-result';
            if (!result.ok) {
                panel.innerHTML = `<span class="muted">${esc(errorMessage(result))}</span>`;
                return;
            }
            const allowed = result.data.allowed;
            panel.classList.add(allowed ? 'allowed' : 'denied');
            panel.innerHTML = `${allowed ? ICONS.check : ICONS.x}<span>${esc(result.data.userId)} ${allowed ? 'pode' : 'não pode'} <b>${esc(PERMISSIONS[result.data.permission] ?? result.data.permission)}</b></span>`;
        });
    });

    $('#form-lab').addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const openDays = $$('input[name="openDays"]:checked', form).map(input => input.value);
        if (!openDays.length) {
            toast('Selecione ao menos um dia', '', 'err');
            return;
        }
        await withBusy(event.submitter, async () => {
            const result = await registerLab({
                requesterId: form.requesterId.value.trim(),
                name: form.name.value.trim(),
                capacity: Number(form.capacity.value),
                opensAt: withSeconds(form.opensAt.value),
                closesAt: withSeconds(form.closesAt.value),
                openDays,
            });
            if (!result.ok) {
                toast('Laboratório não cadastrado', errorMessage(result), 'err');
                return;
            }
            save();
            renderAll();
            form.name.value = '';
            toast('Laboratório cadastrado', result.data.name);
        });
    });

    $('#form-lab-lookup').addEventListener('submit', async event => {
        event.preventDefault();
        const id = event.currentTarget.id.value.trim();
        const output = $('#lab-lookup-result');
        await withBusy(event.submitter, async () => {
            const result = await api('catalog', 'GET', `/api/laboratories/${encodeURIComponent(id)}`);
            if (!result.ok) {
                output.innerHTML = `<div class="responses">${responseCard('Resposta', result)}</div>`;
                return;
            }
            upsert(state.labs, result.data);
            save();
            renderAll();
            output.innerHTML = labTile(result.data, false);
        });
    });

    $('#form-reservation').addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const laboratoryId = form.laboratoryId.value === '__manual' ? form.manualLabId.value.trim() : form.laboratoryId.value;
        if (!laboratoryId) {
            toast('Informe o laboratório', '', 'err');
            return;
        }
        const body = {
            laboratoryId,
            requesterId: form.requesterId.value.trim(),
            start: `${form.start.value}:00`,
            end: `${form.end.value}:00`,
        };
        const variants = reservationVariant === 'both' ? ['scheduling-clean', 'scheduling-layered'] : [reservationVariant];
        await withBusy(event.submitter, async () => {
            const results = await Promise.all(variants.map(variant => requestReservation(variant, body)));
            save();
            renderAll();
            $('#reservation-result').innerHTML = `<div class="responses">${results.map((result, index) => responseCard(
                `<span class="badge ${variants[index] === 'scheduling-clean' ? 'clean' : 'layered'}">${variants[index] === 'scheduling-clean' ? 'Clean' : 'Layered'}</span>`,
                result,
            )).join('')}</div>`;
            if (variants.length === 1) {
                const [result] = results;
                result.ok
                    ? toast('Reserva solicitada', `Status ${result.data.status}`)
                    : toast('Reserva recusada', errorMessage(result), 'err');
            }
        });
    });

    $('#form-item').addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        await withBusy(event.submitter, async () => {
            const result = await registerItem({
                requesterId: form.requesterId.value.trim(),
                name: form.name.value.trim(),
                unit: form.unit.value.trim(),
            });
            if (!result.ok) {
                toast('Item não cadastrado', errorMessage(result), 'err');
                return;
            }
            save();
            renderAll();
            $('#movement-item').value = result.data.id;
            form.name.value = '';
            form.unit.value = '';
            toast('Item cadastrado', result.data.name);
        });
    });

    $('#form-movement').addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const itemId = form.itemId.value;
        if (!itemId) {
            toast('Cadastre um item primeiro', '', 'err');
            return;
        }
        await withBusy(event.submitter, async () => {
            const result = await registerMovement(itemId, {
                requesterId: form.requesterId.value.trim(),
                type: movementType,
                quantity: Number(form.quantity.value),
            });
            if (!result.ok) {
                toast('Movimentação recusada', errorMessage(result), 'err');
                return;
            }
            save();
            await refreshBalance(itemId);
            toast(movementType === 'IN' ? 'Entrada registrada' : 'Saída registrada', `${result.data.quantity} unidade(s)`);
        });
    });
}

function bindClicks() {
    document.addEventListener('click', async event => {
        const target = event.target.closest('button, [data-go], .activity-row');
        if (!target) return;
        const data = target.dataset;

        if (data.go) location.hash = data.go;
        else if (data.copy) copy(data.copy);
        else if (data.actAs) setActiveUser(data.actAs);
        else if (data.removeUser) {
            state.users = state.users.filter(user => user.userId !== data.removeUser);
            if (state.activeUser === data.removeUser) state.activeUser = '';
            save(); renderAll();
        } else if (data.removeLab) {
            state.labs = state.labs.filter(lab => lab.id !== data.removeLab);
            save(); renderAll();
        } else if (data.removeReservation) {
            state.reservations = state.reservations.filter(reservation => reservation.id !== data.removeReservation);
            save(); renderAll();
        } else if (data.removeItem) {
            state.items = state.items.filter(item => item.id !== data.removeItem);
            state.movements = state.movements.filter(movement => movement.itemId !== data.removeItem);
            delete state.balances[data.removeItem];
            save(); renderAll();
        } else if (data.checkLab) {
            const result = await api('catalog', 'GET', `/api/laboratories/${data.checkLab}`);
            if (result.ok) {
                upsert(state.labs, result.data);
                save(); renderAll();
                toast('Laboratório confirmado no catálogo', result.data.name);
            } else {
                toast('Laboratório não encontrado no catálogo', errorMessage(result), 'err');
            }
        } else if (data.reserveLab) {
            location.hash = 'scheduling';
            $('#reservation-lab').value = data.reserveLab;
            $('#manual-lab-field').hidden = true;
            suggestSlot();
        } else if (data.refreshBalance) {
            await refreshBalance(data.refreshBalance);
        } else if (data.variant) {
            reservationVariant = data.variant;
            $$('#variant-switch button').forEach(button => button.classList.toggle('active', button === target));
        } else if (data.type) {
            movementType = data.type;
            $$('#movement-type button').forEach(button => button.classList.toggle('active', button === target));
        } else if (data.activity) {
            const detail = $(`[data-detail="${data.activity}"]`);
            if (detail) detail.hidden = !detail.hidden;
        }
    });

    $('#refresh-status').addEventListener('click', refreshStatus);
    $('#seed-data').addEventListener('click', event => seed(event.currentTarget));
    $('#clear-local').addEventListener('click', () => {
        if (!confirm('Apagar o histórico local de usuários, laboratórios, reservas e itens? Os dados nos serviços não são afetados.')) return;
        Object.assign(state, emptyState());
        save();
        renderAll();
        fillRequesterFields(true);
        toast('Histórico local apagado');
    });
    $('#clear-activity').addEventListener('click', () => {
        activity = [];
        renderActivity();
    });
    $('#active-user').addEventListener('change', event => setActiveUser(event.target.value));
    $('#reservation-lab').addEventListener('change', event => {
        $('#manual-lab-field').hidden = event.target.value !== '__manual';
        suggestSlot();
    });
}

// ---------------------------------------------------------------------------
// Inicialização
// ---------------------------------------------------------------------------

function buildStaticControls() {
    $('#role-chips').innerHTML = Object.entries(ROLES)
        .map(([role, label]) => `<label class="chip"><input type="checkbox" name="roles" value="${role}" ${role === 'STUDENT' ? 'checked' : ''}><span>${label}</span></label>`)
        .join('');
    $('#day-chips').innerHTML = DAYS
        .map(([day, label], index) => `<label class="chip"><input type="checkbox" name="openDays" value="${day}" ${index < 5 ? 'checked' : ''}><span>${label}</span></label>`)
        .join('');
    $('#permission-select').innerHTML = Object.entries(PERMISSIONS)
        .map(([permission, label]) => `<option value="${permission}">${label}</option>`)
        .join('');
    renderRoleMatrix();
}

buildStaticControls();
bindForms();
bindClicks();
renderAll();
renderActivity();
renderServices();
suggestSlot();
route();
refreshStatus();

window.addEventListener('hashchange', route);
setInterval(refreshStatus, 15_000);
