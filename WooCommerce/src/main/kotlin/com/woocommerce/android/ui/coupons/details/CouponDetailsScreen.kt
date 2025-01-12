package com.woocommerce.android.ui.coupons.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.woocommerce.android.R
import com.woocommerce.android.ui.coupons.components.CouponExpirationLabel
import com.woocommerce.android.ui.coupons.details.CouponDetailsViewModel.*
import com.woocommerce.android.ui.coupons.details.CouponDetailsViewModel.CouponPerformanceState.Loading
import com.woocommerce.android.ui.coupons.details.CouponDetailsViewModel.CouponPerformanceState.Success
import com.woocommerce.android.util.FeatureFlag

@Composable
fun CouponDetailsScreen(
    viewModel: CouponDetailsViewModel,
    onBackPress: () -> Boolean
) {
    val couponSummaryState by viewModel.couponState.observeAsState(CouponDetailsState())

    CouponDetailsScreen(
        couponSummaryState,
        onBackPress,
        viewModel::onCopyButtonClick,
        viewModel::onShareButtonClick,
        viewModel::onDeleteButtonClick
    )
}

@Composable
@Suppress("LongMethod")
fun CouponDetailsScreen(
    state: CouponDetailsState,
    onBackPress: () -> Boolean,
    onCopyButtonClick: () -> Unit,
    onShareButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var showMenu by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }

        TopAppBar(
            backgroundColor = MaterialTheme.colors.surface,
            title = { Text(state.couponSummary?.code ?: "") },
            navigationIcon = {
                IconButton(onClick = { onBackPress() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Coupons Menu",
                        tint = colorResource(id = R.color.action_menu_fg_selector)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(onClick = {
                        onCopyButtonClick()
                        showMenu = false
                    }) {
                        Text(stringResource(id = R.string.coupon_details_menu_copy))
                    }
                    DropdownMenuItem(onClick = {
                        onShareButtonClick()
                        showMenu = false
                    }) {
                        Text(stringResource(id = R.string.coupon_details_menu_share))
                    }

                    if (FeatureFlag.COUPONS_M2.isEnabled()) {
                        DropdownMenuItem(onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        }) {
                            Text(
                                stringResource(id = R.string.coupon_details_delete),
                                color = MaterialTheme.colors.secondary
                            )
                        }
                    }
                }
            }
        )

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            state.couponSummary?.let { coupon ->
                CouponSummaryHeading(
                    code = coupon.code,
                    isActive = state.couponSummary.isActive
                )
                CouponSummarySection(coupon)
            }
            state.couponPerformanceState?.let {
                CouponPerformanceSection(it)
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(stringResource(id = R.string.coupon_details_delete))
                },
                text = {
                    Text(stringResource(id = R.string.coupon_details_delete_confirmation))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteButtonClick()
                        }
                    ) {
                        Text(
                            stringResource(id = R.string.delete).uppercase(),
                            color = MaterialTheme.colors.secondary
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(
                            stringResource(id = R.string.cancel).uppercase(),
                            color = MaterialTheme.colors.secondary
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun CouponSummaryHeading(
    code: String?,
    isActive: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.major_100),
                vertical = dimensionResource(id = R.dimen.major_100)
            )
    ) {
        code?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        CouponExpirationLabel(isActive)
    }
}

@Composable
fun CouponSummarySection(couponSummary: CouponSummaryUi) {
    Surface(
        elevation = dimensionResource(id = R.dimen.minor_10),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.major_100))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.major_100)),
            modifier = Modifier.padding(dimensionResource(id = R.dimen.major_100))
        ) {
            Text(
                text = stringResource(id = R.string.coupon_details_heading),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.minor_100))
            )
            SummaryLabel(couponSummary.discountType)
            SummaryLabel(couponSummary.summary)
            if (couponSummary.isForIndividualUse) {
                SummaryLabel(stringResource(id = R.string.coupon_details_individual_use_only))
            }
            if (couponSummary.isShippingFree) {
                SummaryLabel(stringResource(id = R.string.coupon_details_allows_free_shipping))
            }
            if (couponSummary.areSaleItemsExcluded) {
                SummaryLabel(stringResource(id = R.string.coupon_details_excludes_sale_items))
            }
            SummaryLabel(couponSummary.minimumSpending)
            SummaryLabel(couponSummary.maximumSpending)
            SummaryLabel(couponSummary.usageLimitPerCoupon)
            SummaryLabel(couponSummary.usageLimitPerUser)
            SummaryLabel(couponSummary.usageLimitPerItems)
            SummaryLabel(couponSummary.expiration)
            SummaryLabel(couponSummary.emailRestrictions)
        }
    }
}

@Composable
private fun SummaryLabel(text: String?) {
    text?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface,
        )
    }
}

@Composable
private fun CouponPerformanceSection(couponPerformanceState: CouponPerformanceState) {
    Surface(
        elevation = dimensionResource(id = R.dimen.minor_10),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.major_100))
        ) {
            Text(
                text = stringResource(id = R.string.coupon_details_performance_heading),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.major_200)))

            Row {
                CouponPerformanceCount(
                    couponPerformanceState = couponPerformanceState,
                    modifier = Modifier.weight(1f)
                )

                CouponPerformanceAmount(
                    couponPerformanceState = couponPerformanceState,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CouponPerformanceCount(
    couponPerformanceState: CouponPerformanceState,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.major_100)),
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.coupon_details_performance_discounted_order_heading),
            style = MaterialTheme.typography.subtitle1,
            color = colorResource(id = R.color.color_surface_variant)
        )

        Text(
            text = couponPerformanceState.ordersCount?.toString().orEmpty(),
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CouponPerformanceAmount(
    couponPerformanceState: CouponPerformanceState,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.major_100)),
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.coupon_details_performance_amount_heading),
            style = MaterialTheme.typography.subtitle1,
            color = colorResource(id = R.color.color_surface_variant)
        )
        when (couponPerformanceState) {
            is Loading -> CircularProgressIndicator(modifier = Modifier.size(dimensionResource(id = R.dimen.major_200)))
            else -> {
                val amount = (couponPerformanceState as? Success)?.data
                    ?.formattedAmount ?: "-"
                Text(
                    text = amount,
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
