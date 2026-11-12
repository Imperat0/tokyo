package com.fullstack.desafio.endereco;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    Page<Endereco> findByUsuarioId(Long usuarioId, Pageable pageable);

    boolean existsByIdAndUsuario_Email(Long id, String email);
}