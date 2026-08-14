// TODO: quando o endpoint estiver pronto, trocar calcularSimulacaoMock()
// por um fetch('/api/simulador', { method: 'POST', body: JSON.stringify(dados) })

const PERFIS_CONSUMO = {
    eficiente: {
        emoji: '🟢',
        nome: 'Consumo Eficiente',
        explicacao: 'Parabéns! Seu consumo está dentro de uma faixa considerada eficiente. Manter esses hábitos ajuda a preservar essa economia ao longo do tempo.',
        recomendacoes: [
            '✅ Continue monitorando seu consumo mensalmente.',
            '💡 Aproveite ao máximo a iluminação natural durante o dia.',
            '🔌 Fique atento a novos equipamentos antes de instalá-los.'
        ],
        impactoPercentual: 5
    },
    moderado: {
        emoji: '🟡',
        nome: 'Consumo Moderado',
        explicacao: 'Seu consumo está dentro de uma faixa intermediária. Embora não seja considerado elevado, ainda existem oportunidades para aumentar a eficiência energética da residência e reduzir o valor da sua conta.',
        recomendacoes: [
            '⚡ Priorize eletrodomésticos com maior eficiência energética (Selo Procel A).',
            '💡 Utilize lâmpadas LED e aproveite ao máximo a iluminação natural.',
            '🔌 Evite deixar aparelhos em modo de espera (stand-by).',
            '🏠 Concentre o uso de equipamentos de maior consumo apenas quando necessário.'
        ],
        impactoPercentual: 20
    },
    elevado: {
        emoji: '🔴',
        nome: 'Consumo Elevado',
        explicacao: 'Seu consumo está significativamente acima da média, o que indica boas oportunidades imediatas de economia. Pequenos ajustes de hábito e equipamentos podem gerar um impacto expressivo na sua fatura.',
        recomendacoes: [
            '⚡ Substitua equipamentos antigos por modelos com Selo Procel A.',
            '🕐 Redistribua o uso de equipamentos de alto consumo ao longo do dia.',
            '🔌 Elimine o consumo em stand-by desligando aparelhos na tomada.',
            '🧊 Verifique a vedação e a temperatura de geladeiras e freezers.'
        ],
        impactoPercentual: 35
    }
};

function calcularSimulacaoMock({ consumo, equipamentos }) {

    const PRECO_KWH_PLACEHOLDER = 0.75; 
    const custoEstimado = consumo * PRECO_KWH_PLACEHOLDER;

    let chavePerfil = 'eficiente';
    if (consumo > 500 || equipamentos > 60) chavePerfil = 'elevado';
    else if (consumo > 250 || equipamentos > 30) chavePerfil = 'moderado';

    return {
        custoEstimado,
        perfil: PERFIS_CONSUMO[chavePerfil]
    };
}

document.querySelector('#modalSimulador .btn-continuar').addEventListener('click', () => {

    const dados = {
        tipoImovel: document.getElementById('tipo-imovel').value,
        consumo: Number(document.getElementById('consumo').value) || 0,
        equipamentos: Number(document.getElementById('equipamentos').value),
        horasUso: document.getElementById('horas-uso').value
    };

    const { custoEstimado, perfil } = calcularSimulacaoMock(dados);

    document.getElementById('resultadoEmoji')?.replaceChildren(document.createTextNode(perfil.emoji));
    document.getElementById('resultadoNivel').textContent = perfil.nome;
    document.getElementById('resultadoCusto').textContent =
        custoEstimado.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    document.getElementById('resultadoExplicacao').textContent = perfil.explicacao;

    const lista = document.getElementById('resultadoRecomendacoes');
    lista.innerHTML = '';
    perfil.recomendacoes.forEach((texto) => {
        const li = document.createElement('li');
        li.textContent = texto;
        lista.appendChild(li);
    });

    document.getElementById('resultadoImpacto').textContent =
        `📉 Seguindo essas recomendações, sua economia pode chegar a até ${perfil.impactoPercentual}% no consumo de energia, dependendo dos hábitos de uso e dos equipamentos da residência.`;

    document.querySelectorAll('.modal.ativo').forEach((modalAberto) => {
        modalAberto.classList.remove('ativo');
    });
    document.getElementById('modalResultado').classList.add('ativo');

});

async function gerarCanvasQuadrado() {

    const original = document.getElementById('resultadoCard');
    const corFundo = getComputedStyle(original).backgroundColor || '#0e4c3c';

    // 1. Clona o card fora da tela, sem os limites/rolagem do modal,
    //    e captura ele no tamanho NATURAL (sem nenhum scale via CSS)
    const palco = document.createElement('div');
    Object.assign(palco.style, {
        position: 'fixed', top: '-99999px', left: '-99999px',
        width: '720px' 
    });

    const clone = original.cloneNode(true);
    clone.removeAttribute('id');
    Object.assign(clone.style, { height: 'auto', maxHeight: 'none', overflow: 'visible' });

    palco.appendChild(clone);
    document.body.appendChild(palco);

    await new Promise((r) => requestAnimationFrame(r));

    const canvasOriginal = await html2canvas(clone, {
        backgroundColor: corFundo,
        scale: 2
    });

    document.body.removeChild(palco);

    
    /* script para a geração do badge do consumo de energia */
    const tamanho = 1080; 
    const margem = 0.92;  

    const escala = Math.min(
        (tamanho * margem) / canvasOriginal.width,
        (tamanho * margem) / canvasOriginal.height
    );

    const larguraFinal = canvasOriginal.width * escala;
    const alturaFinal = canvasOriginal.height * escala;
    const offsetX = (tamanho - larguraFinal) / 2;
    const offsetY = (tamanho - alturaFinal) / 2;

    const canvasQuadrado = document.createElement('canvas');
    canvasQuadrado.width = tamanho;
    canvasQuadrado.height = tamanho;

    const ctx = canvasQuadrado.getContext('2d');
    ctx.fillStyle = corFundo;
    ctx.fillRect(0, 0, tamanho, tamanho);
    ctx.drawImage(canvasOriginal, offsetX, offsetY, larguraFinal, alturaFinal);

    return canvasQuadrado;
}

document.getElementById('baixarImagem').addEventListener('click', async () => {
    const canvas = await gerarCanvasQuadrado();
    const link = document.createElement('a');
    link.download = 'diagnostico-energetico-lumen.png';
    link.href = canvas.toDataURL('image/png');
    link.click();
});

document.getElementById('baixarPdf').addEventListener('click', async () => {
    const canvas = await gerarCanvasQuadrado();
    const imagemBase64 = canvas.toDataURL('image/png');
    const { jsPDF } = window.jspdf;
    const pdf = new jsPDF({
        orientation: 'portrait',
        unit: 'px',
        format: [canvas.width, canvas.height]
    });
    pdf.addImage(imagemBase64, 'PNG', 0, 0, canvas.width, canvas.height);
    pdf.save('diagnostico-energetico-lumen.pdf');
});