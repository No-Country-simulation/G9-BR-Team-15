# G9-BR-TEAM-15
Hackathon ONE – Projetos G9 | Alura + Oracle

Página com detalhes e documentação das propostas de projeto do Hackthon:
https://alura-es-cursos.github.io/projetos-hackathon-g9-brasil/

## Sobre
**EnergiAI** é uma solução inteligente capaz de analisar padrões de consumo de energia elétrica e gerar informações que auxiliem na tomada de decisões relacionadas à eficiência energética.

## Arquitetura do Sistema

1. **Database (MySQL):** Armazenamento relacional persistente para usuários, perfis de clientes, catálogo de equipamentos, histórico de consumo mensal e avaliações de eficiência energética.
   
2. **Backend (API):** Desenvolvida em **Java com Spring Boot**, responsável pela regra de negócio, segurança (Spring Security com autenticação via Token JWT), mapeamento de entidades via JPA/Hibernate e integração com o microsserviço de IA. 
   
3. **Frontend:** Interface de consumo das APIs, permitindo o gerenciamento de clientes, visualização de consumo e relatórios energéticos.
   
4. **ML-Service:** Microsserviço desenvolvido em **Python** (FastAPI/Pydantic) que processa as regras de inteligência artificial, avalia o consumo energético, calcula probabilidades e retorna recomendações automatizadas.

## Estrutura do projeto

 ```text

 G9-BR-TEAM-15/
├── docs/                   # documentação e diagramas gerados
├── frontend/               # Código do Vue.js
│   ├── src/
│   ├── package.json
│   └── ...
├── backend/            # API em Java (Spring Boot)
│   ├── src/
│   ├── pom.xml 
│   ├──Dockerfile
│   └── ...
├── ml-service/             # Microsserviço Python (FastAPI)
│   ├── app.py
│   ├── requirements.txt
│   ├── modelo_energia.pkl  # Modelo treinado
│   ├── Dockerfile
│   └── ...
├── data-science/           # Notebooks e scripts de treino
│   ├── eda_consumo.ipynb 
│   ├── gerador_dados.py
│   └── ...
├── compose.yaml  
├── .env.example            # arquivo com variáveis de ambiente esperadas   
└── README.md               # documento atual
 
 ```

 ## Como rodar o projeto com Docker


Certifique-se de que você possui o **Docker** e o **Docker Compose** instalados na sua máquina.

### 1. Clonar o repositório
```bash
git clone [https://github.com/No-Country-simulation/G9-BR-Team-15.git](https://github.com/No-Country-simulation/G9-BR-Team-15.git)
cd G9-BR-Team-15
```

### 2. Subir os containers
Na raiz do projeto (onde está localizado o arquivo docker-compose.yml), execute o comando para construir e iniciar os serviços em segundo plano:
```bash
docker compose up --build -d
```
### 3. Verificar se os serviços estão rodando
Você pode listar os containers ativos para garantir que a API, o banco de dados e o serviço de Machine Learning subiram corretamente:

```bash
docker compose ps
```
### 4. Visualizar os logs (Opcional)
Se precisar acompanhar a inicialização da API ou verificar possíveis erros, use os logs:

```bash
docker compose logs -f api-backend
```
### 5. Parar os containers
Para parar e remover os containers mantendo os dados, execute:

```bash
docker compose down
```
(Caso queira limpar também os volumes do banco de dados e começar do zero, use: docker compose down -v --remove-orphans)
<br>
<br>


 **As solicitações da API Java principal para a API do modelo de IA devem ser feitas na url "http://ml-service:8000/api/v1/" + "endpoint-que-deseja-utilizar".**

 **Exemplos válidos: "http://ml-service:8000/api/v1/teste/analise-energetica" e "http://ml-service:8000/api/v1/analise-energetica". O serviço também aceita os aliases curtos "/teste-analise-energetica" e "/analise-energetica".**

<br>

 ## Rotas da API

**Abaixo estão listados os principais endpoints com exemplos de uso. Outras rotas disponíveis podem ser verificadas após a execução local dos containers em: http://localhost:8080/swagger-ui.html**.

### 1. Teste da plataforma com requisitos obrigatórios do hackthon
<br>

**Endpoint:** "POST/teste/analise-energetica"

**Ex:** http://localhost:8080/teste/analise-energetica

**Request:**

