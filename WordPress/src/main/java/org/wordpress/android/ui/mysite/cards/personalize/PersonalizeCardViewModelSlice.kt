package org.wordpress.android.ui.mysite.cards.personalize

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import org.wordpress.android.ui.mysite.MySiteCardAndItem
import org.wordpress.android.ui.mysite.MySiteCardAndItemBuilderParams.PersonalizeCardBuilderParams
import org.wordpress.android.ui.mysite.SiteNavigationAction
import org.wordpress.android.ui.mysite.SiteNavigationAction.OpenDashboardPersonalization
import org.wordpress.android.ui.mysite.cards.dashboard.CardsTracker
import org.wordpress.android.viewmodel.Event
import javax.inject.Inject

class PersonalizeCardViewModelSlice @Inject constructor(
    private val cardsTracker: CardsTracker,
    private val personalizeCardShownTracker: PersonalizeCardShownTracker,
    private val personalizeCardBuilder: PersonalizeCardBuilder
)  {
    private lateinit var scope: CoroutineScope

    private val _onNavigation = MutableLiveData<Event<SiteNavigationAction>>()
    val onNavigation = _onNavigation as LiveData<Event<SiteNavigationAction>>

    private val _uiModel = MutableLiveData<MySiteCardAndItem.Card.PersonalizeCardModel>()
    val uiModel: LiveData<MySiteCardAndItem.Card.PersonalizeCardModel> = _uiModel

    fun initialize(scope: CoroutineScope) {
        this.scope = scope
    }

    fun buildCard() {
        _uiModel.postValue(personalizeCardBuilder.build(getBuilderParams()))
    }

    fun getBuilderParams() = PersonalizeCardBuilderParams(onClick = this::onCardClick)

    fun onCardClick() {
        cardsTracker.trackCardItemClicked(
            CardsTracker.Type.PERSONALIZE_CARD.label,
            CardsTracker.Type.PERSONALIZE_CARD.label
        )
        _onNavigation.value = Event(OpenDashboardPersonalization)
    }

    fun trackShown(itemType: MySiteCardAndItem.Type) {
        personalizeCardShownTracker.trackShown(itemType)
    }

    fun resetShown() {
        personalizeCardShownTracker.resetShown()
    }
}
