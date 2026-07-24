# 🚀 Checklist de Lançamento (Produção)

Este documento mapeia as dívidas técnicas conscientes assumidas durante o desenvolvimento do MVP e lista as tarefas críticas e obrigatórias de infraestrutura que devem ser executadas antes de abrirmos a loja para o tráfego do público final (Go-Live).

---

## 1. Segurança e Rate Limiting (Proteção Anti-DDoS e Brute Force)
*Atualmente utilizamos um "Tarpit" (atraso artificial via Thread.sleep) que resolve ataques simples, mas pode esgotar as threads do nosso próprio servidor em um ataque massivo.*

- [ ] **Remover o Tarpit Simples:** Apagar o `Thread.sleep` do `AuthController`.
- [ ] **Implementar Rate Limiting Real:** Integrar uma solução como **Redis** (para ambiente distribuído) ou **Bucket4j** (em memória local) para rastrear tentativas de login por IP.
- [ ] **Por que é crucial?** Impedir que bots testem milhares de senhas por minuto ou derrubem nosso backend exaurindo conexões.

---

## 2. Integração com ERP (Bling OAuth2)
*Nosso serviço do Bling está mockado/estático para o MVP, porém a documentação oficial deles exige OAuth2.*

- [ ] **Criar Rotina de Refresh Token:** Implementar um `@Scheduled` (ou Quartz) que execute a renovação do Token OAuth2 do Bling (usando o `refresh_token` salvo no banco de forma criptografada).
- [ ] **Por que é crucial?** Os tokens de acesso do Bling expiram (geralmente em 6 horas). Se o token vencer de madrugada, o motor autônomo não conseguirá faturar os pedidos pagos até que um humano gere o token novamente.

---

## 3. Infraestrutura, Deploy e Criptografia
*A API roda perfeitamente em Docker no ambiente local, mas precisamos de proteção no mundo real.*

- [ ] **Provisionar VPS:** Contratar e configurar um servidor Linux (ex: DigitalOcean, AWS EC2, Hetzner, Linode).
- [ ] **Configurar Proxy Reverso (NGINX):** Colocar o NGINX na frente do nosso container Spring Boot (porta 80/443 apontando para a 8080). O NGINX lidará com a compressão GZIP e cache estático.
- [ ] **Certificado SSL (HTTPS):** Configurar o **Certbot (Let's Encrypt)** no NGINX para gerar e renovar os certificados SSL automaticamente.
- [ ] **Banco de Dados Seguro:** Migrar o MySQL do `docker-compose` simples para um **Managed Database** (banco gerenciado pela nuvem com backup Point-in-Time) ou criar uma rotina estrita (cron) de *dump* diário do volume Docker para a AWS S3.
- [ ] **Por que é crucial?** HTTPS impede o roubo de dados de clientes (Sniffing/Man-in-the-Middle) na rede de Wi-Fi, e o backup garante que a loja nunca perca pedidos e notas fiscais no caso do servidor queimar.

---

## 4. Monitoramento, Logs e Observabilidade
*Se a API travar ou o banco corromper, precisamos saber antes do cliente reclamar no ReclameAqui.*

- [ ] **Spring Boot Actuator:** Adicionar a dependência do Actuator para expor a rota `/actuator/health`.
- [ ] **Liveness & Readiness Probes:** Configurar o Docker (ou Kubernetes) para reiniciar o container se a rota de *health* parar de responder.
- [ ] **Centralização de Logs (Sentry ou ELK):** Instalar um SDK como o **Sentry** (ou ELK Stack) para capturar todas as `Exception.class` do nosso `ApiExceptionHandler`.
- [ ] **Alertas em Tempo Real:** Conectar o Sentry (ou Grafana) a um webhook do **Slack** ou **Discord**.
- [ ] **Por que é crucial?** Garantir alta disponibilidade. Se a API de pagamento do Mercado Pago mudar ou quebrar, você será avisado no celular nos primeiros milissegundos do erro.
