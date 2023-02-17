package io.horizontalsystems.marketkit.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    indices = [
        Index(value = arrayOf("uid"))
    ]
)
data class Coin(
    @PrimaryKey
    var uid: String,
    var name: String,
    var code: String,
    var marketCapRank: Int? = null,
    var coinGeckoId: String? = null,
    var coinType: CoinType = CoinType.CRYPTO
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        return other is Coin && other.uid == uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }

    override fun toString(): String {
        return "Coin [uid: $uid; name: $name; code: $code; marketCapRank: $marketCapRank; coinGeckoId: $coinGeckoId]"
    }
}
