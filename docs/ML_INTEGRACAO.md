# Guia de Integração e Uso do Modelo (EnergiAI)

Este documento orienta o uso do modelo de Machine Learning treinado no projeto.

## 1. Onde está o modelo?
O modelo final foi treinado e está disponível no arquivo:
`data-science/modelo_energia.pkl`

## 2. Para o time de Back-end
O modelo foi salvo utilizando a biblioteca `joblib` e utiliza um `Pipeline` do Scikit-Learn. Isso significa que ele já contém todo o pré-processamento necessário (como o `OneHotEncoder`).

**Exemplo de como carregar e usar:**
```python
import joblib
# Carregar o modelo
modelo = joblib.load('data-science/modelo_energia.pkl')

# O modelo está pronto para receber novos dados (DataFrame)
previsao = modelo.predict(dados_novos)