# Relatório Técnico: Ciência de Dados EnergiAI

Este documento detalha o processo de modelagem, desde o tratamento inicial até a escolha do modelo final.

## 🛠 Stack Tecnológico
| Ferramenta | Aplicação |
|---|---|
| **Pandas** | Manipulação e agregação de tabelas |
| **Scikit-learn** | Pipeline, pré-processamento e modelos de ML |
| **Matplotlib/Seaborn** | EDA e visualização estatística |
| **Joblib** | Persistência do modelo (.pkl) |

---

## 1. Tratamento e Limpeza
*   **Limpeza:** Remoção de registros com `quantidade <= 0`.
*   **Consistência:** Verificação de duplicados e valores ausentes (`null`).
*   **Base única:** Consolidação de 3 tabelas (Cliente, Equipamento, Catálogo) em uma base granular por cliente.

## 2. Feature Engineering
*   **Categorização:** Equipamentos mapeados em categorias (Cozinha, Banheiro, etc).
*   **Cálculo de Consumo:** Conversão de potência/uso para `kWh` mensal.
*   **Agregação:** Criação de indicadores de comportamento (diversidade de equipamentos, horas/dia, dias/mês).
*   **Binning:** Transformação de variáveis contínuas em faixas (ex: `faixa_uso_diario` como Baixo/Médio/Alto) para evitar que o modelo aprenda apenas a fórmula matemática do consumo.

## 3. Classificação (O "Rótulo" do Modelo)
*   **Critério:** Classificação via desvio-padrão por `tipo_imovel`.
*   **Proporção Real:** Evitamos balanceamento artificial; o modelo aprende com a distribuição natural (32% Eficiente / 44% Moderado / 23% Ineficiente).
*   **Prevenção de Vazamento (Data Leakage):** O consumo total é usado **apenas** para gerar o rótulo; ele não é fornecido como variável de entrada para evitar que o modelo "decore" a conta em vez de aprender o padrão.

## 4. Treinamento e Avaliação
*   **Pipeline:** Uso de `ColumnTransformer` (OneHotEncoder + Passthrough) e `Pipeline` para garantir reprodutibilidade.
*   **Validação:** Split 80/20 estratificado.
*   **Benchmarking:** Comparação entre Random Forest, Árvore de Decisão e Regressão Logística.
*   **Métrica de Escolha:** **F1-Macro**, por equilibrar o desempenho entre as 3 classes de perfil.

### Comparativo de Performance
| Modelo | Acurácia | F1 Macro |
|---|---|---|
| **Regressão Logística** | **0.66** | **0.66** |
| Random Forest | 0.655 | 0.64 |
| Árvore de Decisão | 0.57 | 0.57 |

*O modelo de **Regressão Logística** foi definido como oficial e salvo em `modelo_energia.pkl`.*