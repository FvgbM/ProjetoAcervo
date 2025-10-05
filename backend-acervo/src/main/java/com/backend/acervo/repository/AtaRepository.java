package com.backend.acervo.repository;

import com.backend.acervo.Ata;
//import com.backend.acervo.Usuario;

//import org.springframework.cglib.core.Local;
//import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtaRepository extends JpaRepository<Ata, Long> {

    Optional<Ata> findByNumeroAta(Integer numeroAta);
    List<Ata> findByStatus(Ata.StatusAta status);
    List<Ata> findByDataReuniao(LocalDate dataReuniao);
    List<Ata> findByDataReuniaoBetween(LocalDate inicio, LocalDate fim);
    List<Ata> findByPautaContainingIgnoreCase(String trechoPauta);
    List<Ata> findByDataReuniaoBetweenAndStatus(LocalDate inicio, LocalDate fim, Ata.StatusAta status);

    @Query("Select MAX(a.numeroAta) FROM Ata a")
    Optional<Integer> findMaxNumeroAta();
    List<Ata> findByStatusOrderByDataReuniaoDesc(Ata.StatusAta status);

}