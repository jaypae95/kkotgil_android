package kr.khs.kkotgil.network

import android.util.Log
import android.webkit.JavascriptInterface
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://ec2-54-180-141-120.ap-northeast-2.compute.amazonaws.com:3000/"

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
private val clientBuilder = OkHttpClient.Builder().apply {
    addInterceptor(loggingInterceptor)
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

interface NHApiService {
    @Headers("Content-Type: application/json")
    @PUT("/users/{id}/token")
    suspend fun updateToken(@Path("id") id : Int, @Body token : TokenDTO) : Response
}

object NHApi {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi)) // json to kotlin
        .baseUrl(BASE_URL)
        .client(clientBuilder.build())
        .build()

    val retrofitService : NHApiService by lazy {
        retrofit.create(NHApiService::class.java)
    }
}

object RegisterToken {
    var id = 1 // for test, default value is 0
    var token : String? = null
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    fun registerToken() {
        Log.d("Firebase", "id : $id ${token ?: "null"}")
        if(token != null && id != 0) {
            coroutineScope.launch {
                try {
                    val result = NHApi.retrofitService.updateToken(id, TokenDTO(token!!))
                    println(result.toString())
                }
                catch (e : Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun registerToken(_token : String?) {
        _token?.let {
            token = it
            registerToken()
        }
    }
}

class JSparser {
    @JavascriptInterface
    fun getUserId(id : Int) {
        Log.d("WEB", "getUserId()")
        RegisterToken.id = id
        RegisterToken.registerToken()
    }
}