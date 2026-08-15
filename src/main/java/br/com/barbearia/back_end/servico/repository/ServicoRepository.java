package br.com.barbearia.back_end.servico.repository;

import br.com.barbearia.back_end.servico.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @Query(value = "SELECT * FROM TBL_SERVICO s WHERE s.NR_PRECO <= :preco AND s.ATIVO = 1 ORDER BY s.NR_PRECO", nativeQuery = true)
    List<Servico> findByPrecoServicoAtivo(@Param("preco") Double preco);

    @Query(value = "SELECT * FROM TBL_SERVICO s WHERE s.NR_PRECO BETWEEN :precoMenor AND :precoMaior AND s.ATIVO = 1 ORDER BY s.NR_PRECO", nativeQuery = true)
    List<Servico> findByPrecoBetweenPrecoMenorPrecoMaior(@Param("precoMenor") Double precoMenor, @Param("precoMaior") Double precoMaior);

    @Query(value = "SELECT * FROM TBL_SERVICO s WHERE s.ATIVO = 1", nativeQuery = true)
    List<Servico> findByAtivoServico();

    @Query(value = "SELECT * FROM TBL_SERVICO s WHERE s.ATIVO = 0", nativeQuery = true)
    List<Servico> findByInativoServico();

    @Query(value = "SELECT * FROM TBL_SERVICO S WHERE lower(S.TX_NOME) LIKE lower(concat('%', :nome, '%'))", nativeQuery = true)
    List<Servico> findByNomeServico(@Param("nome") String nome);

    Optional<Servico> findByIdAndAtivoTrue(Long id);
}
