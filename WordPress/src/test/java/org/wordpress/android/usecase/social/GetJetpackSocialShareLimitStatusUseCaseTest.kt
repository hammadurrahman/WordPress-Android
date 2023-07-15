package org.wordpress.android.usecase.social

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.jetpacksocial.JetpackSocial
import org.wordpress.android.fluxc.store.SiteStore

@ExperimentalCoroutinesApi
class GetJetpackSocialShareLimitStatusUseCaseTest : BaseUnitTest() {
    private val siteStore: SiteStore = mock()
    private val classToTest = GetJetpackSocialShareLimitStatusUseCase(
        siteStore = siteStore,
    )

    @Test
    fun blabla() {
        classToTest.toString()
    }
//    fun `Should return Disabled if site is not swelf hosted`() = test {
//        val siteModel = SiteModel().apply {
//            siteId = 1L
//            setIsJetpackInstalled(false)
//        }
//        val expected = ShareLimit.Disabled
//        val actual = classToTest.execute(siteModel)
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `Should return Disabled if site has social-shares-1000 feature active`() = test {
//        val siteModel = SiteModel().apply {
//            siteId = 1L
//            setIsJetpackInstalled(true)
//            planActiveFeatures = "social-shares-1000"
//        }
//        val expected = ShareLimit.Disabled
//        val actual = classToTest.execute(siteModel)
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `Should return Enabled if active features list is null AND is self hosted`() = test {
//        val jetpackSocial = JetpackSocial(
//            isShareLimitEnabled = true,
//            toBePublicizedCount = 10,
//            shareLimit = 11,
//            publicizedCount = 12,
//            sharedPostsCount = 13,
//            sharesRemaining = 14,
//            isEnhancedPublishingEnabled = true,
//            isSocialImageGeneratorEnabled = true,
//        )
//        val siteModel = SiteModel().apply {
//            siteId = 1L
//            setIsJetpackInstalled(true)
//            planActiveFeatures = null
//        }
//        whenever(siteStore.getJetpackSocial(siteModel.id)).thenReturn(jetpackSocial)
//        val expected = ShareLimit.Enabled(
//            shareLimit = jetpackSocial.shareLimit,
//            publicizedCount = jetpackSocial.publicizedCount,
//            sharedPostsCount = jetpackSocial.sharedPostsCount,
//            sharesRemaining = jetpackSocial.sharesRemaining,
//        )
//        val actual = classToTest.execute(siteModel)
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `Should return Enabled if site is self hosted AND does not have social-shares-1000 feature active`() = test {
//        val jetpackSocial = JetpackSocial(
//            isShareLimitEnabled = true,
//            toBePublicizedCount = 10,
//            shareLimit = 11,
//            publicizedCount = 12,
//            sharedPostsCount = 13,
//            sharesRemaining = 14,
//            isEnhancedPublishingEnabled = true,
//            isSocialImageGeneratorEnabled = true,
//        )
//        val siteModel = SiteModel().apply {
//            siteId = 1L
//            setIsJetpackInstalled(true)
//            planActiveFeatures = ""
//        }
//        whenever(siteStore.getJetpackSocial(siteModel.id)).thenReturn(jetpackSocial)
//        val expected = ShareLimit.Enabled(
//            shareLimit = jetpackSocial.shareLimit,
//            publicizedCount = jetpackSocial.publicizedCount,
//            sharedPostsCount = jetpackSocial.sharedPostsCount,
//            sharesRemaining = jetpackSocial.sharesRemaining,
//        )
//        val actual = classToTest.execute(siteModel)
//        assertEquals(expected, actual)
//    }
}
