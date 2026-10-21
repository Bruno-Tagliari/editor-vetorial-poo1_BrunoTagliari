package org.example.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Padrão Composite: um conjunto de formas (mesmo groupId, ou a seleção atual) que se
 * comporta como UMA forma lógica. Escala, rotação e cisalhamento usam o centroide do
 * conjunto como pivô comum.
 */
public class GrupoFormas implements FormaGeometrica {
    private final List<FormaGeometrica> filhos = new ArrayList<>();

    public GrupoFormas(List<? extends FormaGeometrica> formas) {
        if (formas == null || formas.isEmpty()) {
            throw new IllegalArgumentException("Um grupo precisa de ao menos uma forma.");
        }
        filhos.addAll(formas);
    }

    public List<FormaGeometrica> getFilhos() { return Collections.unmodifiableList(filhos); }

    @Override
    public void adicionarPonto(Ponto p) {
        throw new UnsupportedOperationException("Não é possível adicionar pontos a um grupo.");
    }

    @Override
    public List<Ponto> getPontos() {
        List<Ponto> todos = new ArrayList<>();
        for (FormaGeometrica f : filhos) todos.addAll(f.getPontos());
        return Collections.unmodifiableList(todos);
    }

    @Override
    public int getQuantidadePontos() {
        int total = 0;
        for (FormaGeometrica f : filhos) total += f.getQuantidadePontos();
        return total;
    }

    @Override public String getCorPreenchimentoHex() { return filhos.get(0).getCorPreenchimentoHex(); }
    @Override public String getCorBordaHex() { return filhos.get(0).getCorBordaHex(); }
    @Override public double getEspessuraBorda() { return filhos.get(0).getEspessuraBorda(); }
    @Override public String getGroupId() { return filhos.get(0).getGroupId(); }

    @Override public void setCorPreenchimentoHex(String hex) { for (FormaGeometrica f : filhos) f.setCorPreenchimentoHex(hex); }
    @Override public void setCorBordaHex(String hex) { for (FormaGeometrica f : filhos) f.setCorBordaHex(hex); }
    @Override public void setEspessuraBorda(double e) { for (FormaGeometrica f : filhos) f.setEspessuraBorda(e); }
    @Override public void setGroupId(String id) { for (FormaGeometrica f : filhos) f.setGroupId(id); }

    @Override
    public double calcularArea() {
        double s = 0;
        for (FormaGeometrica f : filhos) s += f.calcularArea();
        return s;
    }

    @Override
    public double calcularPerimetro() {
        double s = 0;
        for (FormaGeometrica f : filhos) s += f.calcularPerimetro();
        return s;
    }

    @Override
    public Ponto getCentroide() {
        double sx = 0, sy = 0;
        for (FormaGeometrica f : filhos) {
            Ponto c = f.getCentroide();
            sx += c.getX();
            sy += c.getY();
        }
        return new Ponto(sx / filhos.size(), sy / filhos.size());
    }

    @Override
    public boolean contemPonto(double px, double py) {
        for (FormaGeometrica f : filhos) if (f.contemPonto(px, py)) return true;
        return false;
    }

    @Override public void transladar(double dx, double dy) { for (FormaGeometrica f : filhos) f.transladar(dx, dy); }

    @Override
    public void escalar(double fator) {
        Ponto c = getCentroide();
        escalarEm(fator, c.getX(), c.getY());
    }

    @Override
    public void rotacionar(double ang) {
        Ponto c = getCentroide();
        rotacionarEm(ang, c.getX(), c.getY());
    }

    @Override
    public void cisalhar(double shx, double shy) {
        Ponto c = getCentroide();
        cisalharEm(shx, shy, c.getX(), c.getY());
    }

    @Override public void escalarEm(double f, double cx, double cy) { for (FormaGeometrica x : filhos) x.escalarEm(f, cx, cy); }
    @Override public void rotacionarEm(double a, double cx, double cy) { for (FormaGeometrica x : filhos) x.rotacionarEm(a, cx, cy); }
    @Override public void cisalharEm(double sx, double sy, double cx, double cy) { for (FormaGeometrica x : filhos) x.cisalharEm(sx, sy, cx, cy); }

    @Override
    public FormaGeometrica clonar() {
        List<FormaGeometrica> copia = new ArrayList<>();
        for (FormaGeometrica f : filhos) copia.add(f.clonar());
        return new GrupoFormas(copia);
    }
}
