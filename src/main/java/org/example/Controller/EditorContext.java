package org.example.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.example.Models.FormaBase;
import org.example.Models.FormaGeometrica;
import org.example.Models.GrupoFormas;
import org.example.Models.Ponto;

/**
 * Contexto do padrão State: guarda o estado do editor (formas, seleção, ferramenta ativa,
 * estilo corrente) e delega o tratamento do mouse à Ferramenta atual.
 * Não conhece as Views: a tela se registra por meio de dois Runnables (redesenho/status).
 */
public class EditorContext {
    public static final double TAMANHO_GRADE = 20.0;

    private final List<FormaGeometrica> formas = new ArrayList<>();
    private final List<FormaGeometrica> selecionados = new ArrayList<>();
    private final HistoricoController historico = new HistoricoController();

    private Ferramenta ferramentaAtual = new FerramentaSelecionar();
    private boolean gridSnappingHabilitado;
    private boolean modificado;

    private String corPreenchimentoAtual = FormaBase.COR_PREENCHIMENTO_PADRAO;
    private String corBordaAtual = FormaBase.COR_BORDA_PADRAO;
    private double espessuraAtual = 1.0;

    private double mouseX;
    private double mouseY;

    // elementos temporários de pré-visualização
    private final List<Ponto> rascunho = new ArrayList<>();
    private FormaGeometrica formaEmAndamento;
    private double[] caixaSelecao;

    private Runnable ouvinteRedesenho = () -> { };
    private Runnable ouvinteStatus = () -> { };

    // ---------- ligação com a View ----------
    public void setOuvintes(Runnable redesenho, Runnable status) {
        this.ouvinteRedesenho = redesenho;
        this.ouvinteStatus = status;
    }

    public void requestRedraw() { ouvinteRedesenho.run(); }
    public void requestStatsUpdate() { ouvinteStatus.run(); }

    private void atualizarTudo() {
        requestRedraw();
        requestStatsUpdate();
    }

    // ---------- ferramenta (State) ----------
    public Ferramenta getFerramentaAtual() { return ferramentaAtual; }

    public void setFerramentaAtual(Ferramenta nova) {
        ferramentaAtual.aoDesativar(this);
        limparRascunho();
        this.formaEmAndamento = null;
        this.caixaSelecao = null;
        this.ferramentaAtual = nova;
        atualizarTudo();
    }

    // ---------- formas ----------
    public List<FormaGeometrica> getFormas() { return Collections.unmodifiableList(formas); }

    public void adicionarForma(FormaGeometrica f) {
        formas.add(f);
        atualizarTudo();
    }

    /** Remove a forma e, se houver, todas as do mesmo grupo (grupo = uma forma lógica). */
    public void removerForma(FormaGeometrica alvo) {
        String id = alvo.getGroupId();
        formas.removeIf(f -> f == alvo || (id != null && id.equals(f.getGroupId())));
        selecionados.removeIf(f -> !formas.contains(f));
        atualizarTudo();
    }

    public void excluirSelecionados() {
        if (selecionados.isEmpty()) return;
        registrarEstado();
        formas.removeAll(selecionados);
        selecionados.clear();
        atualizarTudo();
    }

    /** Retorna a forma mais acima sob o ponto (camadas: fim da lista = frente). */
    public FormaGeometrica formaEm(double x, double y) {
        for (int i = formas.size() - 1; i >= 0; i--) {
            if (formas.get(i).contemPonto(x, y)) return formas.get(i);
        }
        return null;
    }

    /** Substitui o desenho inteiro (usado ao carregar arquivo). */
    public void substituirFormas(List<FormaGeometrica> novas) {
        formas.clear();
        formas.addAll(novas);
        selecionados.clear();
        historico.limpar();
        modificado = false;
        atualizarTudo();
    }

    // ---------- seleção ----------
    public List<FormaGeometrica> getSelecionados() { return Collections.unmodifiableList(selecionados); }
    public boolean estaSelecionada(FormaGeometrica f) { return selecionados.contains(f); }

    public void limparSelecao() {
        selecionados.clear();
        atualizarTudo();
    }

    /** Seleciona a forma e todas as do mesmo grupo. */
    public void selecionarComGrupo(FormaGeometrica f) {
        for (FormaGeometrica g : membrosDoGrupo(f)) {
            if (!selecionados.contains(g)) selecionados.add(g);
        }
        atualizarTudo();
    }

    public void alternarSelecao(FormaGeometrica f) {
        List<FormaGeometrica> membros = membrosDoGrupo(f);
        if (selecionados.contains(f)) selecionados.removeAll(membros);
        else for (FormaGeometrica g : membros) if (!selecionados.contains(g)) selecionados.add(g);
        atualizarTudo();
    }

    private List<FormaGeometrica> membrosDoGrupo(FormaGeometrica f) {
        List<FormaGeometrica> membros = new ArrayList<>();
        String id = f.getGroupId();
        for (FormaGeometrica g : formas) {
            if (g == f || (id != null && id.equals(g.getGroupId()))) membros.add(g);
        }
        return membros;
    }

