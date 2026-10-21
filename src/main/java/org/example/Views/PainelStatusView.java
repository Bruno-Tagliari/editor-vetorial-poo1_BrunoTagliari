package org.example.Views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.example.Controller.EditorContext;
import org.example.Models.GrupoFormas;

/** Barra de status: coordenadas do cursor, total de formas e área/perímetro da seleção. */
public class PainelStatusView extends HBox {
    private final EditorContext contexto;
    private final Label lblCoordenadas = new Label();
    private final Label lblTotal = new Label();
    private final Label lblArea = new Label();
    private final Label lblPerimetro = new Label();

    public PainelStatusView(EditorContext contexto) {
        super(25);
        this.contexto = contexto;
        setPadding(new Insets(4, 10, 4, 10));
        setStyle("-fx-background-color: #eeeeee; -fx-border-color: #bbbbbb transparent transparent transparent;");
        getChildren().addAll(lblCoordenadas, lblTotal, lblArea, lblPerimetro);
        atualizar();
    }

    public void atualizar() {
        lblCoordenadas.setText(String.format("X: %.0f  Y: %.0f", contexto.getMouseX(), contexto.getMouseY()));
        lblTotal.setText("Formas: " + contexto.getFormas().size());
        GrupoFormas selecao = contexto.getSelecaoComoGrupo();
        if (selecao == null) {
            lblArea.setText("Área: —");
            lblPerimetro.setText("Perímetro: —");
        } else {
            lblArea.setText(String.format("Área: %.2f", selecao.calcularArea()));
            lblPerimetro.setText(String.format("Perímetro: %.2f", selecao.calcularPerimetro()));
        }
    }
}
