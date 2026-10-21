package org.example.Views;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Controller.ArquivoCorrompidoException;
import org.example.Controller.ArquivoService;
import org.example.Controller.EditorContext;
import org.example.Controller.FerramentaEditarVertice;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/** Janela principal: monta a interface, liga o mouse/teclado às ferramentas e desenha o canvas. */
public class ContainerApp extends Application {
    private final Canvas areaDeDesenho = new Canvas(900, 650);
    private final EditorContext contexto = new EditorContext();
    private BarraLateralView barraLateral;
    private PainelStatusView painelStatus;
    private Stage stage;
    private Scene cena;
    private File arquivoAtual;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        barraLateral = new BarraLateralView(contexto, this::salvarArquivo, this::carregarArquivo);
        painelStatus = new PainelStatusView(contexto);

        Pane centro = new Pane(areaDeDesenho);
        areaDeDesenho.widthProperty().bind(centro.widthProperty());
        areaDeDesenho.heightProperty().bind(centro.heightProperty());
        areaDeDesenho.widthProperty().addListener((o, a, b) -> redesenharCanvas());
        areaDeDesenho.heightProperty().addListener((o, a, b) -> redesenharCanvas());

        BorderPane raiz = new BorderPane();
        raiz.setCenter(centro);
        raiz.setLeft(barraLateral);
        raiz.setBottom(painelStatus);

        cena = new Scene(raiz, 1150, 740);
        contexto.setOuvintes(this::redesenharCanvas, () -> {
            painelStatus.atualizar();
            atualizarTitulo();
        });
        inicializarEventos();

