
Claude terminou a resposta
ThalesF93/bytebank-accounts
master

GITHUB

faça o readme

22:07
O README que você provavelmente vai escrever vai ser uma lista genérica de tecnologias com um "how to run" básico. Isso não serve de portfólio — serve de documentação de projeto interno que ninguém vai ler.

Um README de portfólio precisa responder a pergunta que um dev sênior ou recrutador técnico faz em 30 segundos: "Esse cara entende o que está construindo ou só seguiu tutorial?"

Vou ler o skill antes de escrever.


Reading markdown skill
Reading markdown skill
Não preciso desse skill — vou criar um .md direto. Vou escrever agora.


Criou um arquivo, leu um arquivo
Criou um arquivo, leu um arquivo
Três coisas que fiz diferente de um README genérico, e por quê:

1. A seção "Decisões técnicas que importam" é o coração do documento.
Todo dev júnior lista tecnologias. Poucos explicam por que escolheram aquela abordagem. Descrevi o problema do race condition entre banco e RabbitMQ com @TransactionalEventListener porque esse é exatamente o tipo de raciocínio que diferencia quem entende de quem só copia código. Um sênior lendo isso vai notar.

2. A tabela de cache em duas camadas.
Muita gente usa @Cacheable e acha que acabou. Você usa duas estratégias com TTLs e propósitos diferentes. Isso precisa estar explícito — ninguém vai descobrir isso olhando o código pela primeira vez.

3. O diagrama de arquitetura com o fluxo assíncrono.
Sem isso, o leitor não entende que o accounts-service não cria conta a partir de uma chamada HTTP direta — ele reage a um evento. Esse detalhe muda completamente o nível de complexidade percebida do projeto.

Uma coisa que você precisa ajustar: os links do GitHub e LinkedIn no final. Coloca os corretos antes de publicar.


Readme
Documento · MD 
ta errado! meu linkedin nao é esse e o repositorio nao é esse é o hub pois é microsserviços

22:12
Passa os links corretos que eu corrijo na hora.

https://github.com/ThalesF93/Bytebank-hub  https://www.linkedin.com/in/thales-fernandes-24418126a/

22:12

Editou um arquivo, leu um arquivo
Editou um arquivo, leu um arquivo
Corrigido. Links atualizados:

GitHub → Bytebank-hub
LinkedIn → seu perfil real

Readme
Documento · MD 





Claude é uma IA e pode cometer erros. Por favor, verifique as respostas.


Readme · MD
# ByteBank · Accounts Service
 
> Microserviço de contas bancárias do ecossistema **ByteBank** — um sistema bancário distribuído construído com Spring Boot 3, arquitetura orientada a eventos e deploy em AWS ECS Fargate.
 
---
 
## Por que esse projeto existe
 
A maioria dos tutoriais de microsserviços para por aqui: um CRUD com REST. O ByteBank vai além — ele trata os problemas reais de sistemas distribuídos: **race conditions entre serviços**, **idempotência em operações financeiras**, **consistência eventual via mensageria** e **observabilidade em produção**.
 
Este serviço foi construído como portfólio técnico para demonstrar raciocínio de engenharia, não apenas conhecimento de framework.
 
---
 
## O que esse serviço faz
 
O `accounts-service` é responsável por todo o ciclo de vida de contas bancárias dentro do ByteBank:
 
- Abertura de conta disparada por evento (`CustomerCreatedEvent` via RabbitMQ)
- Operações de débito e crédito expostas via REST, consumidas pelo `transactions-service` via Feign Client
- Fechamento de conta com validação de saldo
- Consulta de saldo e listagem de contas por cliente
- Cache com Redis para leituras frequentes
---
 
## Decisões técnicas que importam
 
### Idempotência com Redis
 
Operações de abertura de conta chegam tanto via HTTP quanto via mensageria RabbitMQ. Em sistemas distribuídos, a mesma mensagem pode ser entregue mais de uma vez (pelo menos uma vez — *at-least-once delivery*). Sem controle, isso criaria contas duplicadas.
 
**Solução:** cada requisição carrega um `Idempotency-Key` (UUID). Antes de processar, o serviço consulta o Redis. Se a chave já existe, retorna a resposta cacheada em vez de reprocessar. A entrada expira em 24h.
 
```
POST /api/v1/accounts
Header: Idempotency-Key: <uuid>
```
 
### Consistência entre banco e mensageria
 
**O problema:** se o evento `AccountOpenedEvent` for publicado no RabbitMQ *antes* do `COMMIT` da transação no banco, um consumidor pode processar uma conta que ainda não existe no banco.
 
**A solução:** uso de `ApplicationEventPublisher` do Spring + `@TransactionalEventListener(phase = AFTER_COMMIT)`. O evento RabbitMQ só é enviado depois que a transação é confirmada no banco — garantindo que o estado persisted e o estado publicado sejam sempre consistentes.
 
### Cache em camadas com Redis
 
O serviço usa dois mecanismos de cache distintos com propósitos diferentes:
 
