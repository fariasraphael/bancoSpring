package tech.ada.banco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tech.ada.banco.model.Conta;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



class DepositoControllerTest extends BaseContaTest {

    private final String baseUri = "/deposito";

    @Test
    void testeDepositoValorInteiro() throws Exception {
        Conta contaBase = criarConta(BigDecimal.ZERO);

        String response =
                mvc.perform(post(baseUri + "/" + contaBase.getNumeroConta())
                                .param("valor", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        contaBase = obtemContaDoBanco(contaBase);
        assertEquals("10.00", response);
        assertEquals(BigDecimal.TEN.setScale(2), contaBase.getSaldo());
    }

    @Test
    void testeDepositoValorNegativo() throws Exception {
        Conta contaBase = criarConta(BigDecimal.ZERO);

        String response =
                mvc.perform(post(baseUri + "/" + contaBase.getNumeroConta())
                                .param("valor", "-10")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andReturn().getResponse().getErrorMessage();

        contaBase = obtemContaDoBanco(contaBase);
        assertEquals("Valor informado está inválido.", response);
        assertEquals(BigDecimal.ZERO.setScale(2), contaBase.getSaldo());
    }

    @Test
    void testeDepositoValorComDecimal() throws Exception {
        Conta contaBase = criarConta(BigDecimal.valueOf(0.13));

        String response =
                mvc.perform(post(baseUri + "/" + contaBase.getNumeroConta())
                                .param("valor", "0.03")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        contaBase = obtemContaDoBanco(contaBase);
        assertEquals("0.16", response);
        assertEquals(BigDecimal.valueOf(0.16), contaBase.getSaldo());
    }

    @Test
    void testeDepositoEmContaInvalida() throws Exception {
        Conta contaBase = criarConta(BigDecimal.ZERO);
        Optional<Conta> contaInexistente = repository.findContaByNumeroConta(9999);
        assertTrue(contaInexistente.isEmpty());

        mvc.perform(post(baseUri + "/9999")
                        .param("valor", "0.13")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        contaBase = obtemContaDoBanco(contaBase);
        assertEquals(BigDecimal.ZERO.setScale(2), contaBase.getSaldo());
    }
}