        // alerta de alterações não salvas ao fechar
        stage.setOnCloseRequest(e -> {
            if (!confirmarDescarte()) e.consume();
        });
        stage.setScene(cena);
        atualizarTitulo();
        stage.show();
        redesenharCanvas();
    }

    // ---------- eventos ----------
    public void inicializarEventos() {
        areaDeDesenho.setOnMousePressed(e -> {
            registrarMouse(e);
            contexto.getFerramentaAtual().aoPressionarMouse(e, contexto);
        });
        areaDeDesenho.setOnMouseDragged(e -> {
            registrarMouse(e);
            contexto.getFerramentaAtual().aoArrastarMouse(e, contexto);
        });
        areaDeDesenho.setOnMouseReleased(e -> contexto.getFerramentaAtual().aoSoltarMouse(e, contexto));
        areaDeDesenho.setOnMouseClicked(e -> contexto.getFerramentaAtual().aoClicar(e, contexto));
        areaDeDesenho.setOnMouseMoved(e -> {
            registrarMouse(e);
            redesenharCanvas(); // atualiza a linha elástica do polígono
        });

        KeyCombination desfazer = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
        KeyCombination refazer = new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN);
        KeyCombination salvar = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
        KeyCombination abrir = new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN);
        cena.setOnKeyPressed(e -> {
            if (desfazer.match(e)) contexto.desfazer();
            else if (refazer.match(e)) contexto.refazer();
            else if (salvar.match(e)) salvarArquivo();
            else if (abrir.match(e)) carregarArquivo();
            else if (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE) contexto.excluirSelecionados();
            else if (e.getCode() == KeyCode.ESCAPE) {
                contexto.limparRascunho();
                contexto.limparSelecao();
            }
        });
    }

    private void registrarMouse(MouseEvent e) {
        contexto.setMouse(contexto.aplicarSnap(e.getX()), contexto.aplicarSnap(e.getY()));
    }

    // ---------- desenho ----------
    private void redesenharCanvas() {
        GraphicsContext g = areaDeDesenho.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, areaDeDesenho.getWidth(), areaDeDesenho.getHeight());

        if (contexto.isGridSnappingHabilitado()) desenharGrade(g);

        for (FormaGeometrica f : contexto.getFormas()) {
            desenharForma(f, contexto.estaSelecionada(f));
        }

        if (contexto.getFormaEmAndamento() != null) {
            g.setGlobalAlpha(0.55);
            desenharForma(contexto.getFormaEmAndamento(), false);
            g.setGlobalAlpha(1.0);
        }
        desenharRascunho(g);
        desenharCaixaSelecao(g);
    }

    private void desenharForma(FormaGeometrica f, boolean selecionada) {
        GraphicsContext g = areaDeDesenho.getGraphicsContext2D();
        g.setFill(Color.web(f.getCorPreenchimentoHex()));
        g.setStroke(selecionada ? Color.GREEN : Color.web(f.getCorBordaHex()));
        g.setLineWidth(selecionada ? Math.max(2, f.getEspessuraBorda()) : f.getEspessuraBorda());

        if (f instanceof Circulo c) {
            Ponto ctr = c.getCentroide();
            double r = c.getRaio();
            g.fillOval(ctr.getX() - r, ctr.getY() - r, 2 * r, 2 * r);
            g.strokeOval(ctr.getX() - r, ctr.getY() - r, 2 * r, 2 * r);
            return;
        }
        List<Ponto> ps = f.getPontos();
        int n = ps.size();
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = ps.get(i).getX();
            ys[i] = ps.get(i).getY();
        }
        g.fillPolygon(xs, ys, n);
        g.strokePolygon(xs, ys, n);

        // alças dos vértices quando a ferramenta "Editar vértice" está ativa
        if (selecionada && contexto.getFerramentaAtual() instanceof FerramentaEditarVertice) {
            g.setFill(Color.RED);
            for (int i = 0; i < n; i++) g.fillOval(xs[i] - 3.5, ys[i] - 3.5, 7, 7);
        }
    }

    private void desenharGrade(GraphicsContext g) {
        g.setFill(Color.LIGHTGRAY);
        double passo = EditorContext.TAMANHO_GRADE;
        for (double x = 0; x < areaDeDesenho.getWidth(); x += passo) {
            for (double y = 0; y < areaDeDesenho.getHeight(); y += passo) g.fillRect(x - 0.5, y - 0.5, 1.5, 1.5);
        }
    }

    private void desenharRascunho(GraphicsContext g) {
        List<Ponto> r = contexto.getRascunho();
        if (r.isEmpty()) return;
        g.setStroke(Color.DODGERBLUE);
        g.setLineWidth(1.5);
        g.setLineDashes(6, 4);
        for (int i = 0; i + 1 < r.size(); i++) {
            g.strokeLine(r.get(i).getX(), r.get(i).getY(), r.get(i + 1).getX(), r.get(i + 1).getY());
        }
        Ponto ultimo = r.get(r.size() - 1);
        g.strokeLine(ultimo.getX(), ultimo.getY(), contexto.getMouseX(), contexto.getMouseY());
        g.setLineDashes((double[]) null);
        g.setFill(Color.DODGERBLUE);
        for (Ponto p : r) g.fillOval(p.getX() - 3, p.getY() - 3, 6, 6);
    }

    private void desenharCaixaSelecao(GraphicsContext g) {
        double[] c = contexto.getCaixaSelecao();
        if (c == null) return;
        g.setFill(Color.rgb(30, 144, 255, 0.15));
        g.fillRect(c[0], c[1], c[2] - c[0], c[3] - c[1]);
        g.setStroke(Color.DODGERBLUE);
        g.setLineWidth(1);
        g.setLineDashes(5, 4);
        g.strokeRect(c[0], c[1], c[2] - c[0], c[3] - c[1]);
        g.setLineDashes((double[]) null);
    }

    // ---------- arquivos ----------
    /** @return true se o desenho foi salvo. */
    private boolean salvarArquivo() {
        File destino = arquivoAtual;
        if (destino == null) {
            destino = criarSeletor("Salvar desenho").showSaveDialog(stage);
            if (destino == null) return false;
            if (!destino.getName().toLowerCase().endsWith(".json")) {
                destino = new File(destino.getParentFile(), destino.getName() + ".json");
            }
        }
        try {
            ArquivoService.salvar(destino.toPath(), new ArquivoService.Documento(
                    areaDeDesenho.getWidth(), areaDeDesenho.getHeight(),
                    contexto.isGridSnappingHabilitado(), contexto.getFormas()));
            arquivoAtual = destino;
            contexto.marcarSalvo();
            return true;
        } catch (IOException | RuntimeException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao salvar",
                    "Não foi possível salvar o arquivo:\n" + ex.getMessage());
            return false;
        }
    }

    private void carregarArquivo() {
        if (!confirmarDescarte()) return;
        File arquivo = criarSeletor("Carregar desenho").showOpenDialog(stage);
        if (arquivo == null) return;
        try {
            ArquivoService.Documento doc = ArquivoService.carregar(arquivo.toPath());
            contexto.substituirFormas(doc.formas());
            contexto.setGridSnappingHabilitado(doc.grade());
            barraLateral.atualizarCoresInterface();
            arquivoAtual = arquivo;
            atualizarTitulo();
        } catch (ArquivoCorrompidoException ex) {
            // a aplicação continua funcionando com o desenho atual
            mostrarAlerta(Alert.AlertType.WARNING, "Arquivo corrompido",
                    "O arquivo não pôde ser carregado e o desenho atual foi mantido.\n\nMotivo: " + ex.getMessage());
        } catch (IOException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro de leitura",
                    "Não foi possível ler o arquivo:\n" + ex.getMessage());
        }
    }

    private FileChooser criarSeletor(String titulo) {
        FileChooser fc = new FileChooser();
        fc.setTitle(titulo);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Desenho vetorial (*.json)", "*.json"));
        return fc;
    }

    /** Pergunta sobre alterações não salvas. @return true se é seguro prosseguir. */
    private boolean confirmarDescarte() {
        if (!contexto.isModificado()) return true;
        ButtonType salvar = new ButtonType("Salvar");
        ButtonType descartar = new ButtonType("Descartar");
        ButtonType cancelar = new ButtonType("Cancelar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, "Há alterações não salvas. Deseja salvá-las?",
                salvar, descartar, cancelar);
        alerta.setTitle("Alterações não salvas");
        alerta.setHeaderText(null);
        Optional<ButtonType> r = alerta.showAndWait();
        if (r.isEmpty() || r.get() == cancelar) return false;
        if (r.get() == salvar) return salvarArquivo();
        return true;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert a = new Alert(tipo, mensagem, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void atualizarTitulo() {
        String nome = arquivoAtual == null ? "Sem título" : arquivoAtual.getName();
        stage.setTitle("Editor Vetorial — " + nome + (contexto.isModificado() ? " *" : ""));
    }
}
