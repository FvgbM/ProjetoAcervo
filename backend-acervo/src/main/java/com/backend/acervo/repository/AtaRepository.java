package com.backend.acervo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.backend.acervo.Ata;

@Repository
public interface AtaRepository extends JpaRepository<Ata, Long> {
    @Override
    @EntityGraph(attributePaths = "arquivo")
    Optional<Ata> findById(Long id);

    @EntityGraph(attributePaths = "arquivo")
    List<Ata> findByStatusOrderByDataReuniaoDesc(Ata.StatusAta status);

    @EntityGraph(attributePaths = "arquivo")
    List<Ata> findByDataReuniaoBetweenAndStatus(
        LocalDate inicio, LocalDate fim, Ata.StatusAta status);

    @EntityGraph(attributePaths = "arquivo")
    List<Ata> findByPautaContainingIgnoreCaseAndStatus( String pauta, Ata.StatusAta status);
    
    @Query("select max(a.numeroAta) from Ata a")
    Optional<Integer> findMaxNumeroAta();
}