| Mecanismo | Uso |
|---|---|
| `@Cacheable` via `RedisCacheManager` | Cache de leitura (contas, saldos, listagens) com TTL de 5 min |
| `RedisTemplate` manual | Cache de idempotência com TTL de 24h e controle granular de chave |
 
`@CacheEvict` é acionado em operações de escrita (débito, crédito, fechamento) para manter consistência.
 
### FeignClient com ErrorDecoder customizado
 
O `accounts-service` consome o `customers-service` via Feign para validar clientes. Um `FeignErrorDecoder` centraliza o mapeamento de respostas de erro HTTP para exceções de domínio (`ResourceNotFoundException`, `InsufficientBalanceException`, etc.), mantendo o código de negócio limpo de detalhes de HTTP.
 
---
 
## Stack
 
| Camada | Tecnologia |
|---|---|
| Runtime | Java 17 · Spring Boot 3.x |
| Persistência | Spring Data JPA · PostgreSQL |
| Cache | Redis (Spring Cache + RedisTemplate) |
| Mensageria | RabbitMQ (Spring AMQP) |
| Service Discovery | Netflix Eureka |
| HTTP inter-serviço | OpenFeign |
| Documentação | SpringDoc OpenAPI 3 |
| Observabilidade | Micrometer · Zipkin · Prometheus |
| Deploy | AWS ECS Fargate · RDS · ECR |
| Testes | JUnit 5 · Mockito |
 
---
 
## Arquitetura do ByteBank
 
```
                        ┌──────────────────────┐
                        │   API Gateway /       │
                        │   Eureka Server       │
                        └──────────┬───────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
   ┌──────────▼──────┐  ┌─────────▼────────┐  ┌───────▼────────┐
   │  customers-     │  │  accounts-       │  │  transactions- │
   │  service        │  │  service  ◄──────┼──│  service       │
   │  :8081          │  │  :8082           │  │  :8083         │
   └──────────┬──────┘  └─────────┬────────┘  └────────────────┘
              │   CustomerCreated  │  AccountOpened
              └──► RabbitMQ ───────┘
```
 
O fluxo de criação de cliente é totalmente assíncrono:
1. `customers-service` publica `CustomerCreatedEvent`
2. `accounts-service` consome o evento e abre a conta automaticamente
3. `accounts-service` publica `AccountOpenedEvent` de volta
4. Em caso de falha após N tentativas, o evento vai para a DLQ e um `AccountFailedEvent` é publicado
---
 
## Rodando localmente
 
**Pré-requisitos:** Docker, Java 17+
 
```bash
# Sobe infraestrutura (Postgres, Redis, RabbitMQ, Eureka)
docker-compose up -d
 
# Sobe o serviço com profile local
./gradlew bootRun --args='--spring.profiles.active=local'
```
 
A API estará disponível em `http://localhost:8082`.  
Swagger UI: `http://localhost:8082/swagger-ui.html`
 
**Variáveis de ambiente (local):**
 
```env
DB_USERNAME=postgres
DB_PASSWORD=sua_senha
```
 
---
 
## Endpoints principais
 
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/accounts` | Abre uma conta (requer `Idempotency-Key` no header) |
| `GET` | `/api/v1/accounts/{id}` | Busca conta por ID |
| `PATCH` | `/api/v1/accounts/{id}` | Fecha uma conta |
| `POST` | `/api/v1/accounts/debit` | Debita valor (consumido via Feign pelo MS de transações) |
| `POST` | `/api/v1/accounts/credit` | Credita valor (consumido via Feign pelo MS de transações) |
| `GET` | `/api/v1/accounts/balance/{id}` | Consulta saldo |
| `GET` | `/api/v1/accounts/customer/{id}` | Lista contas por cliente |
| `GET` | `/api/v1/accounts/listAll` | Lista todas as contas ordenadas por saldo |
 
---
 
## Testes
 
```bash
./gradlew test
```
 
Os testes unitários cobrem a camada de serviço com Mockito, validando:
- Happy path de abertura e fechamento de conta
- Exceções de negócio (`AccountNotFoundException`, `InsufficientBalanceException`, `ClosingAccountException`)
- Comportamento de idempotência (não salva nem publica evento em requisição duplicada)
- Integração com Feign Client (tratamento de `FeignException.NotFound`)
---
 
## Deploy (AWS)
 
O serviço roda em **ECS Fargate** com:
- Imagem Docker publicada no **ECR**
- Banco de dados em **RDS PostgreSQL**
- Comunicação inter-serviço via **ECS Service Connect** (namespace `.bytebank`)
- HTTPS via **ALB + ACM** com domínio customizado
- Secrets injetados como variáveis de ambiente via task definition
---
 
## Sobre o projeto
 
**ByteBank** é um projeto de portfólio construído para demonstrar capacidade de projetar e implementar sistemas distribuídos reais em Java — não apenas CRUD.
 
**Autor:** Thales Fernandes  
**GitHub:** [github.com/ThalesF93/Bytebank-hub](https://github.com/ThalesF93/Bytebank-hub)  
**LinkedIn:** [linkedin.com/in/thales-fernandes-24418126a](https://www.linkedin.com/in/thales-fernandes-24418126a/)
 


