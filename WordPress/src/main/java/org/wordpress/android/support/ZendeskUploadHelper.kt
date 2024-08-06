package org.wordpress.android.support

import android.content.Context
import android.net.Uri
import com.zendesk.service.ZendeskCallback
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T
import org.wordpress.android.util.extensions.fileName
import org.wordpress.android.util.extensions.mimeType
import zendesk.support.Support
import zendesk.support.UploadResponse
import java.io.File
import javax.inject.Inject

/**
 * https://zendesk.github.io/mobile_sdk_javadocs/supportv2/v301/index.html?zendesk/support/UploadProvider.html
 */
class ZendeskUploadHelper @Inject constructor() {
    fun uploadAttachment(
        context: Context,
        uri: Uri,
        callback: ZendeskCallback<UploadResponse>,
    ) {
        val uploadProvider = Support.INSTANCE.provider()?.uploadProvider()
        if (uploadProvider == null) {
            AppLog.e(T.SUPPORT, "Upload provider is null")
            return
        }

        val file = uri.fileName(context)?.let { File(it) }
        if (file == null) {
            AppLog.e(T.SUPPORT, "Upload file is null")
            return
        }

        uploadProvider.uploadAttachment(
            file.name,
            file,
            uri.mimeType(context),
            callback
        )
    }
}
