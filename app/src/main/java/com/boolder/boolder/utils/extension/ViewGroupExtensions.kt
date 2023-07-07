package com.boolder.boolder.utils.extension

import android.view.LayoutInflater
import android.view.ViewGroup

val ViewGroup.inflater: LayoutInflater get() = LayoutInflater.from(context)
