package org.example.Models;

import java.util.List;

/**
 * Contrato das formas geométricas (padrão Prototype via clonar()).
 * Além dos métodos do enunciado, há as variantes "Em" (com pivô explícito),
 * usadas pelo Composite (GrupoFormas) para transformar várias formas em torno
 * de um centroide comum.
 */
public interface FormaGeometrica {
    void adicionarPonto(Ponto p);
    List<Ponto> getPontos();
    int getQuantidadePontos();
    String getCorPreenchimentoHex();
    void setCorPreenchimentoHex(String hex);
    String getCorBordaHex();
    void setCorBordaHex(String hex);
    double getEspessuraBorda();
    void setEspessuraBorda(double espessura);
    String getGroupId();
    void setGroupId(String groupId);
    double calcularArea();
    double calcularPerimetro();
    Ponto getCentroide();
    boolean contemPonto(double px, double py);
    void transladar(double dx, double dy);
    void escalar(double fator);
    void rotacionar(double anguloRadianos);
    void cisalhar(double shx, double shy);
    FormaGeometrica clonar();

    // --- extensões para transformação em torno de um pivô qualquer ---
    void escalarEm(double fator, double cx, double cy);
    void rotacionarEm(double anguloRadianos, double cx, double cy);
    void cisalharEm(double shx, double shy, double cx, double cy);
}
