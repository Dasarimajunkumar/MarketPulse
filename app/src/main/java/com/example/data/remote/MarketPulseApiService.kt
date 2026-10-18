package com.example.data.remote

import com.example.data.model.MarketEvent
import com.example.data.model.MarketIndex
import com.example.data.model.NotificationItem
import com.example.data.model.SourceHealth
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class NewsApiResponse(
  @field:Json(name = "status") val status: String,
  @field:Json(name = "total") val total: Int,
  @field:Json(name = "lastIngestedTime") val lastIngestedTime: Long?,
  @field:Json(name = "data") val data: List<MarketEvent>
)

data class SourcesApiResponse(
  @field:Json(name = "status") val status: String,
  @field:Json(name = "sources") val sources: List<SourceHealth>
)

data class IndicesApiResponse(
  @field:Json(name = "status") val status: String,
  @field:Json(name = "indices") val indices: List<MarketIndex>
)

data class AlertsApiResponse(
  @field:Json(name = "status") val status: String,
  @field:Json(name = "alerts") val alerts: List<NotificationItem>
)

data class ChatRequest(
  @field:Json(name = "prompt") val prompt: String
)

data class ChatApiResponse(
  @field:Json(name = "status") val status: String,
  @field:Json(name = "text") val text: String,
  @field:Json(name = "citedSources") val citedSources: List<String> = emptyList(),
  @field:Json(name = "citedUrls") val citedUrls: List<String> = emptyList(),
  @field:Json(name = "verificationType") val verificationType: String? = null,
  @field:Json(name = "isGrounded") val isGrounded: Boolean = true
)

interface MarketPulseApiService {

  @GET("api/v1/news")
  suspend fun getNews(
    @Query("category") category: String? = null,
    @Query("source") source: String? = null,
    @Query("entity") entity: String? = null,
    @Query("query") query: String? = null
  ): Response<NewsApiResponse>

  @GET("api/v1/news/{eventId}")
  suspend fun getNewsDetail(@Path("eventId") eventId: String): Response<MarketEvent>

  @GET("api/v1/sources")
  suspend fun getSourcesHealth(): Response<SourcesApiResponse>

  @GET("api/v1/market/indices")
  suspend fun getMarketIndices(): Response<IndicesApiResponse>

  @GET("api/v1/alerts")
  suspend fun getAlerts(): Response<AlertsApiResponse>

  @POST("api/v1/chat")
  suspend fun sendChatMessage(@Body request: ChatRequest): Response<ChatApiResponse>
}
