package org.example.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementação base: guarda pontos/estilo e implementa geometria de polígonos. */
public abstract class FormaBase implements FormaGeometrica {
    public static final String COR_PREENCHIMENTO_PADRAO = "#D3D3D3";
    public static final String COR_BORDA_PADRAO = "#000000";

    protected List<Ponto> pontos = new ArrayList<>();
    protected String corPreenchimentoHex = COR_PREENCHIMENTO_PADRAO;
    protected String corBordaHex = COR_BORDA_PADRAO;
    protected double espessuraBorda = 1.0;
    protected String groupId;

    @Override
    public void adicionarPonto(Ponto p) {
        if (p == null) throw new IllegalArgumentException("Ponto não pode ser nulo.");
        pontos.add(p);
    }

    @Override public List<Ponto> getPontos() { return Collections.unmodifiableList(pontos); }
    @Override public int getQuantidadePontos() { return pontos.size(); }
    @Override public String getCorPreenchimentoHex() { return corPreenchimentoHex; }
    @Override public String getCorBordaHex() { return corBordaHex; }
    @Override public double getEspessuraBorda() { return espessuraBorda; }
    @Override public String getGroupId() { return groupId; }
    @Override public void setGroupId(String groupId) { this.groupId = groupId; }

    @Override
    public void setCorPreenchimentoHex(String hex) {
        validarHex(hex);
        this.corPreenchimentoHex = hex;
    }

    @Override
    public void setCorBordaHex(String hex) {
        validarHex(hex);
        this.corBordaHex = hex;
    }

    @Override
    public void setEspessuraBorda(double espessura) {
        if (!(espessura > 0) || !Double.isFinite(espessura)) {
            throw new IllegalArgumentException("Espessura da borda deve ser positiva.");
        }
        this.espessuraBorda = espessura;
    }

    public static void validarHex(String hex) {
        if (hex == null || !hex.matches("#[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Cor inválida (use #RRGGBB): " + hex);
        }
    }

    /** Fórmula de Gauss (shoelace), eq. (1) do enunciado. */
    @Override
    public double calcularArea() {
        int n = pontos.size();
        if (n < 3) return 0;
        double soma = 0;
        for (int i = 0; i < n; i++) {
            Ponto a = pontos.get(i);
            Ponto b = pontos.get((i + 1) % n); // o último termo fecha com (xn*y1 - x1*yn)
            soma += a.getX() * b.getY() - b.getX() * a.getY();
        }
        return Math.abs(soma) / 2.0;
    }

    /** Soma das distâncias euclidianas entre vértices consecutivos (polígono fechado). */
    @Override
    public double calcularPerimetro() {
        int n = pontos.size();
        if (n < 2) return 0;
        double soma = 0;
        for (int i = 0; i < n; i++) {
            soma += pontos.get(i).distancia(pontos.get((i + 1) % n));
        }
        return soma;
    }

    /** Centroide = média dos vértices (exato para formas regulares). */
    @Override
    public Ponto getCentroide() {
        double sx = 0, sy = 0;
        for (Ponto p : pontos) {
            sx += p.getX();
            sy += p.getY();
        }
        int n = Math.max(1, pontos.size());
        return new Ponto(sx / n, sy / n);
    }

    /** Teste ponto-em-polígono (ray casting). */
    @Override
    public boolean contemPonto(double px, double py) {
        boolean dentro = false;
        int n = pontos.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = pontos.get(i).getX(), yi = pontos.get(i).getY();
            double xj = pontos.get(j).getX(), yj = pontos.get(j).getY();
            if ((yi > py) != (yj > py) && px < (xj - xi) * (py - yi) / (yj - yi) + xi) {
                dentro = !dentro;
            }
        }
        return dentro;
    }

    @Override
    public void transladar(double dx, double dy) {
        for (Ponto p : pontos) p.mover(dx, dy);
    }

    @Override
    public void escalar(double fator) {
        Ponto c = getCentroide();
        escalarEm(fator, c.getX(), c.getY());
    }

    @Override
    public void rotacionar(double anguloRadianos) {
        Ponto c = getCentroide();
        rotacionarEm(anguloRadianos, c.getX(), c.getY());
    }

    @Override
    public void cisalhar(double shx, double shy) {
        Ponto c = getCentroide();
        cisalharEm(shx, shy, c.getX(), c.getY());
    }

    @Override
    public void escalarEm(double fator, double cx, double cy) {
        if (!(fator > 0) || !Double.isFinite(fator)) {
            throw new IllegalArgumentException("Fator de escala deve ser positivo.");
        }
        for (Ponto p : pontos) {
            double nx = cx + (p.getX() - cx) * fator;
            double ny = cy + (p.getY() - cy) * fator;
            p.setX(nx);
            p.setY(ny);
        }
    }

    @Override
    public void rotacionarEm(double ang, double cx, double cy) {
        double cos = Math.cos(ang), sin = Math.sin(ang);
        for (Ponto p : pontos) {
            double dx = p.getX() - cx, dy = p.getY() - cy;
            double nx = cx + dx * cos - dy * sin;
            double ny = cy + dx * sin + dy * cos;
            p.setX(nx);
            p.setY(ny);
        }
    }

    @Override
    public void cisalharEm(double shx, double shy, double cx, double cy) {
        for (Ponto p : pontos) {
            double dx = p.getX() - cx, dy = p.getY() - cy;
            double nx = cx + dx + shx * dy;
            double ny = cy + dy + shy * dx;
            p.setX(nx);
            p.setY(ny);
        }
    }

    // --- apoio ao Prototype: cópia profunda ---
    protected List<Ponto> clonarPontos() {
        List<Ponto> copia = new ArrayList<>();
        for (Ponto p : pontos) copia.add(p.clonar());
        return copia;
    }

    protected <T extends FormaBase> T copiarEstiloPara(T destino) {
        destino.corPreenchimentoHex = corPreenchimentoHex;
        destino.corBordaHex = corBordaHex;
        destino.espessuraBorda = espessuraBorda;
        destino.groupId = groupId;
        return destino;
    }
}
