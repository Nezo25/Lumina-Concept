package tfs.com.ecommerce.api.core.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixNullVersions() {
        log.info("Verificando se há produtos antigos com a coluna 'versao' nula no banco de dados...");
        int updated = jdbcTemplate.update("UPDATE produtos SET versao = 0 WHERE versao IS NULL");
        if (updated > 0) {
            log.info("Corrigidos {} produtos antigos que estavam com a versão Nula. Optimistic Locking restaurado!", updated);
        }
    }
}
