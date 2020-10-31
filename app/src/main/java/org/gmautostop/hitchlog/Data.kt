package org.gmautostop.hitchlog

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

interface HasId {
    var id : String
}

inline fun <reified T : HasId> DocumentSnapshot.toObjectWithId(): T {
    return this.toObject(T::class.java)!!.also {
        it.id = this.id
    }
}

inline fun <reified T : HasId> QuerySnapshot.toObjectsWithId(): List<T> {
    return this.documents.map {
        it.toObjectWithId<T>()
    }
}

data class Hitchlog(
    @get:Exclude
    override var id: String = "",
    val userId: String = "",
    val raceId: String = "",
    val teamId: String? = null,
    @Exclude
    val records: List<HitchLogRecord> = arrayListOf()
) : HasId

open class HitchLogRecord(
    @get:Exclude
    override var id: String = "",
    val time: Date = Date(),
    val type: HitchLogRecordType = HitchLogRecordType.FREE_TEXT,
    val text: String = ""
) : HasId

enum class HitchLogRecordType(val text: Int) {
    START(R.string.start),
    FREE_TEXT(R.string.free_text),

}

class GeoPointRecord(id: String, val point: GeoPoint = GeoPoint(0.0, 0.0), time: Date, type: HitchLogRecordType, text: String)
    : HitchLogRecord(id, time, type, text)

