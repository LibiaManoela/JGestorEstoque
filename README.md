# 📦 Gestor Estoque

Aplicação desktop desenvolvida em **JavaFX** para controle de materiais e insumos. O sistema permite consultar, buscar, cadastrar, editar, excluir e repor itens de estoque de forma rápida, comunicando-se com uma API REST de forma assíncrona para manter a interface sempre fluida e responsiva.

---

## 🎯 Objetivos do Projeto

* **Centralizar o controle de inventário:** Visualizar códigos SKU, quantidades atuais, estoque mínimo e preços unitários em um único catálogo filtrável.
* **Prevenir rupturas de estoque:** Isolar itens em nível crítico (`quantidade <= estoque mínimo`) em um painel dedicado para reposição rápida.
* **Garantir integridade dos dados:** Validar regras de obrigatoriedade, formato e intervalos numéricos antes de qualquer envio pela rede.
* **Comunicação assíncrona resiliente:** Consumir endpoints REST sem travar a interface gráfica, oferecendo retorno visual de carregamento e recuperação de falhas de conexão.

---

## 🛠️ Tecnologias Utilizadas

* **Java 21:** Linguagem principal do projeto.
* **JavaFX 21 (FXML + CSS):** Construção das telas declarativas e estilização visual centralizada.
* **Java `HttpClient` (`sendAsync`):** Cliente HTTP nativo para requisições assíncronas (`GET`, `POST`, `PUT`, `DELETE`) em segundo plano.
* **Jackson Databind:** Serialização e desserialização automática entre objetos Java e JSON.
* **Ikonli (Feather Icons):** Biblioteca de ícones vetoriais *outline* para manter consistência visual em qualquer resolução.
* **Apache Maven:** Gerenciamento de dependências, execução (`javafx:run`) e empacotamento (`javafx:jlink`).
* **DummyJSON API:** API REST pública utilizada para simular operações de leitura e escrita de produtos.

---

## 🏗️ Arquitetura do Sistema

O projeto segue uma divisão modular clara, separando interface, controle de eventos, regras de validação e comunicação externa:

```text
src/main/
├── java/br/com/estoque/
│   ├── App.java                     # Ponto de entrada da aplicação JavaFX
│   ├── controller/
│   │   ├── PrincipalController.java # Gerencia o menu lateral, troca de telas, loading e banners de rede
│   │   ├── CatalogoController.java  # Tabela principal, busca em tempo real, filtro por categoria e exclusão
│   │   ├── FormularioController.java# Cadastro (POST) e edição (PUT) com feedback visual de erros
│   │   └── ReposicaoController.java # Filtra itens abaixo do mínimo e executa entradas rápidas (PUT)
│   ├── model/
│   │   ├── ItemEstoque.java         # Modelo de domínio mapeado com anotações Jackson
│   │   └── RespostaEstoqueApi.java  # Wrapper para a lista retornada pela API
│   ├── service/
│   │   └── EstoqueApiService.java   # Chamadas HTTP assíncronas, timeout e simulação de falha de rede
│   └── validation/
│       └── ValidadorEstoque.java    # Regras de negócio e validação desacopladas da interface
└── resources/
    ├── css/estilo.css               # Folha de estilo global da aplicação
    └── fxml/                        # Telas declarativas (principal, catalogo, formulario, reposicao)