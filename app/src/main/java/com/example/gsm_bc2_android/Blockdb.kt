package com.example.gsm_bc2_android

//import android.arch.persistence.room.*
import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.room.*

@Entity
data class UserInfo(
    @PrimaryKey(autoGenerate = true) val uid: Int?,
    val email:String?,
    val account:Int
)

@Entity
data class block_tbl(
    @PrimaryKey val bid: Int?,
    val email:String?,
    val balance:Int,
    val menu:String,
    val price:Int,
    val quantity: Int
)

@Entity
data class mining_tbl(
    @PrimaryKey val mid: Int?,
    val email:String,
    val balance:Int,
    val charged_money:Int
)

@Dao
interface MiningDao {
    @Insert
    fun insertMining(mining: mining_tbl)

    @Query("select * from mining_tbl")
    fun getAllMining(): List<mining_tbl>
}

@Dao
interface BlockDao{
    @Insert
    fun insertblock(block: block_tbl)

    @Query("select * from block_tbl")
    fun getAllblock(): List<block_tbl>
}

@Dao
interface UserDao {
    // User
    @Insert
    fun insertUser(userInfo: UserInfo) //

    @Query("select * from UserInfo")
    fun getAllUser(): List<UserInfo>


    @Query("select account from UserInfo where email = :email")
    fun getAccountByEmail(email: String):Int

    @Query("update UserInfo set account = account + :account where email = :email") // 포인트 획득했을 때 ( 값 증가만 가능 )
    fun AddAccountByEmail(email: String,account: Int)

    @Query("update UserInfo set account = account - :account where email = :email") // 포인트 사용했을 때 ( 결제, 포인트 차감 )
    fun UseAccountByEmail(email: String,account: Int)

    @Query("delete from UserInfo")
    fun deleteAllUser()
}

@Database(entities = arrayOf(UserInfo::class, block_tbl::class, mining_tbl::class), version = 1)
abstract class Blockdb: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun blockDao() : BlockDao
    abstract fun MiningDao() : MiningDao

    companion object {
        private var instance: Blockdb? = null

        @Synchronized
        fun getInstance(context: Context): Blockdb? {
            if (instance == null) {
                synchronized(Blockdb::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        Blockdb::class.java,
                        "user-database"
                    ).build()
                }
            }
            return instance
        }
    }
}