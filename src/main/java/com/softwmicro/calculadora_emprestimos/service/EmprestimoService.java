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
        BigDecimal monthlyRate = req.getTaxaJuros().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN);

        // construir datas de pagamento
        List<LocalDate> paymentDates = new ArrayList<>();
        LocalDate cur = req.getPrimeiroPagamento();
        while (!cur.isAfter(req.getDataFinal())) {
            paymentDates.add(cur);
            // avançar um mês mantendo o dia do mês; se o dia não existir no próximo mês usar o último dia do mês
            int day = cur.getDayOfMonth();
            YearMonth nextYm = YearMonth.from(cur).plusMonths(1);
            int maxDay = nextYm.lengthOfMonth();
            int dayNext = Math.min(day, maxDay);
            cur = LocalDate.of(nextYm.getYear(), nextYm.getMonth(), dayNext);
        }

        int n = paymentDates.size();

        // calcular parcela fixa (anuidade)
        BigDecimal payment = BigDecimal.ZERO;
        if (n > 0) {
            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
                payment = principal.divide(BigDecimal.valueOf(n), 10, RoundingMode.HALF_EVEN);
            } else {
                // fórmula: pagamento = P * i / (1 - (1+i)^-n)
                double i = monthlyRate.doubleValue();
                double denom = 1 - Math.pow(1 + i, -n);
                double pmt = principal.doubleValue() * i / denom;
                payment = BigDecimal.valueOf(pmt).setScale(10, RoundingMode.HALF_EVEN);
            }
        }

        // pré-calcular detalhes dos pagamentos
        List<BigDecimal> interests = new ArrayList<>();
        List<BigDecimal> amortizacoes = new ArrayList<>();
        List<BigDecimal> balancesAfter = new ArrayList<>();

        BigDecimal balance = principal;
        for (int idx = 0; idx < n; idx++) {
            BigDecimal interest = balance.multiply(monthlyRate).setScale(10, RoundingMode.HALF_EVEN);
            BigDecimal amort = payment.subtract(interest).setScale(10, RoundingMode.HALF_EVEN);
            // ajustar última amortização para evitar valor negativo
            if (idx == n - 1) {
                amort = balance; // quitar o principal restante
                payment = interest.add(amort).setScale(10, RoundingMode.HALF_EVEN);
            }
            balance = balance.subtract(amort).setScale(10, RoundingMode.HALF_EVEN);
            interests.add(interest);
            amortizacoes.add(amort);
            balancesAfter.add(balance);
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
        java.util.function.IntFunction<BigDecimal> balanceAfterK = (k) -> {
            if (k <= 0) return principal;
            return balancesAfter.get(Math.min(k - 1, balancesAfter.size() - 1));
        };

        BigDecimal acumulado = BigDecimal.ZERO;

        for (LocalDate d : timeline) {
            Parcela row = new Parcela();
            row.setDataCompetencia(d);
            row.setValorEmprestimo(principal.setScale(2, RoundingMode.HALF_EVEN));

            // quantos pagamentos ocorreram até esta data
            int pagos = 0;
            for (LocalDate pd : paymentDates) {
                if (!pd.isAfter(d)) pagos++;
            }

            BigDecimal saldoDevedor = balanceAfterK.apply(pagos);
            row.setSaldoDevedor(saldoDevedor.setScale(2, RoundingMode.HALF_EVEN));

            if (pagos > 0 && paymentDates.get(pagos - 1).isEqual(d)) {
                // esta é uma data de pagamento
                int idx = pagos - 1;
                BigDecimal interest = interests.get(idx).setScale(2, RoundingMode.HALF_EVEN);
                BigDecimal amort = amortizacoes.get(idx).setScale(2, RoundingMode.HALF_EVEN);
                BigDecimal total = interest.add(amort).setScale(2, RoundingMode.HALF_EVEN);
                row.setConsolidada((idx + 1) + "/" + n);
                row.setTotal(total);
                row.setAmortizacao(amort);
                row.setSaldo(balancesAfter.get(idx).setScale(2, RoundingMode.HALF_EVEN));
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
                if (!d.isBefore(req.getPrimeiroPagamento()) && paymentDates.stream().anyMatch(pd -> pd.isAfter(d))) {
                    // juros calculados com base no saldo após pagamentos anteriores (pagos)
                    BigDecimal provision = balanceAfterK.apply(pagos).multiply(monthlyRate).setScale(2, RoundingMode.HALF_EVEN);
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

