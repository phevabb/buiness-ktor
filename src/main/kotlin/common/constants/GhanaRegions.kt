package com.example.common.constants



enum class GhanaRegion(val value: String, val displayName: String) {
    GREATER_ACCRA("greater_accra", "Greater Accra"),
    ASHANTI("ashanti", "Ashanti"),
    WESTERN("western", "Western"),
    EASTERN("eastern", "Eastern"),
    CENTRAL("central", "Central"),
    NORTHERN("northern", "Northern"),
    UPPER_EAST("upper_east", "Upper East"),
    UPPER_WEST("upper_west", "Upper West"),
    VOLTA("volta", "Volta"),
    BONO("bono", "Bono"),
    AHAFO("ahafo", "Ahafo"),
    BONO_EAST("bono_east", "Bono East"),
    OTI("oti", "Oti"),
    SAVANNAH("savannah", "Savannah"),
    NORTH_EAST("north_east", "North East");

    companion object {
        fun fromValue(value: String): GhanaRegion? =
            entries.firstOrNull { it.value == value }
    }
}