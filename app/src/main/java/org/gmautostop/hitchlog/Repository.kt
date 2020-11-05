package org.gmautostop.hitchlog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

class FirestoreRepository @Inject constructor(){
    init {
        FirebaseFirestore.setLoggingEnabled(true)
    }

    private var firestoreDB = FirebaseFirestore.getInstance()

    private fun logRecords(logId: String) : CollectionReference = firestoreDB.collection("logs/$logId/records")

    var currentLog: Hitchlog? = null
        set(value) {
            field = value
            currentLogId = value?.id
        }

    var currentLogId: String? = "log" //todo remove
        set(value) {
            field = value
            value?.let {
                logRecords(it)
                    .orderBy("time")
                    .addSnapshotListener(EventListener<QuerySnapshot> { value, exception ->
                        exception?.let {
                            Log.e("HitchLogViewModel", it.message.orEmpty())
                            recordsLiveData.value = null
                            return@EventListener
                        }

                        recordsLiveData.value = value?.map { item ->
                            item.toObjectWithId<HitchLogRecord>()
                        }

                    })
            }
        }

    val recordsLiveData = MutableLiveData<List<HitchLogRecord>>()

    fun addRecord(record: HitchLogRecord) {
        currentLogId?.let {
            logRecords(it).add(record)
        } ?: Log.e("HitchLogViewModel", "No current log")
    }

    fun updateRecord(record: HitchLogRecord) {
        currentLogId?.let {
            logRecords(it).document(record.id).set(record)
        } ?: Log.e("HitchLogViewModel", "No current log")
    }

    fun deleteRecord(record: HitchLogRecord) {
        currentLogId?.let {
            logRecords(it).document(record.id).delete()
        } ?: Log.e("HitchLogViewModel", "No current log")
    }

    fun saveRecord(record: HitchLogRecord) {
        when {
            record.id.isEmpty() -> addRecord(record)
            else -> updateRecord(record)
        }
    }



}