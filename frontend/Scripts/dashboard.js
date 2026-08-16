const CONFIG = window.LUMEN_CONFIG || {};
const API_BASE = String(CONFIG.API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const LOCAL_PREVIEW = CONFIG.USE_LOCAL_PREVIEW === true;
const TOKEN_KEY = 'lumen_token';
const USER_KEY = 'lumen_user';
const SIM_KEY = 'lumen_simulacao_dashboard';
let resultadoAtual = null;
let resumoDashboard = null;
let clientesCache = [];
let rankingTipoAtual = 'PAIS';

const $ = id => document.getElementById(id);
const fmt = (n, d = 1) => Number(n || 0).toLocaleString('pt-BR', { minimumFractionDigits: d, maximumFractionDigits: d });
const money = n => Number(n || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

function setFeedback(texto, erro = false) {
  $('formFeedback').textContent = texto || '';
  $('formFeedback').classList.toggle('is-error', erro);
}
function setApiStatus(texto, ok = false) {
  $('apiStatus').textContent = texto;
  $('apiStatus').className = ok ? 'status status--ok' : 'status';
}
function authHeaders() {
  const token = localStorage.getItem(TOKEN_KEY);
  return token ? { Authorization: `Bearer ${token}` } : {};
}
async function api(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...authHeaders(), ...(options.headers || {}) }
  });
  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try { const body = await response.json(); message = body.message || body.detail || message; } catch {}
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

