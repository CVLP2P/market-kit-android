package io.horizontalsystems.marketkit.syncers

import android.util.Log
import io.horizontalsystems.marketkit.SyncInfo
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.HsProvider
import io.horizontalsystems.marketkit.storage.CoinStorage
import io.horizontalsystems.marketkit.storage.SyncerStateDao
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class CoinSyncer(
    private val hsProvider: HsProvider,
    private val storage: CoinStorage,
    private val syncerStateDao: SyncerStateDao
) {
    private val keyCoinsLastSyncTimestamp = "coin-syncer-coins-last-sync-timestamp"
    private val keyBlockchainsLastSyncTimestamp = "coin-syncer-blockchains-last-sync-timestamp"
    private val keyTokensLastSyncTimestamp = "coin-syncer-tokens-last-sync-timestamp"

    private var disposable: Disposable? = null

    val fullCoinsUpdatedObservable = PublishSubject.create<Unit>()

    fun sync(coinsTimestamp: Int, blockchainsTimestamp: Int, tokensTimestamp: Int) {
        val lastCoinsSyncTimestamp = syncerStateDao.get(keyCoinsLastSyncTimestamp)?.toInt() ?: 0
        val coinsOutdated = lastCoinsSyncTimestamp != coinsTimestamp

        val lastBlockchainsSyncTimestamp =
            syncerStateDao.get(keyBlockchainsLastSyncTimestamp)?.toInt() ?: 0
        val blockchainsOutdated = lastBlockchainsSyncTimestamp != blockchainsTimestamp

        val lastTokensSyncTimestamp = syncerStateDao.get(keyTokensLastSyncTimestamp)?.toInt() ?: 0
        val tokensOutdated = lastTokensSyncTimestamp != tokensTimestamp

        if (!coinsOutdated && !blockchainsOutdated && !tokensOutdated) return

        disposable = Single.zip(
            hsProvider.allCoinsSingle().map { it.map { coinResponse -> coinEntity(coinResponse) } },
            hsProvider.allBlockchainsSingle()
                .map { it.map { blockchainResponse -> blockchainEntity(blockchainResponse) } },
            hsProvider.allTokensSingle()
                .map { it.map { tokenResponse -> tokenEntity(tokenResponse) } }
        ) { r1, r2, r3 -> Triple(r1, r2, r3) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ coinsData ->
                handleFetched(coinsData.first, coinsData.second, coinsData.third)
                saveLastSyncTimestamps(coinsTimestamp, blockchainsTimestamp, tokensTimestamp)
            }, {
                Log.e("CoinSyncer", "sync() error", it)
            })
    }

    private fun coinEntity(response: CoinResponse): Coin =
        Coin(
            response.uid,
            response.name,
            response.code.uppercase(),
            response.market_cap_rank,
            response.coingecko_id
        )

    private fun blockchainEntity(response: BlockchainResponse): BlockchainEntity =
        BlockchainEntity(response.uid, response.name, response.url)

    private fun tokenEntity(response: TokenResponse): TokenEntity =
        TokenEntity(
            response.coin_uid,
            response.blockchain_uid,
            response.type,
            response.decimals,

            when (response.type) {
                "eip20" -> response.address
                "bep2" -> response.symbol
                "spl" -> response.address
                else -> response.address
            }
        )

    fun stop() {
        disposable?.dispose()
        disposable = null
    }

    private fun handleFetched(
        coins: List<Coin>,
        blockchainEntities: List<BlockchainEntity>,
        tokenEntities: List<TokenEntity>
    ) {
        // add all coins & tokens, which u need
        val ourCoinList: List<Coin> = listOf(
            Coin(
                uid = "cvl-civilization",
                name = "Civilization",
                code = "CVL",
                -1,
                coinGeckoId = "cvl-civilization",
                coinType = CoinType.CRYPTO
            ),
            Coin(
                uid = "usdw",
                name = "US Dollar",
                code = "USDW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),
            Coin(
                uid = "eurw",
                name = "EURO",
                code = "EURW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ), Coin(
                uid = "cnyw",
                name = "Chinese yuan",
                code = "CNYW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ), Coin(
                uid = "rubw",
                name = "Russian ruble",
                code = "RUBW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ), Coin(
                uid = "uahw",
                name = "Ukrainian hryvnia",
                code = "UAHW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),
            Coin(
                uid = "gbpw",
                name = "British Pound",
                code = "GBPW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),Coin(
                uid = "kztw",
                name = "Kazakhstan tenge",
                code = "KZTW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),Coin(
                uid = "brlw",
                name = "Brazilian Real",
                code = "BRLW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),Coin(
                uid = "tryw",
                name = "Turkish Lira",
                code = "TRYW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),
            Coin(
                uid = "mxnw",
                name = "Mexican Peso",
                code = "MXNW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),Coin(
                uid = "idrw",
                name = "Indonesian Rupiah",
                code = "IDRW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),Coin(
                uid = "jpyw",
                name = "Japanese yen",
                code = "JPYW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),Coin(
                uid = "cadw",
                name = "Canadian dollar",
                code = "CADW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),
            Coin(
                uid = "audw",
                name = "Australian dollar",
                code = "AUDW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),Coin(
                uid = "chfw",
                name = "Swiss frank",
                code = "CHFW",
                -1,
                null,
                coinType = CoinType.CURRENCY
            ),Coin(
                uid = "wcoin",
                name = "V",
                code = "wCOIN",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "whood",
                name = "Robinhood",
                code = "wHOOD",
                -1,
                null,
                coinType = CoinType.SHARE
            ),
            Coin(
                uid = "wbaba",
                name = "Alibaba",
                code = "wBABA",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "waapl",
                name = "Apple",
                code = "wAAPL",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wtsla",
                name = "Tesla",
                code = "wTSLA",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wgoogl",
                name = "Alphabet",
                code = "wGOOGL",
                -1,
                null,
                coinType = CoinType.SHARE
            ),
            Coin(
                uid = "wamzn",
                name = "Amazon",
                code = "wAMZN",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wnflx",
                name = "Netflix",
                code = "wNFLX",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wpypl",
                name = "Paypal",
                code = "wPYPL",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wtwtr",
                name = "Twitter",
                code = "wTWTR",
                -1,
                null,
                coinType = CoinType.SHARE
            ),
            Coin(
                uid = "wsnap",
                name = "Snapchat",
                code = "wSNAP",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wspot",
                name = "Spotify",
                code = "wSPOT",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wgtlb",
                name = "Gitlab",
                code = "wGTLB",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wrivn",
                name = "Rivian Automotive",
                code = "wRIVN",
                -1,
                null,
                coinType = CoinType.SHARE
            ),
            Coin(
                uid = "wf",
                name = "Ford Motor Company",
                code = "wF",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wbbva",
                name = "BBVA",
                code = "wBBVA",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wsony",
                name = "SONY",
                code = "wSONY",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wtm",
                name = "Toyota Motor",
                code = "wTM",
                -1,
                null,
                coinType = CoinType.SHARE
            ),
            Coin(
                uid = "wntd",
                name = "Nintendo",
                code = "wNTD",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wads",
                name = "Adidas",
                code = "wADS",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wbmw",
                name = "BMW",
                code = "wBMW",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wdb",
                name = "Deutsche Bank",
                code = "wDB",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wiau",
                name = "iShares Gold Trust",
                code = "wIAU",
                -1,
                null,
                coinType = CoinType.SHARE
            ),Coin(
                uid = "wslv",
                name = "iShares Silver Trust",
                code = "wSLV",
                -1,
                null,
                coinType = CoinType.SHARE
            ),
        )
        val ourTokenList: List<TokenEntity> = listOf(
            TokenEntity(
                coinUid = "cvl-civilization",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x9Ae0290cD677dc69A5f2a1E435EF002400Da70F5"
            ),
            TokenEntity(
                coinUid = "usdw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x9bd0232978486e7fd9b4ad2e7a7abeba6d492df3"
            ),TokenEntity(
                coinUid = "eurw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x9772046e4248245b1a67848a9df3a97d24478e74"
            ),TokenEntity(
                coinUid = "cnyw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x992e69f0986d64c6f385fccc7dd0b8572913efea"
            ),TokenEntity(
                coinUid = "rubw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x9c36101784c3e85508932f92389e183270b5a3f7"
            ),TokenEntity(
                coinUid = "uahw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x1eaee6c5e226bbc479fc39d3bdf082ce8749a43f"
            ),
            TokenEntity(
                coinUid = "gbpw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xbaccb3b340e26228142d154698a0bae98caf142c"
            ),TokenEntity(
                coinUid = "kztw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xfea75057902420e6e7d5d2e85a425dbeb589958e"
            ),TokenEntity(
                coinUid = "brlw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xf89a7df788cee2056cef464be1be21e177e1c5f7"
            ),TokenEntity(
                coinUid = "tryw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xf895f0d27ea0f378eb54bd6400843947a7be618f"
            ),
            TokenEntity(
                coinUid = "mxnw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x1c7f1c899bb6c059521f64c76fbeaf493a9c6c81"
            ),TokenEntity(
                coinUid = "idrw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x2bcee9deaec3ef96e0b6418af9d8ece8140a3085"
            ),TokenEntity(
                coinUid = "jpyw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x5971543f3accab6f2bc9fc6d0c3168d6677ba092"
            ),TokenEntity(
                coinUid = "cadw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x51e071ff72da69d8487d6c1730616b2780a48e90"
            ),
            TokenEntity(
                coinUid = "audw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xd953d374c1920cafafa19553030f71e681448809"
            ),TokenEntity(
                coinUid = "chfw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x1b419ebcc7075f03db332e1dd5b7c91b3093d4ed"
            ),TokenEntity(
                coinUid = "wcoin",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x144df7ac0d303802cbbc6431b1d3d9410a6f2e68"
            ),TokenEntity(
                coinUid = "whood",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xec01f4be35a9189f12a02fa9eae150752076b6bb"
            ),
            TokenEntity(
                coinUid = "wbaba",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x7b7bf8e667d5b9e8aea683553ab3d88630a1336d"
            ),TokenEntity(
                coinUid = "waapl",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x595b0b3ba0b43c947ed8b4417a4bfb90b638d50b"
            ),TokenEntity(
                coinUid = "wtsla",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xb3f066e4c0d9523c3e3b3fa0553d9a1972016c5d"
            ),TokenEntity(
                coinUid = "wgoogl",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xe5b6972da183fb47ae649e9b6ece4421e47093b0"
            ),
            TokenEntity(
                coinUid = "wamzn",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x8ce283e4658f2784800862aef6c4ca4f44ef2adf"
            ),TokenEntity(
                coinUid = "wnflx",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xfe8c5587be8bab8c90bfa48796dc59a7fd945bbc"
            ),TokenEntity(
                coinUid = "wpypl",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x14a877bceb0a5b712b509a8496da17bfefde237c"
            ),TokenEntity(
                coinUid = "wtwtr",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xde9f54b2a8b33312400ea5b550e12427190c84de"
            ),
            TokenEntity(
                coinUid = "wsnap",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x66b8cbca7ee29b3a6d592cf4a22e38e1f5b3b6b1"
            ),TokenEntity(
                coinUid = "wspot",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x4c3557c4b975df67ff8db1b9af5bb3b7f2a427b1"
            ),TokenEntity(
                coinUid = "wgtlb",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x4452a9389f8fec18889dc0dad5018bfc23cdfc2d"
            ),TokenEntity(
                coinUid = "wrivn",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xf4a3477c3ac41a7478250f2eda08a1ac28c4eac4"
            ),
            TokenEntity(
                coinUid = "wf",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xfbec6b0ed30d9b87dd7f1043340a171f3c2bd72d"
            ),TokenEntity(
                coinUid = "wbbva",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xfa909badbafb1632b4f65d11bfb66dcd24ba0c0b"
            ),TokenEntity(
                coinUid = "wsony",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x4ddac14bdee714e1cfefd3183669c80244a28576"
            ),TokenEntity(
                coinUid = "wtm",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xbba20092a178e8c4bafded21eb0e1efad7c63cc8"
            ),
            TokenEntity(
                coinUid = "wntd",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x5e4047e62a53111510fae06972f593857e8064c1"
            ),TokenEntity(
                coinUid = "wads",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xe1c16722dfc9ba988b04ce0ccda3b3e6f78e6132"
            ),TokenEntity(
                coinUid = "wbmw",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0x456702a2ee99662872568a36a8b2499d3b999076"
            ),TokenEntity(
                coinUid = "wdb",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xabc8483f4f3dddf37bdd5b0f08e83a8227b780ee"
            ),
            TokenEntity(
                coinUid = "wiau",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xf6bf16f819f83f675e8bf2f116cefa20ed4c8b23"
            ),TokenEntity(
                coinUid = "wslv",
                blockchainUid = "binance-smart-chain",
                type = "eip20",
                decimals = 18,
                reference = "0xfb928aaad76f6035d9282bb530e449c2b27c66d2"
            ),
        )

        storage.update(
            coins.plus(ourCoinList),
            blockchainEntities,
            tokenEntities.plus(ourTokenList)
        )
        fullCoinsUpdatedObservable.onNext(Unit)
    }

    private fun saveLastSyncTimestamps(coins: Int, blockchains: Int, tokens: Int) {
        syncerStateDao.save(keyCoinsLastSyncTimestamp, coins.toString())
        syncerStateDao.save(keyBlockchainsLastSyncTimestamp, blockchains.toString())
        syncerStateDao.save(keyTokensLastSyncTimestamp, tokens.toString())
    }

    fun syncInfo(): SyncInfo {
        return SyncInfo(
            coinsTimestamp = syncerStateDao.get(keyCoinsLastSyncTimestamp),
            blockchainsTimestamp = syncerStateDao.get(keyBlockchainsLastSyncTimestamp),
            tokensTimestamp = syncerStateDao.get(keyTokensLastSyncTimestamp)
        )
    }

}
