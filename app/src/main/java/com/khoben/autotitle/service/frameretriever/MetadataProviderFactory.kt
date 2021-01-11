package com.khoben.autotitle.service.frameretriever

import android.content.Context
import android.net.Uri

object MetadataProviderFactory {
    fun get(providerType: @ProviderType Int, context: Context, uri: Uri): VideoMetaDataProvider {
        return when (providerType) {
            NATIVE_ANDROID -> AndroidNativeMetadataProvider(context, uri)
            MEDIA_CODEC -> AndroidMediaCodecMetadataProvider(context, uri)
            else -> {
                throw RuntimeException("Error MetadataProvider type $providerType")
            }
        }
    }
}