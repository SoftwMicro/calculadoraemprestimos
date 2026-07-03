package com.softwmicro.calculadora_emprestimos.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmprestimoRequest {
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private LocalDate primeiroPagamento;
    private BigDecimal valor;
    private BigDecimal taxaJuros; // percentual mensal

    public EmprestimoRequest() {}

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
}

