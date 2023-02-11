package tech.ada.banco.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.ada.banco.exceptions.ResourceNotFoundException;
import tech.ada.banco.model.Conta;
import tech.ada.banco.model.ModalidadeConta;
import tech.ada.banco.repository.ContaRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PixTest {
    private final ContaRepository repository = Mockito.mock(ContaRepository.class);
    private final Pix pix = new Pix(repository);

    private Conta criaConta(double valor, int numeroDaConta) {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.valueOf(valor));
        when(repository.findContaByNumeroConta(numeroDaConta)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(valor).setScale(2), conta.getSaldo(),
                "O saldo inicial da conta origem deve ser de " + valor);
        return conta;
    }

    @Test
    void testTransferenciaPixValorTotal() {
        Conta contaOrigem = criaConta(10, 3);
        Conta contaDestino = criaConta(0, 5);

        BigDecimal retornoDaTransferencia = pix.executar(3, 5, BigDecimal.valueOf(10));

        verify(repository, times(1)).save(contaOrigem);
        verify(repository, times(1)).save(contaDestino);

        assertEquals(BigDecimal.valueOf(0).setScale(2), retornoDaTransferencia,
                "O valor de retorno da função tem que ser 0. " +
                        "Saldo anterior vale 10 e o valor de saque é 10");
        assertEquals(BigDecimal.valueOf(0).setScale(2), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(10).setScale(2), contaDestino.getSaldo());
    }

    @Test
    void testTransferenciaPixValorParcial() {
        Conta contaOrigem = criaConta(10, 3);
        Conta contaDestino = criaConta(0, 5);

        BigDecimal retornoDaTransferencia = pix.executar(3, 5, BigDecimal.valueOf(7));

        verify(repository, times(1)).save(contaOrigem);
        verify(repository, times(1)).save(contaDestino);

        assertEquals(BigDecimal.valueOf(3).setScale(2), retornoDaTransferencia,
                "O valor de retorno da função tem que ser 9. " +
                        "Saldo anterior vale 13 e o valor de saque é 7");
        assertEquals(BigDecimal.valueOf(3).setScale(2), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(7).setScale(2), contaDestino.getSaldo());
    }

    @Test
    void testTransferenciaPixValorParcialComValorDecimal() {
        Conta contaOrigem = criaConta(13, 3);
        Conta contaDestino = criaConta(0, 5);

        BigDecimal retornoDaTransferencia = pix.executar(3, 5, BigDecimal.valueOf(1.42));

        verify(repository, times(1)).save(contaOrigem);
        verify(repository, times(1)).save(contaDestino);

        assertEquals(BigDecimal.valueOf(11.58).setScale(2), retornoDaTransferencia,
                "O valor de retorno da função tem que ser 11.58. " +
                        "Saldo anterior vale 13 e o valor de saque é 1.42");
        assertEquals(BigDecimal.valueOf(11.58).setScale(2), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(1.42).setScale(2), contaDestino.getSaldo());
    }

    @Test
    void testTransferenciaPixContaOrigemNaoEncontrada() {
        Conta contaOrigem = criaConta(13, 3);
        Conta contaDestino = criaConta(0, 5);

        try {
            pix.executar(7, 5, BigDecimal.valueOf(7));
            fail("A conta de origem deveria não ter sido encontrada.");
        } catch (ResourceNotFoundException e) {

        }

        verify(repository, times(0)).save(any());
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(13).setScale(2), contaOrigem.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
        assertEquals(BigDecimal.valueOf(0).setScale(2), contaDestino.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
    }

    @Test
    void testTransferenciaPixContaDestinoNaoEncontrada() {
        Conta contaOrigem = criaConta(13, 3);
        Conta contaDestino = criaConta(0, 5);

        try {
            pix.executar(3, 7, BigDecimal.valueOf(7));
            fail("A conta de destino deveria não ter sido encontrada.");
        } catch (ResourceNotFoundException e) {

        }

        verify(repository, times(0)).save(any());
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(13).setScale(2), contaOrigem.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
        assertEquals(BigDecimal.valueOf(0).setScale(2), contaDestino.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
    }

    @Test
    void testTransferenciaPixProblemaDeBancoDeDadosGravacaoDaContaDeOrigem() {
        Conta contaOrigem = criaConta(13, 3);
        Conta contaDestino = criaConta(0, 5);

        try {
            pix.executar(3, 7, BigDecimal.valueOf(7));
            fail("A conta Origem deveria não ter sido encontrada. Por problema de conexao de banco de dados");
        } catch (RuntimeException e) {

        }

        verify(repository, times(0)).save(any());
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(13).setScale(2), contaOrigem.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
        assertEquals(BigDecimal.valueOf(0).setScale(2), contaDestino.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
    }

    @Test
    void testTransferenciaPixProblemaDeBancoDeDadosGravacaoDaContaDeDestino() {
        Conta contaOrigem = criaConta(13, 3);
        Conta contaDestino = criaConta(0, 5);

        try {
            pix.executar(3, 7, BigDecimal.valueOf(7));
            fail("A conta Destino deveria não ter sido encontrada. Por problema de conexao de banco de dados");
        } catch (RuntimeException e) {

        }

        verify(repository, times(0)).save(any());
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(13).setScale(2), contaOrigem.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
        assertEquals(BigDecimal.valueOf(0).setScale(2), contaDestino.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
    }

}