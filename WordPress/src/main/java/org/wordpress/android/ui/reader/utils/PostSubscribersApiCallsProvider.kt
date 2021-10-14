package org.wordpress.android.ui.reader.utils

import com.android.volley.VolleyError
import com.wordpress.rest.RestRequest.ErrorListener
import com.wordpress.rest.RestRequest.Listener
import org.json.JSONObject
import org.wordpress.android.R
import org.wordpress.android.WordPress
import org.wordpress.android.ui.reader.utils.PostSubscribersApiCallsProvider.PostSubscribersCallResult.Failure
import org.wordpress.android.ui.reader.utils.PostSubscribersApiCallsProvider.PostSubscribersCallResult.Success
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T
import org.wordpress.android.util.VolleyUtils
import org.wordpress.android.util.config.FollowByPushNotificationFeatureConfig
import org.wordpress.android.viewmodel.ContextProvider
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PostSubscribersApiCallsProvider @Inject constructor(
    private val contextProvider: ContextProvider,
    private val followByPushNotificationFeatureConfig: FollowByPushNotificationFeatureConfig
) {
    suspend fun getCanFollowComments(blogId: Long): Boolean = suspendCoroutine { cont ->
        val endPointPath = "/sites/$blogId/"

        val listener = Listener { jsonObject ->
            val result = canFollowComments(blogId, jsonObject)
            AppLog.d(
                    T.READER,
                    "getCanFollowComments > Succeeded [blogId=$blogId - result = $result]"
            )
            cont.resume(result is Success)
        }
        val errorListener = ErrorListener { volleyError ->
            AppLog.d(
                    T.READER,
                    "getCanFollowComments > Failed [blogId=$blogId - volleyError = $volleyError]"
            )
            cont.resume(false)
        }

        WordPress.getRestClientUtilsV1_1().get(
                endPointPath,
                listener,
                errorListener
        )
    }

    suspend fun getMySubscriptionToPost(
        blogId: Long,
        postId: Long
    ): PostSubscribersCallResult = suspendCoroutine { cont ->
        val endPointPath = "/sites/$blogId/posts/$postId/subscribers/mine"

        val listener = Listener { jsonObject ->
            val result = getFollowingStateResult(jsonObject)
            AppLog.d(
                    T.READER,
                    "getMySubscriptionToPost > Succeeded [blogId=$blogId - postId=$postId - result = $result]"
            )
            cont.resume(result)
        }
        val errorListener = ErrorListener { volleyError ->
            val error = getErrorStringAndLog("getMySubscriptionToPost", blogId, postId, volleyError)
            cont.resume(Failure(error))
        }

        WordPress.getRestClientUtilsV1_1().get(
                endPointPath,
                listener,
                errorListener
        )
    }

    suspend fun subscribeMeToPost(blogId: Long, postId: Long): PostSubscribersCallResult = suspendCoroutine { cont ->
        val endPointPath = "/sites/$blogId/posts/$postId/subscribers/new"

        val listener = Listener { jsonObject ->
            val result = wasSubscribed(jsonObject)
            AppLog.d(
                    T.READER,
                    "subscribeMeToPost > Succeeded [blogId=$blogId - postId=$postId - result = $result]"
            )
            cont.resume(result)
        }
        val errorListener = ErrorListener { volleyError ->
            val error = getErrorStringAndLog("subscribeMeToPost", blogId, postId, volleyError)
            cont.resume(Failure(error))
        }

        WordPress.getRestClientUtilsV1_1().post(
                endPointPath,
                listener,
                errorListener
        )
    }

    suspend fun unsubscribeMeFromPost(
        blogId: Long,
        postId: Long
    ): PostSubscribersCallResult = suspendCoroutine { cont ->
        val endPointPath = "/sites/$blogId/posts/$postId/subscribers/mine/delete"

        val listener = Listener { jsonObject ->
            val result = wasUnsubscribed(jsonObject)
            AppLog.d(
                    T.READER,
                    "unsubscribeMeFromPost > Succeeded [blogId=$blogId - postId=$postId - result = $result]"
            )
            cont.resume(result)
        }
        val errorListener = ErrorListener { volleyError ->
            val error = getErrorStringAndLog("unsubscribeMeFromPost", blogId, postId, volleyError)
            cont.resume(Failure(error))
        }

        WordPress.getRestClientUtilsV1_1().post(
                endPointPath,
                listener,
                errorListener
        )
    }

    suspend fun managePushNotificationsForPost(
        blogId: Long,
        postId: Long,
        activate: Boolean
    ): PostSubscribersCallResult = suspendCoroutine { cont ->
        val endPointPath = "/sites/$blogId/posts/$postId/subscribers/mine/update"

        val listener = Listener { jsonObject ->
            val result = if (activate) {
                wasSubscribedForPushNotifications(jsonObject)
            } else {
                wasUnsubscribedFromPushNotifications(jsonObject)
            }
            AppLog.d(
                    T.READER,
                    "managePushNotificationsForPost > Succeeded [blogId=$blogId - postId=$postId - result = $result]"
            )
            cont.resume(result)
        }
        val errorListener = ErrorListener { volleyError ->
            val error = getErrorStringAndLog("managePushNotificationsForPost", blogId, postId, volleyError)
            cont.resume(Failure(error))
        }

        val params = mutableMapOf<String, String>()
        params["receive_notifications"] = if (activate) "true" else "false"

        WordPress.getRestClientUtilsV1_1().post(
                endPointPath,
                params,
                null,
                listener,
                errorListener
        )
    }

    private fun getErrorStringAndLog(
        functionName: String,
        blogId: Long,
        postId: Long,
        volleyError: VolleyError?
    ): String {
        var error = VolleyUtils.errStringFromVolleyError(volleyError)
        return if (error.isNullOrEmpty()) {
            AppLog.d(
                    T.READER,
                    "$functionName > Failed with empty string " +
                            "[blogId=$blogId - postId=$postId - volleyError = $volleyError]"
            )
            contextProvider.getContext().getString(R.string.reader_follow_comments_get_status_error)
        } else {
            AppLog.d(
                    T.READER,
                    "$functionName > Failed [blogId=$blogId - postId=$postId - error = $error]"
            )
            error
        }
    }

    private fun getFollowingStateResult(json: JSONObject?): PostSubscribersCallResult {
        return json?.let {
            if (it.has("i_subscribe") && (it.has("receives_notifications") || !followByPushNotificationFeatureConfig.isEnabled())) {
                Success(it.optBoolean("i_subscribe", false), it.optBoolean("receives_notifications", false))
            } else {
                Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_bad_format_response))
            }
        } ?: Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_null_response))
    }

    private fun canFollowComments(blogId: Long, json: JSONObject?): PostSubscribersCallResult {
        return json?.let {
            if (it.has("ID") && it.optLong("ID", -1) == blogId) {
                Success(false, false)
            } else {
                Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_bad_format_response))
            }
        } ?: Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_null_response))
    }

    private fun wasSubscribed(json: JSONObject?): PostSubscribersCallResult {
        return json?.let {
            val success = it.optBoolean("success", false)
            val subscribed = it.optBoolean("i_subscribe", false)
            val receivingNotifications = it.optBoolean("receives_notifications", false)

            if (success) {
                if (subscribed) {
                    Success(true, receivingNotifications)
                } else {
                    Failure(contextProvider.getContext().getString(
                            R.string.reader_follow_comments_could_not_subscribe_error
                    ))
                }
            } else {
                Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_bad_format_response))
            }
        } ?: Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_null_response))
    }

    private fun wasUnsubscribed(json: JSONObject?): PostSubscribersCallResult {
        return json?.let {
            val success = it.optBoolean("success", false)
            val subscribed = it.optBoolean("i_subscribe", true)
            val receivingNotifications = it.optBoolean("receives_notifications", false)

            if (success) {
                if (!subscribed) {
                    Success(false, receivingNotifications)
                } else {
                    Failure(contextProvider.getContext().getString(
                            R.string.reader_follow_comments_could_not_unsubscribe_error
                    ))
                }
            } else {
                Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_bad_format_response))
            }
        } ?: Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_null_response))
    }

    private fun wasSubscribedForPushNotifications(json: JSONObject?): PostSubscribersCallResult {
        return json?.let {
            val subscribed = it.optBoolean("i_subscribe", false)
            val receivingNotifications = it.optBoolean("receives_notifications", false)

            if (subscribed) {
                if (receivingNotifications) {
                    Success(subscribed, receivingNotifications)
                } else {
                    Failure(contextProvider.getContext().getString(
                            R.string.reader_follow_comments_could_not_unsubscribe_error
                    ))
                }
            } else {
                Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_bad_format_response))
            }
        } ?: Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_null_response))
    }

    private fun wasUnsubscribedFromPushNotifications(json: JSONObject?): PostSubscribersCallResult {
        return json?.let {
            val subscribed = it.optBoolean("i_subscribe", false)
            val receivingNotifications = it.optBoolean("receives_notifications", true)

            if (subscribed) {
                if (!receivingNotifications) {
                    Success(subscribed, receivingNotifications)
                } else {
                    Failure(contextProvider.getContext().getString(
                            R.string.reader_follow_comments_could_not_unsubscribe_error
                    ))
                }
            } else {
                Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_bad_format_response))
            }
        } ?: Failure(contextProvider.getContext().getString(R.string.reader_follow_comments_null_response))
    }

    sealed class PostSubscribersCallResult {
        data class Success(val isFollowing: Boolean, val isReceivingNotifications: Boolean) : PostSubscribersCallResult()
        data class Failure(val error: String) : PostSubscribersCallResult()
    }
}
