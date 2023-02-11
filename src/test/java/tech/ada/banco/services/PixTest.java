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

    @Test
    void testTransferenciaValorTotal(){
        Conta contaOrigem = new Conta(ModalidadeConta.CC, null);
        Conta contaDestino = new Conta(ModalidadeConta.CC, null);
        contaOrigem.deposito(BigDecimal.valueOf(13));

        when(repository.findContaByNumeroConta(3)).thenReturn(Optional.of(contaOrigem));
        when(repository.findContaByNumeroConta(5)).thenReturn(Optional.of(contaDestino));

        assertEquals(BigDecimal.valueOf(13), contaOrigem.getSaldo(),
                "O saldo inicial da conta origem deve ser de 13");
        assertEquals(BigDecimal.ZERO, contaDestino.getSaldo(),
                "O saldo inicial da conta origem deve ser de 0");

        BigDecimal retornoDaTransferencia = pix.executar(3,5,BigDecimal.valueOf(13));

        verify(repository, times(1)).save(contaOrigem);
        verify(repository, times(1)).save(contaDestino);

        assertEquals(BigDecimal.valueOf(0), retornoDaTransferencia,
                "O valor de retorno da função tem que ser 0. " +
                        "Saldo anterior vale 13 e o valor de saque é 13");
        assertEquals(BigDecimal.valueOf(0), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(13), contaDestino.getSaldo());
    }
    @Test
    void testTransferenciaValorParcial(){
        Conta contaOrigem = new Conta(ModalidadeConta.CC, null);
        Conta contaDestino = new Conta(ModalidadeConta.CC, null);
        contaOrigem.deposito(BigDecimal.valueOf(13));

        when(repository.findContaByNumeroConta(3)).thenReturn(Optional.of(contaOrigem));
        when(repository.findContaByNumeroConta(5)).thenReturn(Optional.of(contaDestino));

        assertEquals(BigDecimal.valueOf(13), contaOrigem.getSaldo(),
                "O saldo inicial da conta origem deve ser de 13");
        assertEquals(BigDecimal.ZERO, contaDestino.getSaldo(),
                "O saldo inicial da conta origem deve ser de 0");

        BigDecimal retornoDaTransferencia = pix.executar(3,5,BigDecimal.valueOf(7));

        verify(repository, times(1)).save(contaOrigem);
        verify(repository, times(1)).save(contaDestino);

        assertEquals(BigDecimal.valueOf(6), retornoDaTransferencia,
                "O valor de retorno da função tem que ser 9. " +
                        "Saldo anterior vale 13 e o valor de saque é 7");
        assertEquals(BigDecimal.valueOf(6), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(7), contaDestino.getSaldo());
    }
    @Test
    void testTransferenciaValorParcialComValorDecimal(){
        Conta contaOrigem = new Conta(ModalidadeConta.CC, null);
        Conta contaDestino = new Conta(ModalidadeConta.CC, null);
        contaOrigem.deposito(BigDecimal.valueOf(13));

        when(repository.findContaByNumeroConta(3)).thenReturn(Optional.of(contaOrigem));
        when(repository.findContaByNumeroConta(5)).thenReturn(Optional.of(contaDestino));

        assertEquals(BigDecimal.valueOf(13), contaOrigem.getSaldo(),
                "O saldo inicial da conta origem deve ser de 13");
        assertEquals(BigDecimal.ZERO, contaDestino.getSaldo(),
                "O saldo inicial da conta origem deve ser de 0");

        BigDecimal retornoDaTransferencia = pix.executar(3,5,BigDecimal.valueOf(1.42));

        verify(repository, times(1)).save(contaOrigem);
        verify(repository, times(1)).save(contaDestino);

        assertEquals(BigDecimal.valueOf(11.58), retornoDaTransferencia,
                "O valor de retorno da função tem que ser 11.58. " +
                        "Saldo anterior vale 13 e o valor de saque é 1.42");
        assertEquals(BigDecimal.valueOf(11.58), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(1.42), contaDestino.getSaldo());
    }
    @Test
    void testTransferenciaContaOrigemNaoEncontrada(){
        Conta contaOrigem = new Conta(ModalidadeConta.CC, null);
        Conta contaDestino = new Conta(ModalidadeConta.CC, null);
        contaOrigem.deposito(BigDecimal.valueOf(13));

        when(repository.findContaByNumeroConta(3)).thenReturn(Optional.of(contaOrigem));
        when(repository.findContaByNumeroConta(5)).thenReturn(Optional.of(contaDestino));

        assertEquals(BigDecimal.valueOf(13), contaOrigem.getSaldo(),
                "O saldo inicial da conta origem deve ser de 13");
        assertEquals(BigDecimal.ZERO, contaDestino.getSaldo(),
                "O saldo inicial da conta origem deve ser de 0");

        try{
            pix.executar(7,5,BigDecimal.valueOf(7));
            fail("A conta de origem deveria não ter sido encontrada.");
        } catch (ResourceNotFoundException e){

        }

        verify(repository, times(0)).save(any());
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(13), contaOrigem.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
        assertEquals(BigDecimal.valueOf(0), contaDestino.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
    }
    @Test
    void testTransferenciaContaDestinoNaoEncontrada(){
        Conta contaOrigem = new Conta(ModalidadeConta.CC, null);
        Conta contaDestino = new Conta(ModalidadeConta.CC, null);
        contaOrigem.deposito(BigDecimal.valueOf(13));

        when(repository.findContaByNumeroConta(3)).thenReturn(Optional.of(contaOrigem));
        when(repository.findContaByNumeroConta(5)).thenReturn(Optional.of(contaDestino));

        assertEquals(BigDecimal.valueOf(13), contaOrigem.getSaldo(),
                "O saldo inicial da conta origem deve ser de 13");
        assertEquals(BigDecimal.ZERO, contaDestino.getSaldo(),
                "O saldo inicial da conta origem deve ser de 0");

        try{
            pix.executar(3,7,BigDecimal.valueOf(7));
            fail("A conta de destino deveria não ter sido encontrada.");
        } catch (ResourceNotFoundException e){

        }

        verify(repository, times(0)).save(any());
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(13), contaOrigem.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
        assertEquals(BigDecimal.valueOf(0), contaDestino.getSaldo(),
                "O saldo da conta não pode ter sido alterado.");
    }
}