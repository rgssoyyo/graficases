
// Referencias a elementos del DOM para evitar busquedas repetidas.
const tableBody = document.querySelector("#gpuTable tbody");
const tableEmpty = document.getElementById("tableEmpty");
const filterInput = document.getElementById("filterInput");
const baseSelect = document.getElementById("baseSelect");
const calcBtn = document.getElementById("calcBtn");
const totalCount = document.getElementById("totalCount");
const relativeList = document.getElementById("relativeList");
const relativeMeta = document.getElementById("relativeMeta");
const relativeCount = document.getElementById("relativeCount");
const compareDrop = document.getElementById("compareDrop");
const compareList = document.getElementById("compareList");
const compareMeta = document.getElementById("compareMeta");
const compareCount = document.getElementById("compareCount");
const compareInsights = document.getElementById("compareInsights");

const modeSwitch = document.getElementById("modeSwitch");
const modeButtons = modeSwitch ? modeSwitch.querySelectorAll("[data-mode]") : [];
const cpuCategorySwitch = document.getElementById("cpuCategorySwitch");
const cpuCategoryButtons = cpuCategorySwitch ? cpuCategorySwitch.querySelectorAll("[data-category]") : [];

const pageTitle = document.getElementById("pageTitle");
const pageSubtitle = document.getElementById("pageSubtitle");
const baseReference = document.getElementById("baseReference");
const baseLabel = document.getElementById("baseLabel");
const tableNameHeader = document.getElementById("tableNameHeader");
const compareHint = document.getElementById("compareHint");
const cpuTestingNote = document.getElementById("cpuTestingNote");
const legendPanel = document.getElementById("legendPanel");
const legendModes = document.querySelectorAll(".legend-mode");
const tableActionHeader = document.getElementById("tableActionHeader");

const cpuModal = document.getElementById("cpuModal");
const cpuModalClose = document.getElementById("cpuModalClose");
const cpuModalSubtitle = document.getElementById("cpuModalSubtitle");
const cpuModalMin = document.getElementById("cpuModalMin");
const cpuModalRec = document.getElementById("cpuModalRec");
const cpuModalMax = document.getElementById("cpuModalMax");

const DEFAULT_BASES = {
  gpu: "GeForce RTX 5090",
  cpuGames: "Ryzen 7 9850X3D",
  cpuApps: "Ryzen 7 9800X3D"
};

const CPU_RECO_DEFAULT = {
  min: "2700X",
  rec: "3700X / 11600K",
  max: "5600X / 12400F"
};

const CPU_RECOMMENDATIONS = {
  "GeForce RTX 5090": { min: "7800X3D / 14900K", rec: "9800X3D", max: "9850X3D" },
  "GeForce RTX 4090": { min: "5800X3D / 12700K", rec: "7800X3D / 14900K", max: "9850X3D" },
  "GeForce RTX 5080": { min: "5700X3D / 12700K", rec: "7700X / 13700K", max: "9850X3D" },
  "GeForce RTX 4080 SUPER": { min: "5700X3D / 12700K", rec: "7700X / 13700K", max: "9850X3D" },
  "GeForce RTX 4080": { min: "5700X3D / 12700K", rec: "7700X / 13700K", max: "9850X3D" },
  "Radeon RX 7900 XTX": { min: "5700X3D / 12700K", rec: "7700X / 13700K", max: "9850X3D" },
  "GeForce RTX 5070 Ti": { min: "5700X3D / 12700K", rec: "7800X3D / 14900K", max: "9800X3D" },
  "Radeon RX 9070 XT": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "9700X / 14600K" },
  "Radeon RX 7900 XT": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "9700X / 14600K" },
  "GeForce RTX 3090 Ti": { min: "5800X3D / 12700K", rec: "7700X / 13700K", max: "9850X3D" },
  "GeForce RTX 4070 Ti SUPER": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "9700X / 14600K" },
  "Radeon RX 9070": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "9700X / 14600K" },
  "GeForce RTX 4070 Ti": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "7700X / 13700K" },
  "GeForce RTX 5070": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "7700X / 13700K" },
  "GeForce RTX 3090": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "7700X / 13700K" },
  "GeForce RTX 3080 Ti": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "7700X / 13700K" },
  "GeForce RTX 4070 SUPER": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "7700X / 13700K" },
  "Radeon RX 7900 GRE": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "7700X / 13700K" },
  "Radeon RX 6950 XT": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "7700X / 13700K" },
  "GeForce RTX 4070": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "7700X / 13700K" },
  "Radeon RX 6900 XT": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "7700X / 13700K" },
  "GeForce RTX 3080": { min: "3600 / 11400F", rec: "5600X / 12400F", max: "7600X / 13600K" },
  "Radeon RX 7800 XT": { min: "3600 / 11400F", rec: "5600X / 12400F", max: "7600X / 13600K" },
  "Radeon RX 6800 XT": { min: "3600 / 11400F", rec: "5600X / 12400F", max: "7600X / 13600K" },
  "GeForce RTX 5060 Ti 8 GB": { min: "5600X / 12400F", rec: "7600X / 13600K", max: "9600X3D" },
  "GeForce RTX 3070 Ti": { min: "5500 / 12100F", rec: "5600X / 12400F", max: "5800X3D / 12700K" },
  "GeForce RTX 3060 Ti": { min: "3600 / 11400F", rec: "12600K", max: "9700X / 14600K / 5700X3D" },
  "GeForce RTX 3060 12 GB": { min: "3600 / 11400F", rec: "12600K", max: "9700X / 14600K / 5700X3D" },
  "GeForce GTX 1080 Ti": { min: "2700X", rec: "3700X / 11600K", max: "5600X / 12400F" },
  "GeForce GTX 1060 6 GB": { min: "2700X", rec: "3700X / 11600K", max: "5600X / 12400F" }
};

// Estado en memoria para datos cargados y datos filtrados.
const state = {
  mode: "gpu",
  cpuCategory: "games",
  items: [],
  filtered: [],
  relativeItems: [],
  relativeBase: "",
  compareSelected: [],
  techByName: {},
  msrpByName: {}
};

