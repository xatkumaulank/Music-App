/*
* Copyright 2014 Adnan A M.

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.naman14.timber.timely.model.core

/**
 * Model class for cubic bezier figure
 */
abstract class Figure protected constructor(controlPoints: Array<FloatArray>) {
    var pointsCount = NO_VALUE
        protected set

    //A chained sequence of points P0,P1,P2,P3/0,P1,P2,P3/0,...
    var controlPoints: Array<FloatArray>? = null
        protected set

    companion object {
        const val NO_VALUE = -1
    }

    init {
        this.controlPoints = controlPoints
        pointsCount = (controlPoints.size + 2) / 3
    }
}