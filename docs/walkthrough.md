# Funcionalidade Concluída: Cotação de Frete Dinâmico

O módulo logístico foi integrado com foco absoluto em blindagem financeira e estabilidade.

## Resumo das Entregas

### 1. Entidade Refatorada (Preparada para Cubagem)
- A entidade `Produto` agora possui os campos `peso_kg`, `altura_cm`, `largura_cm` e `comprimento_cm`. Caso um produto seja cadastrado via banco com esses campos nulos, definimos os valores _default_ (ex: peso 0.3kg) na própria entidade Java, simulando uma caixa padrão para o MVP.

### 2. Controller Autônomo
- Criada a rota `POST /api/fretes/calcular`, que o frontend usará na tela do Carrinho (antes do Checkout) passando apenas o CEP e a lista de `idProduto` + `quantidade`. O backend devolve um array limpo de opções (PAC, SEDEX, etc.).

### 3. Circuit Breaker (Resiliência)
- O `FreteService` utiliza o moderno `RestClient` envolto em um Fallback. Se a API de logística externa sofrer lentidão ou _down-time_, a API interceptará o erro silenciosamente e devolverá uma opção: `"Frete Padrão Fixo (Fallback)" - R$ 30,00`. **A loja nunca perde uma venda por culpa de APIs terceiras!**

### 4. Segurança contra Spoofing (Opção B)
- Acatamos sua diretriz rigorosa: o payload de Checkout (`CheckoutRequestDTO`) agora não aceita o `valor` financeiro do frete, apenas a *string* do serviço (ex: `"Correios PAC"` ou `"Frete Padrão Fixo (Fallback)"`).
- O `PedidoService` foi instruído a orquestrar uma chamada transparente ao `FreteService` em pleno ato de Checkout. Ele pega a string, caça na lista calculada, extrai o valor blindado do nosso próprio servidor, e embute esse valor seguro no `Pedido`, somando-o para geração limpa da cobrança PIX!
