package org.gmautostop.hitchlog

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

sealed class Response<out T> {
    class Loading<out T>: Response<T>()

    data class Success<out T>(
        val data: T
    ): Response<T>()

    data class Failure<out T>(
        val errorMessage: String
    ): Response<T>()
}

@Singleton
class FirestoreRepository @Inject constructor(){
    init {
        FirebaseFirestore.setLoggingEnabled(true)
        Firebase.auth.addAuthStateListener {
            user = it.currentUser
        }
    }

    private var firestoreDB = Firebase.firestore

    var user = Firebase.auth.currentUser

    private fun logs() : CollectionReference = firestoreDB.collection("logs")
    private fun logRecords(logId: String) : CollectionReference = firestoreDB.collection("logs/$logId/records")

    private fun <T> repositoryFlow(body:suspend () -> T): Flow<Response<T>> =
        flow {
            Log.d("Repository","repositoryFlow loading")
            emit(Response.Loading())
            val result = body().also {
                Log.d("Repository","repositoryFlow success $it")
            }
            emit(Response.Success(result))
        }.catch { error ->
            error.message?.let { errorMessage ->
                Log.e("Repository", errorMessage)
                emit(Response.Failure(errorMessage))
            }
        }.flowOn(Dispatchers.IO)

    private fun getUserId() =
        user?.uid ?: throw Exception("Not logged in")

    fun getUserLogs() = repositoryFlow {
        logs().whereEqualTo("userId", getUserId())
            .get().await().documents.map { document ->
                Log.d("Repository", "getUserLogs ${document.id} $document")
                document.toObjectWithId<HitchLog>()
            }.sortedByDescending { it.creationTime }
    }

    fun getLog(logId: String) = repositoryFlow {
        Log.d("Repository", "getLog $logId")
        with (logs().document(logId).get().await()) {
            when {
                !exists() -> throw Exception("Document $logId doesn't exist")
                else -> return@repositoryFlow (toObjectWithId<HitchLog>()).also {
                    Log.d("Repository", "log doc ${toObjectWithId<HitchLog>()}")
                }
            }
        }
    }

    fun addLog(log: HitchLog) = repositoryFlow {
        logs().add(log).await()
    }

    fun updateLog(log: HitchLog) = repositoryFlow {
        logs().document(log.id).set(log).await()
    }

    fun updateLogName(id: String, name: String) = repositoryFlow {
        logs().document(id).update("name", name).await()
    }

    fun saveLog(log: HitchLog) {
        when {
            log.id.isEmpty() -> addLog(log)
            else -> updateLog(log)
        }
    }

    fun deleteLog(id: String) = repositoryFlow {
        logs().document(id).delete().await()
    }

    fun getLogRecords(logId: String) = repositoryFlow {
        logRecords(logId).orderBy("time")
            .get().await().documents.map { document ->
                document.toObjectWithId<HitchLogRecord>()
            }
    }

    fun getRecord(logId: String, recordId: String) = repositoryFlow {
        with(logRecords(logId).document(recordId).get().await()) {
            when {
                !exists() -> throw Exception("Document $recordId doesn't exist")
                else -> return@repositoryFlow (toObjectWithId<HitchLogRecord>()).also {
                    Log.d("Repository", "log record ${toObjectWithId<HitchLogRecord>()}")
                }
            }
        }
    }

    fun addRecord(logId: String, record: HitchLogRecord) = repositoryFlow {
        logRecords(logId).add(record.copy(time = getNextTime(logId, record.time))).await()
    }

    fun updateRecord(logId: String, record: HitchLogRecord) = repositoryFlow {
        val existing = logRecords(logId).document(record.id).get().await().toObjectWithId<HitchLogRecord>()

        val updatedRecord = if (existing.time == record.time) {
            record
        } else {
            record.copy(time = getNextTime(logId, record.time))
        }

        logRecords(logId).document(record.id).set(updatedRecord).await()
    }

    fun deleteRecord(logId: String, record: HitchLogRecord) = repositoryFlow {
        logRecords(logId).document(record.id).delete().await()
    }

    fun saveRecord(logId: String, record: HitchLogRecord) =
        when {
            record.id.isEmpty() -> addRecord(logId, record)
            else -> updateRecord(logId, record)
        }

    private suspend fun getNextTime(logId: String, enteredTime: Date): Date {
        return logRecords(logId)
            .whereGreaterThanOrEqualTo("time", enteredTime.time)
            .whereLessThan("time", enteredTime.addMinute().time)
            .get(Source.CACHE)
            .await()
            .toObjectsWithId<HitchLogRecord>()
            .maxByOrNull { it.time }?.time?.addSecond() ?: enteredTime
    }

    private fun Date.addSecond(): Date = Date(time + 1000)
    private fun Date.addMinute(): Date = Date(time + 60000)
}

@SuppressLint("SimpleDateFormat")
fun Date.toMinutes(): Date {
    val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
    return dateTimeFormat.parse(dateTimeFormat.format(this))!!
}