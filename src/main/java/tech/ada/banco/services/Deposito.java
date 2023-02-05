package tech.ada.banco.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.ada.banco.exceptions.ResourceNotFoundException;
import tech.ada.banco.model.Conta;
import tech.ada.banco.repository.ContaRepository;

import java.math.BigDecimal;

@Service
@Slf4j
public final class Deposito {

    private final ContaRepository repository;

    public Deposito(ContaRepository repository) {
        this.repository = repository;
    }

    public BigDecimal executar(int numeroConta, BigDecimal valor) {
        Conta conta = repository.findContaByNumeroConta(numeroConta).orElseThrow(ResourceNotFoundException::new);

        conta.deposito(valor);
        repository.save(conta);
        log.info("O saldo da conta é de: R$ {}", conta.getSaldo());
        return conta.getSaldo();
    }
}