```json
   {
        "consumo_kwh": 100,
        "uso_horario_pico": true,
        "quantidade_equipamentos": 10,
        "tipo_imovel": "Residencial",
        "horas_alto_consumo": 8
    }
```
**Response:**

```json
    {
        "categoria": "Eficiente",
        "probabilidade": 1.0,
        "recomendacoes": [
            "Parabéns! Seu consumo está dentro do esperado para o seu perfil. Continue assim.",
            "Evite concentrar o uso de equipamentos no horário de ponta da Light (17h30-20h30)."
        ],
        "custo_estimado_mensal": 75.0
    }

   
   
```

### 2. Rota para analise energetica de um cliente pertencente a um usuário logado
<br>

**Endpoint:** "POST/{clienteId}/analise-energetica"

**Ex:** http://localhost:8080/clientes/2/analise-energetica

**Request:**

```json
Requisição sem corpo (No Body)
```
**Response:**

```json
    {
        "avaliacaoId": 2,
        "consumoPrevistoLocalKwh": 5635.5,
        "custoPrevistoLocal": 4226.625,
        "categoriaIA": "Moderado",
        "scoreSustentabilidade": 56,
        "dicasMelhoria": [
            "Seu consumo está na média do seu perfil, mas ainda há espaço pra economizar.",
            "Sua maior fonte de consumo é a categoria 'Cozinha': Concentre o uso de forno elétrico/airfryer fora do horário de ponta da Light (17h30-20h30)."
        ],
        "alertaConsumoAlto": false
    }

```

### 3. Cadastro de usuário
<br>

**Endpoint:** "POST/auth/register""

**Ex:** http://localhost:8080/auth/register


**Request:**

```json
    {
        "nome": "teste1",
        "email": "teste@email.com",
        "senha": "minhaSenha"
    }

```
**Response:**
```json
    {
        "id": 1,
        "nome": "teste1",
        "email": "teste@email.com",
        "mensagem": "Usuário cadastrado com sucesso",
        "token": null
    }

```
### 4. Login do usuário
<br>

**Endpoint:** "POST/auth/login""

**Ex:** http://localhost:8080/auth/login


**Request:**

```json
    {
        "email": "teste@email.com",
        "senha": "minhaSenha"
    }

```
**Response:**
```json
    {
        "id": 1,
        "nome": "teste1",
        "email": "teste@email.com",
        "mensagem": "Login realizado com sucesso",
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJMVU1FTiBBUEkiLCJzdWIiOiJ0ZXN0ZUBlbWFpbC5jb20iLCJpZCI6MSwiZXhwIjoxNzg2MTY0NjcyfQ.lyB0BPN0_2mb5wc2HFf-nrxUq6MZ5vG15QhLYJxmbdo"
    }

```
### 5. Lista de equipamentos cadastrados e válidos para uso com modelo atual (ML-service)

**Endpoint:** "GET/equipamentos"

**Ex:** http://localhost:8080/equipamentos


**Request:**

```json
Requisição sem corpo (No Body)
```
**Response:**
```json
[
	{
		"id": 1,
		"tipo": "Geladeira Frost Free",
		"marca": "Padrao",
		"modelo": "Padrao",
		"potenciaWatts": 150
	},
	{
		"id": 2,
		"tipo": "Micro-ondas",
		"marca": "Padrao",
		"modelo": "Padrao",
		"potenciaWatts": 1200
	},
	{
		"id": 3,
		"tipo": "Airfryer",
		"marca": "Padrao",
		"modelo": "Padrao",
		"potenciaWatts": 1500
	}
]
```
### 6. Cadastro de Clientes

**Endpoint:** "POST/clientes""

**Ex:** http://localhost:8080/clientes


**Request:**

