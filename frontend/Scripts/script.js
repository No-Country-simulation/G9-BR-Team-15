const CONFIG = window.LUMEN_CONFIG || {};
const API_BASE = String(CONFIG.API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const LOCAL_PREVIEW = CONFIG.USE_LOCAL_PREVIEW === true;
const ALLOW_LOCAL_FALLBACK = CONFIG.ALLOW_LOCAL_FALLBACK !== false;
const TOKEN_KEY = 'lumen_token';
const USER_KEY = 'lumen_user';
const SIM_KEY = 'lumen_simulacao_dashboard';

function normalizarTipoImovel(valor) {
  const x = String(valor || '').toLowerCase();
  if (x.includes('industrial')) return 'Industrial';
  if (x.includes('comercial')) return 'Comercial';
  return 'Residencial';
}


function limitar(valor, minimo, maximo) {
  return Math.max(minimo, Math.min(maximo, valor));
}

function calcularIndiceLocal(dados) {
  // Métrica de apresentação para manter o front responsivo antes da API pública.
  // Não substitui a inferência do modelo treinado na entrega integrada.
  const referencias = {
    Residencial: { consumo: 350, equipamentos: 12 },
    Comercial: { consumo: 1200, equipamentos: 30 },
    Industrial: { consumo: 3500, equipamentos: 60 }
  };
  const ref = referencias[dados.tipoImovel] || referencias.Residencial;
  const relacaoConsumo = dados.consumo / ref.consumo;
  const penalidadeConsumo = limitar((relacaoConsumo - 0.45) * 35, 0, 45);
  const penalidadeEquipamentos = limitar(Math.max(0, dados.equipamentos - ref.equipamentos) * 0.7, 0, 20);
  const penalidadeHoras = limitar(Math.max(0, dados.horas - 6) * 1.6, 0, 28);
  const penalidadePico = dados.pico ? 7 : 0;

  let indice = Math.round(100 - penalidadeConsumo - penalidadeEquipamentos - penalidadeHoras - penalidadePico);

  // Situações muito intensas nunca devem aparecer como eficientes na prévia visual.
  if (dados.horas >= 20 && dados.pico) indice = Math.min(indice, 49);
  if (relacaoConsumo >= 2.2) indice = Math.min(indice, 44);
  if (dados.equipamentos >= ref.equipamentos * 2.5) indice = Math.min(indice, 48);

  return limitar(indice, 18, 96);
}

function respostaLocal(dados) {
  const indice = calcularIndiceLocal(dados);
  const categoria = indice >= 75 ? 'Eficiente' : indice >= 50 ? 'Moderado' : 'Ineficiente';
  const referencias = {
    Residencial: { consumo: 350, equipamentos: 12 },
    Comercial: { consumo: 1200, equipamentos: 30 },
    Industrial: { consumo: 3500, equipamentos: 60 }
  };
  const ref = referencias[dados.tipoImovel] || referencias.Residencial;

  const mensagens = {
    Eficiente: 'Seu consumo apresenta um bom equilíbrio para o perfil informado. Continue acompanhando os hábitos de uso.',
    Moderado: 'Seu consumo está em uma faixa intermediária e há oportunidades práticas de economia.',
    Ineficiente: 'Seu padrão de uso indica potencial relevante de economia e merece atenção nos pontos de maior consumo.'
  };

  const recomendacoes = [mensagens[categoria]];
  if (dados.consumo > ref.consumo * 1.15) {
    recomendacoes.push('Priorize a identificação dos equipamentos de maior potência e concentre as ações de redução neles.');
  }
  if (dados.equipamentos > ref.equipamentos) {
    recomendacoes.push('Revise aparelhos que permanecem ligados sem necessidade e reduza consumos em modo de espera.');
  }
  if (dados.horas >= 10) {
    recomendacoes.push('Reduza ou distribua melhor o tempo diário de uso dos equipamentos de maior consumo.');
  }
  if (dados.pico) {
    recomendacoes.push('Sempre que possível, evite concentrar equipamentos de alta potência entre 17h30 e 20h30.');
  }
  if (dados.tipoImovel === 'Industrial') {
    recomendacoes.push('Mapeie as cargas contínuas e os equipamentos de maior potência para identificar oportunidades de eficiência operacional.');
  }
  if (recomendacoes.length < 3) {
    recomendacoes.push('Acompanhe o consumo mensal para identificar rapidamente mudanças no seu padrão de uso.');
  }

  return {
    categoria,
    probabilidade: null,
    indice_eficiencia: indice,
    recomendacoes: recomendacoes.slice(0, 4),
    custo_estimado_mensal: Number((dados.consumo * 0.75).toFixed(2)),
    _local: true
  };
}

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const body = await response.json();
      message = body.message || body.detail || message;
    } catch {}
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

function abrirModal(id) {
  document.querySelectorAll('.modal.ativo').forEach(modal => {
    modal.classList.remove('ativo');
    modal.setAttribute('aria-hidden', 'true');
  });
  const modal = document.getElementById(id);
  if (modal) {
    modal.classList.add('ativo');
    modal.setAttribute('aria-hidden', 'false');
  }
}

function abrirDashboardModal() {
  abrirModal('modalDashboard');
  document.body.classList.add('dashboard-aberto');
  const frame = document.getElementById('dashboardFrame');
  if (frame) frame.src = `/Pages/dashboard.html?embed=1&t=${Date.now()}`;
}

