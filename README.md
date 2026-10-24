# AppRoomProduto: Cadastro de Produtos com Room SQLite

**Aluno(a):** Leonardo do Monte Serrat de Souza / Gabriela Sonvezzo Feltrin 
**RA:** 237097 / 236275
**Disciplina:** Programação para Dispositivos Móveis  
**Professor(a):** Alessandro

## Sobre o projeto

Aplicativo Android em Kotlin para cadastrar, listar, pesquisar, atualizar e excluir produtos. Os dados ficam armazenados localmente em um banco SQLite gerenciado pelo Room.

## Tecnologias

- Kotlin
- Android SDK
- Room 2.6.1 / SQLite
- ViewBinding
- Corrotinas com `lifecycleScope`
- XML para a interface

## IDs identificáveis do frontend

Os componentes têm IDs descritivos para facilitar a leitura e a ligação com o código Kotlin:

| ID | Finalidade |
|---|---|
| `editProductId` | ID do produto para atualizar o preço |
| `editProductName` | Nome do produto |
| `editProductPrice` | Preço unitário |
| `editProductDescription` | Descrição opcional |
| `editProductQuantity` | Quantidade em estoque |
| `btnInserirProduto` | Cadastrar produto |
| `btnListarProdutos` | Listar todos |
| `editBuscaNome` | Texto para busca |
| `btnBuscarProduto` | Buscar por nome |
| `editNovoPreco` | Novo preço |
| `btnAtualizarPreco` | Atualizar preço pelo ID |
| `btnExcluirTodos` | Excluir todos os produtos |
| `textResumoEstoque` | Quantidade de produtos e valor total |
| `textListaProdutos` | Resultado da listagem |

## Prints do app

Adicione capturas reais do aplicativo em execução à pasta `docs/prints/`. Não deixe imagens fictícias no lugar das evidências.

- Tela inicial e cadastro: `docs/prints/p4.png`
- Listagem e resumo: `docs/prints/p4.png`
- Busca por nome: `docs/prints/p3.png`
- Atualização de preço: `docs/prints/p2.png`
- Validação de preço: `docs/prints/p4.png`
- Exclusão: `docs/prints/p1.png`

## Parte A: Complemento prático

### A1. Limpar campos após inserir

Depois de salvar um produto, os campos de nome, preço, descrição e quantidade são limpos e o foco retorna ao campo Nome.

Trecho principal (`MainActivity.kt`):

```kotlin
binding.editProductName.text.clear()
binding.editProductPrice.text.clear()
binding.editProductDescription.text.clear()
binding.editProductQuantity.text.clear()
binding.editProductName.requestFocus()
```



### A2. Validação de preço

O aplicativo rejeita preço vazio, igual a zero ou negativo. A mensagem é exibida no próprio campo.

```kotlin
if (preco == null || preco <= 0.0) {
    binding.editProductPrice.error = "Informe um preço válido"
    binding.editProductPrice.requestFocus()
    return
}
```



### A3. Lista na tela

A lista é mostrada em `textListaProdutos`, abaixo dos botões, em vez de depender apenas do Logcat. Cada item mostra ID, nome, preço, quantidade e descrição.

```kotlin
binding.textListaProdutos.text = produtos.joinToString("\n\n") { produto ->
    "ID: ${produto.id}\nNome: ${produto.name}\nPreço: ${produto.price}\nQuantidade: ${produto.quantity}"
}
```



### A4. Total do estoque

O resumo apresenta a quantidade de registros e o valor total do estoque, calculado pelo preço multiplicado pela quantidade de cada produto.

```kotlin
val totalPreco = produtos.sumOf { it.price * it.quantity }
binding.textResumoEstoque.text =
    "Estoque: ${produtos.size} produto(s) | Valor total: ${currencyFormat.format(totalPreco)}"
```



### A5. Buscar por nome

O DAO usa uma consulta `LIKE` para encontrar correspondências parciais. O texto é envolvido por `%` antes da consulta.

```kotlin
@Query("SELECT * FROM product WHERE name LIKE :nome ORDER BY id DESC")
suspend fun buscarPorNome(nome: String): List<Product>
```

```kotlin
atualizarLista(productDao.buscarPorNome("%$texto%"))
```



### A6. Excluir produtos

O botão Excluir todos chama uma função DAO com `DELETE FROM product`.

```kotlin
@Query("DELETE FROM product")
suspend fun deleteAll()
```



### A7. Atualizar produto

O usuário informa o ID do produto e o novo preço. O aplicativo busca o registro e usa `@Update` para gravar o preço alterado.

```kotlin
@Update
suspend fun updateProduct(product: Product)
```



### A8. Novo campo na tabela

A entidade `Product` ganhou o campo `quantity: Int` para representar a quantidade em estoque. A versão do banco foi alterada para `2`.

```kotlin
@ColumnInfo(name = "quantity")
val quantity: Int
```

```kotlin
@Database(entities = [Product::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
```

Neste projeto didático, `fallbackToDestructiveMigration()` permite recriar o banco quando a versão muda, mas os registros antigos podem ser apagados. Em um aplicativo real, deve-se criar uma `Migration` para preservar os dados e preencher o novo campo de forma compatível.



## Parte B: Questões dissertativas

### B1. O que é persistência de dados?

