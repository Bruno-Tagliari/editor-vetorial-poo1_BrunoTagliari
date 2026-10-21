package org.example.Models;

import java.util.List;

/** Hexágono regular com vértices à esquerda e à direita. */
public class Hexagono extends FormaBase {

    public Hexagono(Ponto centro, double raio) {
        if (centro == null || !(raio > 0) || !Double.isFinite(raio)) {
            throw new IllegalArgumentException("Hexágono exige centro e raio positivo.");
        }
        for (int k = 0; k < 6; k++) {
            double ang = k * Math.PI / 3;
            pontos.add(new Ponto(centro.getX() + raio * Math.cos(ang), centro.getY() + raio * Math.sin(ang)));
        }
    }

    public Hexagono(List<Ponto> vertices) {
        if (vertices == null || vertices.size() != 6) {
            throw new IllegalArgumentException("Hexágono exige exatamente 6 vértices.");
        }
        for (Ponto p : vertices) pontos.add(p.clonar());
    }

    @Override
    public FormaGeometrica clonar() {
        return copiarEstiloPara(new Hexagono(pontos));
    }
}