function parseRecs(value) {
  if (Array.isArray(value)) return value;
  if (!value) return [];
  try { const arr = JSON.parse(String(value).replace(/'/g, '"')); return Array.isArray(arr) ? arr : [String(value)]; }
  catch { return [String(value)]; }
}

function perfilKey(categoria) {
  const x = String(categoria || '').toLowerCase();
  if (x.includes('inefic')) return 'ineficiente';
  if (x.includes('moder')) return 'moderado';
  return 'eficiente';
}

function renderAnalise(dados) {
  resultadoAtual = dados;
  $('analysisCard').hidden = false;
  $('analysisSource').textContent = 'Resultado personalizado';
  $('analysisSource').className = 'status status--ok';

  const key = perfilKey(dados.categoria);
  $('profileCard').dataset.profile = key;
  $('categoriaValor').textContent = dados.categoria || '—';
  if (dados.score != null) {
    if (dados.scoreTipo === 'eficiencia') {
      $('probabilidadeValor').textContent = `Índice de eficiência: ${Math.round(dados.score)}/100`;
    } else if (dados.scoreTipo === 'sustentabilidade') {
      $('probabilidadeValor').textContent = `Índice de sustentabilidade: ${Math.round(dados.score)}/100`;
    } else {
      $('probabilidadeValor').textContent = `${Math.round(dados.score)}% de confiança`;
    }
  } else {
    $('probabilidadeValor').textContent = 'Análise concluída';
  }
  if ($('scoreTitulo')) {
    $('scoreTitulo').textContent = dados.scoreTipo === 'eficiencia'
      ? 'Índice de eficiência'
      : dados.scoreTipo === 'sustentabilidade'
        ? 'Índice de sustentabilidade'
        : 'Confiança da análise';
  }
  $('consumoValor').textContent = fmt(dados.consumo, 1);
  $('custoValor').textContent = money(dados.custo);
  $('equipamentosValor').textContent = dados.equipamentos ?? '—';
  $('horasValor').textContent = dados.horas != null ? fmt(dados.horas, 1) : '—';
  $('picoValor').textContent = dados.pico == null ? 'N/D' : (dados.pico ? 'Sim' : 'Não');
  $('picoLegenda').textContent = dados.pico == null ? 'não disponível no cadastro' : 'informado pelo cliente';

  const ul = $('recomendacoesLista');
  ul.innerHTML = '';
  const recs = parseRecs(dados.recomendacoes);
  (recs.length ? recs : ['Nenhuma recomendação retornada.']).forEach(texto => {
    const li = document.createElement('li'); li.textContent = texto; ul.appendChild(li);
  });

  atualizarEconomia();
  renderPicoChart(dados);
  $('analysisCard').scrollIntoView({ behavior: 'smooth', block: 'start' });
}


function mediaPerfil(categoria) {
  if (!resumoDashboard?.consumo_medio_perfil) return null;
  const alvo = resumoDashboard.consumo_medio_perfil.find(x => perfilKey(x.categoria) === perfilKey(categoria));
  return alvo ? Number(alvo.consumo_medio_kwh) : null;
}

function renderGraficosCliente(dados) {
  if (!window.Plotly || !dados) return;
  const section = $('personalChartsSection');
  if (section) section.hidden = false;

  const consumo = Number(dados.consumo || 0);
  const custo = Number(dados.custo || 0);
  const score = dados.score == null ? null : Math.max(0, Math.min(100, Number(dados.score))); 
  const pct = Number($('reducaoRange')?.value || 10);
  const consumoProjetado = consumo * (1 - pct / 100);
  const custoProjetado = custo * (1 - pct / 100);
  const media = mediaPerfil(dados.categoria);
  const corPerfil = perfilKey(dados.categoria) === 'ineficiente' ? '#b9473e' : perfilKey(dados.categoria) === 'moderado' ? '#e0b33d' : '#00A878';
  const baseLayout = {paper_bgcolor:'#fff',plot_bgcolor:'#fff',font:{family:'Lato',color:'#16372f'},margin:{l:55,r:20,t:18,b:55},showlegend:false};

  const nomes = media != null ? ['Seu consumo', `Média ${dados.categoria}`] : ['Seu consumo'];
  const valores = media != null ? [consumo, media] : [consumo];
  Plotly.react('chartClienteComparacao', [{type:'bar',x:nomes,y:valores,marker:{color: media != null ? [corPerfil,'#76D7C4'] : [corPerfil]},text:valores.map(v=>`${fmt(v,0)} kWh`),textposition:'outside',hovertemplate:'%{x}<br>%{y:.1f} kWh<extra></extra>'}], {...baseLayout,yaxis:{title:'kWh/mês',gridcolor:'#e5efeb',rangemode:'tozero'}}, {responsive:true,displaylogo:false});

  Plotly.react('chartClienteProjecao', [{type:'bar',x:['Atual',`Meta -${pct}%`],y:[consumo,consumoProjetado],marker:{color:[corPerfil,'#00A878']},text:[consumo,consumoProjetado].map(v=>`${fmt(v,0)} kWh`),textposition:'outside',hovertemplate:'%{x}<br>%{y:.1f} kWh<extra></extra>'}], {...baseLayout,yaxis:{title:'kWh/mês',gridcolor:'#e5efeb',rangemode:'tozero'}}, {responsive:true,displaylogo:false});

  if (score == null) {
    Plotly.react('chartClienteConfianca', [], {paper_bgcolor:'#fff',plot_bgcolor:'#fff',font:{family:'Lato',color:'#16372f'},margin:{l:25,r:25,t:65,b:30},xaxis:{visible:false},yaxis:{visible:false},annotations:[{text:'O índice será exibido após uma nova análise.',showarrow:false,font:{size:17,color:'#56716a'},x:.5,y:.5,xref:'paper',yref:'paper',align:'center'}]}, {responsive:true,displaylogo:false});
  } else {
    Plotly.react('chartClienteConfianca', [{type:'indicator',mode:'gauge+number',value:score,number:{suffix:'%'},gauge:{axis:{range:[0,100]},bar:{color:corPerfil},steps:[{range:[0,50],color:'#eef4f1'},{range:[50,75],color:'#dceee8'},{range:[75,100],color:'#c9e7dd'}]}}], {paper_bgcolor:'#fff',font:{family:'Lato',color:'#16372f'},margin:{l:35,r:35,t:15,b:20}}, {responsive:true,displaylogo:false});
  }

  Plotly.react('chartClienteCusto', [{type:'bar',x:['Atual',`Meta -${pct}%`],y:[custo,custoProjetado],marker:{color:['#0e4c3c','#00A878']},text:[custo,custoProjetado].map(v=>money(v)),textposition:'outside',hovertemplate:'%{x}<br>R$ %{y:.2f}<extra></extra>'}], {...baseLayout,yaxis:{title:'R$/mês',gridcolor:'#e5efeb',rangemode:'tozero'}}, {responsive:true,displaylogo:false});

  renderHistoricoCliente(dados.historico || []);
}

function renderHistoricoCliente(historico) {
  if (!window.Plotly) return;
  const el = $('chartClienteHistorico');
  const nota = $('historicoNota');
  const itens = Array.isArray(historico) ? historico
    .filter(x => x && x.mesReferencia)
    .sort((a,b)=>String(a.mesReferencia).localeCompare(String(b.mesReferencia))) : [];

  if (!itens.length) {
    Plotly.react(el, [{type:'scatter',x:[],y:[],mode:'lines+markers'}], {paper_bgcolor:'#fff',plot_bgcolor:'#fff',font:{family:'Lato',color:'#16372f'},margin:{l:55,r:20,t:18,b:55},xaxis:{title:'Mês'},yaxis:{title:'kWh',gridcolor:'#e5efeb'},annotations:[{text:'Sem histórico mensal disponível para esta análise',showarrow:false,x:.5,y:.5,xref:'paper',yref:'paper',font:{color:'#687d76'}}]}, {responsive:true,displaylogo:false});
    if (nota) nota.textContent = 'O histórico mensal fica disponível para perfis cadastrados com registros anteriores.';
    return;
  }

  const x = itens.map(i => String(i.mesReferencia).slice(0,7));
  const registrado = itens.map(i => Number(i.consumoRegistradoKwh ?? NaN));
  const previsto = itens.map(i => Number(i.consumoPrevistoKwh ?? i.consumoEstimadoIaKwh ?? NaN));
  Plotly.react(el, [
    {type:'scatter',mode:'lines+markers',name:'Consumo registrado',x,y:registrado,line:{width:3},marker:{size:8},connectgaps:false},
    {type:'scatter',mode:'lines+markers',name:'Consumo previsto/IA',x,y:previsto,line:{width:3,dash:'dot'},marker:{size:7},connectgaps:false}
  ], {paper_bgcolor:'#fff',plot_bgcolor:'#fff',font:{family:'Lato',color:'#16372f'},margin:{l:55,r:20,t:18,b:55},xaxis:{title:'Mês',gridcolor:'#eef4f1'},yaxis:{title:'kWh',gridcolor:'#e5efeb',rangemode:'tozero'},legend:{orientation:'h',y:1.12}}, {responsive:true,displaylogo:false});
  if (nota) nota.textContent = 'Histórico mensal do perfil selecionado.';
}

function atualizarEconomia() {
  if (!resultadoAtual) return;
  const pct = Number($('reducaoRange').value);
  const atual = Number(resultadoAtual.consumo || 0);
  const projetado = atual * (1 - pct / 100);
  const tarifa = atual > 0 ? Number(resultadoAtual.custo || 0) / atual : 0.75;
  $('reducaoValor').textContent = pct;
  $('consumoProjetado').textContent = `${fmt(projetado, 1)} kWh`;
  $('economiaReais').textContent = `${money((atual - projetado) * tarifa)} / mês`;
  renderGraficosCliente(resultadoAtual);
}
$('reducaoRange').addEventListener('input', atualizarEconomia);

function renderPicoChart(dados) {
  if (!window.Plotly) return;
  const el = $('chartPico');
  if (dados.horas == null || dados.pico == null) {
    Plotly.purge(el);
    $('picoNota').textContent = 'O horário de pico não está disponível para este perfil.';
    return;
  }
  $('picoNota').textContent = dados.pico
    ? 'O cliente informou uso relevante em horário de pico.'
    : 'O cliente não informou uso relevante em horário de pico.';
  Plotly.newPlot(el, [{
    x: [Number(dados.horas)],
    y: [Number(dados.consumo)],
    mode: 'markers+text',
    text: [`${fmt(dados.consumo,0)} kWh`],
    textposition: 'top center',
    marker: { size: 24, color: dados.pico ? '#e08a2f' : '#00A878', line: { color: '#0e4c3c', width: 2 } },
    hovertemplate: `Horas de alto consumo: %{x}h<br>Consumo: %{y:.0f} kWh<extra></extra>`
  }], {
    margin: { l: 55, r: 20, t: 15, b: 55 },
    xaxis: { title: 'Horas de alto consumo por dia', rangemode: 'tozero', gridcolor: '#e5efeb' },
    yaxis: { title: 'Consumo mensal (kWh)', rangemode: 'tozero', gridcolor: '#e5efeb' },
    paper_bgcolor: '#fff', plot_bgcolor: '#fff', showlegend: false, font: { family: 'Lato', color: '#16372f' }
  }, { responsive: true, displaylogo: false });
}

async function carregarResumo() {
  const response = await fetch('../Data/dashboard-resumo.json');
  const data = await response.json();
  resumoDashboard = data;
  const k = data.kpis;
  $('kpiClientes').textContent = Number(k.total_clientes).toLocaleString('pt-BR');
  $('kpiConsumoTotal').textContent = fmt(k.consumo_total_kwh, 0);
  $('kpiConsumoMedio').textContent = fmt(k.consumo_medio_kwh, 1);
  $('kpiEquipamentos').textContent = fmt(k.quantidade_media_equipamentos, 1);
  $('kpiHoras').textContent = fmt(k.tempo_medio_diario_horas, 1);
  $('kpiEficientes').textContent = fmt(k.percentual_eficientes, 1);

  const colors = { Eficiente:'#00A878', Moderado:'#76D7C4', Ineficiente:'#003B32' };
  Plotly.newPlot('chartConsumoPerfil', [{
    type:'bar', x:data.consumo_medio_perfil.map(x=>x.categoria), y:data.consumo_medio_perfil.map(x=>x.consumo_medio_kwh),
    marker:{ color:data.consumo_medio_perfil.map(x=>colors[x.categoria] || '#0e4c3c') },
    text:data.consumo_medio_perfil.map(x=>`${fmt(x.consumo_medio_kwh,0)} kWh`), textposition:'outside', hovertemplate:'%{x}<br>%{y:.0f} kWh<extra></extra>'
  }], { margin:{l:55,r:20,t:10,b:50}, yaxis:{title:'kWh',gridcolor:'#e5efeb'}, xaxis:{title:''}, paper_bgcolor:'#fff',plot_bgcolor:'#fff',showlegend:false,font:{family:'Lato',color:'#16372f'} }, {responsive:true,displaylogo:false});

  Plotly.newPlot('chartDistribuicao', [{
    type:'pie', labels:data.perfil_contagem.map(x=>x.categoria), values:data.perfil_contagem.map(x=>x.quantidade), hole:.55,
    marker:{colors:data.perfil_contagem.map(x=>colors[x.categoria] || '#0e4c3c')}, textinfo:'percent+label', hovertemplate:'%{label}: %{value} clientes<extra></extra>'
  }], { margin:{l:15,r:15,t:10,b:20}, paper_bgcolor:'#fff', showlegend:true, legend:{orientation:'h',y:-.08}, font:{family:'Lato',color:'#16372f'} }, {responsive:true,displaylogo:false});
  if (resultadoAtual) renderGraficosCliente(resultadoAtual);
}

function carregarSimulacaoSalva() {
  try {
    const pacote = JSON.parse(sessionStorage.getItem(SIM_KEY) || 'null');
    if (!pacote) return;
    const e = pacote.entrada || {};
    const r = pacote.resultado || {};
    const usaIndiceLocal = Boolean(r.local) && r.indiceEficiencia != null;
    renderAnalise({
      categoria: r.categoria,
      score: usaIndiceLocal
        ? Number(r.indiceEficiencia)
        : (r.probabilidade == null ? null : Number(r.probabilidade) * 100),
      scoreTipo: usaIndiceLocal ? 'eficiencia' : 'confianca',
      consumo: Number(e.consumoKwh || 0),
      custo: Number(r.custoEstimadoMensal || 0),
      equipamentos: Number(e.quantidadeEquipamentos || 0),
      horas: Number(e.horasAltoConsumo || 0),
      pico: Boolean(e.usoHorarioPico),
      recomendacoes: r.recomendacoes || []
    });
  } catch (e) { console.warn('Não foi possível restaurar a simulação.', e); }
}


const RANKING_LABELS = { PAIS:'Brasil', ESTADO:'Estado', CIDADE:'Cidade' };
const RANKING_PREVIEW = [
  {posicao:1,nomeRazaoSocial:'Participante 01',localidade:'Brasil',tipoImovel:'RESIDENCIAL',pontuacao:94,categoriaEficiencia:'EFICIENTE'},
  {posicao:2,nomeRazaoSocial:'Participante 02',localidade:'Brasil',tipoImovel:'COMERCIAL',pontuacao:89,categoriaEficiencia:'EFICIENTE'},
  {posicao:3,nomeRazaoSocial:'Participante 03',localidade:'Brasil',tipoImovel:'RESIDENCIAL',pontuacao:84,categoriaEficiencia:'MODERADO'},
  {posicao:4,nomeRazaoSocial:'Participante 04',localidade:'Brasil',tipoImovel:'INDUSTRIAL',pontuacao:78,categoriaEficiencia:'MODERADO'},
  {posicao:5,nomeRazaoSocial:'Participante 05',localidade:'Brasil',tipoImovel:'RESIDENCIAL',pontuacao:73,categoriaEficiencia:'MODERADO'}
];

function setRankingStatus(texto, tipo='normal') {
  const el = $('rankingStatus');
  if (!el) return;
  el.textContent = texto;
  el.className = tipo === 'ok' ? 'status status--ok' : tipo === 'warning' ? 'status status--warning' : 'status';
}
function categoriaRankingTexto(valor) {
  const x = String(valor || '').replaceAll('_',' ').toLowerCase();
  return x ? x.charAt(0).toUpperCase() + x.slice(1) : 'Sem categoria';
}
function renderRanking(lista) {
  const root = $('rankingList');
  if (!root) return;
  root.innerHTML = '';
  if (!Array.isArray(lista) || !lista.length) {
    root.innerHTML = '<div class="ranking-empty">Ainda não há posições calculadas para este recorte.</div>';
    return;
  }
  lista.slice(0,10).forEach(item => {
    const row = document.createElement('div');
    row.className = `ranking-row ${Number(item.posicao) <= 3 ? 'ranking-row--podium' : ''}`;
    const pos = document.createElement('div');
    pos.className = 'ranking-position';
    pos.textContent = `${item.posicao || '—'}º`;
    const name = document.createElement('div');
    name.className = 'ranking-name';
    const strong = document.createElement('strong');
    strong.textContent = item.nomeRazaoSocial || 'Cliente';
    const sub = document.createElement('span');
    const local = item.localidade || RANKING_LABELS[rankingTipoAtual] || '';
    const categoria = categoriaRankingTexto(item.categoriaEficiencia);
    sub.textContent = `${local} · ${categoria}`;
    name.append(strong, sub);
    const score = document.createElement('div');
    score.className = 'ranking-score';
    const scoreStrong = document.createElement('strong');
    scoreStrong.textContent = item.pontuacao ?? '—';
    const scoreLabel = document.createElement('span');
    scoreLabel.textContent = 'pontos';
    score.append(scoreStrong, scoreLabel);
    row.append(pos, name, score);
    root.appendChild(row);
  });
}
function resetMinhaPosicao(texto='Entre para acompanhar sua posição') {
  if ($('minhaPosicaoValor')) $('minhaPosicaoValor').textContent = '—';
  if ($('minhaPontuacaoValor')) $('minhaPontuacaoValor').textContent = texto;
  if ($('minhaPosicaoDetalhe')) $('minhaPosicaoDetalhe').textContent = 'Cadastre-se para acompanhar sua posição e sua evolução no ranking.';
}
function renderMinhaPosicao(item) {
  if (!item) return resetMinhaPosicao('Posição ainda não calculada');
  $('minhaPosicaoValor').textContent = `${item.posicao || '—'}º`;
  $('minhaPontuacaoValor').textContent = `${item.pontuacao ?? '—'} pontos`;
  $('minhaPosicaoDetalhe').textContent = `${item.localidade || RANKING_LABELS[rankingTipoAtual]} · ${categoriaRankingTexto(item.categoriaEficiencia)}`;
}
async function carregarMinhaPosicao() {
  if (LOCAL_PREVIEW) return resetMinhaPosicao('Entre para acompanhar sua posição');
  if (!localStorage.getItem(TOKEN_KEY)) return resetMinhaPosicao();
  const clienteId = Number($('clienteSelect')?.value || 0);
  if (!clienteId) return resetMinhaPosicao('Selecione um cliente');
  try {
    const item = await api(`/rankings/clientes/${clienteId}/posicao?tipo=${encodeURIComponent(rankingTipoAtual)}`);
    renderMinhaPosicao(item);
  } catch (e) {
    resetMinhaPosicao('Posição ainda não calculada');
    if ($('minhaPosicaoDetalhe')) $('minhaPosicaoDetalhe').textContent = 'Sua posição ainda não está disponível para este recorte.';
  }
}
async function carregarRanking(tipo='PAIS') {
  rankingTipoAtual = tipo;
  document.querySelectorAll('[data-ranking-type]').forEach(btn => btn.classList.toggle('is-active', btn.dataset.rankingType === tipo));
  if ($('rankingScopeLabel')) $('rankingScopeLabel').textContent = RANKING_LABELS[tipo] || tipo;
  if (LOCAL_PREVIEW) {
    const preview = RANKING_PREVIEW.map((item, idx) => ({...item, localidade: tipo === 'PAIS' ? 'Brasil' : tipo === 'ESTADO' ? 'RJ' : 'Maricá', posicao:idx+1}));
    renderRanking(preview);
    setRankingStatus('Ranking de eficiência', 'ok');
    resetMinhaPosicao('Entre para acompanhar sua posição');
    return;
  }
  setRankingStatus('Carregando ranking');
  try {
    const lista = await api(`/rankings/top10?tipo=${encodeURIComponent(tipo)}`);
    renderRanking(lista);
    setRankingStatus('Ranking atualizado', 'ok');
    await carregarMinhaPosicao();
  } catch (e) {
    renderRanking([]);
    setRankingStatus('Ranking indisponível');
  }
}

document.querySelectorAll('[data-ranking-type]').forEach(btn => {
  btn.addEventListener('click', () => carregarRanking(btn.dataset.rankingType));
});
$('clienteSelect')?.addEventListener('change', carregarMinhaPosicao);

async function login(email, senha) {
  const dados = await api('/auth/login', { method:'POST', body:JSON.stringify({email,senha}) });
  localStorage.setItem(TOKEN_KEY, dados.token);
  localStorage.setItem(USER_KEY, JSON.stringify({id:dados.id,nome:dados.nome,email:dados.email}));
}
async function carregarClientes() {
  clientesCache = await api('/clientes');
  const select = $('clienteSelect'); select.innerHTML='';
  clientesCache.forEach(c=>{ const o=document.createElement('option'); o.value=c.id; o.textContent=`${c.nomeRazaoSocial} · ${c.tipoPessoa} · ${c.tipoImovel}`; select.appendChild(o); });
  if (!clientesCache.length) setFeedback('Seu usuário ainda não possui cliente cadastrado.', true);
  else carregarMinhaPosicao();
}
function atualizarAuthUI() {
  const token = localStorage.getItem(TOKEN_KEY);
  let user = null; try { user = JSON.parse(localStorage.getItem(USER_KEY) || 'null'); } catch {}
  $('loginArea').hidden = Boolean(token);
  $('clienteArea').hidden = !token;
  if (token) {
    $('usuarioLogado').textContent = `Conectado como ${user?.nome || user?.email || 'usuário'}.`;
    setApiStatus('Autenticado', true);
    carregarClientes().catch(e=>{ setApiStatus('Não foi possível carregar'); setFeedback('Não foi possível carregar seus perfis agora.', true); });
  } else if (LOCAL_PREVIEW) {
    setApiStatus('Acesso personalizado');
    setFeedback('');
  } else {
    setApiStatus('Conta LU|MEN');
  }
}
$('loginForm').addEventListener('submit', async e=>{ e.preventDefault(); setFeedback('Entrando...'); try { await login($('loginEmail').value,$('loginSenha').value); setFeedback('Login realizado.'); atualizarAuthUI(); } catch(err){ setFeedback(`Falha no login: ${err.message}`,true); } });
$('sairBtn').addEventListener('click',()=>{ localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); clientesCache=[]; atualizarAuthUI(); resetMinhaPosicao(); setFeedback('Sessão encerrada.'); });
$('analisarClienteBtn').addEventListener('click', async()=>{
  const id = Number($('clienteSelect').value); if(!id) return setFeedback('Selecione um cliente.',true);
  setFeedback('Atualizando sua análise...');
  try {
    const raw = await api(`/clientes/${id}/analise-energetica`, {method:'POST'});
    let historico = [];
    try { historico = await api(`/clientes/${id}/consumos`); } catch (historicoErro) { console.warn('Histórico mensal indisponível:', historicoErro); }
    const cliente = clientesCache.find(c=>Number(c.id)===id);
    const equipamentos = Array.isArray(cliente?.equipamentos) ? cliente.equipamentos : [];
    const qtd = equipamentos.reduce((s,e)=>s+Number(e.quantidade||0),0);
    const horas = equipamentos.length ? equipamentos.reduce((s,e)=>s+Number(e.horasUsoDiario||0),0)/equipamentos.length : null;
    renderAnalise({
      categoria:raw.categoriaIA || raw.categoria,
      score:Number(raw.scoreSustentabilidade ?? (Number(raw.probabilidade||0)*100)),
      scoreTipo:'sustentabilidade',
      consumo:Number(raw.consumoPrevistoLocalKwh ?? raw.consumo_estimado_kwh ?? 0),
      custo:Number(raw.custoPrevistoLocal ?? raw.custo_estimado_mensal ?? 0),
      equipamentos:qtd || null, horas, pico:null,
      recomendacoes:raw.dicasMelhoria || raw.recomendacoes || [],
      historico
    });
    setFeedback('Análise atualizada com sucesso.');
    carregarMinhaPosicao();
  } catch(err){ setFeedback(`Não foi possível gerar a análise: ${err.message}`,true); }
});

carregarResumo().catch(e=>console.error('Falha ao carregar indicadores do dashboard:',e));
carregarSimulacaoSalva();
carregarRanking('PAIS');
atualizarAuthUI();
