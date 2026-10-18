package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CashLedgerEntry
import com.example.data.model.LedgerCategoryAggregate
import com.example.data.model.PaperAccount
import com.example.data.model.PaperOrder
import com.example.data.model.PaperPosition
import com.example.data.model.TradingJournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface PaperTradingDao {

  @Query("SELECT * FROM paper_account WHERE id = 1 LIMIT 1")
  fun getAccountFlow(): Flow<PaperAccount?>

  @Query("SELECT * FROM paper_account WHERE id = 1 LIMIT 1")
  suspend fun getAccount(): PaperAccount?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateAccount(account: PaperAccount)

  // Positions
  @Query("SELECT * FROM paper_positions WHERE quantity > 0")
  fun getAllPositions(): Flow<List<PaperPosition>>

  @Query("SELECT * FROM paper_positions WHERE symbol = :symbol LIMIT 1")
  suspend fun getPositionBySymbol(symbol: String): PaperPosition?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun savePosition(position: PaperPosition)

  @Query("DELETE FROM paper_positions WHERE symbol = :symbol")
  suspend fun deletePosition(symbol: String)

  @Query("DELETE FROM paper_positions")
  suspend fun clearAllPositions()

  // Orders
  @Query("SELECT * FROM paper_orders ORDER BY timestamp DESC")
  fun getAllOrders(): Flow<List<PaperOrder>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: PaperOrder)

  @Update
  suspend fun updateOrder(order: PaperOrder)

  @Query("DELETE FROM paper_orders")
  suspend fun clearAllOrders()

  // =========================================================================
  // Transaction History Retrieval Queries
  // =========================================================================

  /** Retrieve full chronological transaction history */
  @Query("SELECT * FROM cash_ledger_entries ORDER BY timestamp DESC")
  fun getAllLedgerEntries(): Flow<List<CashLedgerEntry>>

  /** Retrieve transactions filtered by transaction type ("CREDIT" or "DEBIT") */
  @Query("SELECT * FROM cash_ledger_entries WHERE type = :type ORDER BY timestamp DESC")
  fun getLedgerEntriesByType(type: String): Flow<List<CashLedgerEntry>>

  /** Retrieve transactions filtered by category (e.g. STOCK_BUY_DEBIT, STOCK_SELL_CREDIT, BROKERAGE_DEBIT) */
  @Query("SELECT * FROM cash_ledger_entries WHERE category = :category ORDER BY timestamp DESC")
  fun getLedgerEntriesByCategory(category: String): Flow<List<CashLedgerEntry>>

  /** Retrieve transactions within a specific timestamp date range */
  @Query("SELECT * FROM cash_ledger_entries WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
  fun getLedgerEntriesByDateRange(startTime: Long, endTime: Long): Flow<List<CashLedgerEntry>>

  /** Retrieve all audit debit/credit entries linked to a specific paper order ID */
  @Query("SELECT * FROM cash_ledger_entries WHERE linkedOrderId = :orderId ORDER BY timestamp ASC")
  fun getLedgerEntriesForOrder(orderId: String): Flow<List<CashLedgerEntry>>

  /** Retrieve recent N transactions with limit */
  @Query("SELECT * FROM cash_ledger_entries ORDER BY timestamp DESC LIMIT :limit")
  fun getRecentLedgerEntries(limit: Int): Flow<List<CashLedgerEntry>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLedgerEntry(entry: CashLedgerEntry)

  @Query("DELETE FROM cash_ledger_entries")
  suspend fun clearLedger()

  // =========================================================================
  // Balance Aggregation Queries
  // =========================================================================

  /** Total Credits (cash inflows: deposits, stock sale proceeds) */
  @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_ledger_entries WHERE type = 'CREDIT'")
  fun getTotalCreditsFlow(): Flow<Double>

  @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_ledger_entries WHERE type = 'CREDIT'")
  suspend fun getTotalCredits(): Double

  /** Total Debits (cash outflows: stock purchases, brokerage/taxes) */
  @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_ledger_entries WHERE type = 'DEBIT'")
  fun getTotalDebitsFlow(): Flow<Double>

  @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_ledger_entries WHERE type = 'DEBIT'")
  suspend fun getTotalDebits(): Double

  /** Net Cash Balance derived strictly from Double-Entry ledger aggregation: (Credits - Debits) */
  @Query("SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0.0) FROM cash_ledger_entries")
  fun getNetAggregatedCashBalanceFlow(): Flow<Double>

  @Query("SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0.0) FROM cash_ledger_entries")
  suspend fun getNetAggregatedCashBalance(): Double

  /** Total Brokerage & Exchange Turnover Fees aggregated */
  @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_ledger_entries WHERE category = 'BROKERAGE_DEBIT'")
  fun getTotalBrokeragePaidFlow(): Flow<Double>

  @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_ledger_entries WHERE category = 'BROKERAGE_DEBIT'")
  suspend fun getTotalBrokeragePaid(): Double

  /** Latest recorded running balance from most recent transaction entry */
  @Query("SELECT runningBalance FROM cash_ledger_entries ORDER BY timestamp DESC LIMIT 1")
  fun getLatestRunningBalanceFlow(): Flow<Double?>

  @Query("SELECT runningBalance FROM cash_ledger_entries ORDER BY timestamp DESC LIMIT 1")
  suspend fun getLatestRunningBalance(): Double?

  /** Total count of recorded ledger audit transactions */
  @Query("SELECT COUNT(*) FROM cash_ledger_entries")
  fun getTransactionCountFlow(): Flow<Int>

  @Query("SELECT COUNT(*) FROM cash_ledger_entries")
  suspend fun getTransactionCount(): Int

  /** Category breakdown aggregation (category name, total amount, entry count) */
  @Query("SELECT category, SUM(amount) AS totalAmount, COUNT(*) AS entryCount FROM cash_ledger_entries GROUP BY category")
  fun getCategoryAggregates(): Flow<List<LedgerCategoryAggregate>>

  // =========================================================================
  // Trading Journal
  // =========================================================================

  @Query("SELECT * FROM trading_journal ORDER BY timestamp DESC")
  fun getAllJournalEntries(): Flow<List<TradingJournalEntry>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJournalEntry(entry: TradingJournalEntry)

  @Query("DELETE FROM trading_journal")
  suspend fun clearJournal()
}
