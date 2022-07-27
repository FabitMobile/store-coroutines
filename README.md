# Store

## UDF - Unidirectional Data Flow

Store хранит состояние, также он может хранить списки SideEffect, ActionSource, ActionHandler и
BindActionSource.

Компоненты:

- Action - событие возникшее в системе
- State - состояние
- ActionSource - реактивный источник событий для Store
- BindActionSource - реактивный источник событий для Store, который наблюдает за событиями
- SideEffect - побочное действие, возникшее, из-за изменения состояния, а так же является источником
  новых событий
- ActionHandler - наблюдатель событий, выполняет действия, но ничего не отдает
- Reducer - функция, которая принимает Action и State, и возвращает новый State.

![UDF Store_Flow](https://github.com/FabitMobile/store-coroutines/raw/main/readme/udf_store1.png)

Рис. 1. Поток событий в Store

В Store извне могут приходить события от ViewController, но помимо этого, компоненты Store (
ActionSource, BindActionSource, SideEffect) также являются источниками событий. Store постоянно
следим за ними и обновляет свое состояние.

### Принцип работы

![UDF Store_Flow2](https://github.com/FabitMobile/store-coroutines/raw/main/readme/udf_store2.png)

Рис. 2. Поток событий и состояний в Store

На рис. 2 мы можем видеть, что все компоненты обмениваются событиями через Reducer. При этом State
всегда записывается и является единственным для конкретного времени. То есть в одно и тоже время в
системе должен быть один и тот же экземпляр состояния.

ActionSource - является источником событий, он не зависит от других событий и может их только
испускать. При создании Store ActionSource не пересоздается и всегда находится в единственном
экземпляре. В проекте ActionSource является наследником класса

```kotlin
open class ActionSource<Action>(
    private val source: suspend () -> Flow<Action>,
    private val error: (Throwable) -> Action = { t: Throwable -> throw t }
) {
    suspend operator fun invoke() = source()

    operator fun invoke(throwable: Throwable) = error(throwable)
}
```

BindActionSource также является источником событий, но уже зависимый от других событий.

```kotlin
open class BindActionSource<State, Action>(
    val requirement: (Action) -> Boolean,
    private val source: suspend (State, Action) -> Flow<Action>,
    private val error: (Throwable) -> Action = { t: Throwable -> throw t }
) {
    suspend operator fun invoke(state: State, action: Action) = source(state, action)

    operator fun invoke(throwable: Throwable) = error(throwable)
}
```

Из исходного кода мы видим, что у BindActionSource добавляется новая переменная `requirement`. Она
является условием запуска основной функции `source`. Таким образом мы можем задавать, на какие
события будет реагировать BindActionSource, и перезапускать его при их возникновении.

SideEffect - источник событий, который также запускается при возникновении нового события, но, в
отличие от выше перечесленный, не может создавать потоки событий, а выполняется только один раз.

```kotlin
open class SideEffect<State, Action>(
    val requirement: (State, Action) -> Boolean,
    private val effect: suspend (State, Action) -> Action,
    private val error: (Throwable) -> Action
) {
    suspend operator fun invoke(state: State, action: Action) = effect(state, action)

    operator fun invoke(throwable: Throwable) = error(throwable)
}
```

Также важным является то, что при возникновении нового события и последующего за ним изменения
состояния, произойдет отписка от SideEffect и он запустится заново.

ActionHandler предназначен для сторонних действий, которые не влияют на State и не меняют его, и
выполняются, обычно, в основном потоке

```kotlin
open class ActionHandler<State, Action>(
    val requirement: (Action) -> Boolean,
    private val handler: suspend (State, Action) -> Unit,
    val dispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    suspend operator fun invoke(state: State, action: Action) = handler(state, action)
}
```

## Events

EventsStore, EventsReducer и EventsState являются надстройкой на базовым Store, добавляющие
возможность посылать одноразовые события. После успешной доставки такого события (или группы
событий), оно будет уничтожено. До этого момента события накапливаются внутри State.

Для использования Events необходимо унаследовать Store, Reducer и State от соответсвующих классов.

В Reducer необходимо реализовать функция для копирования состояния (например, используя
метод `copy()` data класса или иной подобный)

```kotlin
    override fun copy(state: State) = state.copy()
```













