package org.example.Controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;
import org.junit.jupiter.api.Test;

class HistoricoControllerTest {

    @Test
    void desfazerERefazerUsamCopiasProfundas() {
        HistoricoController h = new HistoricoController();
        List<FormaGeometrica> formas = new ArrayList<>();
        formas.add(new Circulo(new Ponto(0, 0), 5));

        h.registrarEstado(formas);           // memento do estado ANTES da alteração
        formas.get(0).transladar(100, 0);    // alteração

        List<FormaGeometrica> anterior = h.desfazer(formas);
        assertEquals(0, anterior.get(0).getCentroide().getX(), 1e-9);

        List<FormaGeometrica> refeito = h.refazer(anterior);
        assertEquals(100, refeito.get(0).getCentroide().getX(), 1e-9);
        assertNull(h.refazer(refeito));
    }

    @Test
    void novaAcaoLimpaPilhaDeRefazer() {
        HistoricoController h = new HistoricoController();
        List<FormaGeometrica> formas = new ArrayList<>();
        h.registrarEstado(formas);
        h.desfazer(formas);
        assertTrue(h.podeRefazer());
        h.registrarEstado(formas);
        assertFalse(h.podeRefazer());
    }
}
