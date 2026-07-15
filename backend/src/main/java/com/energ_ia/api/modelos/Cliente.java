package com.energ_ia.api.modelos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;

import jakarta.persistence.Table;

import jakarta.persistence.GeneratedValue;

import jakarta.persistence.Id;

import jakarta.persistence.ManyToOne;

import jakarta.persistence.JoinColumn;

import jakarta.persistence.Column;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

@Entity
@Table(name = "Cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "nome_razao_social", nullable = false, length = 255)
    private String nomeRazaoSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false)
    private TipoPessoa tipoPessoa;

    @Column(name = "tipo_imovel", nullable = false, length = 100)
    private String tipoImovel;

    @Column(length = 20)
    private String cep;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(length = 50)
    private String pais = "Brasil";

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "desativado_em")
    private LocalDateTime desativadoEm;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    public String toString() {
        return "Cliente{" +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", nomeRazaoSocial='" + nomeRazaoSocial + '\'' +
                ", tipoPessoa=" + tipoPessoa +
                ", tipoImovel='" + tipoImovel + '\'' +
                ", cep='" + cep + '\'' +
                ", cidade='" + cidade + '\'' +
                ", estado='" + estado + '\'' +
                ", pais='" + pais + '\'' +
                ", ativo=" + ativo +
                ", desativadoEm=" + desativadoEm +
                ", criadoEm=" + criadoEm +
                '}';
    }
} 