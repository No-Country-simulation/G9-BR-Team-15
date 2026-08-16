# Arquitetura OCI — LUMEN API

## Objetivo

Disponibilizar a aplicação LUMEN em ambiente Oracle Cloud Infrastructure (OCI), com API, banco de dados e serviço de análise energética executados em contêineres Docker.

## Visão geral

```mermaid
flowchart LR
    U[Usuário / Front-end] -->|HTTP :8080| API
    subgraph OCI[Oracle Cloud Infrastructure — sa-saopaulo-1]
      subgraph VCN[VCN: vcn-energia]
        subgraph SUB[Sub-rede pública]
          VM[Compute: vm-energia-deploy\nOracle Linux 9 ARM]
          API[API Spring Boot\nporta 8080]
          ML[Serviço ML / FastAPI\nporta interna 8000]
          DB[(MySQL 8\nporta interna 3306)]
          VM --> API
          API --> ML
          API --> DB
        end
      end
      OS[(Object Storage\nBucket energia-models-g9)]
    end
    ML -. modelo .pkl publicado .-> OS
```

## Recursos utilizados

| Camada | Recurso configurado |
|---|---|
| Região | Brazil East (São Paulo), `sa-saopaulo-1` |
| Rede | VCN `vcn-energia` e sub-rede pública |
| Servidor | Instância Compute `vm-energia-deploy` |
| Sistema operacional | Oracle Linux 9, arquitetura ARM (A1 Flex) |
| Execução | Docker Compose |
| Aplicação | API Spring Boot, serviço ML/FastAPI e MySQL 8 |
| Armazenamento de modelo | Bucket Object Storage `energia-models-g9` |

## Acesso externo e segurança

- A API é exposta publicamente somente na porta **8080**.
- O Swagger está disponível em `http://147.15.79.80:8080/swagger-ui/index.html`.
- A porta **22** permanece liberada para administração por SSH; após a apresentação, recomenda-se restringi-la aos IPs autorizados.
- As portas do MySQL e do serviço ML não possuem regra de entrada pública na lista de segurança da OCI. A comunicação entre os serviços ocorre pela rede interna do Docker.
- Senhas e segredos foram mantidos no arquivo `.env` da VM e não devem ser enviados ao Git.

## Deploy realizado

1. Clonagem da branch `main` do repositório na VM.
2. Criação do arquivo `.env` a partir das variáveis exigidas pela aplicação.
3. Instalação e ativação do Docker e Docker Compose.
4. Execução de `docker compose up --build -d`.
5. Validação dos três contêineres: `lumen_api`, `lumen_ml` e `lumen_mysql`.
6. Validação externa pelo Swagger da API.

## Observação de compatibilidade ARM

A instância Compute utiliza arquitetura ARM. Para permitir o build da API, foi necessário substituir as imagens Alpine do Dockerfile por imagens compatíveis com ARM e ajustar a criação do usuário do contêiner. Essas alterações devem ser incorporadas ao Dockerfile no repositório para que próximos deploys reproduzam o ambiente sem ajustes manuais.

## Status de validação

- API acessível externamente: **validada**.
- Swagger carregado no ambiente cloud: **validado**.
- Contêineres API, ML e banco em execução: **validado**.
- Testes funcionais completos das rotas e das previsões: **pendentes da equipe de backend/dados**.
- Upload do arquivo `modelo_mvp.pkl` no bucket: informado como concluído pela equipe de dados; a integração do serviço ML com o bucket deve ser confirmada pela responsável pelo modelo.

