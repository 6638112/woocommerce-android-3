package com.woocommerce.android.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.woocommerce.android.ui.products.ProductDetailViewModel
import com.woocommerce.android.ui.products.ProductListViewModel
import com.woocommerce.android.ui.refunds.IssueRefundViewModel
import com.woocommerce.android.ui.refunds.RefundDetailViewModel
import com.woocommerce.android.viewmodel.ViewModelFactory
import com.woocommerce.android.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(ProductDetailViewModel::class)
    internal abstract fun pluginProductDetailViewModel(viewModel: ProductDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProductListViewModel::class)
    internal abstract fun pluginProductListViewModel(viewModel: ProductListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(IssueRefundViewModel::class)
    internal abstract fun issueRefundViewModel(viewModel: IssueRefundViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RefundDetailViewModel::class)
    internal abstract fun refundDetailViewModel(viewModel: RefundDetailViewModel): ViewModel

    @Binds
    internal abstract fun provideViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}
