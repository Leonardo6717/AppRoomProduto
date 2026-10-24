package br.universidade.approomproduto.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import br.universidade.approomproduto.databinding.ActivityMainBinding
import br.universidade.approomproduto.db.AppDatabase
import br.universidade.approomproduto.model.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: br.universidade.approomproduto.dao.ProductDao

    private val currencyFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "database-name"
        )
            // Para este trabalho, recria o banco se a estrutura mudar.
            // Em produção, prefira criar uma Migration para preservar os dados.
            .fallbackToDestructiveMigration()
            .build()

        productDao = db.productDao()

        binding.btnInserirProduto.setOnClickListener { inserirProduto() }
        binding.btnListarProdutos.setOnClickListener { listarProdutos() }
        binding.btnBuscarProduto.setOnClickListener { buscarProdutos() }
        binding.btnExcluirTodos.setOnClickListener { excluirTodos() }
        binding.btnAtualizarPreco.setOnClickListener { atualizarPreco() }
    }

    private fun inserirProduto() {
        val nome = binding.editProductName.text.toString().trim()
        val precoTexto = binding.editProductPrice.text.toString().trim().replace(",", ".")
        val descricao = binding.editProductDescription.text.toString().trim()
        val quantidadeTexto = binding.editProductQuantity.text.toString().trim()
        val preco = precoTexto.toDoubleOrNull()
        val quantidade = quantidadeTexto.toIntOrNull()

        if (nome.isEmpty()) {
            binding.editProductName.error = "Informe o nome do produto"
            binding.editProductName.requestFocus()
            return
        }

        if (preco == null || preco <= 0.0) {
            binding.editProductPrice.error = "Informe um preço válido"
            binding.editProductPrice.requestFocus()
            return
        }

        if (quantidade == null || quantidade < 0) {
            binding.editProductQuantity.error = "Informe uma quantidade igual ou maior que zero"
            binding.editProductQuantity.requestFocus()
            return
        }

        val produto = Product(
            name = nome,
            price = preco,
            description = descricao.ifEmpty { null },
            quantity = quantidade
        )

        lifecycleScope.launch {
            productDao.insertAll(produto)
            Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()

            // Parte A1: limpar os campos e devolver o foco ao campo Nome.
            binding.editProductName.text.clear()
            binding.editProductPrice.text.clear()
            binding.editProductDescription.text.clear()
            binding.editProductQuantity.text.clear()
            binding.editProductName.requestFocus()

            atualizarLista(productDao.getAll())
        }
    }

    private fun listarProdutos() {
        lifecycleScope.launch {
            atualizarLista(productDao.getAll())
        }
    }

    private fun buscarProdutos() {
        val texto = binding.editBuscaNome.text.toString().trim()
        if (texto.isEmpty()) {
            binding.editBuscaNome.error = "Digite um nome para buscar"
            return
        }

        lifecycleScope.launch {
            atualizarLista(productDao.buscarPorNome("%$texto%"))
        }
    }

    private fun excluirTodos() {
        lifecycleScope.launch {
            productDao.deleteAll()
            atualizarLista(emptyList())
            Toast.makeText(this@MainActivity, "Todos os produtos foram excluídos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizarPreco() {
        val id = binding.editProductId.text.toString().trim().toIntOrNull()
        val novoPreco = binding.editNovoPreco.text.toString().trim()
            .replace(",", ".").toDoubleOrNull()

        if (id == null || id <= 0) {
            binding.editProductId.error = "Informe um ID válido"
            return
        }
        if (novoPreco == null || novoPreco <= 0.0) {
            binding.editNovoPreco.error = "Informe um preço válido"
            return
        }

        lifecycleScope.launch {
            val produtoExistente = productDao.buscarPorId(id)
            if (produtoExistente == null) {
                Toast.makeText(this@MainActivity, "Não existe produto com esse ID.", Toast.LENGTH_SHORT).show()
            } else {
                productDao.updateProduct(produtoExistente.copy(price = novoPreco))
                Toast.makeText(this@MainActivity, "Preço atualizado!", Toast.LENGTH_SHORT).show()
                atualizarLista(productDao.getAll())
            }
        }
    }

    private fun atualizarLista(produtos: List<Product>) {
        val totalPreco = produtos.sumOf { it.price * it.quantity }
        binding.textResumoEstoque.text =
            "Estoque: ${produtos.size} produto(s) | Valor total: ${currencyFormat.format(totalPreco)}"

        binding.textListaProdutos.text = if (produtos.isEmpty()) {
            "Nenhum produto encontrado."
        } else {
            produtos.joinToString(separator = "\n\n") { produto ->
                "ID: ${produto.id}\n" +
                    "Nome: ${produto.name}\n" +
                    "Preço unitário: ${currencyFormat.format(produto.price)}\n" +
                    "Quantidade: ${produto.quantity}\n" +
                    "Valor em estoque: ${currencyFormat.format(produto.price * produto.quantity)}\n" +
                    "Descrição: ${produto.description ?: "Sem descrição"}"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized) db.close()
    }
}
