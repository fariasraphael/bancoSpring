package tech.ada.banco.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tech.ada.banco.model.Conta;
import tech.ada.banco.model.ModalidadeConta;
import tech.ada.banco.model.Pessoa;
import tech.ada.banco.repository.ContaRepository;
import tech.ada.banco.repository.PessoaRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
abstract class BaseContaTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ContaRepository repository;
    protected PessoaRepository repositoryP;

    protected Conta criarConta(BigDecimal saldo) {
        Conta contaBase = repository.save(new Conta(ModalidadeConta.CC, null));
        contaBase.deposito(saldo);
        contaBase = repository.save(contaBase);
        assertEquals(saldo.setScale(2), contaBase.getSaldo());
        return contaBase;
    }

    protected Conta obtemContaDoBanco(Conta contaBase) {
        return repository.findContaByNumeroConta(contaBase.getNumeroConta())
                .orElseThrow(NullPointerException::new);
    }

    protected Pessoa obtemPessoa (Pessoa pessoaBase) {
        return repositoryP.findPessoaByCPF(pessoaBase.getCPF())
                .orElseThrow(NullPointerException::new);
    }

}
