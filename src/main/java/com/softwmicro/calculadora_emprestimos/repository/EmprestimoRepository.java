package com.softwmicro.calculadora_emprestimos.repository;

import com.softwmicro.calculadora_emprestimos.model.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
}

