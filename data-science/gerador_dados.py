import pandas as pd
import numpy as np

# Gerar sempre os mesmo dados
np.random.seed(42)

# Total de registros simulados
n_clientes = 1500

# Tipos de clientes e seus portes
tipos =['Residencial', 'Comercial', 'Industrial']
portes = ['Pequeno', 'Medio', 'Grande']

# Escolha aleatoria: tipo ou porte, para cada registro
tipo_cliente = np.random.choice(tipos, size=n_clientes, p=[0.6, 0.3, 0.1]) # 60% residencial, 30% comercial, 10% industrial
porte_cliente = np.random.choice(portes, size=n_clientes, p=[0.5, 0.3, 0.2]) # Distribuicaoo de tamanhos

quantidade_equipamentos = []
tempo_medio_uso = []
uso_horario_pico = []
consumo_base_lista = []

# Gerando as variaveis de entrada (Features) baseadas no tipo e porte do cliente
for i in range(n_clientes):
    if tipo_cliente[i] == 'Residencial':
        # Casas e Apartamentos 
        equipset = np.random.randint(5, 30)
        tempo = np.random.uniform(2.0, 12.0)
        pico = np.random.uniform(0.5, 4.0)
        consumo_base = (equipset * tempo * 1.2) + (pico * 50)
    else:
        # Empresas (Pequeno, Medio e Grande Porte)
        if porte_cliente[i] == 'Pequeno':
            equipset = np.random.randint(20, 80)
            tempo = np.random.uniform(8.0, 14.0) 
            pico = np.random.uniform(1.0, 5.0)
            consumo_base = (equipset * tempo * 1.5) + (pico * 100)
        elif porte_cliente[i] == 'Medio':
            equipset = np.random.randint(80, 300)
            tempo = np.random.uniform(10.0, 18.0)
            pico = np.random.uniform(2.0, 6.0)
            consumo_base = (equipset * tempo * 1.8) + (pico * 300)
        else: # Grande porte (Fabricas / Grandes empresas)
            equipset = np.random.randint(300, 1200)
            tempo = np.random.uniform(12.0, 24.0) # Muitas funcionam 24h
            pico = np.random.uniform(4.0, 8.0)
            consumo_base = (equipset * tempo * 2.2) + (pico * 1000)
            
    quantidade_equipamentos.append(equipset)
    tempo_medio_uso.append(round(tempo, 1))
    uso_horario_pico.append(round(pico, 1))
    consumo_base_lista.append(consumo_base)

# Adicionei ruido para ficar mais realista
ruido = np.random.normal(0, 50, size=n_clientes)
consumo_kwh = np.clip(np.array(consumo_base_lista) + ruido, 50, 50000).astype(int)

# Target do Machine Learning para regra de negocio para classificacao
perfil_energetico = []
for i in range(n_clientes):
    # Regra proporcional: avalia o consumo em relacao ao porte do cliente
    if tipo_cliente[i] == 'Residencial':
        limite_alto, limite_baixo, limite_pico = 450, 220, 2.5
    elif porte_cliente[i] == 'Pequeno':
        limite_alto, limite_baixo, limite_pico = 1500, 600, 3.5
    elif porte_cliente[i] == 'Médio':
        limite_alto, limite_baixo, limite_pico = 8000, 3000, 4.5
    else: # Grande
        limite_alto, limite_baixo, limite_pico = 35000, 15000, 6.0

    # Classificacao final
    if consumo_kwh[i] > limite_alto and uso_horario_pico[i] > limite_pico:
        perfil_energetico.append('Ineficiente')
    elif consumo_kwh[i] < limite_baixo and uso_horario_pico[i] < (limite_pico - 0.5):
        perfil_energetico.append('Eficiente')
    else:
        perfil_energetico.append('Moderado')

# DataFrame do Pandas 
df_energia = pd.DataFrame({
    'id_cliente': range(1, n_clientes + 1),
    'tipo_cliente': tipo_cliente,
    'porte_cliente': porte_cliente,
    'consumo_kwh': consumo_kwh,
    'uso_horario_pico_horas': uso_horario_pico,
    'quantidade_equipamentos': quantidade_equipamentos,
    'tempo_medio_uso_diario': tempo_medio_uso,
    'perfil_energetico': perfil_energetico
})

# Salvei em CSV na pasta
df_energia.to_csv('data-science/consumo_original.csv', index=False)

print("Nova base de dados (Residencias + Empresas) criada com sucesso!")
print(df_energia['tipo_cliente'].value_counts())
print("\nClassificacao Geral:")
print(df_energia['perfil_energetico'].value_counts())