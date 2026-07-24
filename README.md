🌟 Lumina Concept




Sobre o Projeto O Lumina Concept é um projeto conceitual completo que une uma identidade visual marcante a uma experiência digital envolvente. Muito mais do que uma interface moderna, o projeto contempla uma arquitetura robusta no backend, construída para um ecossistema de e-commerce com processamento de pagamentos e segurança de ponta.

O Desafio e a Solução O foco foi criar uma aplicação ponta a ponta (Full-stack/API) onde o design limpo do frontend se conectasse a uma API RESTful de alta performance. O backend foi desenvolvido focado em escalabilidade, utilizando as melhores práticas de mercado para garantir autenticação segura de usuários e integração direta com gateways de pagamento externos.

💻 Tecnologias e Ferramentas Utilizadas
Neste projeto, utilizei uma stack moderna e consolidada no mercado corporativo, focando em segurança, clareza de código e performance:

Backend & Arquitetura
Java 21: Utilizando os recursos mais recentes da linguagem para garantir performance e manutenibilidade.
Spring Boot 3: Framework principal para criação da API REST de forma rápida, robusta e independente.
Spring Data JPA / Hibernate: Para mapeamento objeto-relacional (ORM) e comunicação limpa com o banco de dados.
MySQL: Banco de dados relacional escolhido para a persistência dos dados em ambiente de desenvolvimento/produção.
Segurança
Spring Security: Camada de segurança responsável por proteger os endpoints da aplicação.
JWT (JSON Web Tokens): Usado em conjunto com a biblioteca java-jwt (da Auth0) para gerenciar a autenticação e autorização stateless dos usuários.
Integrações Externas
API do Mercado Pago: Integração real com o gateway de pagamentos para processar transações e receber via Webhooks as atualizações de status das compras.
Documentação e Qualidade
Swagger (Springdoc OpenAPI): Para gerar uma documentação viva e interativa da API, facilitando os testes de integração pelo frontend.
Lombok: Para reduzir o boilerplate (código repetitivo) nas entidades, como Getters, Setters e Construtores, deixando o código mais limpo.
H2 Database: Banco de dados em memória configurado para rodar testes automatizados de forma isolada e rápida.
