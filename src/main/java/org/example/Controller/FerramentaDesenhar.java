package org.example.Controller;

import java.util.List;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.Poligono;
import org.example.Models.Ponto;

/** Desenha polígonos por cliques. Finaliza com duplo clique ou clicando no 1º ponto; botão direito cancela. */
public class FerramentaDesenhar implements Ferramenta {
    private static final double RAIO_FECHAMENTO = 10;

    @Override public void aoPressionarMouse(MouseEvent e, EditorContext c) { }
    @Override public void aoSoltarMouse(MouseEvent e, EditorContext c) { }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext c) {
        c.setMouse(c.aplicarSnap(e.getX()), c.aplicarSnap(e.getY()));
        c.requestRedraw();
    }

    @Override
    public void aoClicar(MouseEvent e, EditorContext c) {
        if (e.getButton() == MouseButton.SECONDARY) {
            c.limparRascunho();
            c.requestRedraw();
            return;
        }
        if (e.getButton() != MouseButton.PRIMARY) return;

        Ponto clique = new Ponto(c.aplicarSnap(e.getX()), c.aplicarSnap(e.getY()));
        List<Ponto> rascunho = c.getRascunho();
        boolean fechar = e.getClickCount() >= 2
                || (rascunho.size() >= 3 && rascunho.get(0).distancia(clique) <= RAIO_FECHAMENTO);
        if (fechar) {
            finalizar(c);
        } else {
            rascunho.add(clique);
        }
        c.requestRedraw();
    }

    private void finalizar(EditorContext c) {
        List<Ponto> rascunho = c.getRascunho();
        if (rascunho.size() >= 3) { // regra: polígono precisa de ao menos 3 pontos
            c.registrarEstado();
            Poligono p = new Poligono();
            for (Ponto pt : rascunho) p.adicionarPonto(pt.clonar());
            p.validar();
            c.adicionarForma(c.aplicarEstiloAtual(p));
        }
        c.limparRascunho();
    }

    @Override
    public void aoDesativar(EditorContext c) {
        c.limparRascunho();
    }
}
