package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    // Pesquisar Atores existentes e ignorar o Case da letra E Avaliacoes maior o igual
    List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeAtores, double avaliacao);

    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    List<Serie> findByGenero(Categoria categoria);

    List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(int totalTemporadas, double avaliacao);

    // Consultas com Native Quere, ficam muito presas ao banco de dados, caso queira usar outro banco de dados, ela teria que ser mudado, logo nao é muito recomendad
    // @Query(value = "select * from series where total_temporadas <= 1 and avaliacao >= 6.0", nativeQuery = true)

    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :totalTemporadas AND s.avaliacao >= :avaliacao")
    List<Serie> seriesPorTemporadasEAvaliacao (int totalTemporadas, double avaliacao);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:trechoEpisodio%")
    List<Episodio> episodioPorTrecho(String trechoEpisodio);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.avaliacao DESC LIMIT 5")
    List<Episodio> topEpisodioPorSerie(Serie serie);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie AND YEAR(e.dataLancamento) >= :anoLancamento")
    List<Episodio> episodioPorSerieEAno(Serie serie, int anoLancamento);
}
