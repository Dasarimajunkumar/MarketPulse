package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CorporateDisclosure
import com.example.data.model.MarketEvent
import com.example.data.model.MarketIndex
import com.example.data.model.NotificationItem
import com.example.data.model.RegulatoryCircular
import com.example.data.model.SourceHealth
import com.example.data.model.StockQuote
import com.example.data.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {

  // Market Events
  @Query("SELECT * FROM market_events ORDER BY timestamp DESC")
  fun getAllEvents(): Flow<List<MarketEvent>>

  @Query("SELECT * FROM market_events WHERE category = :category ORDER BY timestamp DESC")
  fun getEventsByCategory(category: String): Flow<List<MarketEvent>>

  @Query("SELECT * FROM market_events WHERE isBookmarked = 1 ORDER BY timestamp DESC")
  fun getBookmarkedEvents(): Flow<List<MarketEvent>>

  @Query("SELECT * FROM market_events WHERE id = :id LIMIT 1")
  suspend fun getEventById(id: String): MarketEvent?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEvents(events: List<MarketEvent>)

  @Update
  suspend fun updateEvent(event: MarketEvent)

  @Query("UPDATE market_events SET isBookmarked = :isBookmarked WHERE id = :id")
  suspend fun setBookmark(id: String, isBookmarked: Boolean)

  // Dedicated Corporate Disclosures
  @Query("SELECT * FROM corporate_disclosures ORDER BY filingDate DESC")
  fun getAllDisclosures(): Flow<List<CorporateDisclosure>>

  @Query("SELECT * FROM corporate_disclosures WHERE companySymbol = :symbol ORDER BY filingDate DESC")
  fun getDisclosuresForCompany(symbol: String): Flow<List<CorporateDisclosure>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDisclosures(disclosures: List<CorporateDisclosure>)

  @Query("DELETE FROM corporate_disclosures")
  suspend fun clearDisclosures()

  // Dedicated Regulatory Circulars
  @Query("SELECT * FROM regulatory_circulars ORDER BY publicationDate DESC")
  fun getAllCirculars(): Flow<List<RegulatoryCircular>>

  @Query("SELECT * FROM regulatory_circulars WHERE regulator = :regulator ORDER BY publicationDate DESC")
  fun getCircularsByRegulator(regulator: String): Flow<List<RegulatoryCircular>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCirculars(circulars: List<RegulatoryCircular>)

  @Query("DELETE FROM regulatory_circulars")
  suspend fun clearCirculars()

  // Source Health
  @Query("SELECT * FROM source_health ORDER BY sourceName ASC")
  fun getAllSourceHealth(): Flow<List<SourceHealth>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSourceHealth(sources: List<SourceHealth>)

  @Query("UPDATE source_health SET lastCheckTime = :checkTime, lastSuccessfulRefresh = :refreshTime, status = :status, errorMessage = :error WHERE sourceId = :sourceId")
  suspend fun updateSourceStatus(sourceId: String, checkTime: Long, refreshTime: Long, status: String, error: String?)

  // User Push Notifications
  @Query("SELECT * FROM user_notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<NotificationItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotifications(notifications: List<NotificationItem>)

  @Query("UPDATE user_notifications SET isRead = 1 WHERE id = :id")
  suspend fun markNotificationAsRead(id: String)

  @Query("UPDATE user_notifications SET isRead = 1")
  suspend fun markAllNotificationsAsRead()

  // Market Indices
  @Query("SELECT * FROM market_indices")
  fun getAllIndices(): Flow<List<MarketIndex>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIndices(indices: List<MarketIndex>)

  // Stock Quotes
  @Query("SELECT * FROM stock_quotes ORDER BY symbol ASC")
  fun getAllStockQuotes(): Flow<List<StockQuote>>

  @Query("SELECT * FROM stock_quotes WHERE symbol = :symbol LIMIT 1")
  suspend fun getQuoteBySymbol(symbol: String): StockQuote?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStockQuotes(quotes: List<StockQuote>)

  // Watchlist
  @Query("SELECT * FROM watchlist_items ORDER BY addedAt DESC")
  fun getWatchlist(): Flow<List<WatchlistItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addToWatchlist(item: WatchlistItem)

  @Delete
  suspend fun removeFromWatchlist(item: WatchlistItem)

  @Query("DELETE FROM watchlist_items WHERE symbol = :symbol")
  suspend fun removeFromWatchlistBySymbol(symbol: String)

  @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items WHERE symbol = :symbol)")
  suspend fun isInWatchlist(symbol: String): Boolean
}
