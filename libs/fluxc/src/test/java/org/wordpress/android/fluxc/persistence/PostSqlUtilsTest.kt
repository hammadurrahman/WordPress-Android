package org.wordpress.android.fluxc.persistence

import com.yarolegovich.wellsql.WellSql
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.wordpress.android.fluxc.model.PostModel
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpcom.post.PostRemoteAutoSaveModel
import kotlin.test.assertNull

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class PostSqlUtilsTest {
    @Before
    fun setUp() {
        val appContext = RuntimeEnvironment.application.applicationContext

        val config = WellSqlConfig(appContext)
        WellSql.init(config)
        config.reset()
    }

    @Test
    fun `post autoSave fields get updated`() {
        val localSiteId = 100
        val remotePostId = 200L
        val revisionId = 999L
        val modifiedDate = "test date"
        val previewUrl = "test url"

        val site = SiteModel()
        site.id = localSiteId
        var post = PostModel()
        post.localSiteId = site.id
        post.remotePostId = remotePostId

        post = PostSqlUtils.insertPostForResult(post)

        assertNull(post.autoSaveModified)
        assertNull(post.autoSavePreviewUrl)
        assertEquals(0, post.autoSaveRevisionId)

        PostSqlUtils.updatePostsAutoSave(
                site,
                PostRemoteAutoSaveModel(revisionId, remotePostId, modifiedDate, previewUrl)
        )

        val postsForSite = PostSqlUtils.getPostsForSite(site, false)

        assertEquals(1, postsForSite.size)
        assertEquals(revisionId, postsForSite.first().autoSaveRevisionId)
        assertEquals(remotePostId, postsForSite.first().remotePostId)
        assertEquals(modifiedDate, postsForSite.first().autoSaveModified)
        assertEquals(modifiedDate, postsForSite.first().remoteAutoSaveModified)
        assertEquals(previewUrl, postsForSite.first().autoSavePreviewUrl)
    }
}
