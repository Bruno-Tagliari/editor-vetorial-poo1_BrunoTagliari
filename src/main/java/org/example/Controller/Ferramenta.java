package org.example.Controller;

import javafx.scene.input.MouseEvent;

/** Interface de Estado (padrão State): cada ferramenta define como o mouse é tratado. */
public interface Ferramenta {
    void aoPressionarMouse(MouseEvent e, EditorContext contexto);
    void aoArrastarMouse(MouseEvent e, EditorContext contexto);
    void aoSoltarMouse(MouseEvent e, EditorContext contexto);
    void aoClicar(MouseEvent e, EditorContext contexto);

    /** Chamado quando outra ferramenta assume o lugar desta (limpa estados temporários). */
    default void aoDesativar(EditorContext contexto) { }
}
