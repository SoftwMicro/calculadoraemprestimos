# Calculadora de Empréstimos

Uma aplicação Spring Boot para cálculo de parcelas de empréstimos com sistema de amortização. A aplicação calcula detalhadamente as parcelas, juros, amortizações e saldos ao longo do período do empréstimo.

## 📋 Sumário

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Requisitos do Sistema](#requisitos-do-sistema)
- [Dependências e Tecnologias](#dependências-e-tecnologias)
- [Instalação e Preparação](#instalação-e-preparação)
- [Compilação](#compilação)
- [Execução](#execução)
- [Uso da API](#uso-da-api)


## 🎯 Visão Geral

A **Calculadora de Empréstimos** é uma API REST que realiza cálculos complexos de amortização de empréstimos. A aplicação:

- Calcula parcelas mensais fixas com juros compostos
- Gera cronograma detalhado de pagamentos
- Registra histórico de empréstimos em banco de dados
- Fornece informações de saldo devedor, provisão de juros e amortização
- Oferece interface Swagger/OpenAPI para testes

### Principais Características

- ✅ Cálculo automático de parcelas com sistema de amortização (Price/Anuidade)
- ✅ Suporte a taxa de juros mensais configurável
- ✅ Validação de datas de competência
- ✅ Persistência de dados em banco de dados
- ✅ Documentação interativa via Swagger UI
- ✅ Logging detalhado de requisições e erros
- ✅ Console H2 para inspeção do banco de dados
- ✅ CORS configurado para requisições de diferentes origens

---

## 🏗️ Arquitetura

A aplicação segue o padrão **MVC (Model-View-Controller)** com separação clara de responsabilidades:

### Estrutura em Camadas

```
┌─────────────────────────────────────────┐
│         API REST (Controller)            │
│   EmprestimoController                  │
│   - POST /api/emprestimos/calcular      │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│         Lógica de Negócio (Service)      │
│   EmprestimoService                     │
│   - Validações                          │
│   - Cálculo de parcelas                 │
│   - Lógica de amortização               │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│      Acesso a Dados (Repository)        │
│   EmprestimoRepository                  │
│   - Operações CRUD                      │
│   - Persistência em banco de dados      │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│      Banco de Dados (H2 ou MySQL)       │
│   - Tabela: emprestimos                 │
│   - Dados: valores, datas, juros        │
└─────────────────────────────────────────┘
```

### Componentes Principais

| Componente | Descrição |
|-----------|-----------|
| **EmprestimoController** | Recebe requisições HTTP e retorna respostas JSON |
| **EmprestimoService** | Implementa lógica de cálculo de empréstimos e amortização |
| **EmprestimoRepository** | Interface JPA para persistência de dados |
| **Emprestimo (Entity)** | Modelo de dados do empréstimo |
| **EmprestimoRequest** | DTO com parâmetros da requisição |
| **Parcela** | DTO com detalhes de cada parcela/período |

### Configurações

| Classe | Responsabilidade |
|--------|-----------------|
| **CorsConfig** | Configuração de CORS para requisições cross-origin |
| **GlobalExceptionHandler** | Tratamento centralizado de exceções |
| **RequestLoggingConfig** | Configuração de logging de requisições |
| **SwaggerConfig** | Configuração do Swagger/OpenAPI |

---

## 💻 Requisitos do Sistema

### Requisitos Obrigatórios

- **Java Development Kit (JDK)**: Versão 17 ou superior
- **Apache Maven**: Versão 3.6 ou superior (para compilação e execução)
- **Sistema Operacional**: Windows, Linux ou macOS

### Requisitos Opcionais

- **Git**: Para clonar o repositório
- **IDE**: IntelliJ IDEA, Eclipse ou VS Code com extensões Java
- **MySQL Server**: Se desejar usar MySQL como banco de dados (padrão é H2 em memória)

### Verificar Requisitos

```powershell
# Verificar versão do Java
java -version

# Verificar versão do Maven
mvn --version
```

---

## 📦 Dependências e Tecnologias

### Dependências Principais

| Dependência | Versão | Descrição |
|-------------|--------|-----------|
| **Spring Boot** | 4.0.8-SNAPSHOT | Framework principal |
| **Spring Data JPA** | Latest | Persistência de dados |
| **Spring Web MVC** | Latest | Framework web |
| **H2 Database** | Latest | Banco de dados em memória (padrão) |
| **MySQL Connector** | Latest | Driver para MySQL (opcional) |
| **SpringDoc OpenAPI** | 3.0.2 | Documentação Swagger/OpenAPI |
| **Jakarta Persistence** | Latest | Especificação JPA |
| **Hibernate** | Latest | Implementação JPA |

### Dependências de Teste

- Spring Boot Test (Data JPA Test, Web MVC Test)

### Tecnologias Utilizadas

- **Linguagem**: Java 17
- **Build Tool**: Apache Maven
- **Framework Web**: Spring Boot
- **Banco de Dados**: H2 (padrão) / MySQL (configurável)
- **ORM**: Hibernate + Spring Data JPA
- **Documentação API**: OpenAPI 3.0 (Swagger UI)
- **Log**: SLF4J + Logback

---

## 🔧 Instalação e Preparação

### Passo 1: Pré-requisitos

Certifique-se de ter Java 17+ instalado:

```powershell
java -version
```

Se não tiver instalado, baixe do [site oficial do Java](https://www.oracle.com/java/technologies/downloads/#java17).

### Passo 2: Clonar ou Preparar o Projeto

#### Se usando Git:
```powershell
git clone https://github.com/SoftwMicro/calculadoraemprestimos.git

cd calculadoraemprestimos
```

#### Se já tem o projeto:
```powershell
cd C:\Projetos\calculadoraemprestimos
```

### Passo 3: Verificar Estrutura do Projeto

```powershell
# Verificar arquivo pom.xml
Test-Path -Path ".\pom.xml"

# Listar estrutura
Get-ChildItem -Recurse -Directory src\
```

### Passo 4: Limpar Build Anterior (Opcional)

Se houver build anterior, limpe os arquivos compilados:

```powershell
mvn clean
```

---

## 🛠️ Compilação

### Compilar o Projeto

```powershell
# Navegar até o diretório do projeto
cd C:\Projetos\calculadoraemprestimos

# Compilar com Maven
mvn compile
```

### Compilar com Testes

```powershell
mvn clean install
```

Isso irá:
1. Limpar artefatos anteriores
2. Compilar o código-fonte
3. Executar os testes
4. Criar o artefato JAR

### Compilar sem Executar Testes

```powershell
mvn clean install -DskipTests
```

### Gerar o JAR Executável

```powershell
mvn clean package
```

O arquivo JAR será gerado em: `target\calculadora-emprestimos-0.0.1-SNAPSHOT.jar`

---

## 🚀 Execução

### Opção 1: Usando Maven

```powershell
# Navegar até o projeto
cd C:\Projetos\calculadoraemprestimos

# Executar via Maven
mvn spring-boot:run
```

### Opção 2: Executar o JAR Compilado

```powershell
# Primeiro compilar se não tiver feito
mvn clean package

# Depois executar
java -jar .\target\calculadora-emprestimos-0.0.1-SNAPSHOT.jar
```

### Opção 3: Usando o Script de Execução

```powershell
# Executar o script incluído no projeto
.\run.bat
```

### Verificar se Aplicação Iniciou

Após executar, você deve ver logs similares a:

```
...
2026-07-05 10:30:45.123 INFO ... : Starting CalculadoraEmprestimosApplication
2026-07-05 10:30:47.456 INFO ... : Tomcat started on port(s): 8080
2026-07-05 10:30:47.789 INFO ... : Started CalculadoraEmprestimosApplication
```

A aplicação estará disponível em: **http://localhost:8080**

---

## 📡 Uso da API

### Endpoint Principal

**POST** `/api/emprestimos/calcular`

Calcula as parcelas de um empréstimo baseado nos parâmetros fornecidos.

### Requisição (JSON)

```json
{
  "dataInicial": "2026-01-15",
  "dataFinal": "2026-12-15",
  "primeiroPagamento": "2026-02-15",
  "valor": 10000.00,
  "taxaJuros": 1.5
}
```

### Parâmetros de Requisição

| Campo | Tipo | Descrição | Exemplo |
|-------|------|-----------|---------|
| **dataInicial** | Date | Data de início do empréstimo | 2026-01-15 |
| **dataFinal** | Date | Data final do empréstimo | 2026-12-15 |
| **primeiroPagamento** | Date | Data da primeira parcela | 2026-02-15 |
| **valor** | BigDecimal | Valor total do empréstimo em reais | 10000.00 |
| **taxaJuros** | BigDecimal | Taxa de juros mensal em percentual | 1.5 (para 1,5% ao mês) |

### Resposta (JSON)

```json
[
  {
    "dataCompetencia": "2026-01-15",
    "valorEmprestimo": 10000.00,
    "saldoDevedor": 10000.00,
    "consolidada": "",
    "total": 0.00,
    "amortizacao": 0.00,
    "saldo": 10000.00,
    "provisao": 150.00,
    "acumulado": 150.00,
    "pago": 0.00
  },
  {
    "dataCompetencia": "2026-02-15",
    "valorEmprestimo": 10000.00,
    "saldoDevedor": 10000.00,
    "consolidada": "1/12",
    "total": 1000.00,
    "amortizacao": 850.00,
    "saldo": 9150.00,
    "provisao": 150.00,
    "acumulado": 0.00,
    "pago": 1000.00
  }
]
```

### Parâmetros de Resposta

| Campo | Tipo | Descrição |
|-------|------|-----------|
| **dataCompetencia** | Date | Data do período/parcela |
| **valorEmprestimo** | BigDecimal | Valor original do empréstimo |
| **saldoDevedor** | BigDecimal | Saldo devedor nesta data |
| **consolidada** | String | Número da parcela (ex: "1/12") ou vazio se não é data de pagamento |
| **total** | BigDecimal | Valor total pago (juros + amortização) |
| **amortizacao** | BigDecimal | Valor de amortização do principal |
| **saldo** | BigDecimal | Saldo após pagamento |
| **provisao** | BigDecimal | Juros provisionados neste período |
| **acumulado** | BigDecimal | Juros acumulados até esta data |
| **pago** | BigDecimal | Valor efetivamente pago nesta data |


## 📚 Documentação da API

### Swagger UI

A documentação interativa da API está disponível via **Swagger UI**:

**URL**: http://localhost:8080/swagger-ui.html

Nesta página você pode:
- ✅ Ver todos os endpoints disponíveis
- ✅ Entender parâmetros e respostas
- ✅ Testar os endpoints diretamente
- ✅ Ver exemplos de requisição/resposta

### OpenAPI JSON

A especificação OpenAPI completa está em:

**URL**: http://localhost:8080/v3/api-docs

---

## 📁 Estrutura do Projeto

```
calculadoraemprestimos/
├── src/
│   ├── main/
│   │   ├── java/com/softwmicro/calculadora_emprestimos/
│   │   │   ├── CalculadoraEmprestimosApplication.java      # Classe principal
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java                         # Configuração CORS
│   │   │   │   ├── GlobalExceptionHandler.java             # Tratamento de exceções
│   │   │   │   ├── RequestLoggingConfig.java               # Logging de requisições
│   │   │   │   └── SwaggerConfig.java                      # Configuração Swagger
│   │   │   ├── controller/
│   │   │   │   └── EmprestimoController.java               # Endpoints REST
│   │   │   ├── model/
│   │   │   │   ├── Emprestimo.java                         # Entity JPA
│   │   │   │   ├── EmprestimoRequest.java                  # DTO de requisição
│   │   │   │   └── Parcela.java                            # DTO de resposta
│   │   │   ├── repository/
│   │   │   │   └── EmprestimoRepository.java               # Data Access Object
│   │   │   └── service/
│   │   │       └── EmprestimoService.java                  # Lógica de negócio
│   │   └── resources/
│   │       ├── application.properties                      # Configurações
│   │       ├── static/                                     # Arquivos estáticos
│   │       └── templates/                                  # Templates HTML
│   └── test/
│       └── java/com/softwmicro/calculadora_emprestimos/
│           └── CalculadoraEmprestimosApplicationTests.java # Testes
├── target/                                                   # Artefatos compilados
├── pom.xml                                                   # Configuração Maven
├── mvnw e mvnw.cmd                                          # Maven Wrapper
├── run.bat                                                   # Script de execução
├── README.md                                                 # Este arquivo
└── HELP.md                                                   # Ajuda adicional
```

