package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CashLedgerEntry
import com.example.data.model.CorporateDisclosure
import com.example.data.model.MarketEvent
import com.example.data.model.MarketIndex
import com.example.data.model.NotificationItem
import com.example.data.model.PaperAccount
import com.example.data.model.PaperOrder
import com.example.data.model.PaperPosition
import com.example.data.model.RegulatoryCircular
import com.example.data.model.SourceHealth
import com.example.data.model.StockQuote
import com.example.data.model.TradingJournalEntry
import com.example.data.model.WatchlistItem

@Database(
  entities = [
    MarketEvent::class,
    CorporateDisclosure::class,
    RegulatoryCircular::class,
    SourceHealth::class,
    NotificationItem::class,
    MarketIndex::class,
    StockQuote::class,
    PaperAccount::class,
    PaperOrder::class,
    PaperPosition::class,
    CashLedgerEntry::class,
    WatchlistItem::class,
    TradingJournalEntry::class
  ],
  version = 6,
  exportSchema = false
)
abstract class MarketPulseDatabase : RoomDatabase() {

  abstract fun marketDao(): MarketDao
  abstract fun paperTradingDao(): PaperTradingDao

  companion object {
    @Volatile
    private var INSTANCE: MarketPulseDatabase? = null

    val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE market_indices SET tradingStatus = 'STALE', provider = 'DEMO_FIXTURE' WHERE tradingStatus = 'LIVE' AND provider = 'NSE_OFFICIAL'")
      }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE market_indices ADD COLUMN marketSessionStatus TEXT NOT NULL DEFAULT 'CLOSED'")
        db.execSQL("ALTER TABLE market_indices ADD COLUMN dataFreshnessStatus TEXT NOT NULL DEFAULT 'LAST_CLOSE'")
        db.execSQL("ALTER TABLE stock_quotes ADD COLUMN marketSessionStatus TEXT NOT NULL DEFAULT 'CLOSED'")
        db.execSQL("ALTER TABLE stock_quotes ADD COLUMN dataFreshnessStatus TEXT NOT NULL DEFAULT 'LAST_CLOSE'")
      }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE source_health ADD COLUMN publisherName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE source_health ADD COLUMN officialTermsUrl TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE source_health ADD COLUMN integrationMethod TEXT NOT NULL DEFAULT 'OFFICIAL_RSS'")
        db.execSQL("ALTER TABLE source_health ADD COLUMN rightsStatus TEXT NOT NULL DEFAULT 'PERMISSION_CONFIRMED'")
        db.execSQL("ALTER TABLE source_health ADD COLUMN permittedContentFields TEXT NOT NULL DEFAULT 'title,summary,link'")
        db.execSQL("ALTER TABLE source_health ADD COLUMN attributionRequirements TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE source_health ADD COLUMN cachingPermissions INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE source_health ADD COLUMN redistributionPermissions INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE source_health ADD COLUMN lastRightsReviewedAt INTEGER NOT NULL DEFAULT 0")
      }
    }

    fun getDatabase(context: Context): MarketPulseDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MarketPulseDatabase::class.java,
          "marketpulse_db"
        )
          .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