function getConfig() {
  if (state.mode === "gpu") {
    return {
      labelSingular: "GPU",
      labelPlural: "GPUs",
      tableName: "GPU",
      valueDecimals: 0,
      filterPlaceholder: "Buscar GPU",
      title: "Explorador de GPUs",
      subtitle: "Comparador de rendimiento relativo entre GPUs.",
      baseReference: `Referencia: ${DEFAULT_BASES.gpu} = 100`,
      compareHint: "Arrastra GPUs desde el listado base.",
      compareDrop: "Arrastra aqui las GPUs que deseas comparar.",
      compareEmpty: "Arrastra aqui las GPUs que deseas comparar.",
      compareInsightsEmpty: "Agrega al menos 2 GPUs para comparar tecnologias.",
      relativeMeta: "Selecciona una base y pulsa calcular.",
      defaultBase: DEFAULT_BASES.gpu,
      endpoint: "/gpus",
      relativeEndpoint: (base) => `/gpus/relative/${encodeURIComponent(base)}`
    };
  }

  const isApps = state.cpuCategory === "apps";
  const categoryLabel = isApps ? "Aplicaciones" : "Juegos";
  const baseText = isApps
    ? `Referencia: ${DEFAULT_BASES.cpuApps} = 100 (Apps)`
    : `Referencia: ${DEFAULT_BASES.cpuGames} = 100 (Juegos)`;

  return {
    labelSingular: "CPU",
    labelPlural: "CPUs",
    tableName: "CPU",
    valueDecimals: 1,
    filterPlaceholder: "Buscar CPU",
    title: "Explorador de CPUs",
    subtitle: "Comparador de rendimiento relativo entre CPUs (apps y juegos).",
    baseReference: baseText,
    compareHint: "Arrastra CPUs desde el listado base.",
    compareDrop: "Arrastra aqui las CPUs que deseas comparar.",
    compareEmpty: "Arrastra aqui las CPUs que deseas comparar.",
    compareInsightsEmpty: "Agrega al menos 2 CPUs para comparar rendimiento.",
    relativeMeta: "Selecciona una base y pulsa calcular.",
    defaultBase: isApps ? DEFAULT_BASES.cpuApps : DEFAULT_BASES.cpuGames,
    endpoint: isApps ? "/cpus/apps" : "/cpus/games",
    relativeEndpoint: (base) => `/cpus/${state.cpuCategory}/relative/${encodeURIComponent(base)}`,
    categoryLabel
  };
}

function updateModeButtons() {
  modeButtons.forEach((button) => {
    button.classList.toggle("is-active", button.dataset.mode === state.mode);
  });
}

function updateCategoryButtons() {
  cpuCategoryButtons.forEach((button) => {
    button.classList.toggle("is-active", button.dataset.category === state.cpuCategory);
  });
}

function updateLegendVisibility() {
  legendModes.forEach((mode) => {
    mode.hidden = mode.dataset.mode !== state.mode;
  });
}

function applyModeUI() {
  const config = getConfig();

  if (pageTitle) {
    pageTitle.textContent = config.title;
  }
  if (pageSubtitle) {
    pageSubtitle.textContent = config.subtitle;
  }
  if (baseReference) {
    baseReference.textContent = config.baseReference;
  }
  if (cpuTestingNote) {
    cpuTestingNote.hidden = state.mode !== "cpu";
  }

  filterInput.placeholder = config.filterPlaceholder;
  baseLabel.textContent = `${config.labelSingular} base`;
  tableNameHeader.textContent = config.tableName;
  compareHint.textContent = config.compareHint;
  compareDrop.textContent = config.compareDrop;
  totalCount.textContent = `0 ${config.labelPlural}`;
  compareCount.textContent = `0 ${config.labelPlural}`;
  relativeMeta.textContent = config.relativeMeta;
  relativeCount.textContent = "0 resultados";

  if (cpuCategorySwitch) {
    cpuCategorySwitch.hidden = state.mode !== "cpu";
  }
  if (legendPanel) {
    legendPanel.hidden = state.mode === "cpu";
  }
  if (tableActionHeader) {
    tableActionHeader.hidden = state.mode !== "gpu";
  }

  updateModeButtons();
  updateCategoryButtons();
  updateLegendVisibility();
}

function resetStateForMode() {
  state.items = [];
  state.filtered = [];
  state.relativeItems = [];
  state.relativeBase = "";
  state.compareSelected = [];
  state.techByName = {};
  state.msrpByName = {};
  filterInput.value = "";
  baseSelect.innerHTML = "";
  tableBody.innerHTML = "";
  renderCompareEmpty(getConfig().compareEmpty);
  renderCompareInsights();
}

function getNoDataMessage() {
  return "Sin datos en esta categoria.";
}

// Muestra un mensaje de carga o estado en la tabla.
function setTableLoading(message) {
  tableEmpty.textContent = message;
  tableEmpty.style.display = "flex";
}

// Oculta el overlay de estado en la tabla.
function hideTableEmpty() {
  tableEmpty.style.display = "none";
}

// Actualiza el contador visible de items filtrados.
function updateCount() {
  const config = getConfig();
  totalCount.textContent = `${state.filtered.length} ${config.labelPlural}`;
}

function formatPercent(value) {
  const config = getConfig();
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) {
    return "n/a";
  }
  return `${numeric.toFixed(config.valueDecimals)}%`;
}

function normalizeMsrp(value) {
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) {
    return null;
  }
  const rounded = Math.round(numeric);
  if (rounded <= 0) {
    return null;
  }
  return rounded;
}

function formatMsrpLabel(value) {
  const normalized = normalizeMsrp(value);
  if (normalized == null) {
    return "";
  }
  return `MSRP = $${normalized}`;
}

function getMsrpByName(name) {
  if (!name) {
    return null;
  }
  if (!state.msrpByName) {
    return null;
  }
  return normalizeMsrp(state.msrpByName[name]);
}

