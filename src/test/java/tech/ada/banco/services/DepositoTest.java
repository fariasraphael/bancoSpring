package tech.ada.banco.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.ada.banco.exceptions.ResourceNotFoundException;
import tech.ada.banco.exceptions.ValorInvalidoException;
import tech.ada.banco.model.Conta;
import tech.ada.banco.model.ModalidadeConta;
import tech.ada.banco.repository.ContaRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepositoTest {
    private final ContaRepository repository = Mockito.mock(ContaRepository.class);
    private final Deposito deposito = new Deposito(repository);

    @Test
    void testeDepositoEmConta() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.ONE);
        when(repository.findContaByNumeroConta(11)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(1), conta.getSaldo(), "O valor inicial da conta é 1");

        BigDecimal resp = deposito.executar(11, BigDecimal.TEN);

        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.valueOf(11), resp, "O valor de retorno da função deve ser 11, a conta inicia com 1 e recebe 10");
        assertEquals(BigDecimal.valueOf(11), conta.getSaldo());
    }

    @Test
    void testeDepositoEmContaNaoEncontrada() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.ONE);
        when(repository.findContaByNumeroConta(12)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(1), conta.getSaldo(), "O saldo inicial da conta deve ser alterado para 1");

        try {
            deposito.executar(13, BigDecimal.TEN);
            fail("A conta não deveria ter sido encontrada");
        } catch (ResourceNotFoundException e) {

        }

        verify(repository, times(0)).save(any());
        verify(repository, times(1)).findContaByNumeroConta(anyInt());
        assertEquals(BigDecimal.valueOf(0), conta.getSaldo(), "O saldo da conta não pode ter sido alterado.");
    }

    @Test
    void testeDepositoProblemaDeBancoDeDados() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.ONE);
        when(repository.findContaByNumeroConta(14)).thenThrow(RuntimeException.class);
        assertEquals(BigDecimal.valueOf(1), conta.getSaldo(), "O saldo inicial da conta deve ser alterado para 1");

        try {
            deposito.executar(2, BigDecimal.TEN);
            fail("A conta deveria não ter sido encontrada. Por problema de conexao de banco de dados");
        } catch (RuntimeException e) {

        }
    }

    @Test
    void testeDepositoDeValorNegativo() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.ONE);
        when(repository.findContaByNumeroConta(15)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(1), conta.getSaldo(), "O saldo inicial da conta deve ser alterado para 1");

        assertThrows(ValorInvalidoException.class, () -> deposito.executar(15, BigDecimal.valueOf(-10)));
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(1), conta.getSaldo(), "O saldo da conta não se alterou");
    }

}