package tfs.com.ecommerce.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tfs.com.ecommerce.api.domain.enums.UserRole;
import tfs.com.ecommerce.api.domain.model.Usuario;
import tfs.com.ecommerce.api.domain.repository.UsuarioRepository;

@Slf4j
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            log.info("Nenhum usuário encontrado no banco. Criando Administrador padrão...");
            
            Usuario admin = new Usuario(
                    "admin@tfs.com.br",
                    passwordEncoder.encode("123456"),
                    UserRole.ADMIN
            );
            
            usuarioRepository.save(admin);
            log.info("Administrador padrão criado com sucesso (email: admin@tfs.com.br).");
        } else {
            log.info("Banco de dados já contém usuários. AdminSeeder ignorado.");
        }
    }
}