function formatNameWithMsrp(name) {
  if (!name) {
    return "";
  }
  if (state.mode !== "gpu") {
    return name;
  }

  const label = formatMsrpLabel(getMsrpByName(name));
  return label ? `${name} ${label}` : name;
}

function formatItemName(item) {
  if (!item) {
    return "";
  }
  const name = item.nombre ? String(item.nombre) : "";
  if (state.mode !== "gpu") {
    return name;
  }

  const label = formatMsrpLabel(item.msrp);
  return label ? `${name} ${label}` : name;
}

// Renderiza filas de la tabla segun la lista recibida.
function renderTable(list) {
  tableBody.innerHTML = "";

  if (!list.length) {
    setTableLoading("Sin resultados");
    return;
  }

  hideTableEmpty();

  // Construir filas en un fragmento para mejor performance.
  const fragment = document.createDocumentFragment();

  list.forEach((item) => {
    const row = document.createElement("tr");
    row.dataset.name = item.nombre;
    row.draggable = true;
    row.title = "Arrastra para comparar o click izquierdo para usar como base";

    const nameCell = document.createElement("td");
    if (state.mode === "gpu") {
      const wrap = document.createElement("div");
      wrap.className = "name-wrap";

      const nameLine = document.createElement("div");
      nameLine.className = "name-line";

      const nameSpan = document.createElement("span");
      nameSpan.className = "item-name";
      nameSpan.textContent = item.nombre;

      nameLine.appendChild(nameSpan);

      const msrpLabel = formatMsrpLabel(item.msrp);
      if (msrpLabel) {
        const msrpSpan = document.createElement("span");
        msrpSpan.className = "msrp-tag";
        msrpSpan.textContent = msrpLabel;
        nameLine.appendChild(msrpSpan);
      }

      const marketRow = document.createElement("div");
      marketRow.className = "market-row";

      const marketSpan = document.createElement("span");
      marketSpan.className = "market-info";
      marketSpan.dataset.marketName = item.nombre;

      const market = item.market;
      marketSpan.textContent = market && market.texto ? market.texto : "sin datos";
      applyMarketClass(marketSpan, market && market.status);

      const refreshBtn = document.createElement("button");
      refreshBtn.type = "button";
      refreshBtn.className = "market-refresh-btn";
      refreshBtn.textContent = "Actualizar precio";
      refreshBtn.dataset.gpu = item.nombre;
      refreshBtn.title = "Buscar precio en internet y guardar en la base de datos";

      marketRow.appendChild(marketSpan);
      marketRow.appendChild(refreshBtn);

      wrap.appendChild(nameLine);
      wrap.appendChild(marketRow);
      nameCell.appendChild(wrap);
    } else {
      nameCell.textContent = item.nombre;
    }

    row.appendChild(nameCell);
    if (state.mode === "gpu") {
      const actionCell = document.createElement("td");
      actionCell.className = "action-cell";

      const button = document.createElement("button");
      button.type = "button";
      button.className = "cpu-reco-btn";
      button.textContent = "CPUs recomendadas";
      button.dataset.gpu = item.nombre;
      button.title = "Ver CPUs recomendadas";

      actionCell.appendChild(button);
      row.appendChild(actionCell);
    }
    fragment.appendChild(row);
  });

  tableBody.appendChild(fragment);
  highlightBase();
}

// Llena el selector de base con opciones.
function populateSelect(items) {
  baseSelect.innerHTML = "";

  items.forEach((item) => {
    const option = document.createElement("option");
    option.value = item.nombre;
    option.textContent = formatItemName(item);
    baseSelect.appendChild(option);
  });
}

// Aplica filtro de texto por nombre (case-insensitive).
function applyFilter() {
  const query = filterInput.value.trim().toLowerCase();
  state.filtered = state.items.filter((item) => item.nombre.toLowerCase().includes(query));
  renderTable(state.filtered);
  updateCount();
}

// Resalta en la tabla la base seleccionada.
function highlightBase() {
  const selectedName = baseSelect.value;
  const rows = tableBody.querySelectorAll("tr");
  rows.forEach((row) => {
    row.classList.toggle("selected", row.dataset.name === selectedName);
  });
}

function applyMarketClass(element, status) {
  if (!element) {
    return;
  }
  element.classList.remove("is-available", "is-unavailable", "is-error", "is-pending");
  if (!status) {
    return;
  }
  const normalized = String(status).toUpperCase();
  if (normalized === "AVAILABLE") {
    element.classList.add("is-available");
  } else if (normalized === "UNAVAILABLE") {
    element.classList.add("is-unavailable");
  } else if (normalized === "ERROR") {
    element.classList.add("is-error");
  }
}