```json
{
    "nomeRazaoSocial": "cliente1",
    "tipoPessoa": "PJ",
    "tipoImovel": "RESIDENCIAL",
    "cep": "24000-000",
    "cidade": "Marica",
    "estado":"RJ",
	"pais": "Brasil",
    "equipamentos": [
        {
        "equipamentoId": 10, 
        "quantidade": 2, 
        "horasUsoDiario": 8.5, 
        "diasUsoMes": 30
        },
        {
            "equipamentoId": 3, 
        "quantidade": 5, 
        "horasUsoDiario": 8.5, 
        "diasUsoMes": 30
        },
        {
            "equipamentoId": 4, 
        "quantidade": 2, 
        "horasUsoDiario": 8.5, 
        "diasUsoMes": 30
        }
  ]
}

```
**Response:**
```json
{
	"id": 1,
	"nomeRazaoSocial": "Cliente1",
	"tipoPessoa": "PJ",
	"tipoImovel": "RESIDENCIAL",
	"cep": "24000-000",
	"cidade": "Marica",
	"estado": "RJ",
	"ativo": true,
	"equipamentos": [
		{
			"equipamentoId": 10,
			"tipo": "Chuveiro Elétrico",
			"marca": "Padrao",
			"modelo": "Padrao",
			"potenciaWatts": 5500,
			"quantidade": 2,
			"horasUsoDiario": 8.5,
			"diasUsoMes": 30
		},
		{
			"equipamentoId": 3,
			"tipo": "Airfryer",
			"marca": "Padrao",
			"modelo": "Padrao",
			"potenciaWatts": 1500,
			"quantidade": 5,
			"horasUsoDiario": 8.5,
			"diasUsoMes": 30
		},
		{
			"equipamentoId": 4,
			"tipo": "Forno Elétrico",
			"marca": "Padrao",
			"modelo": "Padrao",
			"potenciaWatts": 1800,
			"quantidade": 2,
			"horasUsoDiario": 8.5,
			"diasUsoMes": 30
		}
	]
}

```
### 7. Lista de Clientes associados ao usuário

**Endpoint:** "GET/clientes"

**Ex:** http://localhost:8080/clientes


**Request:**

```json
Requisição sem corpo (No Body)
```
**Response:**
```json
[
	{
		"id": 1,
		"nomeRazaoSocial": "Cliente1",
		"tipoPessoa": "PF",
		"tipoImovel": "RESIDENCIAL",
		"cep": "24000-000",
		"cidade": "Marica",
		"estado": "RJ",
		"ativo": true,
		"equipamentos": [
			{
				"equipamentoId": 10,
				"tipo": "Chuveiro Elétrico",
				"marca": "Padrao",
				"modelo": "Padrao",
				"potenciaWatts": 5500,
				"quantidade": 2,
				"horasUsoDiario": 8.5,
				"diasUsoMes": 30
			},
			{
				"equipamentoId": 5,
				"tipo": "Liquidificador",
				"marca": "Padrao",
				"modelo": "Padrao",
				"potenciaWatts": 400,
				"quantidade": 1,
				"horasUsoDiario": 8.5,
				"diasUsoMes": 30
			},
			{
				"equipamentoId": 3,
				"tipo": "Airfryer",
				"marca": "Padrao",
				"modelo": "Padrao",
				"potenciaWatts": 1500,
				"quantidade": 5,
				"horasUsoDiario": 8.5,
				"diasUsoMes": 30
			},
			{
				"equipamentoId": 4,
				"tipo": "Forno Elétrico",
				"marca": "Padrao",
				"modelo": "Padrao",
				"potenciaWatts": 1800,
				"quantidade": 2,
				"horasUsoDiario": 8.5,
				"diasUsoMes": 30
			}
		]
	},
	{
		"id": 2,
		"nomeRazaoSocial": "Cliente2",
		"tipoPessoa": "PJ",
		"tipoImovel": "RESIDENCIAL",
		"cep": "24000-000",
		"cidade": "Marica",
		"estado": "RJ",
		"ativo": true,
		"equipamentos": [
			{
				"equipamentoId": 10,
				"tipo": "Chuveiro Elétrico",
				"marca": "Padrao",
				"modelo": "Padrao",
				"potenciaWatts": 5500,
				"quantidade": 2,
				"horasUsoDiario": 8.5,
				"diasUsoMes": 30
			},
			{
				"equipamentoId": 3,
				"tipo": "Airfryer",
				"marca": "Padrao",
				"modelo": "Padrao",
				"potenciaWatts": 1500,
				"quantidade": 5,
				"horasUsoDiario": 8.5,
				"diasUsoMes": 30
			},
			{
				"equipamentoId": 4,
				"tipo": "Forno Elétrico",
				"marca": "Padrao",
				"modelo": "Padrao",
				"potenciaWatts": 1800,
				"quantidade": 2,
				"horasUsoDiario": 8.5,
				"diasUsoMes": 30
			}
		]
	}
]

```
### 7. Cadastrar consumo mensal

