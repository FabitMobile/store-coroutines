package ru.fabit.storecoroutines.order

import ru.fabit.storecoroutines.Reducer

class OrderReducer : Reducer<OrderState, OrderAction> {
    override fun reduce(state: OrderState, action: OrderAction): OrderState {
        return when (action) {
            is OrderAction.BootstrapAction -> state.copy(
                value = state.value + action.value
            )
            is OrderAction.Action -> state.copy(
                value = state.value + action.value
            )

            is OrderAction.BindAction -> state.copy(
                value = state.value + action.value
            )
            else -> state.copy()
        }
    }
}