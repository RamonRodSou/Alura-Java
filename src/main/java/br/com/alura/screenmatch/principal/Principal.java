package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private Optional<Serie> serieBuscada;

    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar série por ator
                    6 - Top 5 Séries
                    7 - Buscar séries por categoria
                    8 - Filtrar séries
                    9 - Buscar Episodio por trecho
                    10 - Top 5 Episodios por série
                    11 - Buscar episódio a partir de uma data
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorCategoria();
                    break;
                case 8:
                    filtrarSeriesPorTemporadasEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodioPorSerie();
                case 11:
                    buscarEpisodioDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {

        System.out.println("Digite o nome da série para busca.");
        var nomeSerie =  leitura.nextLine();

        serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("Dados da série: " + serieBuscada.get());
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriePorAtor() {
        System.out.println("Qual o nome para a busca?");
        var nomeAtor = leitura.nextLine();

        System.out.println("Avaliações a partir de que valor?");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Séries em que " + nomeAtor + " trabalhou: ");
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + " avaliação " + s.getAvaliacao()));
    }

    private void buscarTop5Series() {

        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s -> System.out.println(s.getTitulo() + " avaliação " + s.getAvaliacao()));
    }

    private void buscarSeriePorCategoria() {
        System.out.println("Deseja buscar séries de que categoria/gênero? ");
        var nomeGenero = leitura.nextLine();

        Categoria categoria = Categoria.fromPtBr(nomeGenero);
        List<Serie> seriresPorCategoria = repositorio.findByGenero(categoria);

        System.out.println("Séries da categoria " + nomeGenero);

        seriresPorCategoria.forEach(System.out::println);
    }

    private void filtrarSeriesPorTemporadasEAvaliacao() {
        System.out.println("Qual o maximo de temporadas?");
        var totalTemporadas = leitura.nextInt();
        System.out.println("Avaliações a partir de que valor?");
        var avaliacao = leitura.nextDouble();

//      List<Serie> seriesEncontradas = repositorio.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(totalTemporadas, avaliacao);

//      JPQL - Java Pesist Query Language
        List<Serie> filtrarSeries = repositorio.seriesPorTemporadasEAvaliacao(totalTemporadas, avaliacao);
        System.out.println("**** Séries Filtradas ****");
        filtrarSeries.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho(){
        System.out.println("Qual nome do episódio para busca");
        var trechoEpisodio = leitura.nextLine();

        List<Episodio> episodiosEncontrados  = repositorio.episodioPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Séries: %s Temporadas %s - Episodio %s - %s\n",
                        e.getSerie().getTitulo(),
                        e.getTemporada(),
                        e.getNumeroEpisodio(),
                        e.getTitulo())
        );
    }

    private void topEpisodioPorSerie(){
        buscarSeriePorTitulo();

        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.topEpisodioPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Séries: %s Temporadas %s - Episodio %s - %s\n",
                            e.getSerie().getTitulo(),
                            e.getTemporada(),
                            e.getNumeroEpisodio(),
                            e.getTitulo(),
                            e.getAvaliacao())
            );
        }
    }
    private void buscarEpisodioDepoisDeUmaData(){
        buscarSeriePorTitulo();
        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            System.out.println("Digite o ano limite de lançamento: ");
            var anoLancamento = leitura.nextInt();
            List<Episodio> episodiosAno = repositorio.episodioPorSerieEAno(serie, anoLancamento);
            episodiosAno.forEach(System.out::println);
        }
    }
}