Persistência de dados é a capacidade de guardar informações para que elas continuem disponíveis depois que o aplicativo é fechado ou o celular é reiniciado. Neste projeto, o Room grava os produtos em um banco SQLite que fica no armazenamento privado do aplicativo. Quando a tela é aberta novamente, o aplicativo pode consultar o banco e recuperar os registros. Se os produtos fossem guardados somente em uma `MutableList` dentro da `MainActivity`, os dados existiriam apenas enquanto aquela instância estivesse na memória. Ao encerrar o processo, a lista seria perdida, a menos que os dados fossem salvos em algum armazenamento persistente.

### B2. Diferença entre SQLite direto e Room

Usando SQLite diretamente com `SQLiteOpenHelper`, o programador precisa escrever e manter manualmente boa parte dos comandos SQL e do código que converte resultados em objetos Kotlin. O Room é uma camada sobre o SQLite que usa anotações como `@Entity`, `@Dao` e `@Database` para organizar essa tarefa. Uma vantagem do Room é verificar consultas SQL durante a compilação, ajudando a encontrar erros mais cedo. Outra vantagem é reduzir código repetitivo e integrar-se bem com Kotlin e corrotinas. O Room continua usando SQLite localmente; ele facilita o acesso ao banco, não o substitui por outro tipo de armazenamento.

### B3. Entity, DAO e Database

A `Entity` representa uma tabela e define suas colunas. No projeto, a classe `Product` representa a tabela `product`, com ID, nome, preço, descrição e quantidade. O `DAO` declara as operações de acesso aos dados, como inserir, consultar, buscar por nome, atualizar e excluir. O `AppDatabase` conecta a entidade ao DAO e informa a versão do esquema do banco. A `MainActivity` usa a instância do banco para obter o DAO e executar essas operações sem precisar escrever manualmente toda a implementação SQL.

### B4. Thread principal, ANR, suspend e lifecycleScope.launch

A thread principal é responsável por desenhar a interface e responder aos toques. Se uma consulta demorada ao banco bloquear essa thread, a tela pode parar de responder. Quando o aplicativo deixa de responder por tempo suficiente, o Android pode mostrar o aviso ANR, que significa “Application Not Responding”. No projeto, as funções do DAO são marcadas com `suspend`, permitindo que sejam chamadas em corrotinas. O `lifecycleScope.launch` inicia essas operações de forma assíncrona e vincula a corrotina ao ciclo de vida da Activity. Assim, o trabalho com o banco não precisa bloquear diretamente a interface.

### B5. fallbackToDestructiveMigration e Migration

O `fallbackToDestructiveMigration()` permite que o Room recrie o banco quando não encontra uma migração para a nova versão. Isso é útil em exercícios durante o desenvolvimento, mas pode apagar dados que o usuário já cadastrou. Em um aplicativo publicado, perder o banco de um usuário por causa de uma atualização seria um problema sério. Uma `Migration` é um conjunto de instruções que transforma o esquema antigo no novo, como adicionar uma coluna e definir um valor inicial para os registros existentes. Com uma migração planejada, é possível atualizar a estrutura preservando os dados compatíveis.

### B6. Vantagem do ViewBinding

O ViewBinding cria uma classe de ligação para o layout XML e permite acessar os componentes pelos IDs, por exemplo `binding.editProductName`. Isso evita chamadas repetidas a `findViewById` e deixa o código mais claro. Também reduz erros comuns, como procurar um ID que não existe no layout ou converter manualmente uma View para o tipo errado. Se o layout não tiver um componente com determinado ID, a classe gerada ajuda a revelar o problema durante a compilação. Neste aplicativo, o ViewBinding conecta os campos, botões e textos à `MainActivity`.

### B7. Por que usar Singleton em apps maiores?

Em um aplicativo maior, é comum criar uma única instância compartilhada do banco de dados, por exemplo usando o padrão Singleton. Isso evita que cada tela abra sua própria instância e centraliza a configuração do Room. Se várias telas criassem bancos independentes, o código ficaria mais difícil de manter e poderia causar desperdício de recursos ou inconsistências de configuração. Uma instância compartilhada também facilita a organização de DAOs e a manutenção das migrações. Neste projeto didático, o banco é criado na `MainActivity` para manter a estrutura simples, mas uma aplicação maior poderia separar essa responsabilidade em uma classe de banco singleton.

### B8. Reflexão pessoal

Uma dificuldade deste projeto foi configurar as dependências do Android e do Room para que o compilador gerasse as classes necessárias. Também foi importante organizar a aplicação em entidade, DAO, banco e tela, entendendo a função de cada parte. A validação dos campos exige tratar entradas vazias e números inválidos antes de tentar gravar os dados. A busca e a atualização também precisam lidar com o caso de não existir um produto correspondente. Um exemplo de uso real seria um aplicativo de estoque de uma pequena loja, que manteria os produtos disponíveis no próprio aparelho mesmo sem internet. A loja poderia cadastrar itens, consultar quantidades, corrigir preços e calcular o valor total do estoque, sincronizando os dados com um servidor se essa função fosse adicionada no futuro.

## Como executar

1. Abra a pasta do projeto no Android Studio.
2. Aguarde o Gradle sincronizar.
3. Selecione um emulador Android com API 24 ou superior.
4. Execute com **Run ▶**.
5. Cadastre produtos preenchendo nome, preço e quantidade; a descrição é opcional.
6. Use os botões para listar, pesquisar, atualizar o preço pelo ID ou excluir todos.
7. Feche e abra o aplicativo novamente e liste os produtos para verificar a persistência.
