package com.waxd.pos.fcmb.hilt

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.waxd.pos.fcmb.BuildConfig
import com.waxd.pos.fcmb.app.FcmbApp
import com.waxd.pos.fcmb.base.SingletonWrapper
import com.waxd.pos.fcmb.datastore.DataStoreWrapper
import com.waxd.pos.fcmb.datastore.KeyStore
import com.waxd.pos.fcmb.datastore.KeyStore.decryptData
import com.waxd.pos.fcmb.rest.ApiHelper
import com.waxd.pos.fcmb.rest.RestRepository
import com.waxd.pos.fcmb.utils.Util.isInternetAvailable
import com.waxd.pos.fcmb.utils.Util.showToast
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Provides
    @Singleton
    fun provideAppContext(): Context {
        return FcmbApp.instance.applicationContext
    }

    @Provides
    @Singleton
    fun provideDataStoreWrapper(@ApplicationContext context: Context): DataStoreWrapper {
        return DataStoreWrapper(context)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Provides
    @Singleton
    fun networkConnectionInterceptor(context: Context): NetworkConnectionInterceptor {
        return NetworkConnectionInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideSingletonWrapper(): SingletonWrapper {
        return SingletonWrapper()
    }

    @Provides
    @Singleton
    fun provideFirebaseWrapper(context: Context): FirebaseWrapper {
        return FirebaseWrapper(context)
    }

    private var appInterceptor: Interceptor? = null

    @Provides
    @Singleton
    fun provideHttpClient(
        context: Context,
        logging: HttpLoggingInterceptor,
        networkCheckIntercept: NetworkConnectionInterceptor,
        preferencesHelper: DataStoreWrapper
    ): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        if (appInterceptor == null)
            appInterceptor = RadarIntercept(context, preferencesHelper)
        httpClient.addInterceptor(appInterceptor!!)
        httpClient.addInterceptor(networkCheckIntercept)
        httpClient.addInterceptor(logging)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
        return httpClient.build()
    }

    @Provides
    @Singleton
    fun provideApiProvider(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiHelper {
        return retrofit.create(ApiHelper::class.java)
    }

    @Provides
    @Singleton
    fun provideRestRepository(
        apiHelper: ApiHelper,
        context: Context
    ): RestRepository {
        return RestRepository(apiHelper, context)
    }

    class RadarIntercept(val context: Context, private val preferencesHelper: DataStoreWrapper) :
        Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val token = context.decryptData(KeyStore.USER_TOKEN)
            var request = chain.request()
            val headers = request.headers.newBuilder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add(
                    "Authorization",
                    "Bearer $token"
                )
                .build()

            request = request.newBuilder().headers(headers).build()
            return chain.proceed(request)
        }
    }


    class NetworkConnectionInterceptor(val context: Context) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            if (!context.isInternetAvailable()) {
                Handler(Looper.getMainLooper()).post {
                    context.showToast(
                        Constants.CONNECTION_ERROR
                    )
                }

                throw NoConnectivityException()
            }
            return chain.proceed(chain.request())
        }

        class NoConnectivityException : IOException() {
            override val message: String
                get() = Constants.CONNECTION_ERROR
        }
    }

}