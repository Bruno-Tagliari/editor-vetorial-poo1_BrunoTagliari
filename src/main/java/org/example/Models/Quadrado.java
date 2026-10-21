package org.example.Models;

import java.util.List;

/** Quadrado alinhado aos eixos; vértices calculados a partir do centro e do raio (circunscrito). */
public class Quadrado extends FormaBase {

    public Quadrado(Ponto centro, double raio) {
        if (centro == null || !(raio > 0) || !Double.isFinite(raio)) {
            throw new IllegalArgumentException("Quadrado exige centro e raio positivo.");
        }
        for (int k = 0; k < 4; k++) {
            double ang = Math.PI / 4 + k * Math.PI / 2;
            pontos.add(new Ponto(centro.getX() + raio * Math.cos(ang), centro.getY() + raio * Math.sin(ang)));
        }
    }

    /** Usado ao clonar/carregar de arquivo (vértices já calculados ou editados). */
    public Quadrado(List<Ponto> vertices) {
        if (vertices == null || vertices.size() != 4) {
            throw new IllegalArgumentException("Quadrado exige exatamente 4 vértices.");
        }
        for (Ponto p : vertices) pontos.add(p.clonar());
    }

    @Override
    public FormaGeometrica clonar() {
        return copiarEstiloPara(new Quadrado(pontos));
    }
}
