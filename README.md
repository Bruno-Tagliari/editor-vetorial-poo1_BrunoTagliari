# Editor de Desenho Vetorial Interativo (JavaFX)

Trabalho N3 — Programação Orientada a Objetos 1 — UniRV (2026/1)

Editor desktop 2D em Java 17 + JavaFX + Maven: cria, edita, agrupa, transforma e salva (JSON) formas geométricas.

## Como executar

Requisitos: JDK 17+ e Maven 3.8+.

```bash
mvn test          # testes unitários (modelos e histórico)
mvn javafx:run    # abre o editor
```

## Arquitetura (MVC) e pacotes

| Pacote | Conteúdo |
|---|---|
| `org.example.Models` | `FormaGeometrica`, `FormaBase`, `Ponto`, `Poligono`, `Circulo`, `Quadrado`, `Hexagono`, `GrupoFormas` |
| `org.example.Controller` | `EditorContext`, `Ferramenta` + ferramentas, `HistoricoController`, `ArquivoService` |
| `org.example.Views` | `ContainerApp`, `BarraLateralView`, `PainelStatusView` |

O Controller não depende das Views: a tela se registra no `EditorContext` por dois `Runnable` (redesenhar / atualizar status).

## Padrões de projeto

- **State**: `EditorContext` (contexto) delega o mouse à `Ferramenta` atual (`FerramentaDesenhar`, `FerramentaSelecionar`, `FerramentaTransformar`, `FerramentaEditarVertice`, `FerramentaInserirForma`). Sem `if/switch` por ferramenta no tratamento de eventos.
- **Memento (Undo/Redo)**: `HistoricoController` guarda cópias profundas da lista de formas em duas pilhas. `registrarEstado()` é chamado antes de cada alteração.
- **Prototype**: `FormaGeometrica.clonar()` produz cópia profunda (pontos e estilo), usada pelo histórico.
- **Composite**: `GrupoFormas` trata um conjunto de formas (mesmo `groupId` ou seleção atual) como uma única forma; escala/rotação/cisalhamento usam o centroide comum.

## Regras de negócio implementadas

- Polígono com no mínimo 3 pontos (`Poligono()` + `adicionarPonto()` + `validar()`, que lança `IllegalStateException`); círculo com raio > 0 (`IllegalArgumentException`).
- Quadrado e hexágono calculam os vértices a partir de centro e raio.
- Área por Gauss (shoelace) e perímetro pela soma das distâncias euclidianas.
- Translação, escala (em relação ao centroide), rotação (radianos, em torno do centroide) e cisalhamento.

Observação: além dos métodos do enunciado, `FormaGeometrica` tem `escalarEm`, `rotacionarEm` e `cisalharEm` (com pivô explícito), necessários para o Composite transformar várias formas em torno de um pivô comum.

## Uso

- **Desenhar polígono**: cliques adicionam vértices; duplo clique (ou clicar no 1º ponto) fecha; botão direito cancela.
- **Inserir quadrado/círculo/hexágono**: pressione no centro e arraste para definir o raio (clique simples usa tamanho padrão).
- **Selecionar**: clique, Shift+clique (alterna) ou caixa de seleção; arrastar move a seleção.
- **Escalar / Rotacionar / Cisalhar**: arraste sobre a forma. **Excluir**: clique na forma. **Editar vértice**: arraste os pontos vermelhos.
- **Grade magnética**: arredonda o cursor para a grade de 20 px.
- **Camadas/Grupo**: trazer para frente, enviar para trás, agrupar, desagrupar.
- Atalhos: `Ctrl+Z`, `Ctrl+Y`, `Ctrl+S`, `Ctrl+O`, `Delete`, `Esc`.
- Barra de status: coordenadas, total de formas, área e perímetro da seleção.

## Persistência (JSON)

Arquivo `.json` com `versao`, `canvas` (largura, altura, grade) e `formas` (tipo, pontos, raio, cores, espessura, `groupId`); a ordem da lista é a ordem das camadas. Arquivo corrompido ou inválido gera um alerta explicativo e o editor continua com o desenho atual. Ao fechar, carregar ou sair com alterações pendentes, o usuário é perguntado se deseja salvar. A gravação usa arquivo temporário para não destruir o arquivo anterior em caso de falha.

## Diagrama UML

Fonte PlantUML em `docs/diagrama_classes.puml`. Para gerar o PDF: `plantuml -tpdf docs/diagrama_classes.puml` (ou cole o conteúdo em https://www.plantuml.com/plantuml e exporte).
