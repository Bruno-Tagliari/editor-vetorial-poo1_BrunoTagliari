package org.example.Models;

public class Poligono extends FormaBase {

    /** Polígono vazio; os vértices entram por adicionarPonto(). */
    public Poligono() { }

    /** Regra: um polígono precisa de no mínimo 3 pontos (chamar ao finalizar o desenho / carregar). */
    public void validar() {
        if (pontos.size() < 3) {
            throw new IllegalStateException("Um polígono deve ter no mínimo 3 pontos.");
        }
    }

    @Override
    public FormaGeometrica clonar() {
        Poligono copia = new Poligono();
        for (Ponto p : pontos) copia.adicionarPonto(p.clonar());
        return copiarEstiloPara(copia);
    }
}
