# Guia de HTTP Status Codes & Tratamento de Erros da API

Bem-vindo à documentação oficial de códigos de status HTTP da nossa API de E-commerce. 
Nosso objetivo com este documento é fornecer transparência e previsibilidade total para as equipes de Frontend e parceiros de integração, garantindo que o consumo dos nossos serviços seja o mais suave possível.

Nossa API segue fielmente as diretrizes arquiteturais do estilo RESTful e adota o padrão **Problem Details for HTTP APIs (RFC 7807)**. Isso significa que, em caso de erro, você nunca receberá uma string solta ou um stack trace, mas sim um objeto JSON padronizado e semântico.

---

## 🟢 Códigos de Sucesso (2xx)

Estes códigos indicam que a sua requisição foi recebida, compreendida e aceita com sucesso pela nossa API.

### `200 OK`
- **Quando ocorre:** Em consultas padrão de leitura, como buscar a lista de opções de frete disponíveis (`/api/v1/fretes`).
- **O que esperar:** O corpo da resposta (JSON) contendo a entidade ou lista solicitada.

### `201 Created`
- **Quando ocorre:** Quando a sua requisição resulta na criação bem-sucedida de um novo recurso no servidor. Exemplo principal: realizar um Checkout bem-sucedido (`/api/v1/pedidos/checkout`).
- **O que esperar:** O objeto JSON representando o recurso recém-criado (geralmente contendo o novo ID gerado no banco de dados).

---

## 🟡 Erros do Cliente (4xx)

Estes códigos indicam que a falha ocorreu do lado de quem chamou a API. Geralmente, significa que você precisa corrigir o payload ou o ID enviado antes de tentar novamente.

### `400 Bad Request`
- **Quando ocorre:** Ocorre quando o payload da requisição contém falhas de validação sintática (ex: campos obrigatórios ausentes, e-mails em formato inválido, CPF incorreto).
- **Tratamento:** O nosso Global Exception Handler intercepta falhas de `@Valid` e injeta as propriedades detalhadas na resposta.
- **Formato da Resposta:** O JSON retornará uma lista `invalidParams` detalhando exatamente quais campos falharam para facilitar a exibição no formulário do Frontend.

### `404 Not Found`
- **Quando ocorre:** A URL solicitada não existe ou o ID de um recurso passado na requisição (ex: `produtoId`) não foi encontrado no banco de dados.
- **Tratamento:** Acionado ao lançarmos uma `EntityNotFoundException`.

### `409 Conflict`
- **Quando ocorre:** **(A nossa principal defesa contra Concorrência Simultânea)**. Ocorre quando a requisição tenta modificar um recurso que acabou de ser atualizado por outra transação no exato mesmo momento.
- **Tratamento:** Acionado pelo mecanismo de _Optimistic Locking_ do banco de dados para evitar vendas duplas. O Frontend deve usar este status para informar ao cliente que o estoque acabou de esgotar ou sofrer alteração.

### `422 Unprocessable Entity`
- **Quando ocorre:** O payload está formatado corretamente, mas contém erros semânticos ou viola regras de negócio intransponíveis do nosso domínio.
- **Tratamento:** Exemplo clássico: Tentar finalizar um carrinho onde o produto existe, mas a quantidade solicitada é maior do que a quantidade disponível no estoque (`EstoqueInsuficienteException`).

---

## 🔴 Erros do Servidor (5xx)

Estes códigos indicam que a requisição do cliente era perfeitamente válida, mas o servidor falhou em completá-la devido a um erro inesperado.

### `500 Internal Server Error`
- **Quando ocorre:** Falhas não mapeadas, quedas de banco de dados, exceções de ponteiro nulo (NullPointer) ou timeouts imprevistos.
- **Tratamento:** Para garantir a segurança (PCI-DSS), a nossa API **jamais** vaza o *Stack Trace* original para o Frontend. A resposta 500 sempre devolverá uma mensagem padronizada e segura ("Ocorreu um erro interno inesperado no servidor"), enquanto o erro real será registrado apenas nos logs protegidos do servidor.

---

## Estrutura do Payload de Erro (RFC 7807)

Quando qualquer erro `4xx` ou `5xx` ocorrer, o Payload de resposta seguirá obrigatoriamente a estrutura `ProblemDetail` nativa do Spring Boot 3:

```json
{
  "type": "https://ecommerce.com/erros/dados-invalidos",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.",
  "instance": "/api/v1/pedidos/checkout",
  "invalidParams": {
    "nome": "O nome é obrigatório",
    "cpf": "O CPF é obrigatório"
  }
}
```

**Benefício para o Frontend:** Com este contrato sólido, o desenvolvedor UI/UX só precisa ler os campos `title` e `detail` (e iterar sobre `invalidParams` se existir) para construir componentes visuais e *toasts* de aviso perfeitos para o usuário.