function salvarSimulacaoNoDashboard(dados, resultado) {
  const pacote = {
    origem: 'simulador',
    criadoEm: new Date().toISOString(),
    entrada: {
      tipoImovel: dados.tipoImovel,
      consumoKwh: dados.consumo,
      quantidadeEquipamentos: dados.equipamentos,
      horasAltoConsumo: dados.horas,
      usoHorarioPico: dados.pico
    },
    resultado: {
      categoria: resultado.categoria,
      probabilidade: resultado.probabilidade == null ? null : Number(resultado.probabilidade),
      recomendacoes: Array.isArray(resultado.recomendacoes) ? resultado.recomendacoes : [],
      custoEstimadoMensal: Number(resultado.custo_estimado_mensal || dados.consumo * 0.75),
      indiceEficiencia: resultado.indice_eficiencia == null ? null : Number(resultado.indice_eficiencia),
      local: Boolean(resultado._local),
      fallback: Boolean(resultado._fallback)
    }
  };

  sessionStorage.setItem(SIM_KEY, JSON.stringify(pacote));
  abrirDashboardModal();
}

document.querySelectorAll('[data-abrir-dashboard]').forEach(link => {
  link.addEventListener('click', (e) => {
    e.preventDefault();
    abrirDashboardModal();
  });
});

document.getElementById('voltarDashboardSimulador')?.addEventListener('click', () => {
  document.body.classList.remove('dashboard-aberto');
  abrirModal('modalSimulador');
});

document.querySelector('#modalSimulador .btn-continuar')?.addEventListener('click', async () => {
  const dados = {
    consumo: Number(document.getElementById('consumo').value) || 0,
    equipamentos: Number(document.getElementById('equipamentos').value) || 0,
    horas: Number(document.getElementById('horas-uso').value) || 0,
    tipoImovel: normalizarTipoImovel(document.getElementById('tipo-imovel').value),
    pico: Boolean(document.getElementById('uso-horario-pico')?.checked)
  };

  if (!document.getElementById('tipo-imovel').value) return alert('Selecione o tipo de imóvel.');
  if (!dados.consumo) return alert('Informe o consumo mensal.');

  const btn = document.querySelector('#modalSimulador .btn-continuar');
  const textoOriginal = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Analisando...';
  const feedback = document.getElementById('simuladorFeedback');
  if (feedback) { feedback.textContent = ''; feedback.classList.remove('is-error'); }

  try {
    let resultado;
    if (LOCAL_PREVIEW) {
      await new Promise(resolve => setTimeout(resolve, 220));
      resultado = respostaLocal(dados);
    } else {
      try {
        resultado = await api('/teste/analise-energetica', {
          method: 'POST',
          body: JSON.stringify({
            consumo_kwh: Math.round(dados.consumo),
            uso_horario_pico: dados.pico,
            quantidade_equipamentos: Math.round(dados.equipamentos),
            tipo_imovel: dados.tipoImovel,
            horas_alto_consumo: Math.round(dados.horas)
          })
        });
      } catch (erroApi) {
        if (!ALLOW_LOCAL_FALLBACK) throw erroApi;
        console.warn('API indisponível; usando estimativa local de continuidade.', erroApi);
        resultado = { ...respostaLocal(dados), _fallback: true };
      }
    }
    if (feedback) feedback.textContent = '';
    salvarSimulacaoNoDashboard(dados, resultado);
  } catch (erro) {
    console.error('Falha ao consultar a IA pelo backend:', erro);
    const feedback = document.getElementById('simuladorFeedback');
    if (feedback) {
      feedback.textContent = 'Não foi possível concluir a análise agora. Tente novamente em instantes.';
      feedback.classList.add('is-error');
    } else {
      alert('Não foi possível concluir a análise agora. Tente novamente.');
    }
  } finally {
    btn.disabled = false;
    btn.textContent = textoOriginal;
  }
});

document.querySelector('#modalLogin .btn-continuar')?.addEventListener('click', async () => {
  const btn = document.querySelector('#modalLogin .btn-continuar');
  const email = document.getElementById('email-login').value.trim();
  const senha = document.getElementById('senha-login').value;
  if (!email || !senha) return alert('Preencha e-mail e senha.');

  const textoOriginal = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Entrando...';
  try {
    const dados = await api('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha })
    });
    localStorage.setItem(TOKEN_KEY, dados.token);
    localStorage.setItem(USER_KEY, JSON.stringify({ id: dados.id, nome: dados.nome, email: dados.email }));
    abrirDashboardModal();
  } catch (erro) {
    alert(`Não foi possível entrar: ${erro.message}`);
  } finally {
    btn.disabled = false;
    btn.textContent = textoOriginal;
  }
});

document.querySelector('#modalCadastro .btn-continuar')?.addEventListener('click', async () => {
  const nome = document.getElementById('nome').value.trim();
  const email = document.getElementById('email-cadastro').value.trim();
  const senha = document.getElementById('senha-cadastro').value;
  if (!nome || !email || !senha) return alert('Preencha nome, e-mail e senha.');

  try {
    await api('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ nome, email, senha })
    });
    alert('Conta criada. Agora entre na plataforma.');
    abrirModal('modalLogin');
  } catch (erro) {
    alert(`Não foi possível cadastrar: ${erro.message}`);
  }
});

document.querySelector('.dashboard-window__close')?.addEventListener('click',()=>document.body.classList.remove('dashboard-aberto'));

// Mantém o scroll da home normal ao fechar o dashboard por X, clique fora ou Esc.
document.getElementById('modalDashboard')?.addEventListener('click', (e) => {
  if (e.target?.id === 'modalDashboard') document.body.classList.remove('dashboard-aberto');
});
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') document.body.classList.remove('dashboard-aberto');
});





window.addEventListener('message', (event) => {
  if (event?.data?.type === 'lumen:voltar-simulador') {
    document.body.classList.remove('dashboard-aberto');
    abrirModal('modalSimulador');
  }
});
