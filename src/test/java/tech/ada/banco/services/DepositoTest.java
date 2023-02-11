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

    private Conta criaConta (double valor, int numeroDaConta){
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.valueOf(valor));
        when(repository.findContaByNumeroConta(numeroDaConta)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(valor).setScale(2), conta.getSaldo(),
                "O valor inicial da conta é " + valor);

        return conta;
    }
    @Test
    void testeDepositoEmConta() {
        Conta conta = criaConta(1,10);

        BigDecimal retorno = deposito.executar(10, BigDecimal.TEN);

        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.valueOf(11).setScale(2), retorno,
                "O valor de retorno da função deve ser 11, a conta inicia com 1 e recebe 10");
        assertEquals(BigDecimal.valueOf(11).setScale(2), conta.getSaldo());
    }

    @Test
    void testeDepositoEmContaNaoEncontrada() {
        Conta conta = criaConta(1,10);

        try {
            deposito.executar(13, BigDecimal.TEN);
            fail("A conta não deveria ter sido encontrada");
        } catch (ResourceNotFoundException e) {

        }

        verify(repository, times(0)).save(any());
        verify(repository, times(1)).findContaByNumeroConta(anyInt());
        assertEquals(BigDecimal.valueOf(1).setScale(2), conta.getSaldo(), "O saldo da conta não pode ter sido alterado.");
    }

    @Test
    void testeDepositoProblemaDeBancoDeDados() {
        Conta conta = criaConta(1,10);
        try {
            deposito.executar(2, BigDecimal.TEN);
            fail("A conta deveria não ter sido encontrada. Por problema de conexao de banco de dados");
        } catch (RuntimeException e) {

        }
    }

    @Test
    void testeDepositoDeValorNegativo() {
        Conta conta = criaConta(1,10);
        assertThrows(ResourceNotFoundException.class,
                () -> deposito.executar(15, BigDecimal.valueOf(-10)));
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(1).setScale(2), conta.getSaldo(), "O saldo da conta não se alterou");
    }

    @Test
    void testeDepositoArredontamentoPraCima() {
        Conta conta = criaConta(10,10);

        BigDecimal retorno = deposito.executar(10, BigDecimal.valueOf(1.42857));
        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.valueOf(11.43).setScale(2), retorno,
                "O valor de retorno da função deve ser 11.43, a conta inicia com 10 e recebe 1.43");
        assertEquals(BigDecimal.valueOf(11.43).setScale(2), conta.getSaldo());
    }
    @Test
    void testeDepositoArredontamentoPraBaixo() {
        Conta conta = criaConta(10,10);

        BigDecimal retorno = deposito.executar(10, BigDecimal.valueOf(1.42321));
        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.valueOf(11.42).setScale(2), retorno,
                "O valor de retorno da função deve ser 11.42, a conta inicia com 10 e recebe 1.42");
        assertEquals(BigDecimal.valueOf(11.42).setScale(2), conta.getSaldo());


    }

}