package br.universidade.approomproduto.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.universidade.approomproduto.model.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM product ORDER BY id DESC")
    suspend fun getAll(): List<Product>

    @Query("SELECT * FROM product WHERE name LIKE :nome ORDER BY id DESC")
    suspend fun buscarPorNome(nome: String): List<Product>

    @Insert
    suspend fun insertAll(vararg products: Product)

    @Query("DELETE FROM product")
    suspend fun deleteAll()

    @Update
    suspend fun updateProduct(product: Product)

    @Query("SELECT * FROM product WHERE id = :productId LIMIT 1")
    suspend fun buscarPorId(productId: Int): Product?
}
