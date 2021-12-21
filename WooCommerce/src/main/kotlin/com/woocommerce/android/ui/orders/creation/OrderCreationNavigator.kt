package com.woocommerce.android.ui.orders.creation

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.woocommerce.android.ui.orders.creation.OrderCreationNavigationTarget.AddSimpleProduct
import com.woocommerce.android.ui.orders.creation.OrderCreationNavigationTarget.EditCustomer
import com.woocommerce.android.ui.orders.creation.OrderCreationNavigationTarget.EditCustomerNote

object OrderCreationNavigator {
    fun navigate(fragment: Fragment, target: OrderCreationNavigationTarget) {
        val navController = fragment.findNavController()

        val action = when (target) {
            EditCustomer ->
                OrderCreationFormFragmentDirections.actionOrderCreationFragmentToOrderCreationCustomerFragment()
            EditCustomerNote ->
                OrderCreationFormFragmentDirections.actionOrderCreationFragmentToOrderCreationCustomerNoteFragment()
            AddSimpleProduct ->
                OrderCreationFormFragmentDirections.actionOrderCreationFragmentToOrderCreationProductSelectionFragment()
        }

        navController.navigate(action)
    }
}