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

class PixControllerTest extends BaseContaTest {

    private final String baseUri = "/pix";

    @Test
    void testeTransferenciaPixValorInteiro() throws Exception {

        Conta contaOrigem = criarConta(BigDecimal.valueOf(11));
        Conta contaDestino = criarConta(BigDecimal.ZERO);

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem.getNumeroConta())
                                .param("destino", String.valueOf(contaDestino.getNumeroConta()))
                                .param("valor", "3")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);
        assertEquals("8", response);
        assertEquals(BigDecimal.valueOf(8), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(3), contaDestino.getSaldo());
    }

    @Test
    void testeTransferenciaPixValorDecimal() throws Exception {

        Conta contaOrigem = criarConta(BigDecimal.valueOf(0.11));
        Conta contaDestino = criarConta(BigDecimal.ZERO);

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem.getNumeroConta())
                                .param("destino", String.valueOf(contaDestino.getNumeroConta()))
                                .param("valor", "0.07")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);
        assertEquals("0.04", response);
        assertEquals(BigDecimal.valueOf(0.04), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(0.07), contaDestino.getSaldo());

    }

    @Test
    void testeTransferenciaPixValorMaiorQueSaldoEmConta() throws Exception {

        Conta contaOrigem = criarConta(BigDecimal.ZERO);
        Conta contaDestino = criarConta(BigDecimal.ONE);

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem.getNumeroConta())
                                .param("destino", String.valueOf(contaDestino.getNumeroConta()))
                                .param("valor", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andReturn().getResponse().getErrorMessage();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);
        assertEquals("Limite acima do saldo disponível!", response);
        assertEquals(BigDecimal.ZERO, contaOrigem.getSaldo());
        assertEquals(BigDecimal.ONE, contaDestino.getSaldo());
    }

    @Test
    void testeTransferenciaPixValorNegativo() throws Exception {

        Conta contaOrigem = criarConta(BigDecimal.ZERO);
        Conta contaDestino = criarConta(BigDecimal.ONE);

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem.getNumeroConta())
                                .param("destino", String.valueOf(contaDestino.getNumeroConta()))
                                .param("valor", "-1")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andReturn().getResponse().getErrorMessage();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);
        assertEquals("Valor informado está inválido.", response);
        assertEquals(BigDecimal.ZERO, contaOrigem.getSaldo());
        assertEquals(BigDecimal.ONE, contaDestino.getSaldo());
    }

    @Test
    void testeTransferenciaPixDeContaOrigemIvalida() throws Exception {

        Conta contaOrigem = criarConta(BigDecimal.TEN);
        Conta contaDestino = criarConta(BigDecimal.ONE);
        Optional<Conta> contaInexistente = repository.findContaByNumeroConta(9998);
        assertTrue(contaInexistente.isEmpty());

        String response =
                mvc.perform(post(baseUri + "/9998")
                                .param("destino", String.valueOf(contaDestino.getNumeroConta()))
                                .param("valor", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andReturn().getResponse().getErrorMessage();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        assertEquals("Recurso não encontrado.", response);
        assertEquals(BigDecimal.TEN, contaOrigem.getSaldo());
        assertEquals(BigDecimal.ONE, contaDestino.getSaldo());
    }
    @Test
    void testeTransferenciaPixParaContaDestinoIvalida() throws Exception {

        Conta contaOrigem = criarConta(BigDecimal.TEN);
        Conta contaDestino = criarConta(BigDecimal.ONE);
        Optional<Conta> contaInexistente = repository.findContaByNumeroConta(9997);
        assertTrue(contaInexistente.isEmpty());

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem)
                                .param("destino", "9997")
                                .param("valor", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andReturn().getResponse().getErrorMessage();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        //assertEquals("Recurso não encontrado.", response);
        assertEquals(BigDecimal.TEN, contaOrigem.getSaldo());
        assertEquals(BigDecimal.ONE, contaDestino.getSaldo());
    }
}