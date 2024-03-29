package com.raywenderlich.android.creaturemon.allcreatures

import com.raywenderlich.android.creaturemon.viewbase.MviAction

sealed class AllCreaturesAction : MviAction {
    object LoadAllCreaturesAction : AllCreaturesAction()
    object ClearAllCreaturesAction : AllCreaturesAction()
}