function cssEscape(value) {
  if (window.CSS && typeof window.CSS.escape === "function") {
    return window.CSS.escape(value);
  }
  return String(value).replace(/["\\]/g, "\\$&");
}

function updateMarketInDom(nombre, market) {
  const safeName = cssEscape(nombre);
  const spans = tableBody.querySelectorAll(`.market-info[data-market-name="${safeName}"]`);
  const text = market && market.texto ? String(market.texto) : "sin datos";
  const status = market && market.status ? String(market.status) : "";
  spans.forEach((span) => {
    span.textContent = text;
    applyMarketClass(span, status);
  });
}


function setMarketInState(nombre, market) {
  const item = state.items.find((it) => it.nombre === nombre);
  if (item) {
    item.market = market;
  }
}

const marketRefreshInFlight = new Set();

async function refreshGpuMarket(nombre, button) {
  if (!nombre) {
    return;
  }
  if (marketRefreshInFlight.has(nombre)) {
    return;
  }

  marketRefreshInFlight.add(nombre);
  const originalLabel = button ? button.textContent : "";

  if (button) {
    button.disabled = true;
    button.textContent = "Actualizando...";
  }

  try {
    const response = await fetch(`/gpus/market/${encodeURIComponent(nombre)}/refresh`, { method: "POST" });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const market = await response.json();
    setMarketInState(nombre, market);
    updateMarketInDom(nombre, market);
  } catch (error) {
    const fallback = { texto: "error consultando", status: "ERROR" };
    setMarketInState(nombre, fallback);
    updateMarketInDom(nombre, fallback);
  } finally {
    marketRefreshInFlight.delete(nombre);
    if (button) {
      button.disabled = false;
      button.textContent = originalLabel || "Actualizar precio";
    }
  }
}

function updateCompareMeta() {
  const base = baseSelect.value;
  compareMeta.textContent = base ? `Base actual: ${formatNameWithMsrp(base)}` : "Base actual: -";
}

function renderCompareEmpty(message) {
  compareList.innerHTML = `<div class="empty">${message}</div>`;
  compareCount.textContent = `0 ${getConfig().labelPlural}`;
  updateCompareMeta();
}

function getRelativeItem(name) {
  if (!state.relativeItems.length) {
    return null;
  }

  if (state.relativeBase !== baseSelect.value) {
    return null;
  }

  return state.relativeItems.find((item) => item.name === name) || null;
}

function getRelativeValue(name) {
  const match = getRelativeItem(name);
  return match ? match.value : null;
}

function getRelativeNumber(name) {
  const match = getRelativeItem(name);
  return match ? match.valueNumber : null;
}

function parseSeriesNumber(name, regex) {
  const match = name.match(regex);
  if (!match) {
    return null;
  }
  return parseInt(match[1], 10);
}

function getGpuIdentity(name) {
  const lower = name.toLowerCase();
  let vendor = "other";

  if (lower.includes("arc")) {
    vendor = "intel";
  } else if (lower.includes("radeon") || lower.includes("rx") || lower.includes("r9")) {
    vendor = "amd";
  } else if (lower.includes("geforce") || lower.includes("rtx") || lower.includes("gtx") || lower.includes("gt ")) {
    vendor = "nvidia";
  }

  const rtxNumber = parseSeriesNumber(name, /RTX\s+(\d{3,4})/i);
  const rtxSeries = rtxNumber ? parseInt(String(rtxNumber).slice(0, 2), 10) : null;
  const rxNumber = parseSeriesNumber(name, /RX\s+(\d{4})/i);
  const isArc = /Arc\s+[AB]\d{3}/i.test(name);

  return { vendor, rtxSeries, rxNumber, isArc };
}

function getGpuTechMeta(name) {
  if (state.techByName[name]?.meta) {
    return state.techByName[name].meta;
  }

  const { vendor, rtxSeries, rxNumber, isArc } = getGpuIdentity(name);
  const badges = [];

  if (vendor === "nvidia" && rtxSeries) {
    badges.push({ label: "DLSS 4.5", className: "badge-nvidia" });
    if (rtxSeries < 40) {
      badges.push({ label: "DLSS 4.0 recomendado", className: "badge-nvidia" });
    }
    badges.push({ label: "DLSS SR", className: "badge-nvidia" });
    if (rtxSeries >= 40) {
      badges.push({ label: "DLSS FG", className: "badge-nvidia" });
    }
    if (rtxSeries >= 50) {
      badges.push({ label: "DLSS MFG", className: "badge-nvidia" });
    }
    badges.push({ label: "Ray Recon", className: "badge-nvidia" });
    badges.push({ label: "Reflex", className: "badge-nvidia" });
  } else if (vendor === "nvidia") {
    badges.push({ label: "FSR 1/2/3", className: "badge-nvidia" });
  } else if (vendor === "amd") {
    if (rxNumber && rxNumber >= 9000) {
      badges.push({ label: "FSR 1/2/3", className: "badge-amd" });
      badges.push({ label: "FSR 4 FG", className: "badge-amd" });
      badges.push({ label: "Anti-Lag", className: "badge-amd" });
    } else if (rxNumber && rxNumber >= 6000) {
      badges.push({ label: "FSR 1/2/3", className: "badge-amd" });
      badges.push({ label: "FSR 3 FG", className: "badge-amd" });
      badges.push({ label: "Anti-Lag", className: "badge-amd" });
    } else {
      badges.push({ label: "FSR 1/2", className: "badge-amd" });
      badges.push({ label: "Anti-Lag", className: "badge-amd" });
    }
  } else if (vendor === "intel") {
    badges.push({ label: "XeSS 1/2", className: "badge-intel" });
    if (isArc) {
      badges.push({ label: "XeSS FG", className: "badge-intel" });
      badges.push({ label: "Low Latency", className: "badge-intel" });
    }
  } else {
    badges.push({ label: "FSR 1/2", className: "badge-neutral" });
  }

  const meta = { vendor, badges };
  const existing = state.techByName[name] || {};
  state.techByName[name] = { ...existing, meta };
  return meta;
}

function getGpuTechProfile(name) {
  if (state.techByName[name]?.profile) {
    return state.techByName[name].profile;
  }

  const { vendor, rtxSeries, rxNumber, isArc } = getGpuIdentity(name);

  let upscaler = { label: "FSR 3", rank: 1 };
  if (vendor === "nvidia" && rtxSeries) {
    if (rtxSeries >= 40) {
      upscaler = { label: "DLSS 4.5", rank: 6 };
    } else {
      upscaler = { label: "DLSS 4.0", rank: 5 };
    }
  } else if (vendor === "amd") {
    if (rxNumber && rxNumber >= 9000) {
      upscaler = { label: "FSR 4", rank: 4 };
    } else {
      upscaler = { label: "FSR 3", rank: 1 };
    }
  } else if (vendor === "intel") {
    upscaler = { label: "XeSS", rank: 2 };
  }

  let frameGen = { label: "Sin frame gen", tier: "NONE", rank: 0 };
  if (vendor === "nvidia" && rtxSeries) {
    if (rtxSeries >= 50) {
      frameGen = { label: "DLSS MFG", tier: "MFG", rank: 3 };
    } else if (rtxSeries >= 40) {
      frameGen = { label: "DLSS FG", tier: "DLSS_FG", rank: 2 };
    }
  } else if (vendor === "amd" && rxNumber && rxNumber >= 6000) {
    frameGen = { label: "FSR FG", tier: "FG", rank: 1 };
  } else if (vendor === "intel" && isArc) {
    frameGen = { label: "XeSS FG", tier: "FG", rank: 1 };
  }

  let latency = { label: "Sin low latency", rank: 0 };
  if (vendor === "nvidia") {
    latency = { label: "Reflex", rank: 3 };
  } else if (vendor === "intel") {
    latency = { label: "XeLL", rank: 2 };
  } else if (vendor === "amd") {
    latency = { label: "Anti-Lag", rank: 1 };
  }

  let rayRecon = { label: "Sin RR", rank: 0 };
  if (vendor === "nvidia" && rtxSeries) {
    rayRecon = { label: "Ray Reconstruction", rank: 1 };
  }

  let gameSupport = { label: "FSR", rank: 2 };
  if (upscaler.label.startsWith("DLSS")) {
    gameSupport = { label: "DLSS", rank: 4 };
  } else if (upscaler.label.startsWith("XeSS")) {
    gameSupport = { label: "XeSS", rank: 0 };
  }

  const profile = { upscaler, frameGen, latency, rayRecon, gameSupport };
  if (!state.techByName[name]) {
    state.techByName[name] = { profile };
  } else {
    state.techByName[name].profile = profile;
  }

  return profile;
}

function getRelativeSign(baseRank, otherRank) {
  if (baseRank === otherRank) {
    return "=";
  }

  const delta = Math.abs(baseRank - otherRank);
  let sign = ">";
  if (delta >= 4) {
    sign = ">>>+";
  } else if (delta === 3) {
    sign = ">>>";
  } else if (delta === 2) {
    sign = ">>";
  }

  if (baseRank > otherRank) {
    return sign;
  }

  return sign.replace(/>/g, "<");
}

function getFrameGenSign(baseTier, otherTier) {
  if (baseTier === otherTier) {
    return "=";
  }

  const rank = { MFG: 3, DLSS_FG: 2, FG: 1, NONE: 0 };
  const baseRank = rank[baseTier] ?? 0;
  const otherRank = rank[otherTier] ?? 0;

  const signWhenBetter = (better, worse) => {
    if (better === "MFG" && worse === "NONE") {
      return ">>>+";
    }
    if (worse === "NONE") {
      return ">>";
    }
    if (better === "MFG" && worse === "FG") {
      return ">>";
    }
    return ">";
  };

  if (baseRank > otherRank) {
    return signWhenBetter(baseTier, otherTier);
  }

  return signWhenBetter(otherTier, baseTier).replace(/>/g, "<");
}

function getRayReconSign(baseHas, otherHas) {
  if (baseHas === otherHas) {
    return "=";
  }
  return baseHas ? ">>>+" : "<<<+";
}

function getGpuDisclaimer(name) {
  const { vendor, rtxSeries } = getGpuIdentity(name);
  if (vendor === "nvidia" && rtxSeries && rtxSeries < 40) {
    return "DLSS 4.5 pierde un 20% de rendimiento o mas en esta grafica; mejor quedarse en 4.0.";
  }
  return "";
}

function getPerformanceSign(baseValue, otherValue) {
  if (baseValue == null || otherValue == null) {
    return "=";
  }

  if (baseValue === otherValue) {
    return "=";
  }

  const delta = Math.abs(baseValue - otherValue);
  if (delta < 3) {
    return "=";
  }

  let sign = ">";
  if (delta >= 25) {
    sign = ">>>+";
  } else if (delta >= 15) {
    sign = ">>>";
  } else if (delta >= 8) {
    sign = ">>";
  }

  if (baseValue > otherValue) {
    return sign;
  }

  return sign.replace(/>/g, "<");
}

function renderCompareInsightsGpu() {
  if (!compareInsights) {
    return;
  }

  const base = baseSelect.value;
  if (!base) {
    compareInsights.innerHTML = `<div class="empty">Selecciona una GPU base.</div>`;
    return;
  }

  if (state.compareSelected.length < 2) {
    compareInsights.innerHTML = `<div class="empty">${getConfig().compareInsightsEmpty}</div>`;
    return;
  }

  const baseProfile = getGpuTechProfile(base);
  const others = state.compareSelected.filter((name) => name !== base);

  if (!others.length) {
    compareInsights.innerHTML = `<div class="empty">Selecciona otra GPU distinta a la base.</div>`;
    return;
  }

  compareInsights.innerHTML = "";
  const fragment = document.createDocumentFragment();

  others.forEach((other) => {
    const otherProfile = getGpuTechProfile(other);
    const card = document.createElement("div");
    card.className = "insight-card";

    const title = document.createElement("div");
    title.className = "insight-title";
    title.textContent = `${formatNameWithMsrp(base)} vs ${formatNameWithMsrp(other)}`;

    const lines = document.createElement("div");
    lines.className = "insight-lines";

    const addLine = (label, baseValue, sign, otherValue) => {
      const line = document.createElement("div");
      line.className = "insight-line";

      const labelSpan = document.createElement("span");
      labelSpan.className = "insight-label";
      labelSpan.textContent = `${label}:`;

      const left = document.createElement("span");
      left.textContent = ` ${base}'s ${baseValue} `;

      const signSpan = document.createElement("span");
      signSpan.className = "insight-sign";
      signSpan.textContent = sign;

      const right = document.createElement("span");
      right.textContent = ` ${other}'s ${otherValue}`;

      line.appendChild(labelSpan);
      line.appendChild(left);
      line.appendChild(signSpan);
      line.appendChild(right);
      lines.appendChild(line);
    };

    addLine(
      "Upscaler",
      baseProfile.upscaler.label,
      getRelativeSign(baseProfile.upscaler.rank, otherProfile.upscaler.rank),
      otherProfile.upscaler.label
    );

    addLine(
      "Frame Gen",
      baseProfile.frameGen.label,
      getFrameGenSign(baseProfile.frameGen.tier, otherProfile.frameGen.tier),
      otherProfile.frameGen.label
    );

    addLine(
      "Latencia",
      baseProfile.latency.label,
      getRelativeSign(baseProfile.latency.rank, otherProfile.latency.rank),
      otherProfile.latency.label
    );

    addLine(
      "Ray Reconstruction",
      baseProfile.rayRecon.label,
      getRayReconSign(baseProfile.rayRecon.rank === 1, otherProfile.rayRecon.rank === 1),
      otherProfile.rayRecon.label
    );

    addLine(
      "Soporte de juegos",
      baseProfile.gameSupport.label,
      getRelativeSign(baseProfile.gameSupport.rank, otherProfile.gameSupport.rank),
      otherProfile.gameSupport.label
    );

    card.appendChild(title);
    card.appendChild(lines);
    fragment.appendChild(card);
  });

  compareInsights.appendChild(fragment);
}

function renderCompareInsightsCpu() {
  if (!compareInsights) {
    return;
  }

  const base = baseSelect.value;
  if (!base) {
    compareInsights.innerHTML = `<div class="empty">Selecciona una CPU base.</div>`;
    return;
  }

  if (state.compareSelected.length < 2) {
    compareInsights.innerHTML = `<div class="empty">${getConfig().compareInsightsEmpty}</div>`;
    return;
  }

  if (state.relativeBase !== baseSelect.value || !state.relativeItems.length) {
    compareInsights.innerHTML = `<div class="empty">Pulsa calcular para ver la comparativa.</div>`;
    return;
  }

  const others = state.compareSelected.filter((name) => name !== base);

  if (!others.length) {
    compareInsights.innerHTML = `<div class="empty">Selecciona otra CPU distinta a la base.</div>`;
    return;
  }

  const baseValue = getRelativeNumber(base);
  const baseDisplay = baseValue != null ? formatPercent(baseValue) : "n/a";
  const categoryLabel = state.cpuCategory === "apps" ? "Apps" : "Juegos";

  compareInsights.innerHTML = "";
  const fragment = document.createDocumentFragment();

  others.forEach((other) => {
    const otherValue = getRelativeNumber(other);
    const otherDisplay = otherValue != null ? formatPercent(otherValue) : "n/a";
    const sign = getPerformanceSign(baseValue, otherValue);
    const diff = otherValue != null && baseValue != null ? otherValue - baseValue : null;
    const diffDisplay = diff == null ? "n/a" : `${diff > 0 ? "+" : ""}${diff.toFixed(1)}%`;
    const diffSign = diff == null ? "=" : getPerformanceSign(0, diff);

    const card = document.createElement("div");
    card.className = "insight-card";

    const title = document.createElement("div");
    title.className = "insight-title";
    title.textContent = `${base} vs ${other}`;

    const lines = document.createElement("div");
    lines.className = "insight-lines";

    const addLine = (label, baseValueText, signText, otherValueText) => {
      const line = document.createElement("div");
      line.className = "insight-line";

      const labelSpan = document.createElement("span");
      labelSpan.className = "insight-label";
      labelSpan.textContent = `${label}:`;

      const left = document.createElement("span");
      left.textContent = ` ${baseValueText} `;

      const signSpan = document.createElement("span");
      signSpan.className = "insight-sign";
      signSpan.textContent = signText;

      const right = document.createElement("span");
      right.textContent = ` ${otherValueText}`;

      line.appendChild(labelSpan);
      line.appendChild(left);
      line.appendChild(signSpan);
      line.appendChild(right);
      lines.appendChild(line);
    };

    addLine(`Rendimiento ${categoryLabel}`, baseDisplay, sign, otherDisplay);
    addLine("Diferencia", "0%", diffSign, diffDisplay);

    card.appendChild(title);
    card.appendChild(lines);
    fragment.appendChild(card);
  });

  compareInsights.appendChild(fragment);
}

function renderCompareInsights() {
  if (state.mode === "gpu") {
    renderCompareInsightsGpu();
  } else {
    renderCompareInsightsCpu();
  }
}

// Renderiza las seleccionadas para comparar.
function renderCompareList() {
  updateCompareMeta();
  const config = getConfig();

  if (!state.compareSelected.length) {
    renderCompareEmpty(config.compareEmpty);
    renderCompareInsights();
    return;
  }

  compareList.innerHTML = "";
  const fragment = document.createDocumentFragment();
  const needsRecalc = state.relativeBase !== baseSelect.value || !state.relativeItems.length;

  state.compareSelected.forEach((name) => {
    const item = document.createElement("div");
    item.className = "compare-item";
    item.dataset.name = name;

    const nameEl = document.createElement("div");
    nameEl.className = "name";
    nameEl.textContent = formatNameWithMsrp(name);

    const valueEl = document.createElement("div");
    valueEl.className = "value";
    const value = getRelativeValue(name);
    valueEl.textContent = value || (needsRecalc ? "Pulsa calcular" : "Sin datos");

    const removeBtn = document.createElement("button");
    removeBtn.type = "button";
    removeBtn.className = "compare-remove";
    removeBtn.textContent = "Quitar";
    removeBtn.dataset.remove = name;

    item.appendChild(nameEl);
    item.appendChild(valueEl);
    item.appendChild(removeBtn);

    if (state.mode === "gpu") {
      const meta = getGpuTechMeta(name);
      const metaRow = document.createElement("div");
      metaRow.className = "compare-meta";

      const badgeRow = document.createElement("div");
      badgeRow.className = "badge-row";

      meta.badges.forEach((badge) => {
        const span = document.createElement("span");
        span.className = `badge ${badge.className}`;
        span.textContent = badge.label;
        badgeRow.appendChild(span);
      });

      metaRow.appendChild(badgeRow);

      const disclaimerText = getGpuDisclaimer(name);
      if (disclaimerText) {
        const note = document.createElement("div");
        note.className = "compare-note";
        note.textContent = disclaimerText;
        metaRow.appendChild(note);
      }

      item.appendChild(metaRow);
    } else {
      const metaRow = document.createElement("div");
      metaRow.className = "compare-meta";
      const badge = document.createElement("span");
      badge.className = "badge badge-neutral";
      badge.textContent = state.cpuCategory === "apps" ? "Apps" : "Juegos";
      metaRow.appendChild(badge);
      item.appendChild(metaRow);
    }

    fragment.appendChild(item);
  });

  compareList.appendChild(fragment);
  compareCount.textContent = `${state.compareSelected.length} ${config.labelPlural}`;
  renderCompareInsights();
}

function addToCompare(name) {
  if (!name) {
    return { added: false, baseChanged: false };
  }

  if (state.compareSelected.includes(name)) {
    return { added: false, baseChanged: false };
  }

  const wasEmpty = state.compareSelected.length === 0;
  state.compareSelected.push(name);
  renderCompareList();

  if (wasEmpty) {
    setBase(name, { autoCalculate: true });
    return { added: true, baseChanged: true };
  }

  return { added: true, baseChanged: false };
}

function removeFromCompare(name) {
  state.compareSelected = state.compareSelected.filter((item) => item !== name);
  renderCompareList();
}

function setBase(name, { autoCalculate = false } = {}) {
  if (!name) {
    return;
  }

  if (baseSelect.value !== name) {
    baseSelect.value = name;
  }

  highlightBase();
  renderCompareList();

  if (autoCalculate) {
    calculateRelative();
  }
}

function handleRowDragStart(event) {
  const row = event.target.closest("tr");
  if (!row) {
    return;
  }

  event.dataTransfer.setData("text/plain", row.dataset.name);
  event.dataTransfer.effectAllowed = "copy";
}

function getCpuRecommendationForGpu(gpuName) {
  if (!gpuName) {
    return CPU_RECO_DEFAULT;
  }
  if (CPU_RECOMMENDATIONS[gpuName]) {
    return CPU_RECOMMENDATIONS[gpuName];
  }

  const trimmed = gpuName.replace(/\s+\d+\s*GB$/i, "");
  if (CPU_RECOMMENDATIONS[trimmed]) {
    return CPU_RECOMMENDATIONS[trimmed];
  }

  return CPU_RECO_DEFAULT;
}

function openCpuModal(gpuName) {
  if (!cpuModal) {
    return;
  }
  const data = getCpuRecommendationForGpu(gpuName);
  if (cpuModalSubtitle) {
    cpuModalSubtitle.textContent = gpuName ? formatNameWithMsrp(gpuName) : "-";
  }
  if (cpuModalMin) {
    cpuModalMin.textContent = data.min;
  }
  if (cpuModalRec) {
    cpuModalRec.textContent = data.rec;
  }
  if (cpuModalMax) {
    cpuModalMax.textContent = data.max;
  }
  cpuModal.hidden = false;
}

function closeCpuModal() {
  if (!cpuModal) {
    return;
  }
  cpuModal.hidden = true;
}

function handleCompareDragOver(event) {
  event.preventDefault();
  compareDrop.classList.add("is-over");
  event.dataTransfer.dropEffect = "copy";
}

function handleCompareDragLeave(event) {
  if (!compareDrop.contains(event.relatedTarget)) {
    compareDrop.classList.remove("is-over");
  }
}

function handleCompareDrop(event) {
  event.preventDefault();
  compareDrop.classList.remove("is-over");
  const name = event.dataTransfer.getData("text/plain");
  const result = addToCompare(name);

  if (!result.baseChanged && baseSelect.value && state.relativeBase !== baseSelect.value) {
    calculateRelative();
  }
}

// Muestra estado vacio o mensaje en el panel de relativo.
function renderRelativeEmpty(message) {
  relativeList.innerHTML = `<div class="empty">${message}</div>`;
  relativeCount.textContent = "0 resultados";
}

// Renderiza tarjetas de resultados relativos.
function renderRelative(items) {
  relativeList.innerHTML = "";
  const fragment = document.createDocumentFragment();

  items.forEach((item) => {
    const card = document.createElement("div");
    card.className = "relative-item";

    const name = document.createElement("div");
    name.className = "name";
    name.textContent = formatNameWithMsrp(item.name);

    const value = document.createElement("div");
    value.className = "value";
    value.textContent = item.value;

    card.appendChild(name);
    card.appendChild(value);
    fragment.appendChild(card);
  });

  relativeList.appendChild(fragment);
  relativeCount.textContent = `${items.length} resultados`;
}

// Parsea el arreglo de strings devuelto por /relative.
function parseRelative(lines) {
  if (!Array.isArray(lines)) {
    return { error: "Respuesta invalida" };
  }

  if (lines.length === 1) {
    const single = String(lines[0]);
    const lower = single.toLowerCase();
    const hasDelimiter = single.includes("->") || single.includes("â†’") || single.includes("Ã¢â€ â€™");

    // Backends can return a single-line error message (no delimiter).
    if (!hasDelimiter) {
      if (
        lower.includes("no encontrada") ||
        lower.includes("listado vacio") ||
        lower.includes("no indicada") ||
        lower.includes("sin rendimiento") ||
        lower.includes("invalida")
      ) {
        return { error: single };
      }
    }
  }

  const items = lines.map((line) => {
    const text = String(line);

    // Prefer explicit delimiter parsing instead of prefix-matching item names.
    // Prefix matching breaks for names like "7950X" vs "7950X3D".
    let rawName = text.trim();
    let rawValue = "";

    if (text.includes("->")) {
      const idx = text.indexOf("->");
      rawName = text.slice(0, idx).trim();
      rawValue = text.slice(idx + 2).trim();
    } else if (text.includes("â†’")) {
      const idx = text.indexOf("â†’");
      rawName = text.slice(0, idx).trim();
      rawValue = text.slice(idx + 1).trim();
    } else if (text.includes("Ã¢â€ â€™")) {
      const idx = text.indexOf("Ã¢â€ â€™");
      rawName = text.slice(0, idx).trim();
      rawValue = text.slice(idx + 3).trim();
    } else {
      return { name: text, value: "", valueNumber: null };
    }

    const percent = rawValue.match(/(\d+(?:[\.,]\d+)?)\s*%/);
    const valueNumber = percent ? parseFloat(percent[1].replace(",", ".")) : null;

    const match = state.items.find((item) => item.nombre.toLowerCase() === rawName.toLowerCase());
    const name = match ? match.nombre : rawName;

    return {
      name,
      value: percent ? `${percent[1]}%` : rawValue || "n/a",
      valueNumber
    };
  });

  return { items };
}

// Llama al backend para calcular relativos segun la base.
async function calculateRelative() {
  const base = baseSelect.value;
  if (!base) {
    renderRelativeEmpty(`Selecciona una ${getConfig().labelSingular} base.`);
    return;
  }

  if (!state.items.length) {
    renderRelativeEmpty(getNoDataMessage());
    return;
  }

  const config = getConfig();

  // Estado UI durante la llamada.
  calcBtn.disabled = true;
  relativeMeta.textContent = `Base: ${formatNameWithMsrp(base)}`;
  renderRelativeEmpty("Calculando...");

  try {
    const response = await fetch(config.relativeEndpoint(base));
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const data = await response.json();
    const parsed = parseRelative(data);

    if (parsed.error) {
      state.relativeItems = [];
      state.relativeBase = "";
      renderRelativeEmpty(parsed.error);
      renderCompareList();
      return;
    }

    state.relativeItems = parsed.items;
    state.relativeBase = base;
    renderRelative(parsed.items);
    renderCompareList();
  } catch (error) {
    state.relativeItems = [];
    state.relativeBase = "";
    renderRelativeEmpty("Error al consultar el backend.");
    renderCompareList();
  } finally {
    calcBtn.disabled = false;
  }
}

function pickDefaultBase(items, config) {
  if (config.defaultBase) {
    const match = items.find((item) => item.nombre === config.defaultBase);
    if (match) {
      return match.nombre;
    }
  }
  return items.length ? items[0].nombre : "";
}

// Carga el listado base desde el backend.
async function loadItems() {
  const config = getConfig();
  setTableLoading("Cargando listado...");
  renderRelativeEmpty("Cargando listado...");

  try {
    const response = await fetch(config.endpoint);
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const data = await response.json();
    state.items = data;
    state.filtered = data;
    state.msrpByName = {};

    data.forEach((item) => {
      if (!item || !item.nombre) {
        return;
      }
      const msrp = normalizeMsrp(item.msrp);
      if (msrp != null) {
        state.msrpByName[item.nombre] = msrp;
      }
    });

    populateSelect(state.items);
    renderTable(state.filtered);
    updateCount();

    if (!state.items.length) {
      const emptyMessage = getNoDataMessage();
      setTableLoading(emptyMessage);
      renderRelativeEmpty(emptyMessage);
      renderCompareEmpty(emptyMessage);
      if (compareInsights) {
        compareInsights.innerHTML = `<div class="empty">${emptyMessage}</div>`;
      }
      return;
    }

    const baseName = pickDefaultBase(state.items, config);
    if (baseName) {
      baseSelect.value = baseName;
    }

    highlightBase();
    renderCompareList();
    calculateRelative();
  } catch (error) {
    setTableLoading("No se pudo cargar el listado.");
    renderRelativeEmpty("No se pudo cargar el listado.");
    renderCompareEmpty("No se pudo cargar el listado.");
    if (compareInsights) {
      compareInsights.innerHTML = `<div class="empty">No se pudo cargar el listado.</div>`;
    }
  }
}

function setMode(mode) {
  if (state.mode === mode) {
    return;
  }

  state.mode = mode;
  resetStateForMode();
  applyModeUI();
  loadItems();
}

function setCpuCategory(category) {
  if (state.cpuCategory === category) {
    return;
  }

  state.cpuCategory = category;
  resetStateForMode();
  applyModeUI();
  loadItems();
}

// Eventos del UI.
filterInput.addEventListener("input", applyFilter);
baseSelect.addEventListener("change", () => {
  highlightBase();
  renderCompareList();
});
calcBtn.addEventListener("click", calculateRelative);
tableBody.addEventListener("dragstart", handleRowDragStart);
tableBody.addEventListener("click", (event) => {
  const refreshBtn = event.target.closest(".market-refresh-btn");
  if (refreshBtn) {
    refreshGpuMarket(refreshBtn.dataset.gpu, refreshBtn);
    return;
  }
  const recoButton = event.target.closest(".cpu-reco-btn");
  if (recoButton) {
    openCpuModal(recoButton.dataset.gpu);
    return;
  }
  const row = event.target.closest("tr");
  if (!row) {
    return;
  }
  const name = row.dataset.name;
  if (!name) {
    return;
  }

  setBase(name, { autoCalculate: true });
});

compareList.addEventListener("click", (event) => {
  const removeBtn = event.target.closest(".compare-remove");
  if (!removeBtn) {
    const item = event.target.closest(".compare-item");
    if (!item) {
      return;
    }

    setBase(item.dataset.name, { autoCalculate: true });
    return;
  }

  removeFromCompare(removeBtn.dataset.remove);
});

["dragenter", "dragover"].forEach((eventName) => {
  compareDrop.addEventListener(eventName, handleCompareDragOver);
});

compareDrop.addEventListener("dragleave", handleCompareDragLeave);
compareDrop.addEventListener("drop", handleCompareDrop);
compareList.addEventListener("dragover", handleCompareDragOver);
compareList.addEventListener("drop", handleCompareDrop);

modeButtons.forEach((button) => {
  button.addEventListener("click", () => setMode(button.dataset.mode));
});

cpuCategoryButtons.forEach((button) => {
  button.addEventListener("click", () => setCpuCategory(button.dataset.category));
});

if (cpuModalClose) {
  cpuModalClose.addEventListener("click", closeCpuModal);
}

if (cpuModal) {
  cpuModal.addEventListener("click", (event) => {
    if (event.target && event.target.dataset && event.target.dataset.close) {
      closeCpuModal();
    }
  });
}

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && cpuModal && !cpuModal.hidden) {
    closeCpuModal();
  }
});

// Punto de arranque del frontend.
applyModeUI();
loadItems();
