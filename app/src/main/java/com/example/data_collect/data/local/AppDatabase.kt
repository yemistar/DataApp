package com.example.data_collect.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppMetaEntity::class,
        UserEntity::class,
        FlockEntity::class,
        FeedLogEntity::class,
        MortalityLogEntity::class,
        EggLogEntity::class,
        TreatmentLogEntity::class,
        EnvLogEntity::class,
        PendingItemEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
