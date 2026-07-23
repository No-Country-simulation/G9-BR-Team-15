package com.energ_ia.api.dto;
public class AuthResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String mensagem;
    public AuthResponseDTO(Long id, String nome, String email, String mensagem) {
        this.id = id; this.nome = nome; this.email = email; this.mensagem = mensagem;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
