package com.softwmicro.calculadora_emprestimos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emprestimos")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private LocalDate primeiroPagamento;
    private BigDecimal valor;
    private BigDecimal taxaJuros; // percentual mensal (ex.: 1.5 para 1,5% ao mês)

    private LocalDateTime criadoEm;

    public Emprestimo() {
        this.criadoEm = LocalDateTime.now();
    }

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDataInicial() { return dataInicial; }
    public void setDataInicial(LocalDate dataInicial) { this.dataInicial = dataInicial; }

    public LocalDate getDataFinal() { return dataFinal; }
    public void setDataFinal(LocalDate dataFinal) { this.dataFinal = dataFinal; }

    public LocalDate getPrimeiroPagamento() { return primeiroPagamento; }
    public void setPrimeiroPagamento(LocalDate primeiroPagamento) { this.primeiroPagamento = primeiroPagamento; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public BigDecimal getTaxaJuros() { return taxaJuros; }
    public void setTaxaJuros(BigDecimal taxaJuros) { this.taxaJuros = taxaJuros; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}

