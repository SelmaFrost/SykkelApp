package com.in2000team12.sykkelapp.models

data class DistanceMatrixResponseDto(val rows: List<DistanceMatrixRow>)

data class DistanceMatrixRow(val elements: List<DistanceMatrixElement>)

data class DistanceMatrixElement(val distance: DistanceMatrixDistance, val duration: DistanceMatrixDuration, val status: String)

data class DistanceMatrixDistance(val text: String, val value: Double)

data class DistanceMatrixDuration(val text: String, val value: Double)
