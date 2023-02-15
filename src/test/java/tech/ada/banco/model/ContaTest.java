package tech.ada.banco.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContaTest {

    private final Pessoa pessoa = new Pessoa("João", "123.456.789-00",LocalDate.of(1982,01,19));
    private Conta conta;



    @Test
    void testContaDeposito() {
        conta = new Conta(ModalidadeConta.CC, pessoa);
        conta.deposito(BigDecimal.TEN);

        assertEquals(BigDecimal.TEN.setScale(2), conta.getSaldo());
    }
    @Test
    void testeContaSaqueTotal(){
        conta = new Conta(ModalidadeConta.CC, pessoa);
        conta.deposito(BigDecimal.TEN);
        conta.saque(BigDecimal.TEN);
        assertEquals(BigDecimal.ZERO.setScale(2), conta.getSaldo());
    }
    @Test
    void testeContaSaqueParcial(){
        conta = new Conta(ModalidadeConta.CC, pessoa);
        conta.deposito(BigDecimal.TEN);
        conta.saque(BigDecimal.valueOf(2));
        assertEquals(BigDecimal.valueOf(8).setScale(2), conta.getSaldo());
    }
    @Test
    void testeContaSaqueValorDecimal(){
        conta = new Conta(ModalidadeConta.CC, pessoa);
        conta.deposito(BigDecimal.TEN);
        conta.saque(BigDecimal.valueOf(2.1));
        assertEquals(BigDecimal.valueOf(7.9).setScale(2), conta.getSaldo());
    }

}