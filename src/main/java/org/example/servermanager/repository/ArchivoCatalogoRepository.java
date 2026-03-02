package org.example.servermanager.repository;

import org.example.servermanager.entity.ArchivoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivoCatalogoRepository extends JpaRepository<ArchivoCatalogo, Long> {

    List<ArchivoCatalogo> findByEmpresaIdAndActivoTrue(Long empresaId);

    List<ArchivoCatalogo> findByEmpresaId(Long empresaId);
}
