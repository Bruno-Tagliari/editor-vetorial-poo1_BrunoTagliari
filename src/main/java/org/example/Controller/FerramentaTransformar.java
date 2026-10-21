package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.GrupoFormas;
import org.example.Models.Ponto;

/**
 * Escalar, rotacionar, cisalhar ou excluir (conforme o ModoFerramenta).
 * Arraste sobre uma forma: a transformação é aplicada à seleção como um único grupo (Composite).
 */
public class FerramentaTransformar implements Ferramenta {
    private final ModoFerramenta modo;
    private boolean ativo;
    private boolean estadoRegistrado;
    private double ultimoX;
    private double ultimoY;

    public FerramentaTransformar(ModoFerramenta modo) {
        this.modo = modo;
    }

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext c) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        ativo = false;
        estadoRegistrado = false;
        FormaGeometrica alvo = c.formaEm(e.getX(), e.getY());
        if (alvo == null) return;

        if (modo == ModoFerramenta.EXCLUIR) {
            c.registrarEstado();
            c.removerForma(alvo);
            return;
        }
        if (!c.estaSelecionada(alvo)) {
            c.limparSelecao();
            c.selecionarComGrupo(alvo);
        }
        ativo = true;
        ultimoX = e.getX();
        ultimoY = e.getY();
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext c) {
        c.setMouse(e.getX(), e.getY());
        GrupoFormas grupo = c.getSelecaoComoGrupo();
        if (!ativo || grupo == null) return;

        double x = e.getX();
        double y = e.getY();
        Ponto centro = grupo.getCentroide();
        double cx = centro.getX();
        double cy = centro.getY();
        boolean mudou = false;

        switch (modo) {
            case ESCALAR: {
                double d0 = Math.hypot(ultimoX - cx, ultimoY - cy);
                double d1 = Math.hypot(x - cx, y - cy);
                if (d0 > 1e-6 && d1 > 1e-6) {
                    double fator = Math.max(0.5, Math.min(2.0, d1 / d0));
                    antesDeAlterar(c);
                    grupo.escalar(fator);
                    mudou = true;
                }
                break;
            }
            case ROTACIONAR: {
                double angulo = Math.atan2(y - cy, x - cx) - Math.atan2(ultimoY - cy, ultimoX - cx);
                antesDeAlterar(c);
                grupo.rotacionar(angulo);
                mudou = true;
                break;
            }
            case CISALHAR: {
                double dx = x - ultimoX;
                double dy = y - ultimoY;
                antesDeAlterar(c);
                if (Math.abs(dx) >= Math.abs(dy)) grupo.cisalhar(dx / 100.0, 0);
                else grupo.cisalhar(0, dy / 100.0);
                mudou = true;
                break;
            }
            default:
                break;
        }
        if (mudou) {
            ultimoX = x;
            ultimoY = y;
        }
        c.requestRedraw();
        c.requestStatsUpdate();
    }

    private void antesDeAlterar(EditorContext c) {
        if (!estadoRegistrado) {
            c.registrarEstado();
            estadoRegistrado = true;
        }
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext c) {
        ativo = false;
    }

    @Override public void aoClicar(MouseEvent e, EditorContext c) { }
}
