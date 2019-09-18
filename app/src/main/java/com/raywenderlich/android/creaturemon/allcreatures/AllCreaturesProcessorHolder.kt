package com.raywenderlich.android.creaturemon.allcreatures

import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesAction.*
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesResult.*
import com.raywenderlich.android.creaturemon.data.repository.CreatureRepository
import com.raywenderlich.android.creaturemon.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import java.lang.IllegalArgumentException

class AllCreaturesProcessorHolder(
        private val creatureRepository: CreatureRepository,
        private val schedulerProvider: BaseSchedulerProvider
) {

    private val loadAllcreaturesProcessor =
            ObservableTransformer<LoadAllCreaturesAction, LoadAllCreaturesResult> { actions ->
                actions.flatMap {
                    creatureRepository.getAllCreatures()
                            .map { creatures -> LoadAllCreaturesResult.Success(creatures) }
                            .cast(LoadAllCreaturesResult::class.java)
                            .onErrorReturn(LoadAllCreaturesResult::Failure)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .startWith(LoadAllCreaturesResult.Loading)
                }
            }

    private val clearAllCreaturesProcessor =
            ObservableTransformer<ClearAllCreaturesAction, ClearAllCreaturesResult> { actions ->
                actions.flatMap {
                    creatureRepository.clearAllCreatures()
                            .map { ClearAllCreaturesResult.Success }
                            .cast(ClearAllCreaturesResult::class.java)
                            .onErrorReturn(ClearAllCreaturesResult::Failure)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .startWith(ClearAllCreaturesResult.Clearing)
                }
            }

    internal var actionProcessor =
            ObservableTransformer<AllCreaturesAction, AllCreaturesResult> { actions ->
                actions.publish { shared ->
                    Observable.merge(
                            shared.ofType(LoadAllCreaturesAction::class.java).compose(loadAllcreaturesProcessor),
                            shared.ofType(ClearAllCreaturesAction::class.java).compose(clearAllCreaturesProcessor))
                            .mergeWith(
                                    //Error for not implemented actions
                                    shared.filter { v ->
                                        v !is LoadAllCreaturesAction
                                                && v !is ClearAllCreaturesAction
                                    }.flatMap { w ->
                                        Observable.error<AllCreaturesResult>(
                                                IllegalArgumentException("Unknown action tyupe: $w")
                                        )
                                    }
                            )

                }
            }
}