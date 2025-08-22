package com.waxd.pos.fcmb.hilt

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.waxd.pos.fcmb.utils.s3.AWSKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AwsModule {

    @Provides @Singleton
    fun provideCredentials(@ApplicationContext ctx: Context): CognitoCachingCredentialsProvider =
        CognitoCachingCredentialsProvider(ctx, AWSKeys.COGNITO_POOL_ID, AWSKeys.MY_REGION)

    @Provides @Singleton
    fun provideS3Client(provider: CognitoCachingCredentialsProvider): AmazonS3Client =
        AmazonS3Client(provider, Region.getRegion(AWSKeys.MY_REGION))

    @Provides
    @Singleton
    fun provideTransferUtility(@ApplicationContext ctx: Context, s3: AmazonS3Client): TransferUtility =
        TransferUtility.builder().context(ctx).s3Client(s3).build()
}