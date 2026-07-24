# Integração de Frete Dinâmico (API de Cotação)

Implementação do motor de cálculo de fretes, conectando o e-commerce a um gateway logístico (ex: Kangu ou Melhor Envio) utilizando `RestClient`. A prioridade máxima arquitetural desta entrega é a **resiliência (Circuit Breaker local)**: garantir que falhas de rede no provedor logístico não paralisem o funil de vendas.

## User Review Required

- **Dimensões Físicas dos Produtos:** A entidade `Produto` atual não possui campos logísticos (`peso`, `altura`, `largura`, `comprimento`), que são requeridos nas apis de frete logístico para o cálculo de cubagem. Você autoriza a criação dessas 4 colunas em `Produto.java` agora, ou prefere mockar uma "caixa padrão" (ex: 1kg, 20x20x20) fixada no Service durante o MVP apenas para poupar o banco de dados? (Sugestão: Adicionar os campos na Entidade para manter o banco preparado pro futuro).
- **Validação de Valor (Vulnerabilidade no Front):** Como o front-end calculará o frete em uma tela e submeterá o Checkout em outra, aceitar o `valorFrete` livremente no `CheckoutRequestDTO` expõe a API ao risco de manipulação de *payload* (um usuário avançado mandando um frete de R$ 0,00). 
  - *Opção A (Ágil MVP):* Aceitar cegamente o que vem no DTO e blindar apenas com um `@PositiveOrZero`.
  - *Opção B (Segura MVP):* O backend re-calcula a cotação no Checkout e barra se o valor enviado divergir gravemente.
  - Como é MVP, prefere seguir com a *Opção A* (menor carga e dependência de rede) por enquanto?

## Proposed Changes

### Entidades (Mediante Aprovação)
#### [MODIFY] Produto.java 
- Adição dos campos `pesoKg`, `alturaCm`, `larguraCm`, `comprimentoCm`.

### DTOs (Data Transfer Objects)
#### [NEW] CalculoFreteRequestDTO.java e ItemFreteDTO.java
- DTO recebendo o `cepDestino` (String) e uma lista com `idProduto` e `quantidade` para somatória.

#### [NEW] FreteResponseDTO.java e OpcaoFreteDTO.java
- DTO de retorno, possuindo uma lista de `OpcaoFreteDTO` (`nomeTransportadora`, `valor`, `prazoDias`).

#### [MODIFY] CheckoutRequestDTO.java
- Adição do campo numérico `valorFrete` (BigDecimal) anotado com `@NotNull` e `@PositiveOrZero`.

---

### Camada Web (REST Controller)
#### [NEW] FreteController.java
- Rota autônoma `POST /api/fretes/calcular`, permitindo simulações de carrinho sem persistência.

---

### Camada de Serviços
#### [NEW] FreteService.java
- Utiliza o `RestClient` nativo para consumir a API de cotação.
- Orquestra a iteração do carrinho, buscando o peso e medidas de cada produto e injetando no payload do parceiro.
- **Resiliência Máxima (Fallback):** Envolto em um bloco estruturado `try-catch`. Se houver *timeout* ou retorno `500` do provedor de frete, o método não propaga exceção. Ele cria localmente um retorno salvador: `[{"nomeTransportadora": "Frete Padrão Fixo (Fallback)", "valor": 30.00, "prazoDias": 7}]`. A loja nunca para!

#### [MODIFY] PedidoService.java
- No método `salvarPedidoNoBanco()`, a atual constante fixa `BigDecimal.valueOf(20.00)` será removida.
- O campo `pedido.setValorFrete()` receberá o valor trafegado via `dto.getValorFrete()`.
- O `valorTotal` do pedido continuará orquestrando a soma precisa, e repassando o total final pro `MercadoPagoService`.
