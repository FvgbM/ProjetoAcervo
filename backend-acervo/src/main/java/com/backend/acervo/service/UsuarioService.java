package com.backend.acervo.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.acervo.Usuario;
import com.backend.acervo.exceptions.UsuarioNaoEncontradoException;
import com.backend.acervo.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final String DOMINIO_INSTITUCIONAL = "@fatec.sp.gov.br";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AutenticacaoService autenticacaoService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AutenticacaoService autenticacaoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.autenticacaoService = autenticacaoService;
    }

    /**
     * Cria uma solicitação de usuário pendente.
     * A senha abaixo é somente um valor interno aleatório para cumprir a
     * restrição NOT NULL. Ela nunca é enviada ao usuário nem deve ser usada
     * para login: o usuário recebe um link seguro somente após aprovação.
     */
    @Transactional
    public Usuario criaUsuario(Usuario usuario) {
        String email = normalizarEmail(usuario.getEmail());
        validarEmailInstitucional(email);

        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-mail já está em uso");
        }

        String rg = normalizarRg(usuario.getRg());
        if (rg != null && usuarioRepository.existsByRg(rg)) {
            throw new IllegalArgumentException("RG já está em uso");
        }

        usuario.setEmail(email);
        usuario.setRg(rg);
        usuario.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setPrimeiroLogin(true);
        usuario.setAtivo(false);
        usuario.setPerfil(null);
        usuario.setStatusUsuario(Usuario.StatusUsuario.PENDENTE);
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);

        return usuarioRepository.save(usuario);
    }

    /**
     * Aprova o usuário, define seu perfil e envia um link de criação de senha.
     */
    @Transactional
    public Usuario aprovarUsuario(Long usuarioId, Usuario.Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("O perfil do usuário é obrigatório");
        }

        Usuario usuario = buscarPorId(usuarioId);
        if (usuario.getStatusUsuario() != Usuario.StatusUsuario.PENDENTE) {
            throw new IllegalStateException("Apenas usuários pendentes podem ser aprovados");
        }

        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuario.setStatusUsuario(Usuario.StatusUsuario.ATIVO);
        usuario.setPrimeiroLogin(true);
        Usuario usuarioAprovado = usuarioRepository.save(usuario);

        // O mesmo fluxo de recuperação cria uma senha sem transmitir credencial por e-mail.
        autenticacaoService.solicitarRecuperacaoSenha(usuarioAprovado.getEmail());

        return usuarioAprovado;
    }

    @Transactional
    public Usuario atualizarUsuario(Long id, Usuario usuarioAtualizado) {
        Usuario usuario = buscarPorId(id);

        String email = normalizarEmail(usuarioAtualizado.getEmail());
        validarEmailInstitucional(email);
        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException("E-mail já está em uso");
        }

        String rg = normalizarRg(usuarioAtualizado.getRg());
        if (rg != null && usuarioRepository.existsByRgAndIdNot(rg, id)) {
            throw new IllegalArgumentException("RG já está em uso");
        }

        usuario.setNome(usuarioAtualizado.getNome());
        usuario.setEmail(email);
        usuario.setRg(rg);

        // Perfil e status são alterados pelos métodos específicos abaixo,
        // para evitar mudanças acidentais durante edição cadastral.
        return usuarioRepository.save(usuario);
    }

    /** Exclusão lógica: preserva o histórico e impede novo login. */
    @Transactional
    public void deletarUsuario(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(false);
        usuario.setStatusUsuario(Usuario.StatusUsuario.INATIVO);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarPorStatus(Usuario.StatusUsuario status) {
        if (status == null) {
            throw new IllegalArgumentException("O status do usuário é obrigatório");
        }
        return usuarioRepository.findByStatusUsuario(status);
    }

    /**
     * Compatibilidade temporária com controllers que ainda enviam status como texto.
     * Prefira o overload que recebe StatusUsuario em novos códigos.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarPorStatus(String status) {
        return listarPorStatus(converterStatus(status));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(long id) {
    return usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNaoEncontradoException(
                    "Usuário não encontrado com ID: " + id
            ));
}

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuariosAtivos() {
        return usuarioRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Usuario> findByAtivoFalse() {
        return usuarioRepository.findByAtivoFalse();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarEmail(String email) {
        return usuarioRepository.findByEmail(normalizarEmail(email));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarRg(String rg) {
        return usuarioRepository.findByRg(normalizarRg(rg));
    }

    @Transactional(readOnly = true)
    public List<Usuario> findByPerfil(Usuario.Perfil perfil) {
        return usuarioRepository.findByPerfil(perfil);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listaPorPerfil(Usuario.Perfil perfil) {
        return usuarioRepository.findByPerfil(perfil);
    }

    @Transactional(readOnly = true)
    public boolean emailExiste(String email) {
        return usuarioRepository.existsByEmail(normalizarEmail(email));
    }

    @Transactional
    public Usuario atualizarPerfil(Long usuarioId, Usuario.Perfil novoPerfil) {
        if (novoPerfil == null) {
            throw new IllegalArgumentException("O perfil do usuário é obrigatório");
        }

        Usuario usuario = buscarPorId(usuarioId);
        usuario.setPerfil(novoPerfil);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizarStatus(Long usuarioId, Usuario.StatusUsuario novoStatus) {
        if (novoStatus == null) {
            throw new IllegalArgumentException("O status do usuário é obrigatório");
        }

        Usuario usuario = buscarPorId(usuarioId);
        usuario.setStatusUsuario(novoStatus);
        usuario.setAtivo(novoStatus == Usuario.StatusUsuario.ATIVO);

        if (novoStatus != Usuario.StatusUsuario.ATIVO) {
            usuario.setBloqueadoAte(null);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Compatibilidade temporária com controllers que ainda enviam status como texto.
     * Prefira o overload que recebe StatusUsuario em novos códigos.
     */
    @Transactional
    public Usuario atualizarStatus(Long usuarioId, String novoStatus) {
        return atualizarStatus(usuarioId, converterStatus(novoStatus));
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorNome(String nome) {
        return usuarioRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Transactional(readOnly = true)
    public boolean podeSerAprovado(Usuario usuario) {
        return usuario != null
                && usuario.getStatusUsuario() == Usuario.StatusUsuario.PENDENTE
                && usuario.getEmail() != null
                && usuario.getEmail().endsWith(DOMINIO_INSTITUCIONAL);
    }

    @Transactional(readOnly = true)
    public boolean existeUsuarioAtivoComEmail(String email) {
        return usuarioRepository.findByEmail(normalizarEmail(email))
                .map(usuario -> usuario.isAtivo()
                        && usuario.getStatusUsuario() == Usuario.StatusUsuario.ATIVO)
                .orElse(false);
    }

    private Usuario.StatusUsuario converterStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("O status do usuário é obrigatório");
        }

        try {
            return Usuario.StatusUsuario.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Status inválido. Use: ATIVO, INATIVO, PENDENTE ou BLOQUEADO"
            );
        }
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail obrigatório");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarRg(String rg) {
        return rg == null || rg.isBlank() ? null : rg.trim();
    }

    private void validarEmailInstitucional(String email) {
        if (!email.endsWith(DOMINIO_INSTITUCIONAL)) {
            throw new IllegalArgumentException("Use um e-mail institucional @fatec.sp.gov.br");
        }
    }
}