package org.example.Models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ModelosTest {
    private static final double EPS = 1e-9;

    private static Poligono triangulo() {
        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(0, 0));
        p.adicionarPonto(new Ponto(4, 0));
        p.adicionarPonto(new Ponto(0, 3));
        return p;
    }

    @Test
    void poligonoExigeTresPontos() {
        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(0, 0));
        p.adicionarPonto(new Ponto(1, 1));
        assertThrows(IllegalStateException.class, p::validar);
        p.adicionarPonto(new Ponto(2, 0));
        assertDoesNotThrow(p::validar);
    }

    @Test
    void circuloExigeRaioPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), 0));
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), -3));
    }

    @Test
    void areaEPerimetroDoQuadrado() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 10); // lado = 10*sqrt(2)
        assertEquals(200, q.calcularArea(), 1e-6);
        assertEquals(4 * 10 * Math.sqrt(2), q.calcularPerimetro(), 1e-6);
    }

    @Test
    void areaDoTrianguloPelaFormulaDeGauss() {
        Poligono t = triangulo();
        assertEquals(6, t.calcularArea(), EPS);
        assertEquals(12, t.calcularPerimetro(), EPS);
    }

    @Test
    void transformacoes() {
        Quadrado q = new Quadrado(new Ponto(50, 50), 10);
        double area = q.calcularArea();
        q.escalar(2);
        assertEquals(area * 4, q.calcularArea(), 1e-6);
        q.rotacionar(Math.PI / 3);
        assertEquals(area * 4, q.calcularArea(), 1e-6); // rotação preserva a área
        Ponto c = q.getCentroide();
        q.transladar(5, -7);
        assertEquals(c.getX() + 5, q.getCentroide().getX(), 1e-6);
        assertEquals(c.getY() - 7, q.getCentroide().getY(), 1e-6);
    }

    @Test
    void clonarNaoCompartilhaReferencias() {
        Poligono original = triangulo();
        FormaGeometrica copia = original.clonar();
        copia.transladar(10, 10);
        assertEquals(0, original.getPontos().get(0).getX(), EPS);
        assertNotSame(original.getPontos().get(0), copia.getPontos().get(0));
    }

    @Test
    void compositeTrataGrupoComoUmaForma() {
        Circulo a = new Circulo(new Ponto(0, 0), 5);
        Circulo b = new Circulo(new Ponto(20, 0), 5);
        GrupoFormas g = new GrupoFormas(List.of(a, b));
        g.escalar(2); // pivô = centroide do grupo (10, 0)
        assertEquals(10, a.getRaio(), EPS);
        assertEquals(-10, a.getCentroide().getX(), EPS);
        assertEquals(30, b.getCentroide().getX(), EPS);
    }

    @Test
    void contemPonto() {
        assertTrue(new Circulo(new Ponto(0, 0), 5).contemPonto(3, 3));
        assertFalse(new Hexagono(new Ponto(0, 0), 5).contemPonto(9, 9));
    }
}