**Endpoint:** "POST/clientes/{clienteId}/consumos"

**Ex:** http://localhost:8080/clientes/3/consumos


**Request:**

```json
{
	"mesReferencia": "2026-09-17",
	"consumoRegistradoKwh": 100
}

```
**Response:**
```json
{
	"id": 3,
	"clienteId": 3,
	"mesReferencia": "2026-09-01",
	"consumoRegistradoKwh": 100.0,
	"consumoPrevistoKwh": null,
	"consumoEstimadoIaKwh": null
}

```

**Para se manter a normalização dos dados do banco e apenas 1 registro de consumo mensal para cada cliente ser guardado, por padrão, a data salva é o 1 dia do mês de referência.**

### 8. Atualização do consumo mensal

**Endpoint:** "PUT/clientes/{clienteId}/consumos/{consumoId}"

**Ex:** http://localhost:8080/clientes/3/consumos/6


**Request:**

```json
{
	"mesReferencia": "2026-08-01",
	"consumoRegistradoKwh": 100
}

```
**Response:**
```json
{
	"id": 6,
	"clienteId": 3,
	"mesReferencia": "2026-08-01",
	"consumoRegistradoKwh": 100.0,
	"consumoPrevistoKwh": 1300.5,
	"consumoEstimadoIaKwh": 1300.5
}

```

### 8. Listar dados de consumo de um cliente

**Endpoint:** "GET/clientes/{clienteId}/consumos/"

**Ex:** http://localhost:8080/clientes/3/consumos


**Request:**

```json
Requisição sem corpo (No Body)
```
**Response:**
```json
[
	{
		"id": 5,
		"clienteId": 3,
		"mesReferencia": "2026-07-01",
		"consumoRegistradoKwh": 100.0,
		"consumoPrevistoKwh": null,
		"consumoEstimadoIaKwh": null
	},
	{
		"id": 6,
		"clienteId": 3,
		"mesReferencia": "2026-08-01",
		"consumoRegistradoKwh": 100.0,
		"consumoPrevistoKwh": 1300.5,
		"consumoEstimadoIaKwh": 1300.5
	}
]

```

### 9. Ranking Top 10

**Endpoint:** "GET/ranking/top10?tipo={tipoRanking}"

**Ex:** http://localhost:8080/rankings/top10?tipo=PAIS


**Request:**

```json
Requisição sem corpo (No Body)
```
**Response:**
```json
[
	{
		"posicao": 1,
		"nomeRazaoSocial": "julia",
		"localidade": "Brasil",
		"tipoImovel": "RESIDENCIAL",
		"pontuacao": 43,
		"categoriaEficiencia": "INEFICIENTE",
		"atualizadoEm": "2026-08-12T15:36:34.985551"
	},
	{
		"posicao": 2,
		"nomeRazaoSocial": "Mariana",
		"localidade": "Brasil",
		"tipoImovel": "RESIDENCIAL",
		"pontuacao": 42,
		"categoriaEficiencia": "INEFICIENTE",
		"atualizadoEm": "2026-08-12T15:36:34.991789"
	}
]

```

## Desenvolvedores e contribuintes
| [![Monique Evellin](https://avatars.githubusercontent.com/u/59670350?v=4&s=115)](https://github.com/niqueve/)<br><sub>Monique Evellin R.Gomes</sub> | [![Luanda Lima](https://avatars.githubusercontent.com/u/177073749?v=4&s=115)](https://github.com/LuandaLl)<br><sub>Luanda Lima</sub> | [![Camila Monteiro](https://avatars.githubusercontent.com/u/184301627?v=4&s=115)](https://github.com/CamilaMonteiroRondon)<br><sub>Camila Monteiro</sub> | [![Kelly Costa](https://avatars.githubusercontent.com/u/223376015?v=4&s=115)](https://github.com/kellycosta-tech)<br><sub>Kelly Costa</sub> | [![Débora Guerra](https://avatars.githubusercontent.com/u/179046239?v=4&s=115)](https://github.com/deboracguerra)<br><sub>Débora Guerra</sub> | [![Thalysson Martins](https://avatars.githubusercontent.com/u/146669020?v=4&s=115)](https://github.com/4909thalysson)<br><sub>Thalysson Martins</sub> |
| :---: | :---: | :---: | :---: | :---: | :---: |

