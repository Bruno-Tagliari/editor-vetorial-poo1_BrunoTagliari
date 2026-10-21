package org.example.Controller;

/** Arquivo de desenho ilegível, com JSON inválido ou com dados que violam as regras das formas. */
public class ArquivoCorrompidoException extends Exception {
    public ArquivoCorrompidoException(String mensagem) { super(mensagem); }
    public ArquivoCorrompidoException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
