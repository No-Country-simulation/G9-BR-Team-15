package com.energ_ia.api.domain.cliente;

import com.energ_ia.api.domain.core.TipoImovel;
import com.energ_ia.api.domain.core.TipoPessoa;
import com.energ_ia.api.domain.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Cliente")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "nome_razao_social", nullable = false, length = 255)
    private String nomeRazaoSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false)
    private TipoPessoa tipoPessoa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_imovel", nullable = false, length = 100)
    private TipoImovel tipoImovel;

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

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClienteEquipamento> equipamentos = new ArrayList<>();

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