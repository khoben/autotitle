package com.khoben.autotitle.service.frameretriever

import android.content.Context
import android.net.Uri

object MetadataProviderFactory {
    fun get(providerType: ProviderType, context: Context, uri: Uri): VideoMetaDataProvider {
        return when (providerType) {
            ProviderType.NATIVE_ANDROID -> AndroidNativeMetadataProvider(context, uri)
            ProviderType.MEDIA_CODEC -> AndroidMediaCodecMetadataProvider(context, uri)
        }
    }
}