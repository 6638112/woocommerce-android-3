package com.woocommerce.android.ui.main

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.push.NotificationChannelType
import com.woocommerce.android.push.NotificationMessageHandler
import com.woocommerce.android.push.NotificationTestUtils
import com.woocommerce.android.push.WooNotificationType
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.main.MainActivityViewModel.*
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.SiteStore

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class MainActivityViewModelTest : BaseUnitTest() {
    companion object {
        private const val TEST_REMOTE_SITE_ID_1 = 1023456789L
        private const val TEST_REMOTE_SITE_ID_2 = 9876543210L

        private const val TEST_NEW_ORDER_REMOTE_NOTE_ID = 5473011602
        private const val TEST_NEW_ORDER_ID_1 = 1915L
        private const val TEST_NEW_ORDER_ID_2 = 1915L

        private const val TEST_NEW_REVIEW_REMOTE_NOTE_ID = 5604993863
        private const val TEST_NEW_REVIEW_ID_1 = 4418L
        private const val TEST_NEW_REVIEW_ID_2 = 4418L

        private const val TEST_ZENDESK_PUSH_NOTIFICATION_ID = 1999999999
    }

    private lateinit var viewModel: MainActivityViewModel
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
    private val selectedSite: SelectedSite = mock()

    private val siteStore: SiteStore = mock()
    private val siteModel: SiteModel = SiteModel().apply {
        id = 1
        siteId = TEST_REMOTE_SITE_ID_1
    }

    private val notificationMessageHandler: NotificationMessageHandler = mock()
    private val testOrderNotification = NotificationTestUtils.generateTestNotification(
        remoteNoteId = TEST_NEW_ORDER_REMOTE_NOTE_ID,
        remoteSiteId = siteModel.siteId,
        uniqueId = TEST_NEW_ORDER_ID_1,
        channelType = NotificationChannelType.NEW_ORDER,
        noteType = WooNotificationType.NEW_ORDER
    )

    private val testReviewNotification = NotificationTestUtils.generateTestNotification(
        remoteNoteId = TEST_NEW_REVIEW_REMOTE_NOTE_ID,
        remoteSiteId = siteModel.siteId,
        uniqueId = TEST_NEW_REVIEW_ID_1,
        channelType = NotificationChannelType.REVIEW,
        noteType = WooNotificationType.PRODUCT_REVIEW
    )

    private val testZendeskNotification = NotificationTestUtils.generateTestNotification(
        noteId = TEST_ZENDESK_PUSH_NOTIFICATION_ID,
        remoteNoteId = TEST_ZENDESK_PUSH_NOTIFICATION_ID.toLong(),
        remoteSiteId = siteModel.siteId,
        uniqueId = 0,
        channelType = NotificationChannelType.OTHER,
        noteType = WooNotificationType.ZENDESK
    )

    @Before
    fun setup() {
        viewModel = spy(
            MainActivityViewModel(
                savedStateHandle,
                siteStore,
                selectedSite,
                notificationMessageHandler
            )
        )

        clearInvocations(
            viewModel,
            siteStore,
            selectedSite,
            notificationMessageHandler
        )

        doReturn(siteModel).whenever(siteStore).getSiteBySiteId(any())
        doReturn(siteModel).whenever(selectedSite).get()
    }

    @Test
    fun `when a blank notification is clicked, then the my store tab is opened`() {
        val localPushId = 1000
        var event: ViewMyStoreStats? = null
        viewModel.event.observeForever {
            if (it is ViewMyStoreStats) event = it
        }

        viewModel.handleIncomingNotification(localPushId, null)
        assertThat(event).isEqualTo(ViewMyStoreStats)
    }

    @Test
    fun `when a new order notification is clicked, then the order detail screen for that order is opened`() {
        val localPushId = 1000
        var event: ViewOrderDetail? = null
        viewModel.event.observeForever {
            if (it is ViewOrderDetail) event = it
        }

        viewModel.handleIncomingNotification(localPushId, testOrderNotification)

        verify(notificationMessageHandler, atLeastOnce()).markNotificationTapped(eq(testOrderNotification.remoteNoteId))
        verify(notificationMessageHandler, atLeastOnce()).removeNotificationByPushIdFromSystemsBar(eq(localPushId))
        assertThat(event).isEqualTo(
            ViewOrderDetail(
                testOrderNotification.uniqueId,
                siteModel.id,
                testOrderNotification.remoteNoteId
            )
        )
    }

    @Test
    fun `when a new order notification for non existent site is clicked, then the my store tab is opened`() {
        doReturn(null).whenever(siteStore).getSiteBySiteId(any())

        val localPushId = 1000
        var event: ViewOrderList? = null
        viewModel.event.observeForever {
            if (it is ViewOrderList) event = it
        }

        viewModel.handleIncomingNotification(localPushId, testOrderNotification)

        verify(notificationMessageHandler, atLeastOnce()).markNotificationTapped(eq(testOrderNotification.remoteNoteId))
        verify(notificationMessageHandler, atLeastOnce()).removeNotificationByPushIdFromSystemsBar(eq(localPushId))
        assertThat(event).isEqualTo(ViewOrderList)
    }

    @Test
    fun `when a new review notification is clicked, then the review detail screen for that review is opened`() {
        val localPushId = 1001
        var event: ViewReviewDetail? = null
        viewModel.event.observeForever {
            if (it is ViewReviewDetail) event = it
        }

        viewModel.handleIncomingNotification(localPushId, testReviewNotification)

        verify(notificationMessageHandler, atLeastOnce())
            .markNotificationTapped(eq(testReviewNotification.remoteNoteId))
        verify(notificationMessageHandler, atLeastOnce()).removeNotificationByPushIdFromSystemsBar(eq(localPushId))
        assertThat(event).isEqualTo(ViewReviewDetail(testReviewNotification.uniqueId))
    }

    @Test
    fun `when a new zendesk notification is clicked, then the my tickets screen of zendesk is opened`() {
        var event1: ViewZendeskTickets? = null
        viewModel.event.observeForever {
            if (it is ViewZendeskTickets) event1 = it
        }

        viewModel.handleIncomingNotification(TEST_ZENDESK_PUSH_NOTIFICATION_ID, testZendeskNotification)

        verify(notificationMessageHandler, atLeastOnce()).markNotificationTapped(
            eq(testZendeskNotification.remoteNoteId)
        )
        verify(notificationMessageHandler, atLeastOnce()).removeNotificationByPushIdFromSystemsBar(
            eq(TEST_ZENDESK_PUSH_NOTIFICATION_ID)
        )
        assertThat(event1).isEqualTo(ViewZendeskTickets)
    }

    @Test
    fun `when multiple order notifications for the same store is clicked, then the order list screen is opened`() {
        val groupOrderPushId = testOrderNotification.getGroupPushId()
        var event: ViewOrderList? = null
        viewModel.event.observeForever {
            if (it is ViewOrderList) event = it
        }

        viewModel.handleIncomingNotification(groupOrderPushId, testOrderNotification)

        verify(selectedSite, never()).set(any())
        verify(notificationMessageHandler, atLeastOnce()).markNotificationsOfTypeTapped(
            eq(testOrderNotification.channelType)
        )
        verify(notificationMessageHandler, atLeastOnce()).removeNotificationsOfTypeFromSystemsBar(
            eq(testOrderNotification.channelType),
            eq(testOrderNotification.remoteSiteId)
        )
        assertThat(event).isEqualTo(ViewOrderList)
    }

    @Test
    fun `when multiple review notifications for the same store is clicked, then the review list screen is opened`() {
        val reviewPushId = testReviewNotification.getGroupPushId()
        var event: ViewReviewList? = null
        viewModel.event.observeForever {
            if (it is ViewReviewList) event = it
        }

        viewModel.handleIncomingNotification(reviewPushId, testReviewNotification)

        verify(selectedSite, never()).set(any())
        verify(notificationMessageHandler, atLeastOnce()).markNotificationsOfTypeTapped(
            eq(testReviewNotification.channelType)
        )
        verify(notificationMessageHandler, atLeastOnce()).removeNotificationsOfTypeFromSystemsBar(
            eq(testReviewNotification.channelType),
            eq(testReviewNotification.remoteSiteId)
        )
        assertThat(event).isEqualTo(ViewReviewList)
    }

    @Test
    fun `when order notifications for a second store is clicked then switch to the this store and restart activity`() {
        val orderNotification2 = testOrderNotification.copy(
            remoteSiteId = TEST_REMOTE_SITE_ID_2, uniqueId = TEST_NEW_ORDER_ID_2
        )
        val groupOrderPushId = orderNotification2.getGroupPushId()

        viewModel.handleIncomingNotification(groupOrderPushId, orderNotification2)

        verify(selectedSite, atLeastOnce()).set(any())
        assertThat(viewModel.event.value)
            .isEqualTo(RestartActivityForNotification(groupOrderPushId, orderNotification2))
    }

    @Test
    fun `when review notifications for second store is clicked then switch to the this store and restart activity`() {
        val reviewNotification2 = testReviewNotification.copy(
            remoteSiteId = TEST_REMOTE_SITE_ID_2, uniqueId = TEST_NEW_REVIEW_ID_2
        )
        val reviewPushId = reviewNotification2.getGroupPushId()

        viewModel.handleIncomingNotification(reviewPushId, reviewNotification2)

        verify(selectedSite, atLeastOnce()).set(any())
        assertThat(viewModel.event.value).isEqualTo(RestartActivityForNotification(reviewPushId, reviewNotification2))
    }

    @Test
    fun `when multiple zendesk notifications is clicked, then the my store tab is opened`() {
        val localPushId = testZendeskNotification.getGroupPushId()
        var event: ViewMyStoreStats? = null
        viewModel.event.observeForever {
            if (it is ViewMyStoreStats) event = it
        }

        viewModel.handleIncomingNotification(localPushId, testZendeskNotification)
        verify(notificationMessageHandler, atLeastOnce()).markNotificationsOfTypeTapped(
            eq(testZendeskNotification.channelType)
        )
        verify(notificationMessageHandler, atLeastOnce()).removeNotificationsOfTypeFromSystemsBar(
            eq(testZendeskNotification.channelType),
            eq(testZendeskNotification.remoteSiteId)
        )
        assertThat(event).isEqualTo(ViewMyStoreStats)
    }
}
