package com.softwmicro.calculadora_emprestimos.controller;

import com.softwmicro.calculadora_emprestimos.model.EmprestimoRequest;
import com.softwmicro.calculadora_emprestimos.model.Parcela;
import com.softwmicro.calculadora_emprestimos.service.EmprestimoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {

    private final EmprestimoService service;

    public EmprestimoController(EmprestimoService service) {
        this.service = service;
    }

    @PostMapping("/calcular")
    public ResponseEntity<List<Parcela>> calcular(@RequestBody EmprestimoRequest req) {
        List<Parcela> result = service.calcular(req);
        return ResponseEntity.ok(result);
    }
}

