package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.GrupoFormas;
import org.example.Models.Ponto;

/** Seleciona por clique (Shift alterna), seleciona por caixa e move as formas selecionadas. */
public class FerramentaSelecionar implements Ferramenta {
    private boolean arrastandoCaixa;
    private double selecaoStartX;
    private double selecaoStartY;

    private boolean movendo;
    private boolean estadoRegistrado;
    private double ultimoX;
    private double ultimoY;

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext c) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        movendo = false;
        arrastandoCaixa = false;
        estadoRegistrado = false;

        FormaGeometrica alvo = c.formaEm(e.getX(), e.getY());
        if (alvo != null) {
            if (e.isShiftDown()) {
                c.alternarSelecao(alvo);
            } else if (!c.estaSelecionada(alvo)) {
                c.limparSelecao();
                c.selecionarComGrupo(alvo);
            }
            movendo = c.estaSelecionada(alvo);
            ultimoX = c.aplicarSnap(e.getX());
            ultimoY = c.aplicarSnap(e.getY());
        } else {
            if (!e.isShiftDown()) c.limparSelecao();
            arrastandoCaixa = true;
            selecaoStartX = e.getX();
            selecaoStartY = e.getY();
            c.setCaixaSelecao(selecaoStartX, selecaoStartY, selecaoStartX, selecaoStartY);
        }
        c.requestRedraw();
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext c) {
        c.setMouse(c.aplicarSnap(e.getX()), c.aplicarSnap(e.getY()));
        if (movendo) {
            double x = c.aplicarSnap(e.getX());
            double y = c.aplicarSnap(e.getY());
            double dx = x - ultimoX;
            double dy = y - ultimoY;
            GrupoFormas selecao = c.getSelecaoComoGrupo();
            if ((dx != 0 || dy != 0) && selecao != null) {
                if (!estadoRegistrado) {
                    c.registrarEstado();
                    estadoRegistrado = true;
                }
                selecao.transladar(dx, dy);
                ultimoX = x;
                ultimoY = y;
            }
        } else if (arrastandoCaixa) {
            c.setCaixaSelecao(selecaoStartX, selecaoStartY, e.getX(), e.getY());
        }
        c.requestRedraw();
        c.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext c) {
        if (arrastandoCaixa) {
            double[] caixa = c.getCaixaSelecao();
            if (caixa != null) {
                for (FormaGeometrica f : c.getFormas()) {
                    Ponto centro = f.getCentroide();
                    if (centro.getX() >= caixa[0] && centro.getX() <= caixa[2]
                            && centro.getY() >= caixa[1] && centro.getY() <= caixa[3]) {
                        c.selecionarComGrupo(f);
                    }
                }
            }
            c.limparCaixaSelecao();
        }
        arrastandoCaixa = false;
        movendo = false;
        c.requestRedraw();
        c.requestStatsUpdate();
    }

    @Override public void aoClicar(MouseEvent e, EditorContext c) { }

    @Override
    public void aoDesativar(EditorContext c) {
        arrastandoCaixa = false;
        movendo = false;
        c.limparCaixaSelecao();
    }
}
