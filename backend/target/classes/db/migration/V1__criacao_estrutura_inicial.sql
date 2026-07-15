
-- ---------------------------------------------------- 1. TABELAS INDEPENDENTES E DE PERFIL

-- Tabela de Autenticação
CREATE TABLE IF NOT EXISTS Usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Tabela de Perfil/Imóvel
CREATE TABLE IF NOT EXISTS Cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nome_razao_social VARCHAR(255) NOT NULL,
    tipo_pessoa ENUM('PF', 'PJ') NOT NULL,
    tipo_imovel VARCHAR(100) NOT NULL, -- Ex: 'Residencial', 'Padaria', 'Indústria'
    cep VARCHAR(20),
    cidade VARCHAR(100) NOT NULL,
    estado CHAR(2) NOT NULL,
    pais VARCHAR(50) DEFAULT 'Brasil',
    ativo BOOLEAN NOT NULL DEFAULT TRUE, -- Mantém o cliente visível/ativo por padrão
    desativado_em TIMESTAMP NULL,        -- Grava o momento da desativação para o cálculo de deleção futura
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id) ON DELETE CASCADE
    );

-- ---------------------------------------------------------- 2. TABELAS DE EQUIPAMENTOS E CONSUMO

-- Catálogo Único de Equipamentos
CREATE TABLE IF NOT EXISTS Equipamento_Catalogo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(100) NOT NULL, -- Ex: 'Ar Condicionado', 'Geladeira'
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    potencia_watts INT NOT NULL
    );

-- Tabela Associativa (Equipamentos do Cliente)
CREATE TABLE IF NOT EXISTS Cliente_Equipamento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_equipamento INT NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    horas_uso_diario DECIMAL(4,2) NOT NULL,
    dias_uso_mes INT NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES Cliente(id) ON DELETE CASCADE,
    FOREIGN KEY (id_equipamento) REFERENCES Equipamento_Catalogo(id) ON DELETE CASCADE
    );

-- TABELA DE TARIFAS (VIGÊNCIA HISTÓRICA)

CREATE TABLE IF NOT EXISTS Tarifa_Energia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    estado CHAR(2) NOT NULL, -- Se a taxa variar por estado (ex: RJ tem taxa diferente de SP)
    tipo_pessoa ENUM('PF', 'PJ') NOT NULL, -- Se PJ pagar mais barato que PF
    valor_kwh DECIMAL(5,3) NOT NULL, -- Ex: 0.750
    data_inicio_vigencia DATE NOT NULL,
    data_fim_vigencia DATE, -- NULL significa que é a taxa ATUAL/VIGENTE
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Inserindo os dados padrão para o Hackathon
INSERT INTO Tarifa_Energia (estado, tipo_pessoa, valor_kwh, data_inicio_vigencia)
VALUES
    ('RJ', 'PF', 0.750, '2026-01-01'),
    ('RJ', 'PJ', 0.750, '2026-01-01');

-- Histórico de Consumo
CREATE TABLE IF NOT EXISTS Consumo_Mensal (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    mes_referencia DATE NOT NULL, -- Dia 1º de cada mês (ex: '2026-07-01')
    consumo_previsto_kwh DECIMAL(10,2) NOT NULL,
    consumo_registrado_kwh DECIMAL(10,2),
    FOREIGN KEY (id_cliente) REFERENCES Cliente(id) ON DELETE CASCADE
    );

-- ------------------------------------------------------------------- 3. TABELAS DE IA E GAMIFICAÇÃO

-- Resultados do Machine Learning
CREATE TABLE IF NOT EXISTS Avaliacao_Eficiencia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    mes_referencia DATE NOT NULL,
    score_sustentabilidade INT NOT NULL, -- Probabilidade do ML (0.95) * 1000 (ex: 950 pontos)
    categoria_eficiencia VARCHAR(50) NOT NULL, -- Ex: 'eficiente'
    dicas_melhoria JSON, -- Payload flexível com dicas da IA
    FOREIGN KEY (id_cliente) REFERENCES Cliente(id) ON DELETE CASCADE
    );


-- ------------------------------------------------------------------ 4. TABELAS DE ALTA PERFORMANCE (RANKING)

-- Tabela de Cache do Ranking (Leitura rápida para o Frontend)
CREATE TABLE IF NOT EXISTS Ranking_Global (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_ranking ENUM('CIDADE', 'ESTADO', 'NACIONAL') NOT NULL,
    localidade VARCHAR(100) NOT NULL,        -- Ex: 'Maricá', 'RJ', 'Brasil'
    tipo_imovel VARCHAR(100) NOT NULL,  -- Ex: 'Padaria', 'Todos'
    posicao INT NOT NULL,
    id_cliente INT NOT NULL,
    nome_razao_social VARCHAR(255) NOT NULL,
    score_sustentabilidade INT NOT NULL,
    categoria_eficiencia VARCHAR(50) NOT NULL,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_cliente) REFERENCES Cliente(id) ON DELETE CASCADE
    );

-- Tabela de Metadados do Job de Atualização
CREATE TABLE IF NOT EXISTS Ranking_Metadata (
    id INT PRIMARY KEY, -- ID fixo (ex: 1) pois haverá apenas uma linha de controle
    nome_job VARCHAR(50) NOT NULL,
    ultima_atualizacao TIMESTAMP NULL,
    proxima_atualizacao TIMESTAMP NULL,
    status_job VARCHAR(20) NOT NULL -- 'SUCESSO', 'EM_ANDAMENTO', 'FALHA'
    );


-- --------------------------------------------------------------------- 5. INSERÇÃO INICIAL (SEED) E ÍNDICES

-- Inserindo a linha inicial de controle do ranking
INSERT IGNORE INTO Ranking_Metadata (id, nome_job, status_job)
VALUES (1, 'ATUALIZACAO_RANKING_DIARIO', 'AGUARDANDO');

-- Índices para buscas e ordenações ultrarrápidas
CREATE INDEX idx_cliente_localizacao ON Cliente(estado, cidade, tipo_pessoa, tipo_imovel);
CREATE INDEX idx_avaliacao_score ON Avaliacao_Eficiencia(mes_referencia, score_sustentabilidade DESC);
CREATE INDEX idx_busca_ranking ON Ranking_Global(tipo_ranking, localidade, tipo_imovel, posicao);