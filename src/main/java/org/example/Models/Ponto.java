package org.example.Models;

/** Ponto 2D mutável. Também serve de "protótipo" simples via clonar(). */
public class Ponto {
    private double x;
    private double y;

    public Ponto(double x, double y) {
        validar(x, y);
        this.x = x;
        this.y = y;
    }

    private static void validar(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Coordenadas devem ser números finitos.");
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { validar(x, y); this.x = x; }
    public void setY(double y) { validar(x, y); this.y = y; }

    public void mover(double dx, double dy) {
        validar(x + dx, y + dy);
        x += dx;
        y += dy;
    }

    public double distancia(Ponto outro) {
        return Math.hypot(x - outro.x, y - outro.y);
    }

    public Ponto clonar() {
        return new Ponto(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
