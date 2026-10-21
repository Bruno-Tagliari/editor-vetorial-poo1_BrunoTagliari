package org.example.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.example.Models.FormaGeometrica;

/**
 * Desfazer/Refazer com o padrão Memento: cada "memento" é uma cópia profunda
 * (via clonar()) da lista de formas. Convenção: registrarEstado() é chamado
 * ANTES de cada alteração, guardando o estado anterior.
 */
public class HistoricoController {
    private static final int LIMITE = 100;
    private final Stack<List<FormaGeometrica>> undoStack = new Stack<>();
    private final Stack<List<FormaGeometrica>> redoStack = new Stack<>();

    public void registrarEstado(List<FormaGeometrica> formas) {
        undoStack.push(copiaProfunda(formas));
        if (undoStack.size() > LIMITE) undoStack.remove(0);
        redoStack.clear();
    }

    /** Devolve o estado anterior (ou null se não há o que desfazer). */
    public List<FormaGeometrica> desfazer(List<FormaGeometrica> atual) {
        if (undoStack.isEmpty()) return null;
        redoStack.push(copiaProfunda(atual));
        return undoStack.pop();
    }

    /** Devolve o próximo estado (ou null se não há o que refazer). */
    public List<FormaGeometrica> refazer(List<FormaGeometrica> atual) {
        if (redoStack.isEmpty()) return null;
        undoStack.push(copiaProfunda(atual));
        return redoStack.pop();
    }

    public boolean podeDesfazer() { return !undoStack.isEmpty(); }
    public boolean podeRefazer() { return !redoStack.isEmpty(); }

    public void limpar() {
        undoStack.clear();
        redoStack.clear();
    }

    private static List<FormaGeometrica> copiaProfunda(List<FormaGeometrica> formas) {
        List<FormaGeometrica> copia = new ArrayList<>();
        for (FormaGeometrica f : formas) copia.add(f.clonar());
        return copia;
    }
}
