val client = OkHttpClient()

val request = Request.Builder()
  .url("https://api-pub.bitfinex.com/v2/platform/status")
  .get()
  .addHeader("accept", "application/json")
  .build()

val response = client.newCall(request).execute()
