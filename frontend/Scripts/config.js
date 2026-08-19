/*
  LU|MEN - configuração do frontend

  Enquanto a API pública não estiver disponível, USE_LOCAL_PREVIEW mantém o
  simulador funcional com uma estimativa dinâmica baseada nos dados preenchidos.

  Na integração final com a OCI:
    USE_LOCAL_PREVIEW: false
    API_BASE_URL: "https://URL-PUBLICA-DO-BACKEND"
*/
window.LUMEN_CONFIG = {
  USE_LOCAL_PREVIEW: true,
  ALLOW_LOCAL_FALLBACK: true,
  API_BASE_URL: "/api"
};
