package uz.gita.musicplayer.data.local.roomDB

/*
@Database(entities = [MusicEntity::class], version = 1, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        private lateinit var instance: MusicDatabase

        fun getDatabase(context: Context): MusicDatabase {
            if (Companion::instance.isInitialized) return instance
            instance = Room.databaseBuilder(context, MusicDatabase::class.java, context.packageName)
                .fallbackToDestructiveMigration()
                .build()
            return instance
        }
    }

}*/
