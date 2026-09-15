package com.example.data

import com.example.data.CommandDao
import com.example.data.CommandHistory
import kotlinx.coroutines.flow.Flow

class HistoryManager(private val commandDao: CommandDao) {
    fun getAllHistory(): Flow<List<CommandHistory>> = commandDao.getAllHistory()
    
    suspend fun addHistory(command: String, response: String, action: String) {
        commandDao.insertHistory(
            CommandHistory(
                userCommand = command,
                jarvisResponse = response,
                actionType = action
            )
        )
    }
    
    suspend fun clearHistory() {
        commandDao.clearHistory()
    }
}
