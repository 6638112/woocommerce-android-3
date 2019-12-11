package com.woocommerce.android.ui.products

import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_LOADED
import com.woocommerce.android.annotations.OpenClassOnDebug
import com.woocommerce.android.model.Product
import com.woocommerce.android.model.toAppModel
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.util.WooLog
import com.woocommerce.android.util.suspendCancellableCoroutineWithTimeout
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode.MAIN
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.WCProductAction.FETCH_SINGLE_PRODUCT
import org.wordpress.android.fluxc.generated.WCProductActionBuilder
import org.wordpress.android.fluxc.store.WCProductStore
import org.wordpress.android.fluxc.store.WCProductStore.OnProductChanged
import javax.inject.Inject
import kotlin.coroutines.resume

@OpenClassOnDebug
class ProductDetailRepository @Inject constructor(
    private val dispatcher: Dispatcher,
    private val productStore: WCProductStore,
    private val selectedSite: SelectedSite
) {
    companion object {
        private const val ACTION_TIMEOUT = 10L * 1000
    }

    private var continuation: CancellableContinuation<Boolean>? = null

    init {
        dispatcher.register(this)
    }

    fun onCleanup() {
        dispatcher.unregister(this)
    }

    suspend fun fetchProduct(remoteProductId: Long): Product? {
        try {
            continuation?.cancel()
            suspendCancellableCoroutineWithTimeout<Boolean>(ACTION_TIMEOUT) {
                continuation = it

                val payload = WCProductStore.FetchSingleProductPayload(selectedSite.get(), remoteProductId)
                dispatcher.dispatch(WCProductActionBuilder.newFetchSingleProductAction(payload))
            }
        } catch (e: CancellationException) {
            WooLog.d(WooLog.T.PRODUCTS, "CancellationException while fetching single product")
        }

        continuation = null
        return getProduct(remoteProductId)
    }

    fun getProduct(remoteProductId: Long): Product? =
            productStore.getProductByRemoteId(selectedSite.get(), remoteProductId)?.toAppModel()

    @SuppressWarnings("unused")
    @Subscribe(threadMode = MAIN)
    fun onProductChanged(event: OnProductChanged) {
        if (event.causeOfChange == FETCH_SINGLE_PRODUCT) {
            if (event.isError) {
                continuation?.resume(false)
            } else {
                AnalyticsTracker.track(PRODUCT_DETAIL_LOADED)
                continuation?.resume(true)
            }
        }
    }
}
