package org.wordpress.android.ui.posts

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.LocalOrRemoteId.LocalId
import org.wordpress.android.fluxc.model.PostModel
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.ui.jetpackoverlay.JetpackFeatureRemovalPhaseHelper
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import org.wordpress.android.ui.uploads.UploadStarter
import org.wordpress.android.viewmodel.Event

@ExperimentalCoroutinesApi
class PostListMainViewModelTest : BaseUnitTest() {
    @Mock
    lateinit var site: SiteModel
    private val currentBottomSheetPostId = LocalId(0)

    @Mock
    lateinit var uploadStarter: UploadStarter

    @Mock
    lateinit var dispatcher: Dispatcher

    @Mock
    lateinit var editPostRepository: EditPostRepository

    @Mock
    lateinit var savePostToDbUseCase: SavePostToDbUseCase

    @Mock
    lateinit var jetpackFeatureRemovalPhaseHelper: JetpackFeatureRemovalPhaseHelper

    private lateinit var viewModel: PostListMainViewModel

    @Mock
    lateinit var prefs: AppPrefsWrapper
    @Before
    fun setUp() {
        whenever(editPostRepository.postChanged).thenReturn(MutableLiveData(Event(PostModel())))

        viewModel = PostListMainViewModel(
            dispatcher = dispatcher,
            postStore = mock(),
            accountStore = mock(),
            uploadStore = mock(),
            mediaStore = mock(),
            networkUtilsWrapper = mock(),
            prefs = prefs,
            previewStateHelper = mock(),
            analyticsTracker = mock(),
            mainDispatcher = testDispatcher(),
            bgDispatcher = testDispatcher(),
            postListEventListenerFactory = mock(),
            uploadStarter = uploadStarter,
            uploadActionUseCase = mock(),
            savePostToDbUseCase = savePostToDbUseCase
        )
    }

    @Test
    fun `when started, it uploads all local drafts`() {
        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository)

        verify(uploadStarter, times(1)).queueUploadFromSite(eq(site))
    }

    @Test
    fun `calling onSearch() updates search query`() {
        val testSearch = "keyword"
        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository)

        var searchQuery: String? = null
        viewModel.searchQuery.observeForever {
            searchQuery = it
        }

        viewModel.onSearch(testSearch)

        assertThat(searchQuery).isEqualTo(testSearch)
    }

    @Test
    fun `expanding and collapsing search triggers isSearchExpanded`() {
        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository)

        var isSearchExpanded = false
        viewModel.isSearchExpanded.observeForever {
            isSearchExpanded = it
        }

        viewModel.onSearchExpanded(false)
        assertThat(isSearchExpanded).isTrue()

        viewModel.onSearchCollapsed(delay = 0)
        assertThat(isSearchExpanded).isFalse()
    }

    @Test
    fun `expanding search after configuration change preserves search query`() {
        val testSearch = "keyword"

        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository)

        var searchQuery: String? = null
        viewModel.searchQuery.observeForever {
            searchQuery = it
        }

        viewModel.onSearch(testSearch)

        assertThat(searchQuery).isNotNull()
        assertThat(searchQuery).isEqualTo(testSearch)

        viewModel.onSearchExpanded(true)
        assertThat(searchQuery).isEqualTo(testSearch)

        viewModel.onSearchCollapsed(0)

        viewModel.onSearchExpanded(false)
        assertThat(searchQuery).isNull()
    }

    @Test
    fun `if currentBottomSheetPostId isn't 0 then set the post in editPostRepository from the postStore`() {
        // arrange
        val bottomSheetPostId = LocalId(2)

        // act
        viewModel.start(site, PostListRemotePreviewState.NONE, bottomSheetPostId, editPostRepository)

        // assert
        verify(editPostRepository, times(1)).loadPostByLocalPostId(any())
    }

    @Test
    fun `if currentBottomSheetPostId is 0 then don't set the post in editPostRepository from the postStore`() {
        // arrange
        val bottomSheetPostId = LocalId(0)

        // act
        viewModel.start(site, PostListRemotePreviewState.NONE, bottomSheetPostId, editPostRepository)

        // assert
        verify(editPostRepository, times(0)).loadPostByLocalPostId(any())
    }

    @Test
    fun `if post in EditPostRepository is modified then the savePostToDbUseCase should update the post`() {
        // arrange
        val editPostRepository = EditPostRepository(
            mock(),
            mock(),
            mock(),
            testDispatcher(),
            testDispatcher()
        )
        editPostRepository.set { mock() }
        val action = { _: PostModel -> true }

        // act
        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository)
        // simulates the Publish Date, Status & Visibility or Tags being updated in the bottom sheet.
        editPostRepository.updateAsync(action, null)

        // assert
        verify(savePostToDbUseCase, times(1)).savePostToDb(any(), any())
    }
}
