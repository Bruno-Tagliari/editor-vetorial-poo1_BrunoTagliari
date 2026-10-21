package org.example.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

/** Persistência em JSON (Gson): configurações do canvas + lista de formas (ordem = camadas). */
public final class ArquivoService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int VERSAO = 1;

    private ArquivoService() { }

    /** Conteúdo lido/gravado em arquivo. */
    public record Documento(double largura, double altura, boolean grade, List<FormaGeometrica> formas) { }

    // ----- DTOs (estrutura do JSON) -----
    static class ArquivoDTO {
        Integer versao;
        CanvasDTO canvas;
        List<FormaDTO> formas;
    }

    static class CanvasDTO {
        double largura;
        double altura;
        boolean gradeMagnetica;
    }

    static class FormaDTO {
        String tipo;
        List<double[]> pontos;
        Double raio;
        String corPreenchimento;
        String corBorda;
        Double espessuraBorda;
        String groupId;
    }

    // ----- salvar -----
    public static void salvar(Path destino, Documento doc) throws IOException {
        ArquivoDTO dto = new ArquivoDTO();
        dto.versao = VERSAO;
        dto.canvas = new CanvasDTO();
        dto.canvas.largura = doc.largura();
        dto.canvas.altura = doc.altura();
        dto.canvas.gradeMagnetica = doc.grade();
        dto.formas = new ArrayList<>();
        for (FormaGeometrica f : doc.formas()) dto.formas.add(paraDTO(f));

        // grava em arquivo temporário e move: evita corromper o arquivo existente se algo falhar
        Path temp = destino.resolveSibling(destino.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(dto), StandardCharsets.UTF_8);
        Files.move(temp, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    private static FormaDTO paraDTO(FormaGeometrica f) {
        FormaDTO d = new FormaDTO();
        if (f instanceof Circulo c) {
            d.tipo = "CIRCULO";
            d.raio = c.getRaio();
        } else if (f instanceof Quadrado) {
            d.tipo = "QUADRADO";
        } else if (f instanceof Hexagono) {
            d.tipo = "HEXAGONO";
        } else {
            d.tipo = "POLIGONO";
        }
        d.pontos = new ArrayList<>();
        for (Ponto p : f.getPontos()) d.pontos.add(new double[] {p.getX(), p.getY()});
        d.corPreenchimento = f.getCorPreenchimentoHex();
        d.corBorda = f.getCorBordaHex();
        d.espessuraBorda = f.getEspessuraBorda();
        d.groupId = f.getGroupId();
        return d;
    }

    // ----- carregar -----
    public static Documento carregar(Path origem) throws IOException, ArquivoCorrompidoException {
        String json = Files.readString(origem, StandardCharsets.UTF_8); // IOException propaga
        ArquivoDTO dto;
        try {
            dto = GSON.fromJson(json, ArquivoDTO.class);
        } catch (JsonParseException ex) {
            throw new ArquivoCorrompidoException("O arquivo não contém um JSON válido.", ex);
        }
        if (dto == null || dto.formas == null || dto.canvas == null) {
            throw new ArquivoCorrompidoException("Estrutura do arquivo incompleta (faltam 'canvas' ou 'formas').");
        }
        List<FormaGeometrica> formas = new ArrayList<>();
        for (int i = 0; i < dto.formas.size(); i++) {
            try {
                formas.add(deDTO(dto.formas.get(i)));
            } catch (RuntimeException ex) {
                throw new ArquivoCorrompidoException("Forma #" + (i + 1) + " inválida: " + ex.getMessage(), ex);
            }
        }
        return new Documento(dto.canvas.largura, dto.canvas.altura, dto.canvas.gradeMagnetica, formas);
    }

    private static FormaGeometrica deDTO(FormaDTO d) {
        if (d == null || d.tipo == null || d.pontos == null) {
            throw new IllegalArgumentException("campos obrigatórios ausentes");
        }
        List<Ponto> pontos = new ArrayList<>();
        for (double[] xy : d.pontos) {
            if (xy == null || xy.length != 2) throw new IllegalArgumentException("ponto malformado");
            pontos.add(new Ponto(xy[0], xy[1]));
        }
        FormaGeometrica forma;
        switch (d.tipo) {
            case "CIRCULO":
                if (pontos.size() != 1 || d.raio == null) throw new IllegalArgumentException("círculo exige 1 ponto e raio");
                forma = new Circulo(pontos.get(0), d.raio);
                break;
            case "QUADRADO": forma = new Quadrado(pontos); break;
            case "HEXAGONO": forma = new Hexagono(pontos); break;
            case "POLIGONO": {
                Poligono pol = new Poligono();
                for (Ponto pt : pontos) pol.adicionarPonto(pt);
                pol.validar();
                forma = pol;
                break;
            }
            default: throw new IllegalArgumentException("tipo desconhecido: " + d.tipo);
        }
        if (d.corPreenchimento != null) forma.setCorPreenchimentoHex(d.corPreenchimento);
        if (d.corBorda != null) forma.setCorBordaHex(d.corBorda);
        if (d.espessuraBorda != null) forma.setEspessuraBorda(d.espessuraBorda);
        forma.setGroupId(d.groupId);
        return forma;
    }
}
