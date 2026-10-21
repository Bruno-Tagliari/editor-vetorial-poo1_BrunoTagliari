package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/** Arrasta vértices individuais das formas selecionadas (círculos não possuem vértices). */
public class FerramentaEditarVertice implements Ferramenta {
    public static final double RAIO_CAPTURA = 10;

    private Ponto verticeSelecionado;
    private boolean estadoRegistrado;

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext c) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        verticeSelecionado = null;
        estadoRegistrado = false;

        Ponto clique = new Ponto(e.getX(), e.getY());
        for (FormaGeometrica f : c.getSelecionados()) {
            if (f.getQuantidadePontos() < 3) continue;
            for (Ponto p : f.getPontos()) {
                if (p.distancia(clique) <= RAIO_CAPTURA) {
                    verticeSelecionado = p;
                    return;
                }
            }
        }
        // não acertou vértice: seleciona a forma sob o cursor (ou limpa a seleção)
        FormaGeometrica alvo = c.formaEm(e.getX(), e.getY());
        c.limparSelecao();
        if (alvo != null) c.selecionarComGrupo(alvo);
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext c) {
        c.setMouse(c.aplicarSnap(e.getX()), c.aplicarSnap(e.getY()));
        if (verticeSelecionado == null) return;
        if (!estadoRegistrado) {
            c.registrarEstado();
            estadoRegistrado = true;
        }
        verticeSelecionado.setX(c.aplicarSnap(e.getX()));
        verticeSelecionado.setY(c.aplicarSnap(e.getY()));
        c.requestRedraw();
        c.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext c) {
        verticeSelecionado = null;
    }

    @Override public void aoClicar(MouseEvent e, EditorContext c) { }

    @Override
    public void aoDesativar(EditorContext c) {
        verticeSelecionado = null;
    }
}
