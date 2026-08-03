FROM python:3.11-slim

WORKDIR /app

# Copia e instala as dependências
COPY ml-service/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copia o código do serviço e os dados necessários
COPY ml-service/ ./
COPY data-science/ ./data-science/

# Porta do FastAPI
EXPOSE 8000

# Inicia o servidor
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]