package com.example.todo.repo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

private const val dbName = "todo.db"

@Database(entities = [ToDoEntity::class], version = 1)
@TypeConverters(TypeTransformer::class)
abstract class ToDoDatabase: RoomDatabase() {
    abstract fun todoStore(): ToDoEntity.Store

    companion object {
        fun newInstance(context: Context) =
            Room.databaseBuilder(context, ToDoDatabase::class.java, dbName).build()

        fun newTestInstance(context: Context) =
            Room.inMemoryDatabaseBuilder(context, ToDoDatabase::class.java).build()
    }
}