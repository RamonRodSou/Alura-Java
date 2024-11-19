package br.com.alura.screenmatch.model;

public enum Categoria {

    DRAMA("Drama", "Drama"),
    COMEDIA("Comedy", "Comédia"),
    ACAO("Action", "Ação"),
    ROMANCE("Romance", "Romance"),
    CRIME("Crime", "Crime"),
    TERROR("Terror", "Terror");

    private String categoriaOmdb;

    private String getCategoriaPtBr;

    Categoria(String categoriaOmdb, String getCategoriaPtBr){
        this.categoriaOmdb = categoriaOmdb;
        this.getCategoriaPtBr = getCategoriaPtBr;
    }

    // Se vir da API Ela vai interpretar ex: se vier "Action" vai saber que é ACAO se acontecer isso ele vai atribuir a categoria.
    public static Categoria fromString(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaOmdb.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
    }

    public static Categoria fromPtBr(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.getCategoriaPtBr.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
    }

}
