package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

/** Insere quadrado, círculo ou hexágono: pressiona no centro e arrasta para definir o raio. */
public class FerramentaInserirForma implements Ferramenta {
    private static final double RAIO_PADRAO = 50;
    private static final double RAIO_MINIMO = 2;

    private final TipoForma tipo;
    private Ponto centroInsercao;

    public FerramentaInserirForma(TipoForma tipo) {
        this.tipo = tipo;
    }

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext c) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        centroInsercao = new Ponto(c.aplicarSnap(e.getX()), c.aplicarSnap(e.getY()));
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext c) {
        c.setMouse(c.aplicarSnap(e.getX()), c.aplicarSnap(e.getY()));
        if (centroInsercao == null) return;
        double raio = centroInsercao.distancia(new Ponto(c.aplicarSnap(e.getX()), c.aplicarSnap(e.getY())));
        c.setFormaEmAndamento(raio >= RAIO_MINIMO ? criar(centroInsercao, raio) : null);
        c.requestRedraw();
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext c) {
        if (centroInsercao == null) return;
        double raio = centroInsercao.distancia(new Ponto(c.aplicarSnap(e.getX()), c.aplicarSnap(e.getY())));
        if (raio < RAIO_MINIMO) raio = RAIO_PADRAO; // clique simples: tamanho padrão
        FormaGeometrica nova = c.aplicarEstiloAtual(criar(centroInsercao, raio));
        c.registrarEstado();
        c.adicionarForma(nova);
        c.limparSelecao();
        c.selecionarComGrupo(nova);
        c.setFormaEmAndamento(null);
        centroInsercao = null;
        c.requestRedraw();
    }

    @Override public void aoClicar(MouseEvent e, EditorContext c) { }

    private FormaGeometrica criar(Ponto centro, double raio) {
        switch (tipo) {
            case QUADRADO: return new Quadrado(centro, raio);
            case CIRCULO: return new Circulo(centro, raio);
            default: return new Hexagono(centro, raio);
        }
    }

    @Override
    public void aoDesativar(EditorContext c) {
        c.setFormaEmAndamento(null);
        centroInsercao = null;
    }
}
