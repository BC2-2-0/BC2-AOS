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
//{
//    @PrimaryKey(autoGenerate = true) val id: Int = 0
//}

@Dao
interface UserDao {
    @Insert
    fun insertUser(userInfo: UserInfo) //

    @Query("select * from UserInfo")
    fun getAllUser(): List<UserInfo>


    @Query("select account from UserInfo where email = :email")
    fun getAccountByEmail(email: String):Int

    @Query("update UserInfo set account = account + :account where email = :email") // 포인트 획득했을 때 ( 값 증가만 가능 )
    fun AddAccountByEmail(email: String,account: Int)
//
//    @Query("update UserInfo set account = account - :account where email = :email")
//    fun UseAccountByEmail(email: String, account: Int)
}

@Database(entities = [UserInfo::class], version = 1)
abstract class Blockdb: RoomDatabase() {
    abstract fun userDao(): UserDao

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