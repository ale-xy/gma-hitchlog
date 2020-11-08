package org.gmautostop.hitchlog

import android.os.Parcel
import android.os.Parcelable
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

data class HitchLog(
    @get:Exclude
    override var id: String = "",
    val userId: String = "",
    val raceId: String = "",
    val teamId: String? = null,
    val name: String? = null,
    @Exclude
    val records: List<HitchLogRecord> = arrayListOf()
) : HasId

open class HitchLogRecord(
    @get:Exclude
    override var id: String = "",
    val time: Date = Date(),
    val type: HitchLogRecordType = HitchLogRecordType.FREE_TEXT,
    val text: String = ""
) : HasId, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        Date(parcel.readLong()),
        HitchLogRecordType.valueOf(parcel.readString() ?: HitchLogRecordType.FREE_TEXT.name),
        parcel.readString().orEmpty()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeLong(time.time)
        parcel.writeString(type.name)
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HitchLogRecord> {
        override fun createFromParcel(parcel: Parcel): HitchLogRecord {
            return HitchLogRecord(parcel)
        }

        override fun newArray(size: Int): Array<HitchLogRecord?> {
            return arrayOfNulls(size)
        }
    }
}

enum class HitchLogRecordType(val text: Int) {
    START(R.string.start),
    LIFT(R.string.lift),
    GET_OFF(R.string.get_off),
    WALK(R.string.walk),
    WALK_END(R.string.walk_end),
    CHECKPOINT(R.string.checkpoint),
    MEET(R.string.meet),
    REST_ON(R.string.rest_on),
    REST_OFF(R.string.rest_off),
    OFFSIDE_ON(R.string.offside_on),
    OFFSIDE_OFF(R.string.offside_off),
    FINISH(R.string.finish),
    RETIRE(R.string.retire),
    FREE_TEXT(R.string.free_text),

}

class GeoPointRecord(id: String, val point: GeoPoint = GeoPoint(0.0, 0.0), time: Date, type: HitchLogRecordType, text: String)
    : HitchLogRecord(id, time, type, text)

