package tech.ada.banco.services;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.ada.banco.exceptions.ResourceNotFoundException;
import tech.ada.banco.exceptions.SaldoInsuficienteException;
import tech.ada.banco.model.Conta;
import tech.ada.banco.model.ModalidadeConta;
import tech.ada.banco.repository.ContaRepository;

import java.math.BigDecimal;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaqueTest {

    private final ContaRepository repository = Mockito.mock(ContaRepository.class);
    private final Saque saque = new Saque(repository);

private Conta criarConta(double valor, int numeroDaConta){
    Conta conta = new Conta(ModalidadeConta.CC, null);
    conta.deposito(BigDecimal.valueOf(valor));
    when(repository.findContaByNumeroConta(numeroDaConta)).thenReturn(Optional.of(conta));
    assertEquals(BigDecimal.valueOf(valor), conta.getSaldo(),
            "O saldo inicial da conta deve ser alterado para " + valor);
    return conta;
}
    @Test
    void testSaqueParcial() {
        Conta conta = criarConta(10,10);

        BigDecimal resp = saque.executar(10, BigDecimal.ONE.setScale(2));

        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.valueOf(9).setScale(2), resp,
                "O valor de retorno da função tem que ser 9.00" +
                        "Saldo anterior vale 10.00 e o valor de saque é 1.00");
        assertEquals(BigDecimal.valueOf(9).setScale(2), conta.getSaldo());
    }

    @Test
    void testSaqueContaNaoEncontrada() {
        Conta conta = criarConta(10,10);

        try {
            saque.executar(1, BigDecimal.ONE);
            fail("A conta deveria não ter sido encontrada.");
        } catch (ResourceNotFoundException e) {

        }

        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(10), conta.getSaldo(), "O saldo da conta não pode ter sido alterado.");
    }

    @Test
    void testSaqueProblemaDeBancoDeDados() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.TEN);
        when(repository.findContaByNumeroConta(10)).thenThrow(RuntimeException.class);
        assertEquals(BigDecimal.valueOf(10), conta.getSaldo(),
                "O saldo inicial da conta deve ser alterado para 10");

        try {
            saque.executar(1, BigDecimal.ONE);
            fail("A conta deveria não ter sido encontrada. Por problema de conexao de banco de dados");
        } catch (RuntimeException e) {

        }

        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(10), conta.getSaldo(), "O saldo da conta não pode ter sido alterado.");
    }

    @Test
    void testSaqueMaiorSaldo() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.valueOf(5));
        when(repository.findContaByNumeroConta(10)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(5), conta.getSaldo(),
                "O saldo inicial da conta deve ser alterado para 5");

        assertThrows(SaldoInsuficienteException.class, () -> saque.executar(10, BigDecimal.valueOf(6)));
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(5), conta.getSaldo(), "O saldo da conta não se alterou");

    }
}