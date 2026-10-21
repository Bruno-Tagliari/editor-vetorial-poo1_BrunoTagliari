package org.example.Models;

public class Circulo extends FormaBase {
    private double raio;

    /** Regra: raio estritamente positivo. O único ponto da lista é o centro. */
    public Circulo(Ponto centro, double raio) {
        if (centro == null) throw new IllegalArgumentException("Centro não pode ser nulo.");
        validarRaio(raio);
        this.raio = raio;
        pontos.add(centro.clonar());
    }

    private static void validarRaio(double r) {
        if (!(r > 0) || !Double.isFinite(r)) {
            throw new IllegalArgumentException("O raio do círculo deve ser estritamente positivo.");
        }
    }

    public double getRaio() { return raio; }
    public void setRaio(double raio) { validarRaio(raio); this.raio = raio; }

    @Override
    public void adicionarPonto(Ponto p) {
        throw new UnsupportedOperationException("Um círculo possui apenas o ponto central.");
    }

    @Override public double calcularArea() { return Math.PI * raio * raio; }
    @Override public double calcularPerimetro() { return 2 * Math.PI * raio; }
    @Override public Ponto getCentroide() { return pontos.get(0).clonar(); }

    @Override
    public boolean contemPonto(double px, double py) {
        return pontos.get(0).distancia(new Ponto(px, py)) <= raio;
    }

    @Override
    public void escalarEm(double fator, double cx, double cy) {
        super.escalarEm(fator, cx, cy); // valida o fator e move o centro
        raio *= fator;
    }

    @Override
    public FormaGeometrica clonar() {
        return copiarEstiloPara(new Circulo(pontos.get(0), raio));
    }
}
