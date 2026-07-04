package com.softwmicro.calculadora_emprestimos.service;

import com.softwmicro.calculadora_emprestimos.model.Emprestimo;
import com.softwmicro.calculadora_emprestimos.model.EmprestimoRequest;
import com.softwmicro.calculadora_emprestimos.model.Parcela;
import com.softwmicro.calculadora_emprestimos.repository.EmprestimoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class EmprestimoService {

    private final EmprestimoRepository repository;

    public EmprestimoService(EmprestimoRepository repository) {
        this.repository = repository;
    }

    public List<Parcela> calcular(EmprestimoRequest req) {
        // validações
        if (!req.getDataFinal().isAfter(req.getDataInicial())) {
            throw new IllegalArgumentException("A data final deve ser maior que a data inicial");
        }
        if (!(req.getPrimeiroPagamento().isAfter(req.getDataInicial()) && req.getPrimeiroPagamento().isBefore(req.getDataFinal()))) {
            throw new IllegalArgumentException("A data de primeiro pagamento deve ser maior que a data inicial e menor que a data final");
        }

        // persistir requisição
        Emprestimo ent = new Emprestimo();
        ent.setDataInicial(req.getDataInicial());
        ent.setDataFinal(req.getDataFinal());
        ent.setPrimeiroPagamento(req.getPrimeiroPagamento());
        ent.setValor(req.getValor());
        ent.setTaxaJuros(req.getTaxaJuros());
        repository.save(ent);

        BigDecimal principal = req.getValor();
        BigDecimal taxaMensal = req.getTaxaJuros().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN);

        // construir datas de pagamento
        List<LocalDate> datasPagamento = new ArrayList<>();
        LocalDate atual = req.getPrimeiroPagamento();
        while (!atual.isAfter(req.getDataFinal())) {
            datasPagamento.add(atual);
            // avançar um mês mantendo o dia do mês; se o dia não existir no próximo mês usar o último dia do mês
            int dia = atual.getDayOfMonth();
            YearMonth proxMesAno = YearMonth.from(atual).plusMonths(1);
            int maxDia = proxMesAno.lengthOfMonth();
            int diaProx = Math.min(dia, maxDia);
            atual = LocalDate.of(proxMesAno.getYear(), proxMesAno.getMonth(), diaProx);
        }

        int qtdParcelas = datasPagamento.size();

        // calcular parcela fixa (anuidade)
        BigDecimal valorParcela = BigDecimal.ZERO;
        if (qtdParcelas > 0) {
            if (taxaMensal.compareTo(BigDecimal.ZERO) == 0) {
                valorParcela = principal.divide(BigDecimal.valueOf(qtdParcelas), 10, RoundingMode.HALF_EVEN);
            } else {
                // fórmula: pagamento = P * i / (1 - (1+i)^-n)
                double i = taxaMensal.doubleValue();
                double denom = 1 - Math.pow(1 + i, -qtdParcelas);
                double pmt = principal.doubleValue() * i / denom;
                valorParcela = BigDecimal.valueOf(pmt).setScale(10, RoundingMode.HALF_EVEN);
            }
        }

        // pré-calcular detalhes dos pagamentos
        List<BigDecimal> juros = new ArrayList<>();
        List<BigDecimal> amortizacoes = new ArrayList<>();
        List<BigDecimal> saldosApos = new ArrayList<>();

        BigDecimal saldo = principal;
        for (int indice = 0; indice < qtdParcelas; indice++) {
            BigDecimal j = saldo.multiply(taxaMensal).setScale(10, RoundingMode.HALF_EVEN);
            BigDecimal amortizacao = valorParcela.subtract(j).setScale(10, RoundingMode.HALF_EVEN);
            // ajustar última amortização para evitar valor negativo
            if (indice == qtdParcelas - 1) {
                amortizacao = saldo; // quitar o principal restante
                valorParcela = j.add(amortizacao).setScale(10, RoundingMode.HALF_EVEN);
            }
            saldo = saldo.subtract(amortizacao).setScale(10, RoundingMode.HALF_EVEN);
            juros.add(j);
            amortizacoes.add(amortizacao);
            saldosApos.add(saldo);
        }

        // construir timeline: data inicial, todos os últimos dias do mês entre as datas, data final
        Set<LocalDate> timelineSet = new HashSet<>();
        timelineSet.add(req.getDataInicial());

        LocalDate cursor = req.getDataInicial().plusDays(1);
        while (cursor.isBefore(req.getDataFinal())) {
            YearMonth ym = YearMonth.from(cursor);
            LocalDate lastDay = LocalDate.of(ym.getYear(), ym.getMonth(), ym.lengthOfMonth());
            if ((lastDay.isAfter(req.getDataInicial()) || lastDay.isEqual(req.getDataInicial())) && (lastDay.isBefore(req.getDataFinal()) || lastDay.isEqual(req.getDataFinal()))) {
                timelineSet.add(lastDay);
            }
            // avançar para o próximo mês
            cursor = cursor.plusMonths(1).withDayOfMonth(1);
        }

        timelineSet.add(req.getDataFinal());

        List<LocalDate> timeline = new ArrayList<>(timelineSet);
        Collections.sort(timeline);

        List<Parcela> resultado = new ArrayList<>();

        // função auxiliar para calcular o saldo após k pagamentos (k pagamentos aplicados)
        java.util.function.IntFunction<BigDecimal> saldoAposK = (k) -> {
            if (k <= 0) return principal;
            return saldosApos.get(Math.min(k - 1, saldosApos.size() - 1));
        };

        BigDecimal acumulado = BigDecimal.ZERO;

        for (LocalDate d : timeline) {
            Parcela row = new Parcela();
            row.setDataCompetencia(d);
            row.setValorEmprestimo(principal.setScale(2, RoundingMode.HALF_EVEN));

            // quantos pagamentos ocorreram até esta data
            int pagos = 0;
            for (LocalDate dp : datasPagamento) {
                if (!dp.isAfter(d)) pagos++;
            }

            BigDecimal saldoDevedor = saldoAposK.apply(pagos);
            row.setSaldoDevedor(saldoDevedor.setScale(2, RoundingMode.HALF_EVEN));

            if (pagos > 0 && datasPagamento.get(pagos - 1).isEqual(d)) {
                // esta é uma data de pagamento
                int indice = pagos - 1;
                BigDecimal interest = juros.get(indice).setScale(2, RoundingMode.HALF_EVEN);
                BigDecimal amortizacao = amortizacoes.get(indice).setScale(2, RoundingMode.HALF_EVEN);
                BigDecimal total = interest.add(amortizacao).setScale(2, RoundingMode.HALF_EVEN);
                row.setConsolidada((indice + 1) + "/" + qtdParcelas);
                row.setTotal(total);
                row.setAmortizacao(amortizacao);
                row.setSaldo(saldosApos.get(indice).setScale(2, RoundingMode.HALF_EVEN));
                row.setProvisao(interest);
                acumulado = acumulado.add(interest).setScale(2, RoundingMode.HALF_EVEN);
                row.setAcumulado(acumulado);
                row.setPago(total);
                // resetar o acumulado após o pagamento
                acumulado = BigDecimal.ZERO;
            } else {
                // linhas que não são de pagamento
                row.setConsolidada("");
                row.setTotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                row.setAmortizacao(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                row.setSaldo(saldoDevedor.setScale(2, RoundingMode.HALF_EVEN));

                // se esta data estiver entre o primeiro pagamento e o próximo pagamento e for fim de mês, provisionar juros
                if (!d.isBefore(req.getPrimeiroPagamento()) && datasPagamento.stream().anyMatch(dp -> dp.isAfter(d))) {
                    // juros calculados com base no saldo após pagamentos anteriores (pagos)
                    BigDecimal provision = saldoAposK.apply(pagos).multiply(taxaMensal).setScale(2, RoundingMode.HALF_EVEN);
                    acumulado = acumulado.add(provision).setScale(2, RoundingMode.HALF_EVEN);
                    row.setProvisao(provision);
                    row.setAcumulado(acumulado);
                } else {
                    row.setProvisao(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
                    row.setAcumulado(acumulado.setScale(2, RoundingMode.HALF_EVEN));
                }

                row.setPago(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
            }

            resultado.add(row);
        }

        return resultado;
    }
}