    /** Composite: a seleção inteira se comporta como uma única forma (ou null se vazia). */
    public GrupoFormas getSelecaoComoGrupo() {
        return selecionados.isEmpty() ? null : new GrupoFormas(selecionados);
    }

    // ---------- camadas e agrupamento ----------
    public void agruparSelecionados() {
        if (selecionados.size() < 2) return;
        registrarEstado();
        String id = UUID.randomUUID().toString();
        for (FormaGeometrica f : selecionados) f.setGroupId(id);
        atualizarTudo();
    }

    public void desagruparSelecionados() {
        boolean temGrupo = selecionados.stream().anyMatch(f -> f.getGroupId() != null);
        if (!temGrupo) return;
        registrarEstado();
        for (FormaGeometrica f : selecionados) f.setGroupId(null);
        atualizarTudo();
    }

    public void trazerParaFrente() {
        if (selecionados.isEmpty()) return;
        registrarEstado();
        List<FormaGeometrica> mover = new ArrayList<>();
        for (FormaGeometrica f : formas) if (selecionados.contains(f)) mover.add(f);
        formas.removeAll(mover);
        formas.addAll(mover);
        atualizarTudo();
    }

    public void enviarParaTras() {
        if (selecionados.isEmpty()) return;
        registrarEstado();
        List<FormaGeometrica> mover = new ArrayList<>();
        for (FormaGeometrica f : formas) if (selecionados.contains(f)) mover.add(f);
        formas.removeAll(mover);
        formas.addAll(0, mover);
        atualizarTudo();
    }

    // ---------- histórico (Memento) ----------
    /** Deve ser chamado ANTES de alterar o desenho. */
    public void registrarEstado() {
        historico.registrarEstado(formas);
        modificado = true;
    }

    public void desfazer() { aplicar(historico.desfazer(formas)); }
    public void refazer() { aplicar(historico.refazer(formas)); }

    private void aplicar(List<FormaGeometrica> estado) {
        if (estado == null) return;
        formas.clear();
        formas.addAll(estado);
        selecionados.clear();
        modificado = true;
        atualizarTudo();
    }

    public boolean isModificado() { return modificado; }

    public void marcarSalvo() {
        modificado = false;
        requestStatsUpdate();
    }

    // ---------- estilo corrente ----------
    public String getCorPreenchimentoAtual() { return corPreenchimentoAtual; }
    public String getCorBordaAtual() { return corBordaAtual; }
    public double getEspessuraAtual() { return espessuraAtual; }

    public void alterarCorPreenchimento(String hex) {
        FormaBase.validarHex(hex);
        corPreenchimentoAtual = hex;
        if (!selecionados.isEmpty()) {
            registrarEstado();
            for (FormaGeometrica f : selecionados) f.setCorPreenchimentoHex(hex);
        }
        atualizarTudo();
    }

    public void alterarCorBorda(String hex) {
        FormaBase.validarHex(hex);
        corBordaAtual = hex;
        if (!selecionados.isEmpty()) {
            registrarEstado();
            for (FormaGeometrica f : selecionados) f.setCorBordaHex(hex);
        }
        atualizarTudo();
    }

    /** Não registra histórico (a View registra uma vez ao começar a arrastar o slider). */
    public void alterarEspessura(double espessura) {
        espessuraAtual = espessura;
        for (FormaGeometrica f : selecionados) f.setEspessuraBorda(espessura);
        atualizarTudo();
    }

    /** Aplica o estilo corrente a uma forma recém-criada. */
    public <T extends FormaGeometrica> T aplicarEstiloAtual(T forma) {
        forma.setCorPreenchimentoHex(corPreenchimentoAtual);
        forma.setCorBordaHex(corBordaAtual);
        forma.setEspessuraBorda(espessuraAtual);
        return forma;
    }

    // ---------- grade / mouse ----------
    public boolean isGridSnappingHabilitado() { return gridSnappingHabilitado; }
    public void setGridSnappingHabilitado(boolean v) { this.gridSnappingHabilitado = v; atualizarTudo(); }

    /** Snap to grid: arredonda para o ponto de grade mais próximo, se habilitado. */
    public double aplicarSnap(double valor) {
        return gridSnappingHabilitado ? Math.round(valor / TAMANHO_GRADE) * TAMANHO_GRADE : valor;
    }

    public void setMouse(double x, double y) {
        this.mouseX = x;
        this.mouseY = y;
        requestStatsUpdate();
    }

    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }

    // ---------- pré-visualizações ----------
    public List<Ponto> getRascunho() { return rascunho; }
    public void limparRascunho() { rascunho.clear(); }

    public FormaGeometrica getFormaEmAndamento() { return formaEmAndamento; }
    public void setFormaEmAndamento(FormaGeometrica f) { this.formaEmAndamento = f; }

    public double[] getCaixaSelecao() { return caixaSelecao; }

    public void setCaixaSelecao(double x1, double y1, double x2, double y2) {
        caixaSelecao = new double[] {Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2)};
    }

    public void limparCaixaSelecao() { caixaSelecao = null; }
}
