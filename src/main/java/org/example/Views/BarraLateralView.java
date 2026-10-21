package org.example.Views;

import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.Controller.EditorContext;
import org.example.Controller.Ferramenta;
import org.example.Controller.FerramentaDesenhar;
import org.example.Controller.FerramentaEditarVertice;
import org.example.Controller.FerramentaInserirForma;
import org.example.Controller.FerramentaSelecionar;
import org.example.Controller.FerramentaTransformar;
import org.example.Controller.ModoFerramenta;
import org.example.Controller.TipoForma;

/** Barra lateral: estilos, ferramentas (toggle), formas, camadas/grupo e sistema. */
public class BarraLateralView extends ScrollPane {
    private final EditorContext contexto;
    private final Runnable aoSalvar;
    private final Runnable aoCarregar;
    private final VBox conteudo = new VBox(5);
    private final ToggleGroup grupoFerramentas = new ToggleGroup();

    private ColorPicker cpPreenchimento;
    private ColorPicker cpBorda;
    private Slider slEspessura;
    private CheckBox cbGrade;

    public BarraLateralView(EditorContext contexto, Runnable aoSalvar, Runnable aoCarregar) {
        this.contexto = contexto;
        this.aoSalvar = aoSalvar;
        this.aoCarregar = aoCarregar;

        conteudo.setPadding(new Insets(8));
        conteudo.setStyle("-fx-background-color: #f0f0f0;");
        setContent(conteudo);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setPrefWidth(200);
        setMinWidth(200);

        // impede "desligar" a ferramenta ativa clicando nela de novo
        grupoFerramentas.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null && antigo != null) antigo.setSelected(true);
        });

        construirSecaoEstilos();
        construirSecaoFerramentas();
        construirSecaoFormas();
        construirSecaoCamadas();
        construirSecaoSistema();
        atualizarCoresInterface();
    }

    // ---------- seções ----------
    private void construirSecaoEstilos() {
        conteudo.getChildren().add(titulo("ESTILOS"));

        cpPreenchimento = new ColorPicker(Color.web(contexto.getCorPreenchimentoAtual()));
        cpPreenchimento.setMaxWidth(Double.MAX_VALUE);
        cpPreenchimento.setOnAction(e -> contexto.alterarCorPreenchimento(paraHex(cpPreenchimento.getValue())));

        cpBorda = new ColorPicker(Color.web(contexto.getCorBordaAtual()));
        cpBorda.setMaxWidth(Double.MAX_VALUE);
        cpBorda.setOnAction(e -> contexto.alterarCorBorda(paraHex(cpBorda.getValue())));

        slEspessura = new Slider(1, 10, contexto.getEspessuraAtual());
        slEspessura.setShowTickLabels(true);
        // registra UM estado no histórico ao começar a arrastar o slider
        slEspessura.setOnMousePressed(e -> {
            if (!contexto.getSelecionados().isEmpty()) contexto.registrarEstado();
        });
        slEspessura.valueProperty().addListener((obs, a, b) -> contexto.alterarEspessura(b.doubleValue()));

        cbGrade = new CheckBox("Grade magnética (snapping)");
        cbGrade.setOnAction(e -> contexto.setGridSnappingHabilitado(cbGrade.isSelected()));

        conteudo.getChildren().addAll(new Label("Cor de preenchimento:"), cpPreenchimento,
                new Label("Cor da borda:"), cpBorda, new Label("Espessura da borda:"), slEspessura, cbGrade);
    }

    private void construirSecaoFerramentas() {
        conteudo.getChildren().add(titulo("FERRAMENTAS"));
        ToggleButton selecionar = botaoFerramenta("Selecionar", FerramentaSelecionar::new);
        conteudo.getChildren().addAll(
                botaoFerramenta("Desenhar polígono", FerramentaDesenhar::new),
                selecionar,
                botaoFerramenta("Escalar", () -> new FerramentaTransformar(ModoFerramenta.ESCALAR)),
                botaoFerramenta("Rotacionar", () -> new FerramentaTransformar(ModoFerramenta.ROTACIONAR)),
                botaoFerramenta("Cisalhar", () -> new FerramentaTransformar(ModoFerramenta.CISALHAR)),
                botaoFerramenta("Excluir", () -> new FerramentaTransformar(ModoFerramenta.EXCLUIR)),
                botaoFerramenta("Editar vértice", FerramentaEditarVertice::new),
                botao("Excluir selecionados", contexto::excluirSelecionados));
        selecionar.setSelected(true);
    }

    private void construirSecaoFormas() {
        conteudo.getChildren().add(titulo("FORMAS"));
        conteudo.getChildren().addAll(
                botaoFerramenta("Inserir quadrado", () -> new FerramentaInserirForma(TipoForma.QUADRADO)),
                botaoFerramenta("Inserir círculo", () -> new FerramentaInserirForma(TipoForma.CIRCULO)),
                botaoFerramenta("Inserir hexágono", () -> new FerramentaInserirForma(TipoForma.HEXAGONO)));
    }

    private void construirSecaoCamadas() {
        conteudo.getChildren().add(titulo("CAMADAS / GRUPO"));
        conteudo.getChildren().addAll(
                botao("Trazer p/ frente", contexto::trazerParaFrente),
                botao("Enviar p/ trás", contexto::enviarParaTras),
                botao("Agrupar", contexto::agruparSelecionados),
                botao("Desagrupar", contexto::desagruparSelecionados));
    }

    private void construirSecaoSistema() {
        conteudo.getChildren().add(titulo("SISTEMA"));
        conteudo.getChildren().addAll(
                botao("Desfazer (Ctrl+Z)", contexto::desfazer),
                botao("Refazer (Ctrl+Y)", contexto::refazer),
                botao("Salvar (Ctrl+S)", aoSalvar),
                botao("Carregar (Ctrl+O)", aoCarregar));
    }

    /** Sincroniza os controles com o estado do contexto (ex.: após carregar um arquivo). */
    public void atualizarCoresInterface() {
        cpPreenchimento.setValue(Color.web(contexto.getCorPreenchimentoAtual()));
        cpBorda.setValue(Color.web(contexto.getCorBordaAtual()));
        cbGrade.setSelected(contexto.isGridSnappingHabilitado());
    }

    // ---------- helpers ----------
    private Label titulo(String texto) {
        Label l = new Label("— " + texto + " —");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0;");
        return l;
    }

    private Button botao(String texto, Runnable acao) {
        Button b = new Button(texto);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> acao.run());
        return b;
    }

    private ToggleButton botaoFerramenta(String texto, Supplier<Ferramenta> fabrica) {
        ToggleButton b = new ToggleButton(texto);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setToggleGroup(grupoFerramentas);
        b.setOnAction(e -> {
            if (b.isSelected()) contexto.setFerramentaAtual(fabrica.get());
        });
        return b;
    }

    static String paraHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }
}
