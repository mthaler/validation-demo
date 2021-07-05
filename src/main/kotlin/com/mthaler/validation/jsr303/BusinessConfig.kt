package com.mthaler.validation.jsr303

import com.mthaler.validation.jsr303.ThresholdConstraint
import javax.validation.constraints.Min
import javax.validation.constraints.Max

@ThresholdConstraint
class BusinessConfig(@Min(0) var thresholdA: Int, var thresholdB: Int, @Max(10000) var thresholdC: Int)